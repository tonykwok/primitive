package jxtras.primitive.shape;

import java.util.List;

import jxtras.primitive.raster.Scanline;

/**
 * The <code>Shape</code> interface defines the necessary methods for
 * geometric shape rasterizer and manipulation.
 */
public interface Shape {
    /**
     * Creates a deep copy of the shape.
     *
     * @return A deep copy of the shape object.
     */
    Shape copy();

    /**
     * Creates a raster scanline representation of the shape.
     *
     * @return Array of raster scanlines representing the shape.
     */
    List<Scanline> rasterize();

    /**
     * Modifies the shape a little, typically with a random component.
     * For improving the shape's fit to a image (trial-and-error style).
     */
    void mutate();

    /**
     * Gets the ShapeType of the shape.
     *
     * @return The ShapeType of the shape.
     */
    ShapeType getType();

    /**
     * Gets a vector of data that represents the shape geometry, the format varies depending on
     * the ShapeType.
     *
     * @return The shape data.
     */
    double[] raw();

    /**
     * Gets a string that represents a SVG element that describes the shape geometry.
     *
     * @return The SVG shape data that represents this shape.
     */
    String svg(String attrs);

    /**
     * A rectangular bounding box which is used to describe the bounds of a shape.
     */
    class BoundingBox {
        public int x, y, width, height;

        /**
         * Constructs and initializes a <code>BoundingBox</code> with
         * bounds (0, 0, 0, 0).
         */
        public BoundingBox() {
            setBounds(0, 0, 0, 0);
        }

        /**
         * Creates a new instance of {@code BoundingBox}.
         *
         * @param x      the X coordinate of the upper-left corner
         * @param y      the Y coordinate of the upper-left corner
         * @param width  the width of the {@code BoundingBox}
         * @param height the height of the {@code BoundingBox}
         */
        public BoundingBox(int x, int y, int width, int height) {
            setBounds(x, y, width, height);
        }

        /**
         * Sets the bounds of the <code>BoundingBox</code>.
         *
         * @param x      the X coordinate of the upper-left corner
         * @param y      the Y coordinate of the upper-left corner
         * @param width  the width of the {@code BoundingBox}
         * @param height the height of the {@code BoundingBox}
         */
        public void setBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
