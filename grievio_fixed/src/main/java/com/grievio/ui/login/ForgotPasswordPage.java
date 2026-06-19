package com.grievio.ui.login;

import com.grievio.email.EmailService;
import com.grievio.db.DatabaseManager;
import com.grievio.ui.components.*;
import com.grievio.ui.RoleSelectionPage;
import com.grievio.util.OTPGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Forgot Password page.
 * Uses REAL Gmail SMTP via EmailService to send OTP.
 * Exact same split-panel dark theme as login page.
 */
public class ForgotPasswordPage extends BaseWindow {

    private final String role;
    private String currentOTP;
    private boolean otpVerified = false;
    private long otpSentTime = 0;

    private RoundedTextField        emailField, otpField;
    private PasswordFieldWithToggle newPassField, confirmPassField;
    private JButton                 sendOTPBtn, verifyOTPBtn, resetBtn;
    private JLabel                  statusLbl;

    public ForgotPasswordPage(String role) {
        super(); this.role = role; buildUI();
    }

    private void buildUI() {
        JPanel split = new JPanel(new BorderLayout()); split.setOpaque(false);
        bg.add(split, BorderLayout.CENTER);
        split.add(buildLeft(),  BorderLayout.WEST);
        split.add(buildRight(), BorderLayout.CENTER);
    }

