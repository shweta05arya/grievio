package com.grievio.ui;

import com.grievio.ui.components.BaseWindow;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class IntroScreen extends BaseWindow {

    private static final String[] TITLES = {
        "Grievio", "Smart Complaint Handling",
        "Because Your Decision Matters", "Real-Time Complaint Updates"
    };
    private static final String[] SUBS = {
        "AI Complaint Management System",
        "AI-powered routing, resolution and complaint tracking",
        "Participate in society decisions and updates with transparency",
        "Track every complaint from submission to completion"
    };
    private static final String[]  ICONS  = {"\u2696", "\uD83E\uDD16", "\uD83C\uDFDB", "\uD83D\uDCE1"};
    private static final Color[][] COLORS = {
        {new Color(37,99,235),   new Color(99,160,255)},
        {new Color(109,40,217),  new Color(167,120,255)},
        {new Color(7,130,100),   new Color(16,200,150)},
        {new Color(180,80,20),   new Color(245,158,11)}
    };

    private int slide=0, nextSlide=0;
    private float alpha=1f;
    private boolean fading=false;
    private JLabel iconLbl, titleLbl, subLbl;
    private JPanel dotsRow, cardPanel;
    private JButton actionBtn, skipBtn;
    private Timer fadeOut, fadeIn;

    public IntroScreen() { super(); buildUI(); }

    private void buildUI() {
        JPanel overlay=new JPanel(new GridBagLayout()); overlay.setOpaque(false);
        bg.add(overlay, BorderLayout.CENTER);
        cardPanel = buildCard();
        overlay.add(cardPanel);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Rich solid card — royal premium look
                Color c1=COLORS[slide][0], c2=COLORS[slide][1];
                GradientPaint gp=new GradientPaint(0,0,new Color(8,18,50),getWidth(),getHeight(),new Color(14,28,80));
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),28,28);
                // Shiny top-left highlight
                GradientPaint shine=new GradientPaint(0,0,new Color(255,255,255,28),getWidth()/2,getHeight()/2,new Color(255,255,255,0));
                g2.setPaint(shine); g2.fillRoundRect(0,0,getWidth(),getHeight()/2,28,28);
                // Accent border with slide color
                g2.setColor(new Color(c1.getRed(),c1.getGreen(),c1.getBlue(),140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,28,28);
                // Colored top strip
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2));
                g2.fillRoundRect(0,0,getWidth(),6,28,28);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false); card.setPreferredSize(new Dimension(680,500));

        // Top bar
        JPanel topBar=new JPanel(new FlowLayout(FlowLayout.RIGHT,22,14)); topBar.setOpaque(false);
        skipBtn=linkBtn("Skip \u2192"); skipBtn.addActionListener(e->switchTo(new RoleSelectionPage()));
        topBar.add(skipBtn); card.add(topBar,BorderLayout.NORTH);

        // Centre
        JPanel centre=new JPanel(); centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre,BoxLayout.Y_AXIS));
        centre.setBorder(new EmptyBorder(0,70,0,70));

        iconLbl=new JLabel(ICONS[0],SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji",Font.PLAIN,72));
        iconLbl.setForeground(new Color(180,215,255)); iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLbl=new JLabel(TITLES[0],SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI",Font.BOLD,36));
        titleLbl.setForeground(Color.WHITE); titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        subLbl=new JLabel("<html><div style='text-align:center;'>"+SUBS[0]+"</div></html>",SwingConstants.CENTER);
        subLbl.setFont(new Font("Segoe UI",Font.PLAIN,15));
        subLbl.setForeground(new Color(160,200,255)); subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLbl.setPreferredSize(new Dimension(500,52)); subLbl.setMaximumSize(new Dimension(500,52));

        centre.add(Box.createVerticalGlue());
        centre.add(iconLbl); centre.add(Box.createVerticalStrut(20));
        centre.add(titleLbl); centre.add(Box.createVerticalStrut(14));
        centre.add(subLbl); centre.add(Box.createVerticalGlue());
        card.add(centre,BorderLayout.CENTER);

        // Bottom
        JPanel bottom=new JPanel(new BorderLayout()); bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0,36,28,36));
        JButton backBtn=linkBtn("\u2190 Back");
        backBtn.addActionListener(e->{ if(slide>0) startFade(slide-1); });
        dotsRow=new JPanel(new FlowLayout(FlowLayout.CENTER,8,0)); dotsRow.setOpaque(false);
        refreshDots();
        actionBtn=solidBtn("Next  \u2192");
        actionBtn.addActionListener(e->{ if(slide<TITLES.length-1) startFade(slide+1); else switchTo(new RoleSelectionPage()); });
        bottom.add(backBtn,BorderLayout.WEST); bottom.add(dotsRow,BorderLayout.CENTER); bottom.add(actionBtn,BorderLayout.EAST);
        card.add(bottom,BorderLayout.SOUTH);

        setupFadeTimers();
        return card;
    }

    private void setupFadeTimers() {
        fadeOut=new Timer(14,null);
        fadeOut.addActionListener(ev->{
            alpha=Math.max(0f,alpha-0.09f); applyAlpha();
            if(alpha<=0f){fadeOut.stop();slide=nextSlide;applySlide();cardPanel.repaint();fadeIn.start();}
        });
        fadeIn=new Timer(14,null);
        fadeIn.addActionListener(ev->{
            alpha=Math.min(1f,alpha+0.09f); applyAlpha();
            if(alpha>=1f){fadeIn.stop();fading=false;}
        });
    }

    private void startFade(int to){ if(fading)return;fading=true;nextSlide=to;alpha=1f;fadeOut.start(); }

    private void applySlide(){
        iconLbl.setText(ICONS[slide]); titleLbl.setText(TITLES[slide]);
        subLbl.setText("<html><div style='text-align:center;'>"+SUBS[slide]+"</div></html>");
        actionBtn.setText(slide==TITLES.length-1?"Get Started  \u2713":"Next  \u2192");
        skipBtn.setVisible(slide<TITLES.length-1); refreshDots();
    }
    private void applyAlpha(){
        int a=Math.max(0,Math.min(255,(int)(alpha*255)));
        titleLbl.setForeground(new Color(255,255,255,a));
        subLbl.setForeground(new Color(160,200,255,a));
        iconLbl.setForeground(new Color(180,215,255,a));
    }
    private void refreshDots(){
        dotsRow.removeAll();
        for(int i=0;i<TITLES.length;i++){
            final int idx=i; boolean active=(i==slide);
            JPanel dot=new JPanel(){
                @Override public Dimension getPreferredSize(){return new Dimension(active?28:9,9);}
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(active){g2.setPaint(new GradientPaint(0,0,COLORS[slide][0],getWidth(),0,COLORS[slide][1]));}
                    else{g2.setColor(new Color(255,255,255,55));}
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),9,9); g2.dispose();
                }
            };
            dot.setOpaque(false); dot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dot.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){startFade(idx);}});
            dotsRow.add(dot);
        }
        dotsRow.revalidate(); dotsRow.repaint();
    }

    private JButton solidBtn(String text){
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1=COLORS[slide][0],c2=COLORS[slide][1];
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,14)); b.setForeground(Color.WHITE);
        b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setPreferredSize(new Dimension(175,42));return b;
    }
    private JButton linkBtn(String text){
        JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.PLAIN,13));b.setForeground(new Color(130,175,255));
        b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;
    }
}
