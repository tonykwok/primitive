package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Scanline;

/**
 * The <code>Rectangle</code> class describes rectangles, defined by their corner's position.
 */
public class Rectangle implements Shape {
    private int x1;
    private int y1;

    private int x2;
    private int y2;

    private int width;
    private int height;

    private Rectangle(int x1, int y1, int x2, int y2, int width, int height) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.width = width;
        this.height = height;
    }

    public static Rectangle random(int width, int height) {
        int x1 = ThreadLocalRandom.current().nextInt(width);
        int y1 = ThreadLocalRandom.current().nextInt(height);

        int x2 = Mathematics.clamp(x1 + ThreadLocalRandom.current().nextInt(32) + 1, 0, width - 1);
        int y2 = Mathematics.clamp(y1 + ThreadLocalRandom.current().nextInt(32) + 1, 0, height - 1);

        return new Rectangle(x1, y1, x2, y2, width, height);
    }

    @Override
    public Rectangle copy() {
        return new Rectangle(x1, y1, x2, y2, width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        List<Scanline> scanlines = new ArrayList<Scanline>();
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);

        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        for (int y = minY; y < maxY; y++) {
            if (x1 != x2) {
                scanlines.add(new Scanline(y, minX, maxX, 0xFFFF));
            }
        }
        return scanlines;
    }

    @Override
    public void mutate() {
        int rnd = ThreadLocalRandom.current().nextInt(2);
        switch (rnd) {
            case 0:
                x1 = Mathematics.clamp(x1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, width - 1);
                y1 = Mathematics.clamp(y1 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, height - 1);
                break;

            case 1:
                x2 = Mathematics.clamp(x2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, width - 1);
                y2 = Mathematics.clamp(y2 + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, height - 1);
                break;
        }
    }

    @Override
    public ShapeType getType() {
        return ShapeType.RECTANGLE;
    }

    @Override
    public double[] raw() {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);

        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        return new double[]{minX, minY, maxX, maxY};
    }

    @Override
    public String svg(String attrs) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);

        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        return String.format("<rect %s x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" />", attrs, minX, minY, (maxX - minX), (maxY - minY));
    }
}
