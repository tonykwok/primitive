package jxtras.primitive.application.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

class FrameBorder implements Border {
    private final Border border;
    private final Border inactiveBorder;

    private Color lightColor;
    private Color mediumColor;
    private Color shadowColor;
    private Color inactiveLightColor;
    private Color inactiveMediumColor;
    private Color inactiveShadowColor;

    FrameBorder() {
        lightColor = Color.decode("#4B5461");
        mediumColor = Color.decode("#3A424F");
        shadowColor = Color.decode("#343A4D");
        inactiveLightColor = Color.decode("#565656");
        inactiveMediumColor = Color.decode("#444444");
        inactiveShadowColor = Color.decode("#404040");

        Border shadow = BorderFactory.createLineBorder(shadowColor);
        Border medium = BorderFactory.createLineBorder(mediumColor);
        Border light = BorderFactory.createLineBorder(lightColor);
        border = new CompoundBorder(light, new CompoundBorder(medium, shadow));

        shadow = BorderFactory.createLineBorder(inactiveShadowColor);
        medium = BorderFactory.createLineBorder(inactiveMediumColor);
        light = BorderFactory.createLineBorder(inactiveLightColor);
        inactiveBorder = new CompoundBorder(light, new CompoundBorder(medium, shadow));
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (SwingUtilities.getWindowAncestor(c).isActive()) {
            border.paintBorder(c, g, x, y, width, height);
        } else {
            inactiveBorder.paintBorder(c, g, x, y, width, height);
        }
    }

    public Insets getBorderInsets(Component c) {
        return border.getBorderInsets(c);
    }

    public boolean isBorderOpaque() {
        return border.isBorderOpaque();
    }
}