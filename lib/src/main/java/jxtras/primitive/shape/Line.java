package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Scanline;

/**
 * The <code>Line</code> class describes a line connecting two points.
 */
public class Line implements Shape {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private double strokeWidth;
    private int width, height;

    private Line(int x1, int y1, int x2, int y2, double strokeWidth, int width, int height) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.strokeWidth = strokeWidth;
        this.width = width;
        this.height = height;
    }

    public static Line random(int width, int height) {
        int x1 = ThreadLocalRandom.current().nextInt(width);
        int y1 = ThreadLocalRandom.current().nextInt(height);
        int x2 = x1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        int y2 = y1 + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        double strokeWidth = 1.0D / 2;
        return new Line(x1, y1, x2, y2, strokeWidth, width, height);
    }

    @Override
    public Line copy() {
        return new Line(x1, y1, x2, y2, strokeWidth, width, height);
    }

    /**
     * See http://members.chello.at/easyfilter/bresenham.html for details.
     */
    @Override
    public List<Scanline> rasterize() {
        List<Scanline> scanlines = new ArrayList<Scanline>();

        int x = x1;
        int y = y1;
        int dx = +Math.abs(x - x2), sx = x < x2 ? 1 : -1;
        int dy = -Math.abs(y - y2), sy = y < y2 ? 1 : -1;
        int error = dx + dy, e2;
        for (;;) {
            scanlines.add(new Scanline(y, x, x, 0xFFFF));
            if (x == x2 && y == y2) {
                break;
            }
            e2 = 2 * error;
            if (e2 >= dy) {
                error += dy;
                x += sx;
            }
            if (e2 <= dx) {
                error += dx;
                y += sy;
            }
        }

        return Scanline.crop(scanlines, width, height);
    }

    @Override
    public void mutate() {
        final int rnd = ThreadLocalRandom.current().nextInt(3);
        switch (rnd) {
            case 0:
                x1 = Mathematics.clamp(x1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, width - 1);
                y1 = Mathematics.clamp(y1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, height - 1);
                break;

            case 1:
                x2 = Mathematics.clamp(x2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, width - 1);
                y2 = Mathematics.clamp(y2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, height - 1);
                break;

            case 2:
                strokeWidth = Mathematics.clamp(strokeWidth + ThreadLocalRandom.current().nextGaussian(), 1, 16);
                break;
        }
    }

    @Override
    public ShapeType getType() {
        return ShapeType.LINE;
    }

    @Override
    public double[] raw() {
        return new double[]{x1, y1, x2, y2};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        // TODO(tonykwok): support stroke-with
        return String.format("<line %s stroke-width=\"%f\" x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" />", attrs, 1.0 /*strokeWidth*/, x1, y1, x2, y2);
        // @formatter:on
    }
}
