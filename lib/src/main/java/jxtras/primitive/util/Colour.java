package jxtras.primitive.util;

public class Colour {
    /**
     * Return a color-int from red, green, blue components.
     * The alpha component is implicitly 255 (fully opaque).
     * These component values should be ([0..255]), so if they are out of
     * range, throws an IllegalArgumentException exception.
     *
     * @param red   Red component ([0..255]) of the color
     * @param green Green component ([0..255]) of the color
     * @param blue  Blue component ([0..255]) of the color
     */
    public static int rgb(int red, int green, int blue) {
        boolean rangeError = false;
        String badComponentString = "";

        if (red < 0 || red > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Red";
        }
        if (green < 0 || green > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Green";
        }
        if (blue < 0 || blue > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Blue";
        }
        if (rangeError == true) {
            throw new IllegalArgumentException(
                    "Color component outside of expected range:" + badComponentString);
        }
        return 0xff000000 | (red << 16) | (green << 8) | blue;
    }

    /**
     * Return a color-int from alpha, red, green, blue components.
     * These component values should be ([0..255]), so if they are out of
     * range, throws an IllegalArgumentException exception.
     *
     * @param alpha Alpha component ([0..255]) of the color
     * @param red   Red component ([0..255]) of the color
     * @param green Green component ([0..255]) of the color
     * @param blue  Blue component ([0..255]) of the color
     */
    public static int argb(int alpha, int red, int green, int blue) {
        boolean rangeError = false;
        String badComponentString = "";

        if (alpha < 0 || alpha > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Alpha";
        }
        if (red < 0 || red > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Red";
        }
        if (green < 0 || green > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Green";
        }
        if (blue < 0 || blue > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Blue";
        }
        if (rangeError == true) {
            throw new IllegalArgumentException(
                    "Color component outside of expected range:" + badComponentString);
        }
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Return the alpha component of a color int.
     * This is the same as saying color >>> 24
     */
    public static int alpha(int color) {
        return color >>> 24;
    }

    /**
     * Return the red component of a color int.
     * This is the same as saying (color >> 16) & 0xFF
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Return the green component of a color int.
     * This is the same as saying (color >> 8) & 0xFF
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Return the blue component of a color int.
     * This is the same as saying color & 0xFF
     */
    public static int blue(int color) {
        return color & 0xFF;
    }

    /**
     * Parse the color string, and return the corresponding color-int.
     * If the string cannot be parsed, throws an IllegalArgumentException exception.
     */
    public static int parse(String colorString) {
        if (colorString.charAt(0) == '#') {
            // Use a long to avoid rollovers on #ffXXXXXX
            long color = Long.parseLong(colorString.substring(1), 16);
            if (colorString.length() == 7) {
                // Set the alpha value
                color |= 0x00000000ff000000;
            } else if (colorString.length() != 9) {
                throw new IllegalArgumentException("Unknown color");
            }
            return (int)color;
        }
        throw new IllegalArgumentException("Unknown color");
    }

    public static int average(int[] pixels) {
         int r = 0, g = 0, b = 0;
        for (int c : pixels) {
            r += Colour.red(c);
            g += Colour.green(c);
            b += Colour.blue(c);
        }
        r /= pixels.length;
        g /= pixels.length;
        b /= pixels.length;
        return Colour.rgb( r, g, b);
    }


//    typedef struct {
//        unsigned char r, g, b;
//    } RGB;
//
//    double ColourDistance(RGB e1, RGB e2)
//    {
//        long rmean = ( (long)e1.r + (long)e2.r ) / 2;
//        long r = (long)e1.r - (long)e2.r;
//        long g = (long)e1.g - (long)e2.g;
//        long b = (long)e1.b - (long)e2.b;
//        return sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
//    }

    public static double distance(int c1, int c2) {
        int r1 = Colour.red(c1);
        int g1 = Colour.green(c1);
        int b1 = Colour.blue(c1);

        int r2 = Colour.red(c2);
        int g2 = Colour.green(c2);
        int b2 = Colour.blue(c2);

        int rmean = (r1 + r2) << 1;
        int r = r1 - r2;
        int g = g1 - g2;
        int b = b1 - b2;
        return ((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
    }
}
