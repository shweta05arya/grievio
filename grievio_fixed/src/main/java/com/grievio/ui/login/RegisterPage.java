package com.grievio.ui.login;

import com.grievio.db.DatabaseManager;
import com.grievio.email.EmailService;
import com.grievio.ui.components.BaseWindow;
import com.grievio.ui.components.PasswordFieldWithToggle;
import com.grievio.ui.components.RoundedTextField;
import com.grievio.util.LocationData;
import com.grievio.util.OTPGenerator;
import com.grievio.session.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseWheelListener;

public class RegisterPage extends BaseWindow {
    private final String role;
    private String currentOTP;
    private long otpSentTime;
    private boolean otpVerified;

    private RoundedTextField nameField, emailField, phoneField, otpField, towerField, flatField, addressField, sectorField, societyField;
    private PasswordFieldWithToggle passField, confirmField;
    private JComboBox<String> districtCombo;
    private JLabel statusLbl;
    private JButton registerBtn;

    public RegisterPage() {
        this("RESIDENT");
    }

    public RegisterPage(String role) {
        super();
        this.role = "RESIDENT";
        buildUI();
    }

    private void buildUI() {
        JPanel split = new JPanel(new BorderLayout());
        split.setOpaque(false);
        bg.add(split, BorderLayout.CENTER);

        split.add(buildLeft(), BorderLayout.WEST);
        split.add(buildRight(), BorderLayout.CENTER);
    }

