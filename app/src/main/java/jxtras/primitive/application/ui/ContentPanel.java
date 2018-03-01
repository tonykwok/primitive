package jxtras.primitive.application.ui;

import org.jdesktop.swingx.util.GraphicsUtilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

class ContentPanel extends JPanel {

    private LinearGradientPaint backgroundGradient;
    private BufferedImage light;
    private float lightOpacity;

    // Intermediate image to avoid gradient repaints
    private Image gradientImage = null;

    ContentPanel() {
        backgroundGradient = new LinearGradientPaint(0, 0, 0, 584, new float[]{0.22f, 0.9f},
                new Color[]{Color.decode("#202737"), Color.decode("#8590A5")});
        try {
            light = GraphicsUtilities.loadCompatibleImage(
                    getClass().getResourceAsStream("/icon/content-light.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lightOpacity = 0.5f;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }

        Graphics2D g2 = (Graphics2D)g;

        if (gradientImage == null) {
            // Only create this once; this assumes that the size of this
            // container never changes
            gradientImage = GraphicsUtilities.createCompatibleImage(getWidth(), getHeight());
            Graphics2D g2d = (Graphics2D)gradientImage.getGraphics();
            Composite composite = g2.getComposite();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setPaint(backgroundGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lightOpacity));
            g2d.drawImage(light, 0, 0, getWidth(), light.getHeight(), null);
            g2d.setComposite(composite);
            g2d.dispose();
            // !!! ONLY BECAUSE WE NEVER RECREATE THE INTERMEDIATE IMAGE
            light = null;
        }

        g2.drawImage(gradientImage, 0, 0, null);
    }
}