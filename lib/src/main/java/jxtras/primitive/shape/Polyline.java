package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Rasterizer;
import jxtras.primitive.raster.Scanline;

public class Polyline implements Shape {
    private final int[] x;
    private final int[] y;

    private final int width;
    private final int height;

    private Polyline(int[] x, int[] y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Polyline random(int width, int height) {
        int[] x = new int[4];
        int[] y = new int[4];

        x[0] = ThreadLocalRandom.current().nextInt(width);
        y[0] = ThreadLocalRandom.current().nextInt(height);
        for (int i = 1; i < 4; i++) {
            x[i] = x[0] + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
            y[i] = y[0] + (int)(ThreadLocalRandom.current().nextDouble() * 40) - 20;
        }

        return new Polyline(x, y, width, height);
    }

    @Override
    public Polyline copy() {
        return new Polyline(Arrays.copyOf(x, x.length), Arrays.copyOf(y, y.length), width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        List<Scanline> scanlines = new ArrayList<Scanline>();
        for (int i = 0; i < x.length - 1; i++) {
            int x1 = x[i];
            int y1 = y[i];

            int x2 = x[i + 1];
            int y2 = y[i + 1];

            scanlines.addAll(Rasterizer.rasterizeLine(x1, y1, x2, y2, width, height));
        }
        return scanlines;
    }

    @Override
    public void mutate() {
        int i = ThreadLocalRandom.current().nextInt(x.length);
        x[i] = Mathematics.clamp(x[i] + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, width - 1);
        y[i] = Mathematics.clamp(y[i] + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, height - 1);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.POLYLINE;
    }

    @Override
    public double[] raw() {
        double[] raw = new double[x.length + y.length];
        for (int i = 0; i < x.length; i++) {
            raw[i * 2 + 0] = x[i];
            raw[i * 2 + 1] = y[i];
        }
        return raw;
    }

    @Override
    public String svg(String attrs) {
        StringBuilder svg = new StringBuilder();
        svg.append(String.format("<polyline %s stroke-width=\"%f\" points=\"", attrs, 1.0f));

        for (int a = 0; a < x.length; a++) {
            svg.append(String.format("%d,%d", x[a], y[a]));
            if (a <= x.length - 2) {
                svg.append(",");
            }
        }

        svg.append("\" />");

        return svg.toString();
    }
}
