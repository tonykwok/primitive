package jxtras.primitive.application.ui;

import org.jdesktop.swingx.util.GraphicsUtilities;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

class TitlePanel extends JComponent {
    private JButton closeButton;
    private JButton iconifyButton;

    /**
     * TitlePanel.preferredHeight=20
     * <p>
     * TitlePanel.lightColor={Common.lightColor}
     * TitlePanel.shadowColor={Common.darkColor}
     * <p>
     * TitlePanel.inactiveLightColor=#565656
     * TitlePanel.inactiveShadowColor=#2B2B2B
     * <p>
     * TitlePanel.grip=/resources/photos/title-grip.png
     * TitlePanel.backgroundGradient=/resources/photos/title-background.png
     * <p>
     * TitlePanel.inactiveGrip=/resources/photos/title-grip-inactive.png
     * TitlePanel.inactiveBackgroundGradient=/resources/photos/title-background-inactive.png
     * <p>
     * TitlePanel.close=/resources/photos/title-close.png
     * TitlePanel.closeInactive=/resources/photos/title-close-inactive.png
     * TitlePanel.closeOver=/resources/photos/title-close-over.png
     * TitlePanel.closePressed=/resources/photos/title-close-pressed.png
     * <p>
     * TitlePanel.minimize=/resources/photos/title-minimize.png
     * TitlePanel.minimizeInactive=/resources/photos/title-minimize-inactive.png
     * TitlePanel.minimizeOver=/resources/photos/title-minimize-over.png
     * TitlePanel.minimizePressed=/resources/photos/title-minimize-pressed.png
     */
    private int preferredHeight;
    private Color lightColor;
    private Color shadowColor;
    private Color inactiveLightColor;
    private Color inactiveShadowColor;
    private BufferedImage grip;
    private BufferedImage backgroundGradient;
    private BufferedImage inactiveGrip;
    private BufferedImage inactiveBackgroundGradient;
    private BufferedImage close;
    private BufferedImage closeInactive;
    private BufferedImage closeOver;
    private BufferedImage closePressed;
    private BufferedImage minimize;
    private BufferedImage minimizeInactive;
    private BufferedImage minimizeOver;
    private BufferedImage minimizePressed;

    TitlePanel() {
        preferredHeight = 20;
        lightColor = Color.decode("#4B5461");
        shadowColor = Color.decode("#202737");
        inactiveLightColor = Color.decode("#565656");
        inactiveShadowColor = Color.decode("#2B2B2B");
        try {
            grip = GraphicsUtilities
                    .loadCompatibleImage(getClass().getResourceAsStream("/icon/title-grip.png"));
            backgroundGradient = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-background.png"));
            inactiveGrip = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-grip-inactive.png"));
            inactiveBackgroundGradient = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-background-inactive.png"));
            close = GraphicsUtilities
                    .loadCompatibleImage(getClass().getResourceAsStream("/icon/title-close.png"));
            closeInactive = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-close-inactive.png"));
            closeOver = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-close-over.png"));
            closePressed = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-close-pressed.png"));
            minimize = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-minimize.png"));
            minimizeInactive = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-minimize-inactive.png"));
            minimizeOver = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-minimize-over.png"));
            minimizePressed = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/title-minimize-pressed.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        setLayout(new GridBagLayout());
        createButtons();
    }

    void installListeners() {
        MouseInputHandler handler = new MouseInputHandler();
        Window window = SwingUtilities.getWindowAncestor(this);
        window.addMouseListener(handler);
        window.addMouseMotionListener(handler);

        window.addWindowListener(new WindowHandler());
    }

    private void createButtons() {
        add(Box.createHorizontalGlue(),
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        add(iconifyButton = createButton(new IconifyAction(), minimize, minimizePressed,
                minimizeOver),
                new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHEAST,
                        GridBagConstraints.NONE, new Insets(1, 0, 0, 2), 0, 0));
        add(closeButton = createButton(new CloseAction(), close, closePressed, closeOver),
                new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHEAST,
                        GridBagConstraints.NONE, new Insets(1, 0, 0, 2), 0, 0));
    }

