package jxtras.primitive;

import java.util.List;

import jxtras.primitive.image.Bitmap;
import jxtras.primitive.util.Colour;
import jxtras.primitive.raster.Scanline;
import jxtras.primitive.shape.Shape;
import jxtras.primitive.shape.ShapeFactory;
import jxtras.primitive.shape.ShapeType;
import jxtras.primitive.util.Mathematics;

public class Core {
    /**
     * Calculates the color of the scanlines.
     *
     * @param target    The target image.
     * @param current   The current image.
     * @param scanlines The scanlines.
     * @param alpha     The alpha of the scanline.
     * @return The color of the scanlines.
     */
    public static int computeColor(Bitmap target, Bitmap current, List<Scanline> scanlines,
            int alpha) {
        double rsum = 0.0D, gsum = 0.0D, bsum = 0.0D, total = 0.0D;
        double a = (0x101 * 255.0d / alpha);

        int width = target.getWidth();
        int height = target.getHeight();
        for (Scanline scanline : scanlines) {
            final int y = scanline.y;
            if (y < 0 || y >= height) {
                continue;
            }

            for (int x = scanline.x1; x <= scanline.x2; x++) {
                if (x < 0 || x >= width) {
                    continue;
                }

                int tc = target.getPixel(x, y);
                int tr = Colour.red(tc);
                int tg = Colour.green(tc);
                int tb = Colour.blue(tc);

                int cc = current.getPixel(x, y);
                int cr = Colour.red(cc);
                int cg = Colour.green(cc);
                int cb = Colour.blue(cc);

                // Mix the red, green and blue components, blending by the given alpha value
                rsum += (tr - cr) * a + cr * 0x101;
                gsum += (tg - cg) * a + cg * 0x101;
                bsum += (tb - cb) * a + cb * 0x101;

                total++;
            }
        }

        if (total == 0) { // Early out to avoid integer divide by 0
            return Colour.argb(0, 0, 0, 0);
        }

        // Scale totals down to range [0, 255] to get average blended color
        int r = Mathematics.clamp(((int)(rsum / total)) >> 8, 0, 255);
        int g = Mathematics.clamp(((int)(gsum / total)) >> 8, 0, 255);
        int b = Mathematics.clamp(((int)(bsum / total)) >> 8, 0, 255);
        // System.out.println("computerColor: argb(" + a + ", " + r + "," + g + "," + b + ")");
        return Colour.argb(alpha, r, g, b);
    }

    /**
     * Draws the scanlines onto an image.
     *
     * @param image     The image to be drawn to.
     * @param color     The color of the scanlines.
     * @param scanlines The scanline to draw.
     */
    public static void drawLines(Bitmap image, int color, List<Scanline> scanlines) {
        final int m = 0xFFFF;

        long sa = Colour.alpha(color);
        long sr = Colour.red(color);
        long sg = Colour.green(color);
        long sb = Colour.blue(color);

        // Convert the non-premultiplied 8-bits per channel ARGB to alpha-premultiplied
        // 16-bits per channel ARGB
        sr |= sr << 8;
        sr *= sa;
        sr = (sr / 255);

        sg |= sg << 8;
        sg *= sa;
        sg = (sg / 255);

        sb |= sb << 8;
        sb *= sa;
        sb = (sb / 255);

        sa |= sa << 8;

        int w = image.getWidth();
        int h = image.getHeight();
        for (Scanline scanline : scanlines) {
            final int y = scanline.y;
            if (y < 0 || y >= h) {
                continue;
            }

            // ma is alpha-premultiplied 16-bits int
            long ma = scanline.alpha;
            long  a = (m - sa * ma / m) * 0x101;

            long sama = sa * ma;
            long srma = sr * ma;
            long sgma = sg * ma;
            long sbma = sb * ma;

            for (int x = scanline.x1; x <= scanline.x2; x++) {
                if (x < 0 || x >= w) {
                    continue;
                }

                // Get the dst color
                int dc = image.getPixel(x, y);
                int da = Colour.alpha(dc);
                int dr = Colour.red(dc);
                int dg = Colour.green(dc);
                int db = Colour.blue(dc);

                // Blend dst and src color as (src in mask) over dst
                int ba = Mathematics.clamp((int)((da * a + sama) / m >> 8), 0, 255);
                int br = Mathematics.clamp((int)((dr * a + srma) / m >> 8), 0, 255);
                int bg = Mathematics.clamp((int)((dg * a + sgma) / m >> 8), 0, 255);
                int bb = Mathematics.clamp((int)((db * a + sbma) / m >> 8), 0, 255);

                image.setPixel(x, y, Colour.argb(ba, br, bg, bb));
            }
        }
    }

