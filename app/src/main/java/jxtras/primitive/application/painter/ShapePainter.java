package jxtras.primitive.application.painter;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;

import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import jxtras.primitive.shape.Shape;
import jxtras.primitive.shape.ShapeType;

public class ShapePainter {
    // @formatter:off
    public static void paint(Graphics2D g2d, Shape shape, int color) {
        final ShapeType shapeType = shape.getType();
        final double[] ptrs = shape.raw();
        switch (shapeType) {
            case CIRCLE: {
                g2d.setColor(new java.awt.Color(color, true));
                g2d.fillOval((int)ptrs[0] - (int)ptrs[2], (int)ptrs[1] - (int)ptrs[2], 2 * (int)ptrs[2], 2 * (int)ptrs[2]);
            }
            break;

            case CUBIC_CURVE: {
                Path2D path2D = new Double();
                path2D.moveTo(ptrs[0], ptrs[1]);
                path2D.curveTo(ptrs[2], ptrs[3], ptrs[4], ptrs[5], ptrs[6], ptrs[7]);
                g2d.setColor(new java.awt.Color(color, true));
                g2d.draw(path2D);
            }
            break;

            case ELLIPSE: {
                g2d.setColor(new java.awt.Color(color, true));
                g2d.fillOval((int)ptrs[0] - (int)ptrs[2], (int)ptrs[1] - (int)ptrs[3], 2 * (int)ptrs[2], 2 * (int)ptrs[3]);
            }
            break;

            case LINE: {
                g2d.setColor(new java.awt.Color(color, true));
                g2d.drawLine((int)ptrs[0], (int)ptrs[1], (int)ptrs[2], (int)ptrs[3]);
            }
            break;

            case TRIANGLE:
                // fall-through
            case POLYGON: {
                final int count = ptrs.length / 2;
                final int[] xptrs = new int[count];
                final int[] yptrs = new int[count];
                for (int i = 0; i < ptrs.length; i += 2) {
                    xptrs[i / 2] = (int)ptrs[i + 0];
                    yptrs[i / 2] = (int)ptrs[i + 1];
                }
                g2d.setColor(new java.awt.Color(color, true));
                g2d.fillPolygon(xptrs, yptrs, count);
            }
            break;

            case POLYLINE: {
                final int count = ptrs.length / 2;
                final int[] xptrs = new int[count];
                final int[] yptrs = new int[count];
                for (int i = 0; i < ptrs.length; i += 2) {
                    xptrs[i / 2] = (int)ptrs[i + 0];
                    yptrs[i / 2] = (int)ptrs[i + 1];
                }
                g2d.setColor(new java.awt.Color(color, true));
                g2d.drawPolyline(xptrs, yptrs, count);
            }
            break;

            case QUADRATIC_CURVE: {
                Path2D path2D = new Double();
                path2D.moveTo(ptrs[0], ptrs[1]);
                path2D.quadTo(ptrs[2], ptrs[3], ptrs[4], ptrs[5]);
                g2d.setColor(new java.awt.Color(color, true));
                g2d.setStroke(new BasicStroke((float)ptrs[6], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(path2D);
            }
            break;

            case RECTANGLE: {
                g2d.setColor(new java.awt.Color(color, true));
                g2d.fillRect((int)ptrs[0], (int)ptrs[1], (int)(ptrs[2] - ptrs[0]), (int)(ptrs[3] - ptrs[1]));
            }
            break;

            case ROTATED_ELLIPSE: {
                g2d.setColor(new java.awt.Color(color, true));
                g2d.rotate(ptrs[4] * Math.PI / 180, ptrs[0], ptrs[1]);
                g2d.fillOval((int)ptrs[0] - (int)ptrs[2], (int)ptrs[1] - (int)ptrs[3], 2 * (int)ptrs[2], 2 * (int)ptrs[3]);
            }
            break;

            case ROTATED_RECTANGLE: {
                g2d.setColor(new java.awt.Color(color, true));
                g2d.translate(ptrs[0], ptrs[1]);
                g2d.rotate(ptrs[4] * Math.PI / 180);
                g2d.fillRect((int)(-ptrs[2] / 2.0), (int)(-ptrs[3] / 2.0), (int)ptrs[2], (int)ptrs[3]);
            }
            break;

            default:
                throw new IllegalArgumentException("Unsupported shape type: " + shapeType);
        }
    }
    // @formatter:on
}
