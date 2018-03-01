package jxtras.primitive.shape;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Rasterizer;
import jxtras.primitive.raster.Scanline;

public class RotatedEllipse implements Shape {
    private int cx;
    private int cy;
    private int rx;
    private int ry;

    private int angle;

    private int width;
    private int height;

    private RotatedEllipse(int cx, int cy, int rx, int ry, int angle, int width, int height) {
        this.cx = cx;
        this.cy = cy;
        this.rx = rx;
        this.ry = ry;
        this.angle = angle;
        this.width = width;
        this.height = height;
    }

    public static RotatedEllipse random(int width, int height) {
        int cx = ThreadLocalRandom.current().nextInt(width);
        int cy = ThreadLocalRandom.current().nextInt(height);
        int rx = ThreadLocalRandom.current().nextInt(32) + 1;
        int ry = ThreadLocalRandom.current().nextInt(32) + 1;
        int angle = ThreadLocalRandom.current().nextInt(360);

        return new RotatedEllipse(cx, cy, rx, ry, angle, width, height);
    }

    @Override
    public RotatedEllipse copy() {
        return new RotatedEllipse(cx, cy, rx, ry, angle, width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        int pointCount = 20;
        int[] x = new int[pointCount];
        int[] y = new int[pointCount];

        double rads = angle * (Math.PI / 180.0);
        double c = Math.cos(rads);
        double s = Math.sin(rads);

        for (int i = 0; i < pointCount; i++) {
            double rot = ((360.0 / pointCount) * i) * (Math.PI / 180.0);
            double crx = rx * Math.cos(rot);
            double cry = ry * Math.sin(rot);

            int tx = (int)(crx * c - cry * s + cx);
            int ty = (int)(crx * s + cry * c + cy);

            x[i] = tx;
            y[i] = ty;
        }

        return Rasterizer.rasterizePolygon(x, y, pointCount, width, height);
    }

    @Override
    public void mutate() {
        int rnd = ThreadLocalRandom.current().nextInt(4);
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

            case 3:
                angle = angle + (int)(ThreadLocalRandom.current().nextGaussian() * 32);
                break;
        }
    }

    @Override
    public ShapeType getType() {
        return ShapeType.ROTATED_ELLIPSE;
    }

    @Override
    public double[] raw() {
        return new double[]{cx, cy, rx, ry, angle};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        StringBuilder svg = new StringBuilder(3);
        svg.append(String.format("<g transform=\"translate(%d %d) rotate(%d) scale(%d %d)\">", cx, cy, angle, rx, ry));
        svg.append(String.format("<ellipse %s cx=\"0\" cy=\"0\" rx=\"1\" ry=\"1\" />", attrs));
        svg.append("</g>");
        return svg.toString();
        // @formatter:on
    }
}
