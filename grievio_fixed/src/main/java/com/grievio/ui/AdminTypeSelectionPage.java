package com.grievio.ui;

import com.grievio.ui.components.BaseWindow;
import com.grievio.ui.login.LoginPage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class AdminTypeSelectionPage extends BaseWindow {

    public AdminTypeSelectionPage() { super(); buildUI(); }

    private void buildUI(){
        JPanel root=new JPanel(new GridBagLayout()); root.setOpaque(false);
        bg.add(root,BorderLayout.CENTER);
        JPanel wrapper=new JPanel(); wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper,BoxLayout.Y_AXIS));

        JButton back=backBtn(); back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e->switchTo(new RoleSelectionPage()));

        JPanel bar=RoleSelectionPage.gradBar(new Color(109,40,217),new Color(167,120,255));

        JLabel title=new JLabel("Select Admin Access",SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI",Font.BOLD,34)); title.setForeground(Color.WHITE); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub=new JLabel("Choose your administrative portal",SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI",Font.PLAIN,15)); sub.setForeground(new Color(160,130,255)); sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardsRow=new JPanel(new GridLayout(1,3,24,0));
        cardsRow.setOpaque(false); cardsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardsRow.setMaximumSize(new Dimension(940,310));

        cardsRow.add(adminCard("\uD83C\uDFE2","Society Admin","Manage complaints\nwithin your society",
            new Color(109,40,217),new Color(167,120,255),new Color(18,6,50), ()->switchTo(new LoginPage("SOCIETY_ADMIN"))));
        cardsRow.add(adminCard("\uD83D\uDDFA","Sector Admin","Oversee multiple\nsociety complaints",
            new Color(7,89,133),new Color(14,200,233),new Color(4,18,36), ()->switchTo(new LoginPage("SECTOR_ADMIN"))));
        cardsRow.add(adminCard("\uD83C\uDFDB","Government Portal","District-level governance\n& policy decisions",
            new Color(120,53,15),new Color(245,158,11),new Color(30,14,4), ()->switchTo(new LoginPage("GOVERNMENT"))));

        JLabel footer=new JLabel("\u00A9 2026 Grievio. All rights reserved.",SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI",Font.PLAIN,11)); footer.setForeground(new Color(80,110,160)); footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        wrapper.add(back); wrapper.add(Box.createVerticalStrut(18));
        wrapper.add(bar); wrapper.add(Box.createVerticalStrut(14));
        wrapper.add(title); wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(sub); wrapper.add(Box.createVerticalStrut(36));
        wrapper.add(cardsRow); wrapper.add(Box.createVerticalStrut(28));
        wrapper.add(footer);
        root.add(wrapper);
    }

    private JPanel adminCard(String icon,String label,String desc,Color c1,Color c2,Color bg2,Runnable onClick){
        JPanel p=new JPanel(){
            boolean hover=false;
            { setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){hover=true;repaint();}
                @Override public void mouseExited(MouseEvent e){hover=false;repaint();}
                @Override public void mouseClicked(MouseEvent e){onClick.run();}
              });
            }
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,bg2.brighter(),0,getHeight(),bg2));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                g2.setPaint(new GradientPaint(0,0,new Color(255,255,255,hover?28:16),0,getHeight()/2,new Color(255,255,255,0)));
                g2.fillRoundRect(0,0,getWidth(),getHeight()/2,24,24);
                g2.setColor(hover?new Color(c2.getRed(),c2.getGreen(),c2.getBlue(),200):new Color(c1.getRed(),c1.getGreen(),c1.getBlue(),100));
                g2.setStroke(new BasicStroke(hover?2f:1.4f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2)); g2.fillRoundRect(0,0,getWidth(),5,24,24);
                int cx=getWidth()/2,cy=82;
                g2.setPaint(new GradientPaint(cx-34,cy-34,c1,cx+34,cy+34,c2)); g2.fillOval(cx-38,cy-38,76,76);
                g2.setColor(new Color(255,255,255,38)); g2.fillOval(cx-30,cy-38,40,28);
                g2.dispose(); super.paintComponent(g);
            }
        };
        p.setOpaque(false); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28,18,26,18)); p.setPreferredSize(new Dimension(258,298));
        p.add(Box.createVerticalStrut(44));
        JLabel ico=new JLabel(icon,SwingConstants.CENTER); ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,28)); ico.setForeground(Color.WHITE); ico.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(ico);
        p.add(Box.createVerticalStrut(50));
        JLabel lbl=new JLabel(label,SwingConstants.CENTER); lbl.setFont(new Font("Segoe UI",Font.BOLD,18)); lbl.setForeground(Color.WHITE); lbl.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(lbl);
        p.add(Box.createVerticalStrut(8));
        for(String line:desc.split("\n")){
            JLabel dl=new JLabel(line,SwingConstants.CENTER); dl.setFont(new Font("Segoe UI",Font.PLAIN,13)); dl.setForeground(new Color(c2.getRed(),c2.getGreen(),c2.getBlue(),190)); dl.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(dl);
        }
        p.add(Box.createVerticalGlue());
        JLabel enter=new JLabel("Enter  \u2192",SwingConstants.CENTER); enter.setFont(new Font("Segoe UI",Font.BOLD,13)); enter.setForeground(new Color(c2.getRed(),c2.getGreen(),c2.getBlue())); enter.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(enter);
        return p;
    }

    static JButton backBtn(){
        JButton b=new JButton("\u2190 Back");
        b.setFont(new Font("Segoe UI",Font.PLAIN,13)); b.setForeground(new Color(130,175,255));
        b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
}
