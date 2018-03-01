package jxtras.primitive.shape;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Rasterizer;
import jxtras.primitive.raster.Scanline;

public class Triangle implements Shape {
    private int x1;
    private int y1;

    private int x2;
    private int y2;

    private int x3;
    private int y3;

    private int width;
    private int height;

    private Triangle(int x1, int y1, int x2, int y2, int x3, int y3, int width, int height) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.width = width;
        this.height = height;
    }

    public static Triangle random(int width, int height) {
        int x1 = ThreadLocalRandom.current().nextInt(width);
        int y1 = ThreadLocalRandom.current().nextInt(height);

        int x2 = x1 + ThreadLocalRandom.current().nextInt(31) - 15;
        int y2 = y1 + ThreadLocalRandom.current().nextInt(31) - 15;

        int x3 = x1 + ThreadLocalRandom.current().nextInt(31) - 15;
        int y3 = y1 + ThreadLocalRandom.current().nextInt(31) - 15;

        Triangle triangle = new Triangle(x1, y1, x2, y2, x3, y3, width, height);
        triangle.mutate();

        return triangle;
    }

    @Override
    public Triangle copy() {
        return new Triangle(x1, y1, x2, y2, x3, y3, width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        return Rasterizer.rasterizeTriangle(x1, y1, x2, y2, x3, y3, width, height);
    }

    @Override
    public void mutate() {
        final int m = 16;
        do {
            int rnd = ThreadLocalRandom.current().nextInt(3);
            switch (rnd) {
                case 0:
                    x1 = Mathematics.clamp(x1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    y1 = Mathematics.clamp(y1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 1:
                    x2 = Mathematics.clamp(x2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    y2 = Mathematics.clamp(y2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;

                case 2:
                    x3 = Mathematics.clamp(x3 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, width - 1 + m);
                    y3 = Mathematics.clamp(y3 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), -m, height - 1 + m);
                    break;
            }
        } while (!isValid());
    }

    private boolean isValid() {
        final int minDegree = 15;
        double a1;
        {
            double dx1 = x2 - x1;
            double dy1 = y2 - y1;
            double dx2 = x3 - x1;
            double dy2 = y3 - y1;
            double d1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
            double d2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

            dx1 /= d1;
            dy1 /= d1;
            dx2 /= d2;
            dy2 /= d2;

            a1 = Mathematics.rad2deg(Math.acos(dx1 * dx2 + dy1 * dy2));
        }

        double a2;
        {
            double dx1 = x1 - x2;
            double dy1 = y1 - y2;
            double dx2 = x3 - x2;
            double dy2 = y3 - y2;
            double d1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
            double d2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

            dx1 /= d1;
            dy1 /= d1;
            dx2 /= d2;
            dy2 /= d2;

            a2 = Mathematics.rad2deg(Math.acos(dx1 * dx2 + dy1 * dy2));
        }

        double a3 = 180 - a1 - a2;

        return a1 > minDegree && a2 > minDegree && a3 > minDegree;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.TRIANGLE;
    }

    @Override
    public double[] raw() {
        return new double[]{x1, y1, x2, y2, x3, y3};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        return String.format("<polygon %s points=\"%d,%d,%d,%d,%d,%d\" />", attrs, x1, y1, x2, y2, x3, y3);
        // @formatter:on
    }
}
