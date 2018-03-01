package jxtras.primitive.application.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;


class Footer extends JPanel {

    /**
     * Footer.preferredHeight=17
     * Footer.lightColor={Common.lightColor}
     * Footer.shadowColor={Common.darkColor}
     * Footer.inactiveLightColor={TitlePanel.inactiveLightColor}
     * Footer.inactiveShadowColor={TitlePanel.inactiveShadowColor}
     * Footer.backgroundGradient=0,0 | 0,15 | #666F7F | #202737
     * Footer.inactiveBackgroundGradient=0,0 | 0,15 | #727272 | #383838
     */
    private GradientPaint backgroundGradient;
    private GradientPaint inactiveBackgroundGradient;
    private int preferredHeight;
    private Color lightColor;
    private Color shadowColor;
    private Color inactiveLightColor;
    private Color inactiveShadowColor;

    Footer() {
        backgroundGradient = new GradientPaint(0, 0, Color.decode("#666F7F"), 0, 15,
                Color.decode("#202737"));
        inactiveBackgroundGradient = new GradientPaint(0, 0, Color.decode("#727272"), 0, 15,
                Color.decode("#383838"));
        preferredHeight = 40; // 17;
        lightColor = Color.decode("#4B5461");
        shadowColor = Color.decode("#202737");
        inactiveLightColor = Color.decode("#565656");
        inactiveShadowColor = Color.decode("#2B2B2B");
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
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

        Paint paint = g2.getPaint();
        g2.setPaint(active ? backgroundGradient : inactiveBackgroundGradient);
        Rectangle clip = g2.getClipBounds();
        clip = clip.intersection(new Rectangle(0, 2, getWidth(), getHeight()));
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setPaint(paint);

        g2.setColor(active ? lightColor : inactiveLightColor);
        g2.drawLine(0, 0, getWidth(), 0);

        g2.setColor(active ? shadowColor : inactiveShadowColor);
        g2.drawLine(0, 1, getWidth(), 1);
    }
}