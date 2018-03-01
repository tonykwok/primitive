package jxtras.primitive.application;

import jxtras.primitive.util.Colour;
import org.jdesktop.swingx.util.GraphicsUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jxtras.primitive.Core;
import jxtras.primitive.Model;
import jxtras.primitive.Model.ShapeResult;
import jxtras.primitive.exporter.SvgExporter;
import jxtras.primitive.image.Bitmap;
import jxtras.primitive.shape.ShapeType;

public class Main2 {

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

        // Run the image runner until the image is geometrized
        final int background = Colour.average(bitmap.getPixels());
        final Model model = new Model(bitmap, background, 1024, Model.NTHREADS);
        final ShapeType shapeType = ShapeType.TEST;
        final int repeat = 0;
        final int alpha = 128;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AppWindow window = new AppWindow(model, shapeType, repeat, alpha, background,
                        model.scaledWidth, model.scaledHeight);
                window.setVisible(true);
            }
        });
    }

    private static class AppWindow extends JFrame {
        private static final int MAX_STEPS = 100;
        private final List<ShapeResult> results = new ArrayList<ShapeResult>();

        private final Model model;
        private final int repeat, alpha;
        private final ShapeType shapeType;
        private final int background;
        private State currentState = State.INIT;

        public AppWindow(Model model, ShapeType shapeType, int repeat, int alpha, int background,
                int width, int height) {
            super("Geometerizer");

            this.model = model;
            this.shapeType = shapeType;
            this.alpha = alpha;
            this.repeat = repeat;
            this.background = background;

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            initComponents(width, height);
            pack();
            setResizable(false);
            setLocationRelativeTo(null);
        }

        private void initComponents(final int width, final int height) {
            final MyCanvas canvas = new MyCanvas(width, height);
            final JButton jbtCtrl = new JButton("Start");
            final JButton jbtSave = new JButton("Save");

            final Runnable worker = () -> {
                while (currentState == State.RUNNING && results.size() < MAX_STEPS) {
                    final int result = model.step(shapeType, alpha, 0);
                    EventQueue.invokeLater(() -> {
                        if (results.size() < MAX_STEPS) {
                            System.out.println("Score: " + Core
                                    .differenceFull(model.target, model.current));
                            canvas.repaint();
                        }
                        if (results.size() >= MAX_STEPS) {
                            jbtCtrl.setEnabled(false);
                            jbtSave.setEnabled(true);
                        }
                    });
                }
            };

            canvas.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    model.step(shapeType, alpha, 0);
                    canvas.repaint();
                }
            });

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

                jbtSave.setEnabled(currentState == State.PAUSE);
            });

            jbtSave.setEnabled(false);
            jbtSave.addActionListener(e -> {
                String pngFilePath = Main.class.getResource("/example/").getFile() + "result.png";
                try {
                    if (!ImageIO.write(canvas.getImage(), "png", new File(pngFilePath))) {
                        System.out.println("Failed to write image to: " + pngFilePath);
                    } else {
                        System.out.println("Wrote image to: " + pngFilePath);
                    }
                } catch (IOException exc) {
                    System.out.println("Failed to write image to: " + pngFilePath);
                }

                String svgFilePath = Main.class.getResource("/example/").getFile() + "result.svg";
                String svg = SvgExporter.export(results, model.scaledWidth, model.scaledHeight, model.scale, background);
                try (FileWriter writer = new FileWriter(svgFilePath)) {
                    writer.write(svg);
                    writer.flush();
                    writer.close();
                    System.out.println("Wrote image to: " + svgFilePath);
                } catch (IOException exc) {
                    System.out.println("Failed to write image to: " + svgFilePath);
                }
            });

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(canvas, BorderLayout.CENTER);

            JPanel panel = new JPanel();
            panel.add(jbtCtrl);
            panel.add(jbtSave);
            getContentPane().add(panel, BorderLayout.SOUTH);
        }

        private enum State {
            INIT, RUNNING, PAUSE
        }

        private class MyCanvas extends JPanel {
            private final int width, height;
            private BufferedImage image = null;

            public MyCanvas(int width, int height) {
                super();
                this.width = width;
                this.height = height;
            }

            @Override
            public void paintComponent(Graphics g) {
                Bitmap current = model.snapshot();
                if (image == null) {
                    image = GraphicsUtilities.createCompatibleImage(current.getWidth(), current.getHeight());
                }
                GraphicsUtilities.setPixels(image, 0, 0, current.getWidth(), current.getHeight(), current.getPixels());

                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setRenderingHints(qualityHints);
                g2d.drawImage(image, 0, 0, width, height, null);
                g2d.setColor(Color.GREEN);
                g2d.fillOval(132* 2, 192 * 2, 5, 5);
                g2d.setColor(Color.BLACK.brighter());
                g2d.setFont(CRYSTA_FONT);
                g2d.drawString(String.format("%04d", results.size()), 30, 30);
                g2d.setFont(BASIC_FONT);
                g2d.drawString("SHAPES", 46, 45);
                g2d.dispose();
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

    private static Font CRYSTA_FONT;
    private static Font BASIC_FONT = new Font("Dialog", Font.ITALIC, 10);

    static {
        try {
            InputStream is = Main.class.getResourceAsStream("/font/Crysta.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            CRYSTA_FONT = font.deriveFont(32.0f);
        } catch (Exception ex) {
            System.err.println("Crysta.ttf not loaded. Using serif font.");
            CRYSTA_FONT = BASIC_FONT.deriveFont(Font.PLAIN, 32.0f);
        }
    }

    /**
     * A set of high-quality rendering hints for the keys ANTIALIASING, INTERPOLATION,
     * COLOR_RENDER, STROKE, and RENDER.
     */
    protected static final RenderingHints qualityHints = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    static {
        qualityHints.put(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        qualityHints.put(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        qualityHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
