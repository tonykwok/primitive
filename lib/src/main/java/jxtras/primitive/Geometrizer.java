package jxtras.primitive;

import java.util.List;

import jxtras.primitive.Model.ShapeResult;
import jxtras.primitive.image.Bitmap;
import jxtras.primitive.util.Colour;
import jxtras.primitive.shape.ShapeType;

public class Geometrizer {
    /**
     * The model for the optimization/fitting algorithm.
     */
    private final Model model;

    /**
     * Creates a <code>Geometrizer</code> to be used for recreating the given image.
     *
     * @param image The target image to be recreated with geometric primitives
     */
    public Geometrizer(Bitmap image) {
        this(image, Colour.average(image.getPixels()), 1024);
    }

    /**
     * Creates a <code>Geometrizer</code> to be used for recreating the given image.
     *
     * @param image      The target image to be recreated with geometric primitives
     * @param background The background color (in ARGB888 format) of the canvas for
     *                   this <code>Geometrizer</code> to draw on
     */
    public Geometrizer(Bitmap image, int background) {
        this(image, background, 1024);
    }

    /**
     * Creates a <code>Geometrizer</code> to be used for recreating the given image.
     *
     * @param image      The target image to be recreated with geometric primitives
     * @param background The background color (in ARGB888 format) of the canvas for
     *                   this <code>Geometrizer</code> to draw on
     * @param size       The output image size
     */
    public Geometrizer(Bitmap image, int background, int size) {
        model = new Model(image, background, size);
    }

    /**
     * Updates the model once.
     *
     * @param shapeTypes The types of shapes to use when generating the image.
     * @return A <ode>ShapeResult</code> which contains info about the shape that
     * just added to the model on this step.
     */
    public ShapeResult step(List<ShapeType> shapeTypes) {
        return model.step(shapeTypes, 128, 1000, 100, 0).get(0);
    }

    /**
     * Updates the model once.
     *
     * @param shapeTypes The types of shapes to use when generating the image.
     * @param alpha      The opacity of the shapes (0-255).
     * @return A <ode>ShapeResult</code> which contains info about the shape that
     * just added to the model on this step.
     */
    public ShapeResult step(List<ShapeType> shapeTypes, int alpha) {
        return model.step(shapeTypes, alpha, 1000, 100, 0).get(0);
    }

    /**
     * Updates the model once.
     *
     * @param shapeTypes             The types of shapes to use when generating the image.
     * @param alpha                  The opacity of the shapes (0-255).
     * @param candidateShapesPerStep The number of candidate shapes to try for this iteration.
     * @return A <ode>ShapeResult</code> which contains info about the shape that
     * just added to the model on this step.
     */
    public ShapeResult step(List<ShapeType> shapeTypes, int alpha, int candidateShapesPerStep) {
        return model.step(shapeTypes, alpha, candidateShapesPerStep, 100, 0).get(0);
    }

    /**
     * Updates the model once.
     *
     * @param shapeTypes             The types of shapes to use when generating the image.
     * @param alpha                  The opacity of the shapes (0-255).
     * @param candidateShapesPerStep The number of candidate shapes to try per model step.
     * @param shapeMutationsPerStep  The number of times to mutate each candidate shape.
     * @return A <ode>ShapeResult</code> which contains info about the shape that
     * just added to the model on this step.
     */
    public ShapeResult step(List<ShapeType> shapeTypes, int alpha, int candidateShapesPerStep, int shapeMutationsPerStep) {
        return model.step(shapeTypes, alpha, candidateShapesPerStep, shapeMutationsPerStep, 0).get(0);
    }

    /**
     * Updates the model once.
     *
     * @param shapeTypes             The types of shapes to use when generating the image.
     * @param alpha                  The opacity of the shapes (0-255).
     * @param candidateShapesPerStep The number of candidate shapes to try per model step.
     * @param shapeMutationsPerStep  The number of times to mutate each candidate shape.
     * @param repeat                 The number of extra shapes added in each iteration with reduced search (mostly good for beziers)
     * @return A <ode>ShapeResult</code> which contains info about the shape that
     * just added to the model on this step.
     */
    public List<ShapeResult> step(List<ShapeType> shapeTypes, int alpha, int candidateShapesPerStep, int shapeMutationsPerStep, int repeat) {
        return model.step(shapeTypes, alpha, candidateShapesPerStep, shapeMutationsPerStep, repeat);
    }

    /**
     * Gets the current image with the shapes drawn on it.
     *
     * @return The current image.
     */
    public Bitmap snapshot() {
        return model.current;
    }

    public Bitmap target() {
        return model.target;
    }

    public double getScore() {
        return model.score;
    }

    public Model getModel() {
        return model;
    }
}