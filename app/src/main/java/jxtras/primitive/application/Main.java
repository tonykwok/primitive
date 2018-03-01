package jxtras.primitive.application;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

import jxtras.primitive.util.Colour;
import org.jdesktop.swingx.util.GraphicsUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import jxtras.primitive.Core;
import jxtras.primitive.Geometrizer;
import jxtras.primitive.Model.ShapeResult;
import jxtras.primitive.application.painter.ShapePainter;
import jxtras.primitive.application.ui.ActionButton;
import jxtras.primitive.application.ui.MainFrame;
import jxtras.primitive.exporter.SvgExporter;
import jxtras.primitive.image.Bitmap;
import jxtras.primitive.shape.ShapeType;

public class Main {
    /**
     * Font used for the text on gui
     */
    private static final Font BASIC_FONT = new Font("San Francisco Display", Font.ITALIC, 12);
    private static Font DIGIT_FONT;
    static {
        try (InputStream is = Main.class.getResourceAsStream("/font/Crysta.ttf")) {
            DIGIT_FONT = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(32.0f);
        } catch (FontFormatException | IOException e) {
            System.err.println("Crysta.ttf not loaded. Using serif font.");
            DIGIT_FONT = BASIC_FONT.deriveFont(Font.PLAIN, 32.0f);
        }
    }