    private JPanel buildLeft() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(2, 8, 28, 200));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };

        left.setOpaque(false);
        left.setPreferredSize(new Dimension(500, 0));
        left.setLayout(new GridBagLayout());

        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(50, 56, 50, 44));

        JLabel brand = new JLabel("Grievio");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);

        JLabel sub = new JLabel("AI Complaint System");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(120, 160, 230));

        JLabel h = new JLabel("<html>" + headlineHtml() + "</html>");
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel d = new JLabel(
                "<html><p style='width:290px;line-height:1.7;color:rgb(155,190,235);font-size:13px;'>"
                        + "Register with OTP verification and save your details for future login."
                        + "</p></html>"
        );
        d.setAlignmentX(Component.LEFT_ALIGNMENT);

        c.add(brand);
        c.add(sub);
        c.add(Box.createVerticalStrut(56));
        c.add(h);
        c.add(Box.createVerticalStrut(16));
        c.add(d);
        c.add(Box.createVerticalGlue());

        left.add(c);
        return left;
    }

    private JPanel buildRight() {
        JPanel right = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 24));
        right.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(236, 242, 252));
        card.setOpaque(true);
        card.setPreferredSize(new Dimension(690, 760));

        JPanel accent = new JPanel();
        accent.setBackground(new Color(37, 99, 235));
        accent.setPreferredSize(new Dimension(690, 4));
        card.add(accent, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(26, 52, 26, 52));

        JButton back = linkBtn("← Back");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> switchTo(new LoginPage("RESIDENT")));
        form.add(back);
        form.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel(pageTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(12, 26, 64));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Complete OTP verification and fill required location details");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(95, 115, 155));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLbl = new JLabel(" ");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(6));
        form.add(statusLbl);
        form.add(Box.createVerticalStrut(14));

        nameField = tf();
        emailField = tf();
        phoneField = tf();
        otpField = tf();
        otpField.setEnabled(false);
        towerField = tf();
        flatField = tf();
        addressField = tf();
        sectorField = tf();
        societyField = tf();
        sectorField.setPlaceholder("Enter sector");
        societyField.setPlaceholder("Enter society");

        passField = pf();
        confirmField = pf();

        String[] districts = LocationData.getDistricts();
        if (districts == null || districts.length == 0) {
            districts = new String[]{"Select district"};
        }

        districtCombo = combo(districts);

        addField(form, "Full Name", nameField);
        addField(form, "Email Address", emailField);
        addOtpRow(form);
        addField(form, "Phone Number", phoneField);
        addField(form, "District", districtCombo);
        addField(form, "Sector", sectorField);
        addField(form, "Society", societyField);

        JPanel two = new JPanel(new GridLayout(1, 2, 10, 0));
        two.setOpaque(false);
        two.setAlignmentX(Component.LEFT_ALIGNMENT);
        two.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        two.add(labeledField("Tower / Block", towerField));
        two.add(labeledField("Flat Number", flatField));
        form.add(two);
        form.add(Box.createVerticalStrut(14));

        addField(form, "Full Address", addressField);
        addField(form, "Password", passField);
        addField(form, "Confirm Password", confirmField);

        registerBtn = primaryBtn("Create Account");
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        registerBtn.setEnabled(false);
        registerBtn.addActionListener(e -> doRegister());
        form.add(registerBtn);
        form.add(Box.createVerticalStrut(10));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setWheelScrollingEnabled(true);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setBlockIncrement(96);
        scroll.getVerticalScrollBar().putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
        installSmoothScrolling(scroll, form);
        scroll.setPreferredSize(new Dimension(690, 720));

        card.add(scroll, BorderLayout.CENTER);
        right.add(card);
        
        return right;
    }

    private void addOtpRow(JPanel form) {
        JLabel lbl = new JLabel("OTP Verification");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(55, 75, 130));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel otpRow = new JPanel(new BorderLayout(8, 0));
        otpRow.setOpaque(false);
        otpRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        otpRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton sendOtpBtn = secondaryBtn("Send OTP");
        JButton verifyOtpBtn = secondaryBtn("Verify");
        sendOtpBtn.setPreferredSize(new Dimension(105, 44));
        verifyOtpBtn.setPreferredSize(new Dimension(85, 44));

        JPanel ob = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        ob.setOpaque(false);
        ob.add(sendOtpBtn);
        ob.add(verifyOtpBtn);

        otpRow.add(otpField, BorderLayout.CENTER);
        otpRow.add(ob, BorderLayout.EAST);

        sendOtpBtn.addActionListener(e -> doSendOTP(sendOtpBtn));
        verifyOtpBtn.addActionListener(e -> doVerifyOTP());

        form.add(lbl);
        form.add(Box.createVerticalStrut(5));
        form.add(otpRow);
        form.add(Box.createVerticalStrut(14));
    }

    private void doSendOTP(JButton btn) {
        String email = emailField.getText().trim().toLowerCase();

        if (email.isBlank() || !email.contains("@")) {
            setStatus("Enter a valid email address.", false);
            return;
        }

        if (DatabaseManager.isEmailRegistered(email)) {
            setStatus("This email is already registered.", false);
            return;
        }

        btn.setEnabled(false);
        setStatus("Sending OTP to " + email + "...", true);

        new Thread(() -> {
            try {
                currentOTP = OTPGenerator.generateOTP();
                otpSentTime = System.currentTimeMillis();
                EmailService.sendOTP(email, currentOTP, "REGISTER");

                SwingUtilities.invokeLater(() -> {
                    otpField.setEnabled(true);
                    setStatus("OTP sent! Check your Gmail inbox.", true);
                    btn.setEnabled(true);
                    btn.setText("Resend OTP");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    otpField.setEnabled(true);
                    currentOTP = currentOTP == null ? OTPGenerator.generateOTP() : currentOTP;
                    setStatus("Email sending failed. Demo OTP opened for verification.", true);
                    JOptionPane.showMessageDialog(this, "Demo OTP: " + currentOTP + "\nUse this OTP to continue registration.", "OTP Fallback", JOptionPane.INFORMATION_MESSAGE);
                    btn.setEnabled(true);
                    btn.setText("Resend OTP");
                });
            }
        }).start();
    }

    private void doVerifyOTP() {
        if (currentOTP == null) {
            setStatus("Please send OTP first.", false);
            return;
        }

        if (System.currentTimeMillis() - otpSentTime > 10 * 60 * 1000L) {
            setStatus("OTP expired. Please resend.", false);
            return;
        }

        if (!otpField.getText().trim().equals(currentOTP)) {
            setStatus("Incorrect OTP. Try again.", false);
            return;
        }

        otpVerified = true;
        registerBtn.setEnabled(true);
        setStatus("Email verified. Complete the form and create your account.", true);
    }

    private void doRegister() {
        if (!otpVerified) {
            setStatus("Please verify OTP first.", false);
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String phone = phoneField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String district = selected(districtCombo);
        String sector = sectorField.getText().trim();
        String society = societyField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank() || confirm.isBlank()) {
            setStatus("Please fill in all mandatory fields.", false);
            return;
        }

        if (!pass.equals(confirm)) {
            setStatus("Passwords do not match.", false);
            return;
        }

        if (district.isBlank() || sector.isBlank() || society.isBlank() || address.isBlank()) {
            setStatus("Please complete district, sector, society and address.", false);
            return;
        }

        boolean ok = DatabaseManager.registerResident(
                name, email, phone, pass,
                district, sector, society,
                towerField.getText().trim(),
                flatField.getText().trim(),
                address
        );

        if (!ok) {
            setStatus("Registration failed. Email may already exist.", false);
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Resident account created successfully. Please log in.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );

        SessionManager.saveLoginForRole("RESIDENT", email, pass);
        switchTo(new LoginPage("RESIDENT", email, pass));
    }

    private void installSmoothScrolling(JScrollPane scroll, Component root) {
        MouseWheelListener listener = e -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            int amount = (int) Math.round(e.getPreciseWheelRotation() * bar.getUnitIncrement());
            bar.setValue(bar.getValue() + amount);
            e.consume();
        };
        bindMouseWheel(root, listener);
    }

    private void bindMouseWheel(Component component, MouseWheelListener listener) {
        component.addMouseWheelListener(listener);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                bindMouseWheel(child, listener);
            }
        }
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(55, 75, 130));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(field);

        return p;
    }

    private void setStatus(String msg, boolean ok) {
        statusLbl.setText(msg);
        statusLbl.setForeground(ok ? new Color(16, 160, 90) : new Color(200, 50, 50));
    }

    private String pageTitle() {
        return "Create Resident Account";
    }

    private String headlineHtml() {
        return "<span style='color:white;font-size:17pt;font-weight:bold;'>Create your <span style='color:rgb(59,130,246)'>resident account</span></span>";
    }

    private RoundedTextField tf() {
        RoundedTextField t = new RoundedTextField(20, 14);
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return t;
    }

    private PasswordFieldWithToggle pf() {
        PasswordFieldWithToggle p = new PasswordFieldWithToggle(20, 14);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return p;
    }

    private JComboBox<String> combo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setPreferredSize(new Dimension(400, 46));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        c.setMinimumSize(new Dimension(120, 46));
        return c;
    }

    private String selected(JComboBox<String> combo) {
        if (combo == null || combo.getSelectedItem() == null) return "";
        String v = combo.getSelectedItem().toString().trim();
        return v.startsWith("Select ") ? "" : v;
    }

    private void addField(JPanel p, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(55, 75, 130));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(field);
        p.add(Box.createVerticalStrut(14));
    }

    private JButton linkBtn(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(new Color(37, 99, 235));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton primaryBtn(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(25, 72, 195),
                        getWidth(), 0, new Color(37, 99, 235)
                ));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setForeground(Color.WHITE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton secondaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(new Color(37, 99, 235));
        b.setBackground(new Color(222, 232, 248));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return b;
    }
}