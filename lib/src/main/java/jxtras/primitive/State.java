package jxtras.primitive;

import java.util.Random;

import jxtras.primitive.image.Bitmap;
import jxtras.primitive.shape.Shape;
import jxtras.primitive.util.Mathematics;

public class State {
    Worker worker;
    /**
     * The geometric shape owned by the state.
     */
    public Shape shape;

    /**
     * The alpha of the shape.
     */
    public int alpha;
    boolean mutateAlpha;

    /**
     * The score of the state, a measure of the improvement applying the state to the current
     * bitmap will have.
     */
    public double score;

    Bitmap target;
    Bitmap current;
    Bitmap buffer;

    public State(Worker worker, Shape shape, int alpha) {
        boolean mutateAlpha = false;
        if (alpha == 0) {
            alpha = 128;
            mutateAlpha = true;
        }
        this.worker = worker;
        this.shape = shape;
        this.alpha = alpha;
        this.mutateAlpha = mutateAlpha;
        this.score = -1;
    }

    private State(Worker worker, Shape shape, int alpha, boolean mutateAlpha, double score) {
        this.worker = worker;
        this.shape = shape;
        this.alpha = alpha;
        this.mutateAlpha = mutateAlpha;
        this.score = score;
    }

    public double energy() {
//        if (score >= 0) {
//            throw new IllegalStateException("Score was not reset");
//        }
        if (score < 0) {
            score = worker.energy(shape, alpha);
        }
        return score;
    }

    public State move() {
        Random rnd = worker.rnd;
        State oldState = copy2();
        shape.mutate();
        if (mutateAlpha) {
            alpha = Mathematics.clamp(alpha + rnd.nextInt(21) - 10, 1, 255);
        }
        score = -1;
        return oldState;
    }

    public void undoMove(State oldState) {
        shape = oldState.shape;
        alpha = oldState.alpha;
        score = oldState.score;
    }

    public State copy2() {
        return new State(worker, shape.copy(), alpha, mutateAlpha, score);
    }

    /**
     * Create a new state.
     *
     * @param shape   A geometric shape.
     * @param alpha   The color alpha of the geometric shape.
     * @param target  The target bitmap.
     * @param current The current bitmap.
     * @param buffer  The buffer bitmap.
     */
    private State(Shape shape, int alpha, boolean mutateAlpha, double score, Bitmap target,
            Bitmap current, Bitmap buffer) {
        this.shape = shape;
        this.alpha = alpha;
        this.mutateAlpha = mutateAlpha;
        this.score = score;
        this.target = target;
        this.current = current;
        this.buffer = buffer;
    }

    /**
     * Create a new state.
     *
     * @param shape   A geometric shape.
     * @param alpha   The color alpha of the geometric shape.
     * @param target  The target bitmap.
     * @param current The current bitmap.
     * @param buffer  The buffer bitmap.
     */
    public State(Shape shape, int alpha, Bitmap target, Bitmap current, Bitmap buffer) {
        this.shape = shape;
//        if (alpha == 0) {
//            this.alpha = 128;
//            this.mutateAlpha = true;
//        } else {
            this.alpha = alpha;
            this.mutateAlpha = false;
//        }
        this.score = -1.0D;

        this.target = target;
        this.current = current;
        this.buffer = buffer;
    }

    /**
     * Calculates a measure of the improvement drawing the shape to the current bitmap will have.
     * The lower the energy, the better. The score is cached, set it to < 0 to recalculate it.
     *
     * @param lastScore The last score recorded by the model.
     * @return The energy measure.
     */
    public double energy(double lastScore) {
        if (score >= 0) {
            throw new IllegalStateException("Score was not reset");
        }
        if (score < 0) {
            score = Core.energy(shape, alpha, target, current, buffer, lastScore);
        }
        return score;
    }

    /**
     * Modify the current state in a random fashion.
     *
     * @return The old state - in case we want to go back to the old state.
     */
    public State mutate() {
        State oldState = copy();
        shape.mutate();
//        if (mutateAlpha) {
//            alpha = Util.clampInt(alpha + Util.getRandom().nextInt(21) - 10, 1, 255);
//        }
        score = -1;
        return oldState;
    }

    /**
     * Creates a copy of the state. Deep copy of the shape and alpha, shallow copy of the bitmap
     * buffers. Score not copied.
     *
     * @return The cloned state.
     */
    public State copy() {
        return new State(shape.copy(), alpha, mutateAlpha, score, target, current, buffer);
    }
}
