package jxtras.primitive.exporter;

import java.util.List;

import jxtras.primitive.Model.ShapeResult;
import jxtras.primitive.util.Colour;
import jxtras.primitive.shape.ShapeType;

/**
 * Exports the shape data as JSON.
 */
class JsonExporter {
    public static String export(List<ShapeResult> shapes) {
        // @formatter:off
        StringBuilder result = new StringBuilder();

        for (int i = 0, count = shapes.size(); i < count; i++) {
            ShapeResult shape = shapes.get(i);
            ShapeType type = shape.shape.getType();
            double[] data = shape.shape.raw();
            int color = shape.color;
            double score = shape.score;
            result.append("    shape_" + i + "\":{\n");
            result.append("        \"type\":" + type + ",\n");

            result.append("        \"data\":" + "[");
            for (int item = 0; item < data.length; item++) {
                if (item <= data.length - 2) {
                    result.append(data[item] + ",");
                } else {
                    result.append(data[item]);
                }
            }
            result.append("],\n");

            result.append("        \"color\":" + "[");
            result.append(Colour.red(color) + ",");
            result.append(Colour.green(color) + ",");
            result.append(Colour.blue(color) + ",");
            result.append(Colour.alpha(color));
            result.append("],\n");

            result.append("        \"score\":" + score + "\n");

            result.append("    }");

            if (i <= count - 2) {
                result.append(",\n");
            }
        }

        result.append("\n}");
        return result.toString();
        // @formatter:on
    }
}
