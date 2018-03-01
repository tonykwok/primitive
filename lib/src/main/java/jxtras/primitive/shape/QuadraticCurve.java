package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.raster.Rasterizer;
import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Scanline;

/**
 * The {@code QuadraticCurve} class defines a quadratic B&eacute;zier parametric
 * curve segment that intersects both the specified coordinates {@code (x1, y1)}
 * and {@code (x2, enfY)}, using the specified point {@code (ctrlX, ctrlY)} as
 * B&eacute;zier control point.
 */
public class QuadraticCurve implements Shape {
    /**
     * The X coordinate of the start point of the quadratic curve segment.
     */
    private int x1;

    /**
     * The Y coordinate of the start point of the quadratic curve segment.
     */
    private int y1;

    /**
     * The X coordinate of the control point of the quadratic curve segment.
     */
    private int ctrlX;

    /**
     * The Y coordinate of the control point of the quadratic curve segment.
     */
    private int ctrlY;

    /**
     * The X coordinate of the end point of the quadratic curve segment.
     */
    private int x2;

    /**
     * The Y coordinate of the end point of the quadratic curve segment.
     */
    private int y2;

    private double strokeWidth;
    private int width, height;

    private final BoundingBox bounds = new BoundingBox();

    /**
     * Constructs and initializes a <code>QuadraticCurve</code> from the
     * specified {@code int} coordinates.
     *
     * @param x1 the X coordinate of the start point
     * @param y1 the Y coordinate of the start point
     * @param ctrlX the X coordinate of the control point
     * @param ctrlY the Y coordinate of the control point
     * @param x2 the X coordinate of the end point
     * @param y2 the Y coordinate of the end point
     */
    private QuadraticCurve(int x1, int y1, int ctrlX, int ctrlY, int x2, int y2, double strokeWidth,
            int width, int height) {
        this.x1 = x1;
        this.y1 = y1;
        this.ctrlX = ctrlX;
        this.ctrlY = ctrlY;
        this.x2 = x2;
        this.y2 = y2;
        this.strokeWidth = strokeWidth;
        this.width = width;
        this.height = height;
    }

    public static QuadraticCurve random(int width, int height) {
        int x1 = ThreadLocalRandom.current().nextInt(width);
        int y1 = ThreadLocalRandom.current().nextInt(height);
        int x2 = x1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int y2 = y1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int x3 = x2 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int y3 = y2 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        double strokeWidth = 1.0D / 2;

        QuadraticCurve instance = new QuadraticCurve(x1, y1, x2, y2, x3, y3, strokeWidth, width, height);
        instance.mutate();

        return instance;
    }

    @Override
    public QuadraticCurve copy() {
        return new QuadraticCurve(x1, y1, ctrlX, ctrlY, x2, y2, strokeWidth, width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        final int pointCount = 20;
        int[] xx = new int[pointCount + 1];
        int[] yy = new int[pointCount + 1];
        for (int i = 0; i <= pointCount; i++) {
            // See https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_B%C3%A9zier_curves
            double t = (double) i / (double) pointCount;
            double u = 1 - t;
            xx[i] = (int)(x1 * u * u + 2 * ctrlX * u * t + x2 * t * t);
            yy[i] = (int)(y1 * u * u + 2 * ctrlY * u * t + y2 * t * t);
        }

        List<Scanline> scanlines = new ArrayList<Scanline>();
        for (int i = 0; i < pointCount; i++) {
            int x1 = xx[i];
            int y1 = yy[i];

            int x2 = xx[i + 1];
            int y2 = yy[i + 1];

            scanlines.addAll(Rasterizer.rasterizeLine(x1, y1, x2, y2, width, height));
        }
        return scanlines;
    }

    @Override
    public void mutate() {
        final int m = 16;
        do {
            final int rnd = ThreadLocalRandom.current().nextInt(4);
            switch (rnd) {
                case 0:
                    x1 = Mathematics.clamp(x1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    y1 = Mathematics.clamp(y1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 1:
                    ctrlX = Mathematics.clamp(ctrlX + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    ctrlY = Mathematics.clamp(ctrlY + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 2:
                    x2 = Mathematics.clamp(x2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    y2 = Mathematics.clamp(y2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 3:
                    strokeWidth = Mathematics.clamp(strokeWidth + ThreadLocalRandom.current().nextGaussian(), 1, 16);
                    break;
            }
        } while (!isValid());
    }

    private boolean isValid() {
        int dx12 = (int)(x1 - ctrlX);
        int dy12 = (int)(y1 - ctrlY);
        int dx23 = (int)(ctrlX - x2);
        int dy23 = (int)(ctrlY - y2);
        int dx13 = (int)(x1 - x2);
        int dy13 = (int)(y1 - y2);
        int d12 = dx12 * dx12 + dy12 * dy12;
        int d23 = dx23 * dx23 + dy23 * dy23;
        int d13 = dx13 * dx13 + dy13 * dy13;
        return d13 > d12 && d13 > d23;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.QUADRATIC_CURVE;
    }

    @Override
    public double[] raw() {
        return new double[]{x1, y1, ctrlX, ctrlY, x2, y2, strokeWidth};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        // TODO(tonykwok): support stroke-with
        return String.format("<path %s stroke-width=\"%f\" d=\"M %f %f Q %f %f, %f %f\" />",
                attrs, strokeWidth, (double)x1, (double)y1, (double)ctrlX, (double)ctrlY, (double)x2, (double)y2);
        // @formatter:on
    }
}
