package jxtras.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

import jxtras.primitive.image.Bitmap;
import jxtras.primitive.raster.Scanline;
import jxtras.primitive.shape.Shape;
import jxtras.primitive.shape.ShapeType;

public class Model {
    /**
     * The <code>ShapeResult</code> class encapsulates the info about
     * a shape added to the mode.
     */
    public static class ShapeResult {
        public final double score;
        public final int color;
        public final Shape shape;

        public ShapeResult(double score, int color, Shape shape) {
            this.score = score;
            this.color = color;
            this.shape = shape;
        }
    }

    public interface StepListener {
        void onResult(ShapeResult result);
    }

    public int scaledWidth, scaledHeight;
    public float scale;

    /**
     * The width of the target image.
     */
    public  int width;

    /**
     * The height of the target image.
     */
    public  int height;

    /**
     * The target image which we aim to approximate.
     */
    public  Bitmap target;

    /**
     * The current image.
     */
    public  Bitmap current;

    /**
     * The buffered image.
     */
    public  Bitmap buffer;

    /**
     * Score derived from calculating the difference between bitmaps.
     */
    public double score;

    int background;

    List<ShapeResult> results = new ArrayList<>();
    List<Worker> workers = new ArrayList<Worker>();
    /**
     * Model for the optimization/fitting algorithm.
     */
    public Model(Bitmap image, int background, int size) {
        this.target = image;
        this.current = image.copy().eraseColor(background);
        this.buffer = image.copy().eraseColor(background);

        this.width = image.getWidth();
        this.height = image.getHeight();
        float aspectRatio = (float) width / (float) height;
        int sw, sh;
        float scale;
        if (aspectRatio >= 1) {
            sw = size;
            sh = (int)((float) size / aspectRatio);
            scale = (float) size / (float) width;
        } else {
            sw = (int) (size * aspectRatio);
            sh = size;
            scale = (float) size / (float) height;
        }

        this.scaledWidth = sw;
        this.scaledHeight = sh;
        this.scale = scale;

        this.score = Core.differenceFull(target, current);
    }

    public Model(Bitmap image, int background, int size, int numOfWorkers) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        float aspectRatio = (float) width / (float) height;
        int sw, sh;
        float scale;
        if (aspectRatio >= 1) {
            sw = size;
            sh = (int)((float) size / aspectRatio);
            scale = (float) size / (float) width;
        } else {
            sw = (int) (size * aspectRatio);
            sh = size;
            scale = (float) size / (float) height;
        }

        this.scaledWidth = sw;
        this.scaledHeight = sh;
        this.scale = scale;
        this.background = background;
        this.target = image;
        this.current = image.copy().eraseColor(background);
        this.score = Core.differenceFull(target, current);
        for (int i = 0; i < numOfWorkers; i++) {
            Worker worker = new Worker(target);
            workers.add(worker);
        }
    }

    public void add(Shape shape, int alpha) {
        Bitmap before = current.copy();
        List<Scanline> scanlines = shape.rasterize();
        int color = Core.computeColor(target, current, scanlines, alpha);
        Core.drawLines(current, color, scanlines);
        score = Core.differencePartial(target, before, current, score, scanlines);

        System.out.println("result: " + Arrays.toString(shape.raw()));
        results.add(new ShapeResult(score, color, shape));
    }

    public int step(ShapeType shapeType, int alpha, int repeat) {
        State state = runWorkers(shapeType, alpha, 1000, 100, 16);
        add(state.shape, state.alpha);
        for (int i = 0; i < repeat; i++) {
            state.worker.init(current, score);
            double a = state.energy();
            state = Core.hillClimb2(state, 100);
            double b = state.energy();
            if (a == b) {
                break;
            }
            add(state.shape, state.alpha);
        }

        int counter = 0;
        for (Worker worker : workers) {
            counter += worker.counter;
        }
        return counter;
    }

    public static int NTHREADS = getNumberOfProcessors();

    private static ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));

    private static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        private final Thread.UncaughtExceptionHandler handler;

        CustomThreadFactory(Thread.UncaughtExceptionHandler handler) {
            this.handler = handler;
        }

        public Thread newThread(Runnable r) {
            Thread t = defaultFactory.newThread(r);
            t.setUncaughtExceptionHandler(handler);
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Shutdowns the thread pool.
     */
    public static void shutdown() {
        THREAD_POOL.shutdown();
    }

    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.
     *
     * @param <T>
     * @param tasks
     *            tasks for execution
     * @return a handle to the task submitted for execution
     */
    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws
            InterruptedException {
        if (THREAD_POOL.isShutdown() || THREAD_POOL.isTerminated()) {
            THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));
        }
        return THREAD_POOL.invokeAll(tasks);
    }

    /**
     * Returns the number of available processors
     *
     * @return number of available processors
     */
    public static int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }


    double bestEnergy = 0.0D;
    State bestState = null;

    public State runWorkers(ShapeType shapeType, int alpha, int n, int age, int m) {
        int wn = workers.size();
        int wm = m / wn;
        if (m % wn != 0) {
            wm++;
        }
        final int wwm = wm;
        List<Callable<State>> callables = new ArrayList<>();
        for (int i = 0; i < wn; i++) {
            Worker worker = workers.get(i);
            worker.init(current, score);
            callables.add(() -> worker.bestHillClimbState(shapeType, alpha, n, age, wwm));
        }

        bestEnergy = 0.0D;
        bestState = null;

        Consumer<State> best = (State s) -> {
            double energy = s.energy();
            if (bestState == null || energy < bestEnergy) {
                bestEnergy = energy;
                bestState = s;
            }
        };


        try {
            invokeAll(callables).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        }
                        catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }).forEach(best);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return bestState;
    }

    /**
     * Steps the optimization/fitting algorithm.
     *
     * @param shapeTypes The shape types to use.
     * @param alpha      The alpha of the shape.
     * @param n          The number of shapes to try.
     * @param age        The number of mutations to apply to each shape.
     * @return A <code>ShapeResult</code> which contains info about the shape just added to the
     * model in this step.
     */
    public List<ShapeResult> step(List<ShapeType> shapeTypes, int alpha, int n, int age, int repeat) {
        State state = Core.bestHillClimbState(shapeTypes, alpha, n, age, target, current, buffer, score);

        List<ShapeResult> results = new ArrayList<>(1 + repeat);

        results.add(addShape(state.shape, state.alpha));
        for (int i = 0; i < repeat; i++) {
            //state.worker.init(current, score);
            double a = state.energy();
            state = Core.hillClimb(state, age, score);
            double b = state.energy();
            if (a == b) {
                break;
            }
            results.add(addShape(state.shape, state.alpha));
        }
        return results;
    }

    /**
     * Adds a shape to the model.
     *
     * @param shape The shape to add.
     * @param alpha The alpha/opacity of the shape.
     * @return Data about the shape just added to the model.
     */
    public ShapeResult addShape(Shape shape, int alpha) {
        Bitmap before = current.copy();
        List<Scanline> scanlines = shape.rasterize();
        int color = Core.computeColor(target, current, scanlines, alpha);
        Core.drawLines(current, color, scanlines);

        score = Core.differencePartial(target, before, current, score, scanlines);

        return new ShapeResult(score, color, shape);
    }

    /**
     * Gets the current image with the shapes drawn on it.
     *
     * @return The current image.
     */
    public Bitmap snapshot() {
        return current;
    }
}
