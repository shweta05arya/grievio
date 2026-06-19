package com.grievio.ui;

import com.grievio.ui.components.BaseWindow;
import com.grievio.ui.login.LoginPage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class RoleSelectionPage extends BaseWindow {

    public RoleSelectionPage() { super(); buildUI(); }

    private void buildUI() {
        JPanel root=new JPanel(new GridBagLayout()); root.setOpaque(false);
        bg.add(root,BorderLayout.CENTER);
        JPanel wrapper=new JPanel(); wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper,BoxLayout.Y_AXIS));

        // Logo
        JPanel logoRow=new JPanel(new FlowLayout(FlowLayout.CENTER,12,0)); logoRow.setOpaque(false); logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel iconBox=makeIconBox("\u2696",new Color(37,99,235),new Color(59,130,246));
        JLabel ln=new JLabel("Grievio"); ln.setFont(new Font("Segoe UI",Font.BOLD,28)); ln.setForeground(Color.WHITE);
        JLabel lt=new JLabel("AI Complaint System"); lt.setFont(new Font("Segoe UI",Font.PLAIN,13)); lt.setForeground(new Color(120,165,255));
        logoRow.add(iconBox); logoRow.add(ln); logoRow.add(lt);

        JPanel bar=gradBar(new Color(37,99,235),new Color(99,200,255));

        JLabel title=new JLabel("Choose Your Portal",SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI",Font.BOLD,34)); title.setForeground(Color.WHITE); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub=new JLabel("Select your role to continue",SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI",Font.PLAIN,15)); sub.setForeground(new Color(130,175,255)); sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardsRow=new JPanel(new GridLayout(1,3,24,0));
        cardsRow.setOpaque(false); cardsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardsRow.setMaximumSize(new Dimension(940,320));

        cardsRow.add(roleCard("\uD83C\uDFE0","Resident","Raise & track\ncomplaints in your society",
            new Color(29,78,216),new Color(59,130,246),new Color(8,16,50), ()->switchTo(new LoginPage("RESIDENT"))));
        cardsRow.add(roleCard("\uD83D\uDEE1","Admin","Manage, assign &\nmonitor complaints",
            new Color(109,40,217),new Color(167,120,255),new Color(20,8,50), ()->switchTo(new AdminTypeSelectionPage())));
        cardsRow.add(roleCard("\uD83D\uDD27","Partner","Resolve assigned\nservice tasks",
            new Color(4,120,87),new Color(16,200,140),new Color(4,20,16), ()->switchTo(new LoginPage("PARTNER"))));

        JLabel footer=new JLabel("\u00A9 2026 Grievio. All rights reserved.",SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI",Font.PLAIN,11)); footer.setForeground(new Color(80,110,160)); footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        wrapper.add(logoRow); wrapper.add(Box.createVerticalStrut(20));
        wrapper.add(bar); wrapper.add(Box.createVerticalStrut(14));
        wrapper.add(title); wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(sub); wrapper.add(Box.createVerticalStrut(38));
        wrapper.add(cardsRow); wrapper.add(Box.createVerticalStrut(30));
        wrapper.add(footer);
        root.add(wrapper);
    }

    private JPanel roleCard(String icon,String label,String desc,Color c1,Color c2,Color bg2,Runnable onClick){
        JPanel p=new JPanel(){
            boolean hover=false;
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter(){
                    @Override public void mouseEntered(MouseEvent e){hover=true;repaint();}
                    @Override public void mouseExited(MouseEvent e){hover=false;repaint();}
                    @Override public void mouseClicked(MouseEvent e){onClick.run();}
                });
            }
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Solid rich card bg
                GradientPaint gp=new GradientPaint(0,0,bg2.brighter(),0,getHeight(),bg2);
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                // Shine overlay
                GradientPaint shine=new GradientPaint(0,0,new Color(255,255,255,hover?30:18),0,getHeight()/2,new Color(255,255,255,0));
                g2.setPaint(shine); g2.fillRoundRect(0,0,getWidth(),getHeight()/2,24,24);
                // Border
                g2.setColor(hover?new Color(c2.getRed(),c2.getGreen(),c2.getBlue(),200):new Color(c1.getRed(),c1.getGreen(),c1.getBlue(),100));
                g2.setStroke(new BasicStroke(hover?2f:1.4f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);
                // Colored top strip
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2));
                g2.fillRoundRect(0,0,getWidth(),5,24,24);
                // Icon circle
                int cx=getWidth()/2, cy=86;
                g2.setPaint(new GradientPaint(cx-34,cy-34,c1,cx+34,cy+34,c2));
                g2.fillOval(cx-38,cy-38,76,76);
                // Inner shine on icon circle
                g2.setColor(new Color(255,255,255,40));
                g2.fillOval(cx-30,cy-38,40,30);
                g2.dispose(); super.paintComponent(g);
            }
        };
        p.setOpaque(false); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(30,18,28,18)); p.setPreferredSize(new Dimension(260,310));

        p.add(Box.createVerticalStrut(48));
        JLabel ico=new JLabel(icon,SwingConstants.CENTER); ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,30)); ico.setForeground(Color.WHITE); ico.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(ico);
        p.add(Box.createVerticalStrut(52));
        JLabel lbl=new JLabel(label,SwingConstants.CENTER); lbl.setFont(new Font("Segoe UI",Font.BOLD,20)); lbl.setForeground(Color.WHITE); lbl.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(lbl);
        p.add(Box.createVerticalStrut(8));
        for(String line:desc.split("\n")){
            JLabel dl=new JLabel(line,SwingConstants.CENTER); dl.setFont(new Font("Segoe UI",Font.PLAIN,13)); dl.setForeground(new Color(c2.getRed(),c2.getGreen(),c2.getBlue(),200)); dl.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(dl);
        }
        p.add(Box.createVerticalGlue());
        JLabel enter=new JLabel("Enter  \u2192",SwingConstants.CENTER); enter.setFont(new Font("Segoe UI",Font.BOLD,13)); enter.setForeground(new Color(c2.getRed(),c2.getGreen(),c2.getBlue())); enter.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(enter);
        return p;
    }

    public static JPanel makeIconBox(String icon,Color c1,Color c2){
        JPanel b=new JPanel(new GridBagLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),getHeight(),c2));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(255,255,255,40)); g2.fillRoundRect(0,0,getWidth(),getHeight()/2,12,12);
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setOpaque(false); b.setPreferredSize(new Dimension(48,48));
        JLabel il=new JLabel(icon); il.setFont(new Font("Segoe UI Emoji",Font.PLAIN,22)); il.setForeground(Color.WHITE); b.add(il);
        return b;
    }

    static JPanel gradBar(Color c1,Color c2){
        JPanel b=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2));g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);g2.dispose();}};
        b.setOpaque(false); b.setMaximumSize(new Dimension(70,3)); b.setAlignmentX(Component.CENTER_ALIGNMENT); return b;
    }
}