    private JPanel buildLeft() {
        JPanel left = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(new Color(2,8,28,200)); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,14)); g2.fillRect(getWidth()-1,0,1,getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        left.setOpaque(false); left.setPreferredSize(new Dimension(520,0)); left.setLayout(new GridBagLayout());
        JPanel c = new JPanel(); c.setOpaque(false); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(48,54,48,44));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); logoRow.setOpaque(false); logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel iconBox = RoleSelectionPage.makeIconBox("\u2696",new Color(37,99,235),new Color(59,130,246));
        JPanel nameCol = new JPanel(); nameCol.setOpaque(false); nameCol.setLayout(new BoxLayout(nameCol,BoxLayout.Y_AXIS));
        JLabel an = new JLabel("Grievio"); an.setFont(new Font("Segoe UI",Font.BOLD,22)); an.setForeground(Color.WHITE);
        JLabel as = new JLabel("AI Complaint System"); as.setFont(new Font("Segoe UI",Font.PLAIN,12)); as.setForeground(new Color(120,160,230));
        nameCol.add(an); nameCol.add(as); logoRow.add(iconBox); logoRow.add(nameCol);

        JLabel headline = new JLabel("<html><span style='color:white;font-size:17pt;font-weight:bold;'>Reset your <span style='color:rgb(245,158,11);'>password</span><br>securely</span></html>");
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><p style='width:290px;line-height:1.7;color:rgb(155,190,235);font-size:13px;'>Verify your email with a real OTP sent to your Gmail inbox, set a new password and return to login.</p></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bullets = new JPanel(); bullets.setOpaque(false); bullets.setLayout(new BoxLayout(bullets,BoxLayout.Y_AXIS));
        bullets.setAlignmentX(Component.LEFT_ALIGNMENT); bullets.setBorder(new EmptyBorder(24,0,24,0));
        bullets.add(blt("\uD83D\uDCE7","Email Recovery","OTP sent to your Gmail"));
        bullets.add(Box.createVerticalStrut(16));
        bullets.add(blt("\uD83D\uDD12","Secure Reset","Set a fresh password"));
        bullets.add(Box.createVerticalStrut(16));
        bullets.add(blt("\u26A1","Quick Access","Return to login instantly"));

        c.add(logoRow); c.add(Box.createVerticalStrut(56)); c.add(headline);
        c.add(Box.createVerticalStrut(16)); c.add(desc); c.add(bullets); c.add(Box.createVerticalGlue());
        left.add(c); return left;
    }

    private JPanel blt(String icon, String title, String sub) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0)); row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel circle = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,new Color(37,99,235),getWidth(),getHeight(),new Color(59,130,246)));
                g2.fillOval(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g);
            }
        };
        circle.setOpaque(false); circle.setPreferredSize(new Dimension(38,38));
        JLabel il = new JLabel(icon); il.setFont(new Font("Segoe UI Emoji",Font.PLAIN,15)); il.setForeground(Color.WHITE); circle.add(il);
        JPanel txt = new JPanel(); txt.setOpaque(false); txt.setLayout(new BoxLayout(txt,BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI",Font.BOLD,13)); t.setForeground(Color.WHITE);
        JLabel s = new JLabel(sub); s.setFont(new Font("Segoe UI",Font.PLAIN,12)); s.setForeground(new Color(120,165,220));
        txt.add(t); txt.add(s); row.add(circle); row.add(txt); return row;
    }

    private JPanel buildRight() {
        JPanel right = new JPanel(new GridBagLayout()); right.setOpaque(false);
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(236,242,252)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false); card.setLayout(new BorderLayout()); card.setPreferredSize(new Dimension(640,680));

        JPanel accent = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(37,99,235),(float)(getWidth()*0.6f),0,new Color(245,158,11)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(0,4)); accent.setOpaque(false);
        card.add(accent, BorderLayout.NORTH);

        JPanel form = new JPanel(); form.setOpaque(false);
        form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28,52,28,52));

        JButton backBtn = makeLink("\u2190 Back to Login");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> switchTo(new LoginPage(role)));

        JLabel titleLbl = new JLabel("Reset password");
        titleLbl.setFont(new Font("Segoe UI",Font.BOLD,28));
        titleLbl.setForeground(new Color(12,26,64)); titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel("Verify OTP sent to your Gmail and set a new password");
        subLbl.setFont(new Font("Segoe UI",Font.PLAIN,13));
        subLbl.setForeground(new Color(95,115,155)); subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Status label
        statusLbl = new JLabel(" ");
        statusLbl.setFont(new Font("Segoe UI",Font.BOLD,12));
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email row
        emailField = new RoundedTextField(20,14);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));

        // OTP row — field + Send OTP + Verify inline
        JPanel otpRow = new JPanel(new BorderLayout(8,0)); otpRow.setOpaque(false);
        otpRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        otpRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        otpField = new RoundedTextField(10,14);
        otpField.setEnabled(false);
        sendOTPBtn  = makeBlueBtn("Send OTP");
        verifyOTPBtn = makeBlueBtn("Verify");
        sendOTPBtn.setPreferredSize(new Dimension(105,44));
        verifyOTPBtn.setPreferredSize(new Dimension(85,44));
        JPanel otpBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); otpBtns.setOpaque(false);
        otpBtns.add(sendOTPBtn); otpBtns.add(verifyOTPBtn);
        otpRow.add(otpField,BorderLayout.CENTER); otpRow.add(otpBtns,BorderLayout.EAST);

        // New password fields
        newPassField = new PasswordFieldWithToggle(20,14);
        newPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));
        newPassField.setEnabled(false);

        confirmPassField = new PasswordFieldWithToggle(20,14);
        confirmPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));
        confirmPassField.setEnabled(false);

        resetBtn = makeSignInBtn("Reset My Password");
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        resetBtn.setEnabled(false);

        // Wire actions
        sendOTPBtn.addActionListener(e -> doSendOTP());
        verifyOTPBtn.addActionListener(e -> doVerifyOTP());
        resetBtn.addActionListener(e -> doReset());

        // Assemble
        form.add(backBtn); form.add(Box.createVerticalStrut(16));
        form.add(titleLbl); form.add(Box.createVerticalStrut(4)); form.add(subLbl);
        form.add(Box.createVerticalStrut(6)); form.add(statusLbl);
        form.add(Box.createVerticalStrut(18));
        addField(form,"Registered Email",emailField);
        form.add(fLabel("OTP Code")); form.add(Box.createVerticalStrut(5)); form.add(otpRow);
        form.add(Box.createVerticalStrut(16));
        addField(form,"New Password",newPassField);
        addField(form,"Confirm Password",confirmPassField);
        form.add(Box.createVerticalStrut(6)); form.add(resetBtn);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(null); scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        right.add(card); return right;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void doSendOTP() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) { setStatus("Enter a valid email address.", false); return; }
        sendOTPBtn.setEnabled(false);
        setStatus("Sending OTP...", true);
        new Thread(() -> {
            try {
                currentOTP = OTPGenerator.generateOTP();
                otpSentTime = System.currentTimeMillis();
                EmailService.sendOTP(email, currentOTP, "FORGOT_PASSWORD");
                SwingUtilities.invokeLater(() -> {
                    otpField.setEnabled(true);
                    setStatus("OTP sent to " + email + ". Valid for 10 minutes.", true);
                    sendOTPBtn.setEnabled(true);
                    sendOTPBtn.setText("Resend OTP");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Failed to send OTP: " + ex.getMessage(), false);
                    sendOTPBtn.setEnabled(true);
                });
            }
        }).start();
    }

    private void doVerifyOTP() {
        if (currentOTP == null) { setStatus("Please send OTP first.", false); return; }
        long elapsed = System.currentTimeMillis() - otpSentTime;
        if (elapsed > 10 * 60 * 1000) { setStatus("OTP expired. Please resend.", false); return; }
        if (otpField.getText().trim().equals(currentOTP)) {
            otpVerified = true;
            newPassField.setEnabled(true);
            confirmPassField.setEnabled(true);
            resetBtn.setEnabled(true);
            setStatus("OTP verified! Now set your new password.", true);
        } else {
            setStatus("Incorrect OTP. Please try again.", false);
        }
    }

    private void doReset() {
        if (!otpVerified) { setStatus("Please verify OTP first.", false); return; }
        String np = new String(newPassField.getPassword()).trim();
        String cp = new String(confirmPassField.getPassword()).trim();
        if (np.length() < 6) { setStatus("Password must be at least 6 characters.", false); return; }
        if (!np.equals(cp))  { setStatus("Passwords do not match.", false); return; }
        if (!DatabaseManager.updatePassword(emailField.getText().trim(), np)) {
            setStatus("Could not update password for this email.", false); return;
        }
        JOptionPane.showMessageDialog(this,"Password reset successful! Please log in with your new password.","Done",JOptionPane.INFORMATION_MESSAGE);
        switchTo(new LoginPage(role));
    }

    private void setStatus(String msg, boolean ok) {
        statusLbl.setText(msg);
        statusLbl.setForeground(ok ? new Color(16,160,90) : new Color(200,50,50));
    }

    private void addField(JPanel p, String label, JComponent field) {
        p.add(fLabel(label)); p.add(Box.createVerticalStrut(5));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));
        p.add(field); p.add(Box.createVerticalStrut(16));
    }
    private JLabel fLabel(String t) {
        JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,13));
        l.setForeground(new Color(55,75,130)); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }
    private JButton makeLink(String t) {
        JButton b=new JButton(t); b.setFont(new Font("Segoe UI",Font.BOLD,13)); b.setForeground(new Color(37,99,235));
        b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton makeBlueBtn(String t) {
        JButton b=new JButton(t){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setPaint(new GradientPaint(0,0,new Color(25,72,195),getWidth(),0,new Color(37,99,235)));g2.fillRoundRect(0,0,getWidth(),getHeight(),11,11);g2.dispose();super.paintComponent(g);}};
        b.setFont(new Font("Segoe UI",Font.BOLD,13));b.setForeground(Color.WHITE);b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;
    }
    private JButton makeSignInBtn(String t) {
        JButton b=new JButton(t){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setPaint(new GradientPaint(0,0,new Color(25,72,195),getWidth(),0,new Color(37,99,235)));g2.fillRoundRect(0,0,getWidth(),getHeight(),13,13);g2.dispose();super.paintComponent(g);}};
        b.setFont(new Font("Segoe UI",Font.BOLD,15));b.setForeground(Color.WHITE);b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;
    }
}
