package jxtras.primitive.shape;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ShapeFactory {
    /**
     * Creates a new shape of the specified type.
     *
     * @param type   The type of shape to create.
     * @param width  The x-bound of the whole canvas.
     * @param height The y-bound of the whole canvas.
     * @return The new shape.
     */
    public static Shape createShapeOf(ShapeType type, int width, int height) {
        switch (type) {
            case CIRCLE:
                return Circle.random(width, height);
            case CUBIC_CURVE:
                return CubicCurve.random(width, height);
            case ELLIPSE:
                return Ellipse.random(width, height);
            case LINE:
                return Line.random(width, height);
            case POLYGON:
                return Polygon.random(width, height);
            case POLYLINE:
                return Polyline.random(width, height);
            case QUADRATIC_CURVE:
                return QuadraticCurve.random(width, height);
            case RECTANGLE:
                return Rectangle.random(width, height);
            case ROTATED_RECTANGLE:
                return RotatedRectangle.random(width, height);
            case ROTATED_ELLIPSE:
                return RotatedEllipse.random(width, height);
            case TRIANGLE:
                return Triangle.random(width, height);
            case TEST:
                return TestRR.random(width, height);
        }
        throw new IllegalArgumentException("Unsupported shape type: " + type);
    }

    /**
     * Creates a random shape from the types supplied.
     *
     * @param shapeTypes The types of shape to possibly create.
     * @param width      The x-bound of the whole canvas.
     * @param height     The y-bound of the whole canvas.
     * @return The new shape.
     */
    public static Shape randomShapeOf(List<ShapeType> shapeTypes, int width, int height) {
        if (shapeTypes == null) {
            throw new IllegalArgumentException("The \"shapeTypes\" argument must not be null");
        }
        if (shapeTypes.size() < 1) {
            throw new IllegalArgumentException("At least one shape type must be specified");
        }
        int index = ThreadLocalRandom.current().nextInt(shapeTypes.size());
        return createShapeOf(shapeTypes.get(index), width, height);
    }
}
