package com.grievio.ui.components;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
public class PasswordFieldWithToggle extends JPasswordField {
    private final int radius;
    public PasswordFieldWithToggle(int columns, int radius) {
        super(columns); this.radius = radius;
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setBackground(new Color(15, 30, 70));
        setForeground(new Color(220, 235, 255));
        setCaretColor(new Color(99, 160, 255));
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius);
        super.paintComponent(g); g2.dispose();
    }
    @Override protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isFocusOwner() ? new Color(59,130,246) : new Color(40,65,120));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius); g2.dispose();
    }
}
