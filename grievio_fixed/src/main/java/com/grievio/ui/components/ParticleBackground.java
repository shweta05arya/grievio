package com.grievio.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleBackground extends JPanel implements ActionListener {

    private static final Color BG_TOP = new Color(4, 10, 28);
    private static final Color BG_MID = new Color(7, 18, 46);
    private static final Color BG_BOTTOM = new Color(10, 24, 62);

    private static final int FAR_COUNT = 40;
    private static final int MID_COUNT = 30;
    private static final int NEAR_COUNT = 18;
    private static final int STREAK_COUNT = 10;
    private static final int CONNECT_DIST = 110;

    private final Random rng = new Random();
    private final List<Particle> farParticles = new ArrayList<>();
    private final List<Particle> midParticles = new ArrayList<>();
    private final List<Particle> nearParticles = new ArrayList<>();
    private final List<Streak> streaks = new ArrayList<>();
    private final Timer timer;

    public ParticleBackground() {
        setOpaque(true);
        setLayout(new BorderLayout());

        for (int i = 0; i < FAR_COUNT; i++) farParticles.add(new Particle(rng, Layer.FAR, true));
        for (int i = 0; i < MID_COUNT; i++) midParticles.add(new Particle(rng, Layer.MID, true));
        for (int i = 0; i < NEAR_COUNT; i++) nearParticles.add(new Particle(rng, Layer.NEAR, true));
        for (int i = 0; i < STREAK_COUNT; i++) streaks.add(new Streak(rng));

        timer = new Timer(16, this);
        timer.start();
    }

    public void stopAnimation() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateParticles(farParticles);
        updateParticles(midParticles);
        updateParticles(nearParticles);
        for (Streak s : streaks) s.update();
        repaint();
    }

    private void updateParticles(List<Particle> particles) {
        for (Particle p : particles) p.update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            paintBackground(g2, w, h);
            paintAmbientGlow(g2, w, h);
            paintStreaks(g2, w, h);
            paintConnections(g2, w, h, farParticles);
            paintConnections(g2, w, h, midParticles);
            paintParticles(g2, w, h, farParticles);
            paintParticles(g2, w, h, midParticles);
            paintParticles(g2, w, h, nearParticles);
        } catch (Exception ex) {
            // Final fallback so the whole window never becomes blank
            g2.setColor(BG_BOTTOM);
            g2.fillRect(0, 0, w, h);
            ex.printStackTrace();
        } finally {
            g2.dispose();
        }
    }

    private void paintBackground(Graphics2D g2, int w, int h) {
        LinearGradientPaint bg = new LinearGradientPaint(
                0, 0, 0, h,
                new float[]{0f, 0.45f, 1f},
                new Color[]{BG_TOP, BG_MID, BG_BOTTOM}
        );
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);
    }

    private void paintAmbientGlow(Graphics2D g2, int w, int h) {
        RadialGradientPaint centerGlow = new RadialGradientPaint(
                new Point2D.Float(w * 0.55f, h * 0.62f),
                Math.max(w, h) * 0.55f,
                new float[]{0f, 1f},
                new Color[]{safeColor(40, 95, 220, 28), safeColor(0, 0, 0, 0)}
        );
        g2.setPaint(centerGlow);
        g2.fillRect(0, 0, w, h);

        RadialGradientPaint leftGlow = new RadialGradientPaint(
                new Point2D.Float(w * 0.18f, h * 0.35f),
                Math.max(w, h) * 0.35f,
                new float[]{0f, 1f},
                new Color[]{safeColor(75, 120, 255, 18), safeColor(0, 0, 0, 0)}
        );
        g2.setPaint(leftGlow);
        g2.fillRect(0, 0, w, h);
    }

    private void paintStreaks(Graphics2D g2, int w, int h) {
        for (Streak s : streaks) {
            try {
                s.draw(g2, w, h);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void paintConnections(Graphics2D g2, int w, int h, List<Particle> particles) {
        for (int i = 0; i < particles.size(); i++) {
            Particle a = particles.get(i);
            int ax = (int) (a.x * w);
            int ay = (int) (a.y * h);

            for (int j = i + 1; j < particles.size(); j++) {
                Particle b = particles.get(j);
                int bx = (int) (b.x * w);
                int by = (int) (b.y * h);

                double d = Math.hypot(ax - bx, ay - by);
                if (d < CONNECT_DIST && a.layer != Layer.NEAR && b.layer != Layer.NEAR) {
                    float t = 1f - (float) (d / CONNECT_DIST);
                    int alpha = clamp((int) (24 * t));
                    g2.setColor(safeColor(95, 150, 255, alpha));
                    g2.setStroke(new BasicStroke(0.7f));
                    g2.drawLine(ax, ay, bx, by);
                }
            }
        }
    }

    private void paintParticles(Graphics2D g2, int w, int h, List<Particle> particles) {
        for (Particle p : particles) {
            int px = (int) (p.x * w);
            int py = (int) (p.y * h);

            int core = Math.max(2, Math.round(p.size));
            int mid = Math.max(core + 3, Math.round(core * 2.1f));
            int outer = Math.max(mid + 4, Math.round(core * 3.3f));

            g2.setColor(safeColor(70, 145, 255, p.alpha / 8));
            g2.fillOval(px - outer / 2, py - outer / 2, outer, outer);

            g2.setColor(safeColor(115, 180, 255, p.alpha / 4));
            g2.fillOval(px - mid / 2, py - mid / 2, mid, mid);

            g2.setColor(safeColor(220, 240, 255, p.alpha));
            g2.fillOval(px - core / 2, py - core / 2, core, core);

            if (core >= 5) {
                int hi = Math.max(1, core / 3);
                g2.setColor(safeColor(255, 255, 255, Math.min(230, p.alpha)));
                g2.fillOval(px - core / 3, py - core / 3, hi, hi);
            }
        }
    }

    private static Color safeColor(int r, int g, int b, int a) {
        return new Color(clamp(r), clamp(g), clamp(b), clamp(a));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private enum Layer {
        FAR, MID, NEAR
    }

    static class Particle {
        float x, y, vx, vy, size;
        int alpha;
        final Layer layer;
        private final Random rng;
        private float driftPhase;

        Particle(Random rng, Layer layer, boolean randomY) {
            this.rng = rng;
            this.layer = layer;
            reset(randomY);
        }

        void reset(boolean randomY) {
            x = rng.nextFloat();
            y = randomY ? rng.nextFloat() : 1.05f;
            driftPhase = rng.nextFloat() * 6.28f;

            switch (layer) {
                case FAR -> {
                    vx = (rng.nextFloat() - 0.5f) * 0.00030f;
                    vy = -(0.0007f + rng.nextFloat() * 0.0008f);
                    size = 2.5f + rng.nextFloat() * 2.0f;
                    alpha = 90 + rng.nextInt(40);
                }
                case MID -> {
                    vx = (rng.nextFloat() - 0.5f) * 0.00045f;
                    vy = -(0.0010f + rng.nextFloat() * 0.0012f);
                    size = 4.0f + rng.nextFloat() * 3.0f;
                    alpha = 120 + rng.nextInt(50);
                }
                case NEAR -> {
                    vx = (rng.nextFloat() - 0.5f) * 0.00065f;
                    vy = -(0.0015f + rng.nextFloat() * 0.0018f);
                    size = 6.0f + rng.nextFloat() * 4.0f;
                    alpha = 155 + rng.nextInt(60);
                }
            }
        }

        void update() {
            driftPhase += 0.02f;
            x += vx + (float) Math.sin(driftPhase) * 0.00010f;
            y += vy;

            if (x < -0.03f) x = 1.03f;
            if (x > 1.03f) x = -0.03f;
            if (y < -0.10f) reset(false);
        }
    }

    static class Streak {
        float x, y, speed, length;
        int alpha;
        private final Random rng;

        Streak(Random rng) {
            this.rng = rng;
            reset();
        }

        void reset() {
            x = rng.nextFloat();
            y = 1.0f + rng.nextFloat() * 0.35f;
            speed = 0.0025f + rng.nextFloat() * 0.0020f;
            length = 0.05f + rng.nextFloat() * 0.08f;
            alpha = 30 + rng.nextInt(40);
        }

        void update() {
            y -= speed;
            if (y + length < 0) reset();
        }

        void draw(Graphics2D g2, int w, int h) {
            int x1 = (int) (x * w);
            int y1 = (int) (y * h);
            int y2 = (int) ((y - length) * h);

            GradientPaint gp = new GradientPaint(
                    x1, y1, safeColor(120, 180, 255, 0),
                    x1, y2, safeColor(185, 225, 255, alpha)
            );

            g2.setPaint(gp);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(x1, y1, x1, y2);
        }
    }
}