    private static JButton createButton(final AbstractAction action, final BufferedImage image,
            final Image pressedImage, final Image overImage) {
        JButton button = new JButton(action);
        button.setIcon(new ImageIcon(image));
        button.setPressedIcon(new ImageIcon(pressedImage));
        button.setRolloverIcon(new ImageIcon(overImage));
        button.setRolloverEnabled(true);
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        return button;
    }

    private void close() {
        Window w = SwingUtilities.getWindowAncestor(this);
        w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
    }

    private void iconify() {
        Frame frame = (Frame)SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.setExtendedState(frame.getExtendedState() | Frame.ICONIFIED);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension size = super.getMinimumSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = super.getMaximumSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }

        boolean active = SwingUtilities.getWindowAncestor(this).isActive();

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        Rectangle clip = g2.getClipBounds();
        g2.drawImage(active ? backgroundGradient : inactiveBackgroundGradient, clip.x, 0,
                clip.width, getHeight() - 2, null);

        g2.setColor(active ? lightColor : inactiveLightColor);
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

        g2.setColor(active ? shadowColor : inactiveShadowColor);
        g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);

        g2.drawImage(active ? grip : inactiveGrip, 0, 0, null);
    }

    private class CloseAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }

    private class IconifyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            iconify();
        }
    }

    private class MouseInputHandler implements MouseInputListener {
        private boolean isMovingWindow;
        private int dragOffsetX;
        private int dragOffsetY;

        private static final int BORDER_DRAG_THICKNESS = 5;

        public void mousePressed(MouseEvent ev) {
            Point dragWindowOffset = ev.getPoint();
            Window w = (Window)ev.getSource();
            if (w != null) {
                w.toFront();
            }
            Point convertedDragWindowOffset = SwingUtilities
                    .convertPoint(w, dragWindowOffset, TitlePanel.this);

            Frame f = null;
            Dialog d = null;

            if (w instanceof Frame) {
                f = (Frame)w;
            } else if (w instanceof Dialog) {
                d = (Dialog)w;
            }

            int frameState = (f != null) ? f.getExtendedState() : 0;

            if (TitlePanel.this.contains(convertedDragWindowOffset)) {
                if ((f != null && ((frameState & Frame.MAXIMIZED_BOTH) == 0) || (d != null)) &&
                        dragWindowOffset.y >= BORDER_DRAG_THICKNESS && dragWindowOffset.x >=
                        BORDER_DRAG_THICKNESS && dragWindowOffset.x < w
                        .getWidth() - BORDER_DRAG_THICKNESS) {
                    isMovingWindow = true;
                    dragOffsetX = dragWindowOffset.x;
                    dragOffsetY = dragWindowOffset.y;
                }
            } else if (f != null && f
                    .isResizable() && ((frameState & Frame.MAXIMIZED_BOTH) == 0) || (d != null && d
                    .isResizable())) {
                dragOffsetX = dragWindowOffset.x;
                dragOffsetY = dragWindowOffset.y;
            }
        }

        public void mouseReleased(MouseEvent ev) {
            isMovingWindow = false;
        }

        public void mouseDragged(MouseEvent ev) {
            Window w = (Window)ev.getSource();

            if (isMovingWindow) {
                Point windowPt = MouseInfo.getPointerInfo().getLocation();
                windowPt.x = windowPt.x - dragOffsetX;
                windowPt.y = windowPt.y - dragOffsetY;
                w.setLocation(windowPt);
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    private class WindowHandler extends WindowAdapter {
        @Override
        public void windowActivated(WindowEvent ev) {
            closeButton.setIcon(new ImageIcon(close));
            iconifyButton.setIcon(new ImageIcon(minimize));
            getRootPane().repaint();
        }

        @Override
        public void windowDeactivated(WindowEvent ev) {
            closeButton.setIcon(new ImageIcon(closeInactive));
            iconifyButton.setIcon(new ImageIcon(minimizeInactive));
            getRootPane().repaint();
        }
    }
}