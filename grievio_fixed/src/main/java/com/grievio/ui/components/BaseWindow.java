package com.grievio.ui.components;

import javax.swing.*;
import java.awt.*;

public class BaseWindow extends JFrame {
    protected final ParticleBackground bg;

    public BaseWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Grievio \u2013 AI Complaint System");
        setSize(1400, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        bg = new ParticleBackground();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);
    }

    /** Fade-transition to next window */
    protected void switchTo(JFrame next) {
        bg.stopAnimation();
        dispose();
        SwingUtilities.invokeLater(() -> next.setVisible(true));
    }
}
