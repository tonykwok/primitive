package jxtras.primitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jxtras.primitive.image.Bitmap;
import jxtras.primitive.raster.Rasterizer;
import jxtras.primitive.raster.Scanline;
import jxtras.primitive.shape.Shape;
import jxtras.primitive.shape.ShapeFactory;
import jxtras.primitive.shape.ShapeType;

public class Worker {
    private int width, height;

    private Bitmap target, current, buffer;

    private Rasterizer rasterizer;

    private List<Scanline> scanlines;

     Random rnd;

    private double score;

     int counter;

    public Worker(Bitmap target) {
        this.width = target.getWidth();
        this.height = target.getHeight();
        this.target = target;
        this.buffer = target.copy().eraseColor(0);
        this.rasterizer = new Rasterizer(width, height);
        this.scanlines = new ArrayList<Scanline>(4096);
        // heatmap
        this.rnd = new Random();
    }

    public void init(Bitmap current, double score) {
        this.current = current;
        this.score = score;
        this.counter = 0;
        // heatmap.clear()
    }

    public double energy(Shape shape, int alpha) {
        this.counter++;
        List<Scanline> scanlines = shape.rasterize();
        // heatmap.add(scanlines);
        int color = Core.computeColor(target, current, scanlines, alpha);
        Core.copyLines(buffer, current, scanlines);
        Core.drawLines(buffer, color, scanlines);
        return Core.differencePartial(target, current, buffer, score, scanlines);
    }

    public State bestHillClimbState(ShapeType shapeType, int alpha, int n /* 1000 */, int age /* 100 */, int m /* 16 */) {
        double bestEnergy = 0.0D;
        State bestState = null;
        for (int i = 0; i < m; i++) {
            State state = bestRandomState(shapeType, alpha, n);
            double before = state.energy();
            state = Core.hillClimb2(state, age);
            double energy = state.energy();
            if (i == 0 || energy < bestEnergy) {
                bestEnergy = energy;
                bestState = state;
            }
        }
        return bestState;
    }

    private State bestRandomState(ShapeType shapeType, int alpha, int n /* 1000 */) {
        double bestEnergy = 0.0D;
        State bestState = null;
        for (int i = 0; i < n; i++) {
            State state = randomState(shapeType, alpha);
            double energy = state.energy();
            if (i == 0 || energy < bestEnergy) {
                bestEnergy = energy;
                bestState = state;
            }
        }
        return bestState;
    }

    private State randomState(ShapeType shapeType, int alpha) {
        return new State(this, ShapeFactory.createShapeOf(shapeType, width, height), alpha);
    }
}
