package jxtras.primitive.shape;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Rasterizer;
import jxtras.primitive.raster.Scanline;

public class Polygon implements Shape {
    /**
     * number of coordinates
     */
    private int order;
    /**
     * x coordinates
     */
    private int[] x;
    /**
     * y coordinates
     */
    private int[] y;
    /**
     * The convexity of the polygon. A polygon is convex if it has a single contour, and only
     * ever curves in a single direction.
     */
    private boolean isConvex;

    private int width;
    private int height;

    private Polygon(int[] x, int[] y, int order, boolean convex, int width, int height) {
        this.x = x;
        this.y = y;
        this.order = order;
        this.isConvex = convex;
        this.width = width;
        this.height = height;
    }

    public static Polygon random(int width, int height) {
        return random(4, false, width, height);
    }

    public static Polygon random(int order, boolean convex, int width, int height) {
        int[] x = new int[order];
        int[] y = new int[order];

        x[0] = (int)(ThreadLocalRandom.current().nextDouble(width));
        y[0] = (int)(ThreadLocalRandom.current().nextDouble(height));
        for (int i = 1; i < order; i++) {
            x[i] = y[0] + (int)(ThreadLocalRandom.current().nextDouble(40)) - 20;
            y[i] = y[0] + (int)(ThreadLocalRandom.current().nextDouble(40)) - 20;
        }

        Polygon polygon = new Polygon(x, y, order, convex, width, height);
        polygon.mutate();
        return polygon;
    }

    @Override
    public Polygon copy() {
        return new Polygon(Arrays.copyOf(x, x.length), Arrays.copyOf(y, y.length), order, isConvex,
                width, height);
    }

    /**
     * See https://imagej.nih.gov/ij/developer/source/ij/process/PolygonFiller.java.html for
     * details.
     */
    @Override
    public List<Scanline> rasterize() {
        return Rasterizer.rasterizePolygon(x, y, order, width, height);
    }

    @Override
    public void mutate() {
        final int mutationStepSize = 16;
        do {
            if (ThreadLocalRandom.current().nextDouble() < 0.25D) {
                int i = ThreadLocalRandom.current().nextInt(order);
                int j = ThreadLocalRandom.current().nextInt(order);
                if (i == j) {
                    continue;
                }
                // swap(xi, xj)
                int xi = x[i];
                x[i] = x[j];
                x[j] = xi;
                // swap(yi, yj)
                int yi = y[i];
                y[i] = y[j];
                y[j] = yi;
            } else {
                int i = ThreadLocalRandom.current().nextInt(order);
                x[i] = Mathematics.clamp((int)(x[i] + ThreadLocalRandom.current().nextGaussian() * mutationStepSize), -mutationStepSize,
                        width - 1 + mutationStepSize);
                y[i] = Mathematics.clamp((int)(y[i] + ThreadLocalRandom.current().nextGaussian() * mutationStepSize), -mutationStepSize,
                        height - 1 + mutationStepSize);
            }
        } while (!isVaild());
    }

    private boolean isVaild() {
        if (!isConvex) {
            return true;
        }
        boolean sign = false;
        for (int a = 0; a < order; a++) {
            int i = (a + 0) % order;
            int j = (a + 1) % order;
            int k = (a + 2) % order;
            double c = cross3(x[i], y[i], x[j], y[j], x[k], y[k]);
            if (a == 0) {
                sign = (c > 0);
            } else if ((c > 0) != sign) {
                return false;
            }
        }
        return true;
    }

    private double cross3(double x1, double y1, double x2, double y2, double x3, double y3) {
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        double dx2 = x3 - x2;
        double dy2 = y3 - y2;
        return dx1 * dy2 - dy1 * dx2;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.POLYGON;
    }

    @Override
    public double[] raw() {
        double[] raw = new double[order * 2];
        for (int i = 0; i < order; i++) {
            raw[i * 2 + 0] = x[i];
            raw[i * 2 + 1] = y[i];
        }
        return raw;
    }

    @Override
    public String svg(String attrs) {
        StringBuilder svg = new StringBuilder();
        svg.append(String.format("<polygon %s points=\"", attrs));

        for (int a = 0; a < order; a++) {
            svg.append(String.format("%d,%d", x[a], y[a]));
            if (a <= order - 2) {
                svg.append(",");
            }
        }

        svg.append("\" />");

        return svg.toString();
    }
}
