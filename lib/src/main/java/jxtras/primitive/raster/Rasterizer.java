package jxtras.primitive.raster;

import jxtras.primitive.shape.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Raster is responsible for rasterizing {@link Shape} into {@link Scanline}s.
 */
public class Rasterizer {
    private final int width;
    private final int height;

    public Rasterizer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static List<Scanline> rasterizeLine(int x1, int y1, int x2, int y2, int width, int height) {
        List<Scanline> scanlines = new ArrayList<Scanline>();
        int x = x1;
        int y = y1;
        int dx = +Math.abs(x - x2), sx = x < x2 ? 1 : -1;
        int dy = -Math.abs(y - y2), sy = y < y2 ? 1 : -1;
        int error = dx + dy, e2;
        for (; ; ) {
            scanlines.add(new Scanline(y, x, x, 0xFFFF));
            if (x == x2 && y == y2) {
                break;
            }
            e2 = 2 * error;
            if (e2 >= dy) {
                error += dy;
                x += sx;
            }
            if (e2 <= dx) {
                error += dx;
                y += sy;
            }
        }

        return Scanline.crop(scanlines, width, height);
    }

    public static List<Scanline> rasterizeTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int width, int height) {
        List<Scanline> scanlines = new ArrayList<Scanline>();
        /**
         * Sort the three vertices ascending by y-coordinate.
         * After calling following apples: y1 <= y2 <= y3
         */
        int x, y;
        if (y1 > y3) {
            // swap(x1, x3)
            x = x1;
            x1 = x3;
            x3 = x;
            // swap(y1, y3)
            y = y1;
            y1 = y3;
            y3 = y;
        }
        if (y1 > y2) {
            // swap(x1, x2)
            x = x1;
            x1 = x2;
            x2 = x;
            // swap(y1, y2)
            y = y1;
            y1 = y2;
            y2 = y;
        }
        if (y2 > y3) {
            // swap(x2, x3)
            x = x2;
            x2 = x3;
            x3 = x;
            // swap(y2, y3)
            y = y2;
            y2 = y3;
            y3 = y;
        }
        if (y2 == y3) {
            rasterizeTriangleBottom(x1, y1, x2, y2, x3, y3, scanlines);
        } else if (y1 == y2) {
            rasterizeTriangleTop(x1, y1, x2, y2, x3, y3, scanlines);
        } else {
            int x4 = x1 + (int)(((double)(y2 - y1) / (double)(y3 - y1)) * (double)(x3 - x1));
            int y4 = y2;
            rasterizeTriangleBottom(x1, y1, x2, y2, x4, y4, scanlines);
            rasterizeTriangleTop(x2, y2, x4, y4, x3, y3, scanlines);
        }
        return Scanline.crop(scanlines, width, height);
    }

    private static void rasterizeTriangleBottom(int x1, int y1, int x2, int y2, int x3, int y3,
            List<Scanline> scanlines) {
        double s1 = (double)(x2 - x1) / (double)(y2 - y1);
        double s2 = (double)(x3 - x1) / (double)(y3 - y1);
        double ax = (double)(x1);
        double bx = (double)(x1);
        for (int y = y1; y <= y2; y++) {
            int a = (int)(ax);
            int b = (int)(bx);
            ax += s1;
            bx += s2;
            if (a > b) {
                // swap(a, b)
                int tmp = a;
                a = b;
                b = tmp;
            }
            scanlines.add(new Scanline(y, a, b, 0xFFFF));
        }
    }

    private static void rasterizeTriangleTop(int x1, int y1, int x2, int y2, int x3, int y3,
            List<Scanline> scanlines) {
        double s1 = (double)(x3 - x1) / (double)(y3 - y1);
        double s2 = (double)(x3 - x2) / (double)(y3 - y2);
        double ax = (double)(x3);
        double bx = (double)(x3);
        for (int y = y3; y > y1; y--) {
            ax -= s1;
            bx -= s2;
            int a = (int)(ax);
            int b = (int)(bx);
            if (a > b) {
                // swap(a, b)
                int tmp = a;
                a = b;
                b = tmp;
            }
            scanlines.add(new Scanline(y, a, b, 0xFFFF));
        }
    }

    public static List<Scanline> rasterizePolygon(int[] x, int[] y, int n, int width, int height) {
        return new PolygonRasterizer(x, y, n, width, height).rasterize();
    }

    /*
     * Just a class for an edge.
     */
    private static class Edge {

        public int x1, y1;       // first vertice
        public int x2, y2;       // second vertice
        public double curX;      // x-coord of intersection with scanline
        public double m;         // slope

        /*
         * Create on edge out of two vertices
         */
        public Edge(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;

            this.x2 = x2;
            this.y2 = y2;

            // m = dy / dx
            m = (float)((float)(y1 - y2) / (float)(x1 - x2));
        }

        /*
         * Called when scanline intersects the first vertice of this edge.
         * That simply means that the intersection point is this vertice.
         */
        public void activate() {
            curX = x1;
        }

        /*
         * Update the intersection point from the scanline and this edge.
         * Instead of explicitly calculate it we just increment with 1/m every time
         * it is intersected by the scanline.
         */
        public void update() {
            curX += (float)((float)1 / (float)m);
        }

        /*
         * Called when scanline intersects the second vertice,
         * so the intersection point is exactly this vertice and from now on
         * we are done with this edge
         */
        public void deactivate() {
            curX = x2;
        }

    }

    private static class PolygonRasterizer {
        int[] x; // x coordinates
        int[] y; // y coordinates
        int n;  // number of coordinates

        int width, height;

        private PolygonRasterizer(int[] x, int[] y, int n, int width, int height) {
            this.x = x;
            this.y = y;
            this.n = n;
            this.width = width;
            this.height = height;
        }

        /**
         * See https://imagej.nih.gov/ij/developer/source/ij/process/PolygonFiller.java.html for
         * details.
         */
        public List<Scanline> rasterize() {
            List<Scanline> scanlines = new ArrayList<Scanline>();

            // create edges array from polygon vertice vector
            // make sure that first vertice of an edge is the smaller one
            Edge[] sortedEdges = this.createEdges();

            // sort all edges by y coordinate, smallest one first, lousy bubblesort
            Edge tmp;
            for (int i = 0; i < sortedEdges.length - 1; i++) {
                for (int j = 0; j < sortedEdges.length - 1; j++) {
                    if (sortedEdges[j].y1 > sortedEdges[j + 1].y1) {
                        // swap both edges
                        tmp = sortedEdges[j];
                        sortedEdges[j] = sortedEdges[j + 1];
                        sortedEdges[j + 1] = tmp;
                    }
                }
            }

            // find biggest y-coord of all vertices
            int scanlineEnd = 0;
            for (int i = 0; i < sortedEdges.length; i++) {
                if (scanlineEnd < sortedEdges[i].y2) {
                    scanlineEnd = sortedEdges[i].y2;
                }
            }

            // this list holds all cutpoints from current scanline with the polygon
            ArrayList<Integer> list = new ArrayList<Integer>();

            // scanline starts at smallest y coordinate and then move scanline step by step down
            // to biggest one
            for (int scanline = sortedEdges[0].y1; scanline <= scanlineEnd; scanline++) {
                //System.out.println("ScanLine: " + scanline); // DEBUG

                list.clear();

                // loop all edges to see which are cut by the scanline
                for (int i = 0; i < sortedEdges.length; i++) {

                    // here the scanline intersects the smaller vertice
                    if (scanline == sortedEdges[i].y1) {
                        if (scanline == sortedEdges[i].y2) {
                            // the current edge is horizontal, so we add both vertices
                            sortedEdges[i].deactivate();
                            list.add((int)sortedEdges[i].curX);
                        } else {
                            sortedEdges[i].activate();
                            // we don't insert it in the list cause this vertice is also
                            // the (bigger) vertice of another edge and already handled
                        }
                    }

                    // here the scanline intersects the bigger vertice
                    if (scanline == sortedEdges[i].y2) {
                        sortedEdges[i].deactivate();
                        list.add((int)sortedEdges[i].curX);
                    }

                    // here the scanline intersects the edge, so calc intersection point
                    if (scanline > sortedEdges[i].y1 && scanline < sortedEdges[i].y2) {
                        sortedEdges[i].update();
                        list.add((int)sortedEdges[i].curX);
                    }

                }

                if (list.size() < 2 || list.size() % 2 != 0) {
                    // System.out.println("This should never happen! list size: " + list.size());
                    continue;
                }

                // now we have to sort our list with our x-coordinates, ascendend
                int swaptmp;
                for (int i = 0; i < list.size(); i++) {
                    for (int j = 0; j < list.size() - 1; j++) {
                        if (list.get(j) > list.get(j + 1)) {
                            swaptmp = list.get(j);
                            list.set(j, list.get(j + 1));
                            list.set(j + 1, swaptmp);
                        }

                    }
                }

                // so draw all line segments on current scanline
                for (int i = 0; i < list.size(); i += 2) {
                    scanlines.add(new Scanline(scanline, list.get(i), list.get(i + 1), 0xFFFF));
                }

            }
            return Scanline.crop(scanlines, width, height);
        }

        /*
         * Create from the polygon vertices an array of edges.
         * Note that the first vertice of an edge is always the one with the smaller y
         * coordinate one of both
         */
        private Edge[] createEdges() {
            Edge[] sortedEdges = new Edge[n];
            for (int i = 0; i < n; i++) {
                int x1 = x[i];
                int y1 = y[i];
                int x2 = x[(i + 1) % n];
                int y2 = y[(i + 1) % n];
                if (y1 < y2) {
                    sortedEdges[i] = new Edge(x1, y1, x2, y2);
                } else {
                    sortedEdges[i] = new Edge(x2, y2, x1, y1);
                }
            }
            return sortedEdges;
        }
    }
}
