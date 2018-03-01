package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Rasterizer;
import jxtras.primitive.raster.Scanline;

/**
 * The {@code CubiCurve} class defines a cubic B&eacute;zier parametric curve segment
 * that intersects both the specified coordinates {@code (x1, y1)} and {@code (x2, y2)},
 * using the specified points {@code (ctrlX1, ctrlY1)} and {@code (ctrlX2, ctrlY2)}
 * as B&eacute;zier control points.
 */
public class CubicCurve implements Shape {
    /**
     * The X coordinate of the start point
     * of the cubic curve segment.
     */
    private int x1;

    /**
     * The Y coordinate of the start point
     * of the cubic curve segment.
     */
    private int y1;

    /**
     * The X coordinate of the first control point
     * of the cubic curve segment.
     */
    private int ctrlX1;

    /**
     * The Y coordinate of the first control point
     * of the cubic curve segment.
     */
    private int ctrlY1;

    /**
     * The X coordinate of the second control point
     * of the cubic curve segment.
     */
    private int ctrlX2;

    /**
     * The Y coordinate of the second control point
     * of the cubic curve segment.
     */
    private int ctrlY2;

    /**
     * The X coordinate of the end point
     * of the cubic curve segment.
     */
    private int x2;

    /**
     * The Y coordinate of the end point
     * of the cubic curve segment.
     */
    private int y2;

    private double strokeWidth;
    private int width, height;

    /**
     * Constructs and initializes a <code>QuadraticCurve</code> from the
     * specified {@code int} coordinates.
     *
     * @param x1     the X coordinate of the start point
     * @param y1     the Y coordinate of the start point
     * @param ctrlX1 the X coordinate of the first control point
     * @param ctrlY1 the Y coordinate of the first control point
     * @param ctrlX2 the X coordinate of the second control point
     * @param ctrlY2 the Y coordinate of the second control point
     * @param x2     the X coordinate of the end point
     * @param y2     the Y coordinate of the end point
     */
    private CubicCurve(int x1, int y1, int ctrlX1, int ctrlY1, int ctrlX2, int ctrlY2, int x2,
            int y2, double strokeWidth, int width, int height) {
        this.x1 = x1;
        this.y1 = y1;
        this.ctrlX1 = ctrlX1;
        this.ctrlY1 = ctrlY1;
        this.ctrlX2 = ctrlX2;
        this.ctrlY2 = ctrlY2;
        this.x2 = x2;
        this.y2 = y2;
        this.strokeWidth = strokeWidth;
        this.width = width;
        this.height = height;
    }

    public static CubicCurve random(int width, int height) {
        int x1 = ThreadLocalRandom.current().nextInt(width);
        int y1 = ThreadLocalRandom.current().nextInt(height);
        int ctrlX1 = x1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int ctrlY1 = y1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int ctrlX2 = ctrlX1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int ctrlY2 = ctrlY1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int x2 = ctrlX2 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int y2 = ctrlY2 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        double strokeWidth = 1.0D / 2;

        CubicCurve curve = new CubicCurve(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2, strokeWidth, width, height);
        curve.mutate();

        return curve;
    }

    @Override
    public CubicCurve copy() {
        // @formatter:off
        return new CubicCurve(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2, strokeWidth, width, height);
        // @formatter:on
    }

    @Override
    public List<Scanline> rasterize() {
        final int pointCount = 20;
        int[] xx = new int[pointCount + 1];
        int[] yy = new int[pointCount + 1];
        for (int i = 0; i <= pointCount; i++) {
            // See https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B%C3%A9zier_curves
            double t = (double)i / (double)pointCount;
            double u = 1 - t;
            // @formatter:off
            xx[i] = (int)(x1 * u * u * u + 3 * ctrlX1 * u * u * t + 3 * ctrlX2 * u * t * t + x2 * t * t * t);
            yy[i] = (int)(y1 * u * u * u + 3 * ctrlY1 * u * u * t + 3 * ctrlY2 * u * t * t + y2 * t * t * t);
            // @formatter:on
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
            final int rnd = ThreadLocalRandom.current().nextInt(5);
            switch (rnd) {
                case 0:
                    x1 = Mathematics.clamp(x1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    y1 = Mathematics.clamp(y1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 1:
                    ctrlX1 = Mathematics.clamp(ctrlX1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    ctrlY1 = Mathematics.clamp(ctrlY1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 2:
                    ctrlX2 = Mathematics.clamp(ctrlX2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    ctrlY2 = Mathematics.clamp(ctrlY2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 3:
                    x2 = Mathematics.clamp(x2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    y2 = Mathematics.clamp(y2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 4:
                    strokeWidth = Mathematics.clamp(strokeWidth + ThreadLocalRandom.current().nextGaussian(), 1, 16);
                    break;
            }
        } while (!isValid());
    }

    private boolean isValid() {
        int dx12 = (int)(x1 - ctrlX1);
        int dy12 = (int)(y1 - ctrlY1);
        int dx23 = (int)(ctrlX1 - ctrlX2);
        int dy23 = (int)(ctrlY1 - ctrlY2);
        int dx34 = (int)(ctrlX2 - x2);
        int dy34 = (int)(ctrlY2 - y2);
        int dx13 = (int)(x1 - x2);
        int dy13 = (int)(y1 - y2);
        int d12 = dx12 * dx12 + dy12 * dy12;
        int d23 = dx23 * dx23 + dy23 * dy23;
        int d34 = dx34 * dx34 + dy34 * dy34;
        int d13 = dx13 * dx13 + dy13 * dy13;
        return d13 > d12 && d13 > d23 && d13 > d34;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.CUBIC_CURVE;
    }

    @Override
    public double[] raw() {
        return new double[]{x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2, strokeWidth};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        // TODO(tonykwok): support stroke-with
        return String.format("<path %s stroke-width=\"%f\" d=\"M %f %f C %f %f, %f %f, %f %f\" />", attrs, 1.0 /*strokeWidth*/,
                (double)x1, (double)y1, (double)ctrlX1, (double)ctrlY1, (double)ctrlX2, (double)ctrlY2, (double)x2, (double)y2);
        // @formatter:on
    }
}
