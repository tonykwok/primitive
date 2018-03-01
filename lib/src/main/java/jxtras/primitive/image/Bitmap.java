package jxtras.primitive.image;

import jxtras.primitive.util.Colour;

import java.util.Arrays;

public class Bitmap {

    /**
     * The width of this <code>Bitmap</code>
     */
    private final int width;

    /**
     * The height of this <code>Bitmap</code>
     */
    private final int height;

    /**
     * The array of pixels, stored as integers, of this <code>Bitmap</code>
     */
    private final int[] pixels;

    /**
     * Represents image data that contains or might contain arbitrary
     * alpha values between and including 0 and 255.
     */
    private final boolean isTranslucent;

    /**
     * Creates a mutable bitmap based on the given dimensions and pixels.
     *
     * @param width  The width of the bitmap
     * @param height The height of the bitmap
     * @param pixels The color (ARGB8888 or RGB888) of each pixel of the bitmap
     * @throws IllegalArgumentException
     */
    public Bitmap(int width, int height, int[] pixels, boolean isTranslucent) {
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }
        if (pixels == null) {
            throw new IllegalArgumentException("pixels must not be null.");
        }
        if (pixels.length != width * height) {
            throw new IllegalArgumentException("pixel format unsupported.");
        }
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.isTranslucent = isTranslucent;
    }

    /**
     * Returns the width of the <code>Bitmap</code>.
     *
     * @return the width of this <code>Bitmap</code>
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Returns the height of the <code>Bitmap</code>.
     *
     * @return the height of this <code>Bitmap</code>
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Represents image data that contains or might contain arbitrary
     * alpha values between and including 0 and 255.
     */
    public boolean isTranslucent() {
        return isTranslucent;
    }

    /**
     * Returns the {@link Colour} at the specified location. Throws an exception
     * if x or y are out of bounds (negative or >= to the width or height
     * respectively). The returned color is a non-premultiplied ARGB value in
     * the sRGB color space.
     *
     * @param x The x coordinate (0...width-1) of the pixel to return
     * @param y The y coordinate (0...height-1) of the pixel to return
     * @return The argb {@link Colour} at the specified coordinate
     * @throws IllegalArgumentException if x, y exceed the bitmap's bounds
     */
    public int getPixel(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        }
        if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }
        if (x >= getWidth()) {
            throw new IllegalArgumentException("x must be < bitmap.width()");
        }
        if (y >= getHeight()) {
            throw new IllegalArgumentException("y must be < bitmap.height()");
        }
        return pixels[y * width + x];
    }


    /**
     * Write the specified {@link Colour} into the bitmap at the x,y coordinate. The
     * color must be a non-premultiplied ARGB value in the sRGB color space.
     *
     * @param x     The x coordinate of the pixel to replace (0...width-1)
     * @param y     The y coordinate of the pixel to replace (0...height-1)
     * @param color The ARGB color to write into the bitmap
     * @throws IllegalArgumentException if x, y are outside of the bitmap's bounds.
     */
    public void setPixel(int x, int y, int color) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        }
        if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }
        if (x >= getWidth()) {
            throw new IllegalArgumentException("x must be < bitmap.width()");
        }
        if (y >= getHeight()) {
            throw new IllegalArgumentException("y must be < bitmap.height()");
        }
        pixels[y * width + x] = color;
    }

    /**
     * Tries to make a new bitmap which has the same dimensions and pixels as the original.
     *
     * @return The new bitmap.
     */
    public Bitmap copy() {
        return new Bitmap(width, height, Arrays.copyOf(pixels, pixels.length), isTranslucent);
    }

    /**
     * Fills the bitmap's pixels with the specified {@link Colour}.
     */
    public Bitmap eraseColor(int color) {
        Arrays.fill(pixels, color);
        return this;
    }

    public int[] getPixels() {
        return pixels;
    }
}
