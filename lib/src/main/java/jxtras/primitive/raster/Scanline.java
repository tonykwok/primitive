package jxtras.primitive.raster;

import java.util.ArrayList;
import java.util.List;

import jxtras.primitive.util.Mathematics;

/**
 * The <code>Scanline</code> describes a row of pixels running across a <code>Bitmap</code>.
 */
public class Scanline {
    /**
     * The Y-coordinate of the <code>Scanline</code>.
     */
    public int y;

    /**
     * The leftmost X-coordinate of the <code>Scanline</code>.
     */
    public int x1;

    /**
     * The rightmost X-coordinate of the <code>Scanline</code>.
     */
    public int x2;

    /**
     * Not used
     */
    public int alpha;

    /**
     * Creates a new scanline based on the specified coordinates.
     *
     * @param y     The y-coordinate.
     * @param x1    The leftmost x-coordinate.
     * @param x2    The rightmost x-coordinate.
     * @param alpha alpha-premultiplied 16-bits
     */
    public Scanline(int y, int x1, int x2, int alpha) {
        this.y = y;
        this.x1 = x1;
        this.x2 = x2;
        this.alpha = alpha;
    }

    /**
     * Crops the scanning width of an array of scanlines so they do not scan outside of the
     * given area.
     *
     * @param scanlines The scanlines to crop.
     * @param w         The width to crop.
     * @param h         The height to crop.
     * @return A list of the cropped scanlines.
     */
    public static List<Scanline> crop(List<Scanline> scanlines, int w, int h) {
        List<Scanline> result = new ArrayList<Scanline>(scanlines.size() /* initialCapacity */);
        for (Scanline scanline : scanlines) {
            if (scanline.y < 0 || scanline.y >= h) {
                continue;
            }
            if (scanline.x1 >= w) {
                continue;
            }
            if (scanline.x2 < 0) {
                continue;
            }
            scanline.x1 = Mathematics.clamp(scanline.x1, 0, w - 1);
            scanline.x2 = Mathematics.clamp(scanline.x2, 0, w - 1);
            if (scanline.x1 > scanline.x2) {
                continue;
            }
            result.add(scanline);
        }
        return result;
    }
}
