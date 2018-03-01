package jxtras.primitive.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jxtras.primitive.util.Mathematics;
import jxtras.primitive.raster.Scanline;

public class TestRR implements Shape {
    int x;
    int y;

    int sx;
    int sy;

    int angle;

    int width;
    int height;

    static int cx = 132;
    static int cy = 192;

    private TestRR(int x, int y, int sx, int sy, int angle, int width, int height) {
        this.x = x;
        this.y = y;
        this.sx = sx;
        this.sy = sy;
        this.angle = angle;
        this.width = width;
        this.height = height;
    }

    public static TestRR random(int width, int height) {
        int x = 140;//ThreadLocalRandom.current().nextInt(width);
        int y = 140;//ThreadLocalRandom.current().nextInt(height);

        int sx = 100;//ThreadLocalRandom.current().nextInt(32) + 1;
        int sy = 50;//ThreadLocalRandom.current().nextInt(32) + 1;

        int angle = 0;//ThreadLocalRandom.current().nextInt(360);
        {
            int ox = x + sx / 2;
            int oy = y + sy / 2;

            if (ox == cx || oy == cy) {
                angle = 0;
            } else {
                double dy = oy - cy;
                double dx = ox - cx;
                angle = (int) Mathematics.rad2deg(Math.atan(dx/dy));
            }
        }
        angle = 0;

        TestRR rotatedRectangle = new TestRR(x, y, sx, sy, angle, width, height);
        rotatedRectangle.mutate();

        return rotatedRectangle;
    }

    @Override
    public TestRR copy() {
        return new TestRR(x, y, sx, sy, angle, width, height);
    }

    @Override
    public List<Scanline> rasterize() {
        double[] xy = new double[2];

        double sx = (double) this.sx;
        double sy = (double) this.sy;
        double angle = Mathematics.deg2rad((double) this.angle);
        Mathematics.rotate(-sx/2, -sy/2, angle, xy);
        double rx1 = xy[0], ry1 = xy[1];
        Mathematics.rotate(sx/2, -sy/2, angle, xy);
        double rx2 = xy[0], ry2 = xy[1];
        Mathematics.rotate(sx/2, sy/2, angle, xy);
        double rx3 = xy[0], ry3 = xy[1];
        Mathematics.rotate(-sx/2, sy/2, angle, xy);
        double rx4 = xy[0], ry4 = xy[1];

        int x1 = (int) rx1 + x, y1 = (int) ry1 + y;
        int x2 = (int) rx2 + x, y2 = (int) ry2 + y;
        int x3 = (int) rx3 + x, y3 = (int) ry3 + y;
        int x4 = (int) rx4 + x, y4 = (int) ry4 + y;

        int miny = Math.min(y1, Math.min(y2, Math.min(y3, y4)));
        int maxy = Math.max(y1, Math.max(y2, Math.max(y3, y4)));
        int n = maxy - miny + 1;
        int[] min = new int[n];
        int[] max = new int[n];
        for (int i = 0; i < n; i++) {
            min[i] = width;
        }
        int[] xs = new int[]{x1, x2, x3, x4, x1};
        int[] ys = new int[]{y1, y2, y3, y4, y1};
        // TODO: this could be better probably
        for (int i = 0; i < 4; i++) {
            double x = (double) xs[i];
            double y = (double) ys[i];
            double dx = (double) (xs[i+1]-xs[i]);
            double dy = (double) (ys[i+1]-ys[i]);
            int count = (int) (Math.sqrt(dx*dx+dy*dy)) * 2;
            for (int j = 0; j < count; j++) {
                double t = (double)j / (double)(count-1);
                int xi = (int)(x + dx*t);
                int yi = (int)(y+dy*t) - miny;
                min[yi] = Math.min(min[yi], xi);
                max[yi] = Math.max(max[yi], xi);
            }
        }
        List<Scanline> scanlines = new ArrayList<Scanline>();
        for (int i = 0; i < n; i++) {
            int y = miny + i;
            if (y < 0 || y >= height) {
                continue;
            }
            int a = Math.max(min[i], 0);
            int b = Math.min(max[i], width - 1);
            if (b >= a) {
                scanlines.add(new Scanline(y, a, b, 0xFFFF));
            }
        }
        return scanlines;
    }

    @Override
    public void mutate() {
        final int rnd = ThreadLocalRandom.current().nextInt(3);
        switch (rnd) {
            case 0:
                x = 140;//Util.clamp(x + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, width - 1);
                y = 140;//Util.clamp(y + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 0, height - 1);
                break;

            case 1:
                sx = 100;//Util.clamp(sx + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 1, width - 1);
                sy = 50;//Util.clamp(sy + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 1, height - 1);
                break;

            case 3:
                //angle = angle + (int)(ThreadLocalRandom.current().nextGaussian() * 32);
                int ox = x + sx / 2;
                int oy = y + sy / 2;

                if (ox == cx || oy == cy) {
                    angle = 0;
                } else {
                    double dy = oy - cy;
                    double dx = ox - cx;
                    angle = (int) Mathematics.rad2deg(Math.atan(dx/dy));
                }
                angle = 0;
                break;
        }

//        while (!isValid()) {
//            sx = Util.clampInt(sx + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 1, width - 1);
//            sy = Util.clampInt(sy + (int)(ThreadLocalRandom.current().nextGaussian() * 16), 1, height - 1);
//        }
    }

    private boolean isValid() {
        int a = sx;
        int b = sy;
        if (a < b) {
            a = sy;
            b = sx;
        }
        double aspect = (double)a / (double)b;
        return aspect <= 5D;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.ROTATED_RECTANGLE;
    }

    @Override
    public double[] raw() {
        return new double[]{x, y, sx, sy, angle};
    }

    @Override
    public String svg(String attrs) {
        // @formatter:off
        StringBuilder svg = new StringBuilder(3);
        svg.append(String.format("<g transform=\"translate(%d %d) rotate(%d) scale(%d %d)\">", x, y, angle, sx, sy));
        svg.append(String.format("<rect %s x=\"-0.5\" y=\"-0.5\" width=\"1\" height=\"1\" />", attrs));
        svg.append("</g>");
        return svg.toString();
        // @formatter:on
    }
}
