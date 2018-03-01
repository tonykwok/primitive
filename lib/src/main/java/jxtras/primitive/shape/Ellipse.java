package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Scanline;

/**
 * The <code>Ellipse</code> class describes an ellipse based on its center coordinate, and both
 * its x and y radius.
 */
public class Ellipse implements Shape {
    /**
     * The Y-coordinate of the center of the ellipse.
     */
    private int cx;

    /**
     * The X-coordinate of the center of the ellipse.
     */
    private int cy;

    /**
     * The X-radius of the ellipse.
     */
    private int rx;

    /**
     * The Y-radius of the ellipse.
     */
    private int ry;

    /**
     * The X-bound of the whole canvas.
     */
    private int width;

    /**
     * The Y-bound of the whole canvas.
     */
    private int height;

    private Ellipse(int cx, int cy, int rx, int ry, int width, int height) {
        this.cx = cx;
        this.cy = cy;
        this.rx = rx;
        this.ry = ry;
        this.width = width;
        this.height = height;
    }

    public static Ellipse random(int width, int height) {
        final int cx = ThreadLocalRandom.current().nextInt(width);
        final int cy = ThreadLocalRandom.current().nextInt(height);
        final int rx = ThreadLocalRandom.current().nextInt(32) + 1;
        final int ry = ThreadLocalRandom.current().nextInt(32) + 1;

        return new Ellipse(cx, cy, rx, ry, width, height);
    }

    @Override
    public Ellipse copy() {
        return new Ellipse(cx, cy, rx, ry, width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        final List<Scanline> scanlines = new ArrayList<Scanline>();
        final double aspect = (double) rx  / (double) ry;
        for (int dy = 0; dy < ry; dy++) {
            int y1 = cy - dy;
            int y2 = cy + dy;
            if ((y1 < 0 || y1 >= height) && (y2 < 0 || y2 >= height)) {
                continue;
            }
            int s = (int) (Math.sqrt((double) (ry * ry - dy * dy)) * aspect);
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
        final int rnd = ThreadLocalRandom.current().nextInt(3);
        switch (rnd) {
            case 0:
                cx = Mathematics.clamp(cx + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, width - 1);
                cy = Mathematics.clamp(cy + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, height - 1);
                break;

            case 1:
                rx = Mathematics.clamp(rx + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 1, width - 1);
                break;

            case 2:
                ry = Mathematics.clamp(ry + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 1, height - 1);
                break;
        }
    }


    @Override
    public ShapeType getType() {
        return ShapeType.ELLIPSE;
    }

    @Override
    public double[] raw() {
        return new double[]{cx, cy, rx, ry};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        return String.format("<ellipse %s cx=\"%d\" cy=\"%d\" rx=\"%d\" ry=\"%d\" />", attrs, cx, cy, rx, ry);
        // @formatter:on
    }
}