    /**
     * Copies source pixels to a destination defined by a set of scanlines.
     *
     * @param destination The destination bitmap to copy the lines to.
     * @param source      The source bitmap to copy the lines from.
     * @param scanlines   The scanlines that comprise the source to destination copying mask.
     */
    public static void copyLines(Bitmap destination, Bitmap source, List<Scanline> scanlines) {
        final int width = source.getWidth();
        final int height = source.getHeight();
        for (Scanline scanline : scanlines) {
            final int y = scanline.y;
            if (y < 0 || y >= height) {
                continue;
            }
            for (int x = scanline.x1; x <= scanline.x2; x++) {
                if (x < 0 || x >= width) {
                    continue;
                }
                destination.setPixel(x, y, source.getPixel(x, y));
            }
        }
    }

    /**
     * Calculates the root-mean-square error between two bitmaps.
     *
     * @param first  The first bitmap.
     * @param second The second bitmap.
     * @return The difference/error measure between the two bitmaps.
     */
    public static double differenceFull(Bitmap first, Bitmap second) {
        double total = 0.0D;
        int width = first.getWidth();
        int height = first.getHeight();
        double count = width * height * (first.isTranslucent() ? 4 : 3);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ac = first.getPixel(x, y);
                int bc = second.getPixel(x, y);
                if (first.isTranslucent()) {
                    int aa = Colour.alpha(ac);
                    int ar = Colour.red(ac);
                    int ag = Colour.green(ac);
                    int ab = Colour.blue(ac);

                    int ba = Colour.alpha(bc);
                    int br = Colour.red(bc);
                    int bg = Colour.green(bc);
                    int bb = Colour.blue(bc);

                    int da = aa - ba;
                    int dr = ar - br;
                    int dg = ag - bg;
                    int db = ab - bb;

                    total += (da * da + dr * dr + dg * dg + db * db);
                } else {
                    total += Colour.distance(ac, bc);
                }
            }
        }
        return Math.sqrt(total / count) / 255.0;
    }

    /**
     * Calculates the root-mean-square error between the parts of the two bitmaps within the
     * scanline mask.
     * <p>
     * This is for optimization purposes, it lets us calculate new error values only for parts
     * of the image we know have changed.
     * <p>
     *
     * @param target    The target bitmap.
     * @param before    The bitmap before the change.
     * @param after     The bitmap after the change.
     * @param score     The score.
     * @param scanlines The scanlines.
     * @return The difference/error between the two bitmaps, masked by the scanlines.
     */

    public static double differencePartial(Bitmap target, Bitmap before, Bitmap after,
            double score, List<Scanline> scanlines) {
        int width = target.getWidth();
        int height = target.getHeight();
        int count = width * height * (target.isTranslucent() ? 4 : 3);
        double total = Math.pow(score * 255, 2) * count;
        for (Scanline scanline : scanlines) {
            final int y = scanline.y;
            if (y < 0 || y >= height) {
                continue;
            }

            for (int x = scanline.x1; x <= scanline.x2; x++) {
                if (x < 0 || x >= width) {
                    continue;
                }

                int tc = target.getPixel(x, y);
                int bc = before.getPixel(x, y);
                int ac = after.getPixel(x, y);

                if (target.isTranslucent()) {

                    int ta = Colour.alpha(tc);
                    int tr = Colour.red(tc);
                    int tg = Colour.green(tc);
                    int tb = Colour.blue(tc);


                    int ba = Colour.alpha(bc);
                    int br = Colour.red(bc);
                    int bg = Colour.green(bc);
                    int bb = Colour.blue(bc);

                    int aa = Colour.alpha(ac);
                    int ar = Colour.red(ac);
                    int ag = Colour.green(ac);
                    int ab = Colour.blue(ac);

                    int da1 = ta - ba;
                    int dr1 = tr - br;
                    int dg1 = tg - bg;
                    int db1 = tb - bb;

                    int da2 = ta - aa;
                    int dr2 = tr - ar;
                    int dg2 = tg - ag;
                    int db2 = tb - ab;

                    // ((u.r-v.r)^2 + (u.g-v.g)^2 + (u.b-v.b)^2 )*u.a*v.a/3 + (u.a-v.a)^2
                    total -= (da1 * da1 + dr1 * dr1 + dg1 * dg1 + db1 * db1);
                    total += (da2 * da2 + dr2 * dr2 + dg2 * dg2 + db2 * db2);
                } else {
                    total -= Colour.distance(tc, bc);
                    total += Colour.distance(tc, ac);
                }
            }
        }
        double result = Math.sqrt(total / count) / 255.0;

        // TODO: This is a workaround because when score/energy is tiny total can
        // unintentionally underflow
//        if (result > 1.0f) {
//            return score;
//        }

        return result;
    }

    /**
     * Gets the best state using a random algorithm.
     *
     * @param shapeTypes The types of shape to use.
     * @param alpha      The opacity of the shape.
     * @param n          The number of states to try.
     * @param target     The target bitmap.
     * @param current    The current bitmap.
     * @param buffer     The buffer bitmap.
     * @param lastScore  The last score recorded by the model.
     * @return The best random state i.e. the one with the lowest energy.
     */
    public static State bestRandomState(List<ShapeType> shapeTypes, int alpha, int n,
            Bitmap target, Bitmap current, Bitmap buffer, double lastScore) {
        State bestState = null;
        double bestEnergy = 0.0D;

        int width = current.getWidth();
        int height = current.getHeight();
        for (int i = 0; i < n; i++) {
            Shape shape = ShapeFactory.randomShapeOf(shapeTypes, width, height);
            State state = new State(shape, alpha, target, current, buffer);
            double energy = state.energy(lastScore);
            if (i == 0 || energy < bestEnergy) {
                bestEnergy = energy;
                bestState = state;
            }
        }
        return bestState;
    }

    /**
     * Gets the best state using a hill climbing algorithm.
     *
     * @param shapeTypes The types of shape to use.
     * @param alpha      The opacity of the shape.
     * @param n          The number of random states to generate.
     * @param age        The number of hillclimbing steps.
     * @param target     The target bitmap.
     * @param current    The current bitmap.
     * @param buffer     The buffer bitmap.
     * @param lastScore  The last score recorded by the model.
     * @return The best state acquired from hill climbing i.e. the one with the lowest energy.
     */
    public static State bestHillClimbState(List<ShapeType> shapeTypes, int alpha, int n, int age,
            Bitmap target, Bitmap current, Bitmap buffer, double lastScore) {
        State state = bestRandomState(shapeTypes, alpha, n, target, current, buffer, lastScore);
        state = hillClimb(state, age, lastScore);
        // System.out.println("bestHillClimbState: " + state.shape.getType());
        return state;
    }

    /**
     * Hill climbing optimization algorithm, attempts to minimize energy (the error/difference).
     *
     * @param state     The state to optimize.
     * @param maxAge    The maximum age.
     * @param lastScore The last score recorded by the model.
     * @return The best state found from hillclimbing.
     */
    public static State hillClimb(State state, int maxAge, double lastScore) {
        State s = state.copy();
        State bestState = state.copy();
        double bestEnergy = state.score;

        int age = 0;
        while (age < maxAge) {
            State undo = s.mutate();
            double energy = s.energy(lastScore);
            if (energy >= bestEnergy) {
                s = undo;
            } else {
                bestEnergy = energy;
                bestState = s.copy();
                age = -1;
            }
            age++;
        }

        return bestState;
    }

    public static State hillClimb2(State state, int maxAge) {
        State s = state.copy2();
        State bestState = state.copy2();
        double bestEnergy = state.energy();

        int age = 0;
        while (age < maxAge) {
            State undo = s.move();
            double energy = s.energy();
            if (energy >= bestEnergy) {
                s.undoMove(undo);
            } else {
                bestEnergy = energy;
                bestState = s.copy2();
                age = -1;
            }
            age++;
        }

        return bestState;
    }

    /**
     * Calculates a measure of the improvement adding the shape provides - lower energy is better.
     *
     * @param shape   The shape to check.
     * @param alpha   The alpha of the shape.
     * @param target  The target bitmap.
     * @param current The current bitmap.
     * @param buffer  The buffer bitmap.
     * @param score   The score.
     * @return The energy measure.
     */
    public static double energy(Shape shape, int alpha, Bitmap target, Bitmap current,
            Bitmap buffer, double score) {
        // Gets the set of scanlines that describe the pixels covered by the shape
        List<Scanline> scanlines = shape.rasterize();
        // Calculate best color for areas covered by the scanlines
        int color = computeColor(target, current, scanlines, alpha);
        // Copy area covered by scanlines to buffer bitmap
        copyLines(buffer, current, scanlines);
        // Blend scanlines into the buffer using the color calculated earlier
        drawLines(buffer, color, scanlines);
        // Get error measure between areas of current and modified buffers covered by scanlines
        return differencePartial(target, current, buffer, score, scanlines);
    }
}