    /**
     * A set of high-quality rendering hints for the keys ANTIALIASING, INTERPOLATION,
     * COLOR_RENDER, STROKE, and RENDER.
     */
    protected static final RenderingHints qualityHints = new RenderingHints(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON) {
        {
            put(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
            put(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
            put(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
            put(KEY_RENDERING, VALUE_RENDER_QUALITY);
        }
    };

    /**
     * Step settings
     */
    private static final int MAX_STEPS = 7000;
    private static final int alpha = 255;
    private static final int shapeMutationsPerStep = 100;
    private static final int candidateShapesPerStep = 1000;
    private static final List<ShapeType> shapeTypes = new ArrayList<ShapeType>() {
        {
//            add(ShapeType.ELLIPSE);
//            add(ShapeType.CIRCLE);
//            add(ShapeType.LINE);
            add(ShapeType.TRIANGLE);
//            add(ShapeType.RECTANGLE);
//            add(ShapeType.POLYGON);
//            add(ShapeType.POLYLINE);
//            add(ShapeType.ROTATED_ELLIPSE);
//            add(ShapeType.ROTATED_RECTANGLE);
//            add(ShapeType.QUADRATIC_CURVE);
//            add(ShapeType.CUBIC_CURVE);
//            add(ShapeType.TEST);
        }
    };

    public static void main(String[] args) {
        BufferedImage image = null;
        try (InputStream is = Main.class.getResourceAsStream("/example/Up.png")) {
            image = GraphicsUtilities.loadCompatibleImage(is);
        } catch (IOException e) {
            System.out.println("Failed to load the image: " + e);
            System.exit(0);
        }

        // resize the input image to 256 for faster
        image = GraphicsUtilities.createThumbnail(image, 512);

        final int[] data = GraphicsUtilities
                .getPixels(image, 0, 0, image.getWidth(), image.getHeight(), null);
        final Bitmap bitmap = new Bitmap(image.getWidth(), image.getHeight(), data, image.getType() == BufferedImage.TYPE_INT_ARGB);

        final int background = Colour.average(bitmap.getPixels());
        final Geometrizer runner = new Geometrizer(bitmap, background, 1024);

        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        EventQueue.invokeLater(() -> {
            AppWindow window = new AppWindow(runner, background, runner.getModel().scaledWidth,
                    runner.getModel().scaledHeight);
            window.setVisible(true);
        });
    }

    private static class AppWindow extends MainFrame {
        private enum State {
            INIT, RUNNING, PAUSE
        }

        private final List<ShapeResult> results = Collections.synchronizedList(new LinkedList<>());
        private BufferedImage image = null;

        private final Geometrizer runner;
        private final int background;
        private State currentState = State.INIT;

        public AppWindow(Geometrizer runner, int background, int width, int height) {
            super();

            this.runner = runner;
            this.background = background;
            if (image == null) {
                image = GraphicsUtilities.createCompatibleImage(width, height);
                Graphics2D g2d = (Graphics2D)image.getGraphics();
                g2d.setRenderingHints(qualityHints);
                g2d.setColor(new Color(background, true));
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();
            }

            initComponents(width, height);
            pack();
            setLocationRelativeTo(null);
        }

        private void initComponents(final int width, final int height) {
            final MyCanvas canvas = new MyCanvas(width, height);
            final JButton jbtCtrl = new ActionButton("Start", true);
            final JButton jbtShot = new ActionButton("Snapshot");

            final Runnable worker = () -> {
                while (currentState == State.RUNNING && results.size() < MAX_STEPS) {
                    final ShapeResult result = runner.step(shapeTypes, alpha, candidateShapesPerStep, shapeMutationsPerStep);
                    results.add(result);
                    if (image != null) {
                        Graphics2D g2d = (Graphics2D)image.getGraphics();
                        g2d.setRenderingHints(qualityHints);
                        g2d.translate(0.5, 0.5);
                        g2d.scale(runner.getModel().scale, runner.getModel().scale);
                        try {
                            ShapePainter.paint(g2d, result.shape, result.color);
                        } finally {
                            g2d.dispose();
                        }
                    }
                    EventQueue.invokeLater(() -> {
                        System.out.println("Score: " + (1.0f - Core.differenceFull(runner.snapshot(), runner.target())));
                        canvas.repaint();
                        if (results.size() >= MAX_STEPS) {
                            jbtCtrl.setEnabled(false);
                            jbtShot.setEnabled(true);
                        }
                    });
                }
            };

            jbtCtrl.addActionListener(e -> {
                switch (currentState) {
                    case INIT:
                        new Thread(worker).start();
                        currentState = State.RUNNING;
                        jbtCtrl.setText("Pause");
                        break;

                    case RUNNING:
                        currentState = State.PAUSE;
                        jbtCtrl.setText("Resume");
                        break;

                    case PAUSE:
                        new Thread(worker).start();
                        currentState = State.RUNNING;
                        jbtCtrl.setText("Pause");
                        break;
                }

                jbtShot.setEnabled(currentState == State.PAUSE);
            });

            jbtShot.setEnabled(false);
            jbtShot.addActionListener(e -> {
                // Java2D
                String pngFilePath1 = Main.class.getResource("/example/").getFile() + "result1.png";
                try {
                    if (!ImageIO.write(canvas.getImage(), "png", new File(pngFilePath1))) {
                        System.out.println("Failed to write image to: " + pngFilePath1);
                    } else {
                        System.out.println("Wrote image to: " + pngFilePath1);
                    }
                } catch (IOException exc) {
                    System.out.println("Failed to write image to: " + pngFilePath1);
                }

                // Bitmap
                String pngFilePath2 = Main.class.getResource("/example/").getFile() + "result2.png";
                try {
                    BufferedImage image = GraphicsUtilities.createCompatibleImage(runner.snapshot().getWidth(), runner.snapshot().getHeight());
                    GraphicsUtilities.setPixels(image, 0, 0, runner.snapshot().getWidth(), runner.snapshot().getHeight(), runner.snapshot().getPixels());
                    if (!ImageIO.write(image, "png", new File(pngFilePath2))) {
                        System.out.println("Failed to write image to: " + pngFilePath2);
                    } else {
                        System.out.println("Wrote image to: " + pngFilePath2);
                    }
                } catch (IOException exc) {
                    System.out.println("Failed to write image to: " + pngFilePath2);
                }

                // SVG
                String svgFilePath = Main.class.getResource("/example/").getFile() + "result3.svg";
                String svg = SvgExporter.export(results, runner.getModel().scaledWidth, runner.getModel().scaledHeight, runner.getModel().scale, background);
                try (FileWriter writer = new FileWriter(svgFilePath)) {
                    writer.write(svg);
                    writer.flush();
                    writer.close();
                    System.out.println("Wrote image to: " + svgFilePath);
                } catch (IOException exc) {
                    System.out.println("Failed to write image to: " + svgFilePath);
                }
            });

            getContentPanel().setLayout(new BorderLayout());
            getContentPanel().add(canvas, BorderLayout.CENTER);
            getFooter().add(jbtCtrl);
            getFooter().add(jbtShot);
        }

        private class MyCanvas extends JPanel {
            private final int width, height;

            public MyCanvas(int width, int height) {
                super();
                this.width = width;
                this.height = height;
            }

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHints(qualityHints);
                g2d.drawImage(image, 0, 0, null);
                g2d.setColor(Color.WHITE.brighter());
                g2d.setFont(DIGIT_FONT);
                g2d.drawString(String.format("%04d", results.size()), 30, 30);
                double score =  (1.0f - Core
                        .differenceFull(runner.snapshot(), runner.target())) * 100;
                g2d.drawString(String.format("%02.2f", score), 150, 30);
                g2d.setFont(BASIC_FONT);
                g2d.drawString("SHAPES", 46, 45);
                g2d.drawString("SCORE", 175, 45);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, height);
            }

            public BufferedImage getImage() {
                return image;
            }
        }
    }
}
