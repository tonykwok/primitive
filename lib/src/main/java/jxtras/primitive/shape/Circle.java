package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Scanline;

/**
 * The {@code Circle} class defines a circle with the specified
 * radius and center location.
 */
public class Circle implements Shape {
    /**
     * The Y-coordinate of the center of the circle.
     */
    private int cx;

    /**
     * The X-coordinate of the center of the circle.
     */
    private int cy;

    /**
     * The radius of the circle.
     */
    private int r;

    /**
     * A rectangular bounding box that the <code>Circle</code>
     * lies entirely within.
     */
    private int x, y, width, height;

    /**
     * Creates a new instance of Circle with a specified position and radius.
     *
     * @param cx the horizontal position of the center of the circle in pixels
     * @param cy the vertical position of the center of the circle in pixels
     * @param r  the radius of the circle in pixels
     */
    private Circle(int cx, int cy, int r, int width, int height) {
        this.cx = cx;
        this.cy = cy;
        this.r = r;
        this.width = width;
        this.height = height;
    }

    /**
     * Randomly creates a new instance of Circle within the given bounds.
     *
     * @param width  The width of the bounds
     * @param height The height of the bounds
     * @return The new circle
     */
    public static Circle random(int width, int height) {
        int centerX = ThreadLocalRandom.current().nextInt(width);
        int centerY = ThreadLocalRandom.current().nextInt(height);
        int radius = ThreadLocalRandom.current().nextInt(32) + 1;
        return new Circle(centerX, centerY, radius, width, height);
    }

    @Override
    public Circle copy() {
        return new Circle(cx, cy, r, width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        final List<Scanline> scanlines = new ArrayList<Scanline>();
        for (int dy = 0; dy < r; dy++) {
            int y1 = cy - dy;
            int y2 = cy + dy;
            if ((y1 < 0 || y1 >= height) && (y2 < 0 || y2 >= height)) {
                continue;
            }
            int s = (int) Math.sqrt((double) (r * r - dy * dy));
            int x1 = cx - s;
            int x2 = cx + s;
            if (x1 < 0) {
                x1 = 0;
            }
            if (x2 >= width) {
                x2 = width - 1;
            }
            if (y1 >= 0 && y1 < height) {
                scanlines.add(new Scanline(y1, x1, x2, 0xFFFF));
            }
            if (y2 >= 0 && y2 < height && dy > 0) {
                scanlines.add(new Scanline(y2, x1, x2, 0xFFFF));
            }
        }
        return scanlines;
    }

    @Override
    public void mutate() {
        final int mutationStepSize = 16;
        int rnd = ThreadLocalRandom.current().nextInt(2);
        switch (rnd) {
            case 0:
                cx = Mathematics.clamp((int)(cx + ThreadLocalRandom.current().nextGaussian() * mutationStepSize), 0, width - 1);
                cy = Mathematics.clamp((int)(cy + ThreadLocalRandom.current().nextGaussian() * mutationStepSize), 0, height - 1);
                break;

            case 1:
                r = Mathematics.clamp((int)(r + ThreadLocalRandom.current().nextGaussian() * mutationStepSize), 1, width - 1);
                r = Mathematics.clamp(r, 1, height - 1);
                break;
        }
    }

    @Override
    public ShapeType getType() {
        return ShapeType.CIRCLE;
    }

    @Override
    public double[] raw() {
        return new double[]{cx, cy, r};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        return String.format("<circle %s cx=\"%d\" cy=\"%d\" r=\"%d\" />", attrs, cx, cy, r);
        // @formatter:on
    }
}
