package jxtras.primitive.util;

public class Mathematics {
    private Mathematics() {}

    public static double[] rotate(double x, double y, double theta, double[] xy) {
        if (xy == null) {
            xy = new double[2];
        } else if (xy.length < 2) {
            throw new IllegalArgumentException("The length of array xy must >= 2");
        }

        xy[0] = x* Math.cos(theta) - y* Math.sin(theta);
        xy[1] = x* Math.sin(theta) + y* Math.cos(theta);

        return xy;
    }

    public static double deg2rad(double degree) {
        return degree * Math.PI / 180;
    }

    public static double rad2deg(double radian) {
        return radian * 180 / Math.PI;
    }

    public static double clamp(double x, double lo, double hi) {
        if (x < lo) {
            return lo;
        }
        if (x > hi) {
            return hi;
        }
        return x;
    }

    public static int clamp(int x, int lo, int hi) {
        if (x < lo) {
            return lo;
        }
        if (x > hi) {
            return hi;
        }
        return x;
    }
}
