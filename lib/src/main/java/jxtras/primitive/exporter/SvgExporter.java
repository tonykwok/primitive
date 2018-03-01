package jxtras.primitive.exporter;

import java.util.List;

import jxtras.primitive.Model.ShapeResult;
import jxtras.primitive.util.Colour;


/**
 * Exports the result as an SVG image.
 */
public class SvgExporter {
    /**
     * Exports the result to an SVG image.
     *
     * @param shapes     The shape data to export
     * @param width      The width of the SVG image
     * @param height     The height of the SVG image
     * @param background The background color of the SVG image
     * @return A string representing the SVG image
     */
    public static String export(List<ShapeResult> shapes, int width, int height, float scale, int background) {
        StringBuilder results = new StringBuilder();
        results.append(getPrelude());
        results.append(getSvgNodeOpen(width, height));
        results.append(exportBackground(width, height, background));
        results.append(scaleOpen(scale));
        results.append(exportShapes(shapes));
        results.append(scaleClose());
        results.append(getSvgNodeClose());
        return results.toString();
    }


    private static String exportBackground(int width, int height, int color) {
        // @formatter:off
        return String.format("<rect width=\"100%%\" height=\"100%%\" fill=\"%s\" />\n", hexForColor(color));
        // @formatter:on
    }

    static String scaleOpen(float scale) {
        return String.format("<g transform=\"scale(%f) translate(0.5 0.5)\">\n", scale);
    }

    static String scaleClose() {
        return String.format("</g>\n");
    }

    /**
     * Exports a list of shapes to SVG elements respectively.
     *
     * @param shapes The shape data to export.
     * @return A string representing the SVG shape data for the shapes.
     */
    public static String exportShapes(List<ShapeResult> shapes) {
        StringBuilder results = new StringBuilder();
        for (ShapeResult shape : shapes) {
            results.append(exportShape(shape));
            results.append("\n");
        }
        return results.toString();
    }

    /**
     * Exports a single shape to the corresponding SVG element.
     *
     * @param shape The shape data to export.
     * @return A string representing the SVG shape data for the shape.
     */
    public static String exportShape(ShapeResult shape) {
        return shape.shape.svg(stylesForShape(shape));
    }

    private static String stylesForShape(ShapeResult result) {
        // @formatter:off
        final int alpha = Colour.alpha(result.color);
        switch (result.shape.getType()) {
            case LINE:
            case POLYLINE:
            case QUADRATIC_CURVE:
            case CUBIC_CURVE:
                return "fill=\"none\" " + strokeForColor(result.color) + " " + strokeOpacityForAlpha(alpha);
            default:
                return fillForColor(result.color) + " " + fillOpacityForAlpha(alpha);
        }
        // @formatter:on
    }

    private static String hexForColor(int color) {
        // @formatter:off
        return String.format("#%02x%02x%02x", Colour.red(color), Colour.green(color), Colour.blue(color));
        // @formatter:on
    }

    private static String strokeForColor(int color) {
        return String.format("stroke=\"%s\"", hexForColor(color));
    }

    private static String fillForColor(int color) {
        return String.format("fill=\"%s\"", hexForColor(color));
    }

    private static String fillOpacityForAlpha(float alpha) {
        return String.format("fill-opacity=\"%f\"", alpha / 255.0f);
    }

    private static String strokeOpacityForAlpha(float alpha) {
        return String.format("stroke-opacity=\"%f\"", alpha / 255.0f);
    }

    private static String getPrelude() {
        return "<?xml version=\"1.0\" standalone=\"no\"?>\n";
    }

    private static String getSvgNodeOpen(int width, int height) {
        // @formatter:off
        return String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 %d %d\">\n", width, height);
        // @formatter:on
    }

    private static String getSvgNodeClose() {
        return "</svg>";
    }
}
