package com.grievio.ui.login;

import com.grievio.db.DatabaseManager;
import com.grievio.integration.PortalLauncher;
import com.grievio.session.SessionManager;
import com.grievio.ui.AdminTypeSelectionPage;
import com.grievio.ui.RoleSelectionPage;
import com.grievio.ui.components.BaseWindow;
import com.grievio.ui.components.PasswordFieldWithToggle;
import com.grievio.ui.components.RoundedTextField;
import com.grievio.util.LocationData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseWheelListener;

public class LoginPage extends BaseWindow {
    private static final String GOVT_LOCKED_EMAIL = "governmentadmin@grievio.com";
    private static final String SOCIETY_ADMIN_EMAIL = "societyadmin@grievio.com";
    private static final String SECTOR_ADMIN_EMAIL = "sectoradmin@grievio.com";
    private static final String PARTNER_EMAIL = "raj.kumar@gravio.com";

    private final String role;
    private final String prefilledEmail;
    private final String prefilledPassword;
    private RoundedTextField emailField;
    private PasswordFieldWithToggle passField;
    private JComboBox<String> districtCombo;
    private RoundedTextField sectorField, societyField;
    private JCheckBox rememberMe;

    public LoginPage(String role) { this(role, null, null); }

    public LoginPage(String role, String prefilledEmail, String prefilledPassword) {
        super();
        this.role = role;
        this.prefilledEmail = prefilledEmail;
        this.prefilledPassword = prefilledPassword;
        buildUI();
        autoFillRemembered();
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
            @Override protected void paintComponent(Graphics g) {
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

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logo.setOpaque(false);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel iconBox = RoleSelectionPage.makeIconBox("⚖", new Color(37, 99, 235), new Color(59, 130, 246));

        JPanel nm = new JPanel();
        nm.setOpaque(false);
        nm.setLayout(new BoxLayout(nm, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("Grievio");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);

        JLabel sub = new JLabel("AI Complaint System");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(120, 160, 230));

        nm.add(brand);
        nm.add(sub);
        logo.add(iconBox);
        logo.add(nm);

        JLabel h = new JLabel("<html>" + headlineHtml() + "</html>");
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel d = new JLabel(
                "<html><p style='width:290px;line-height:1.7;color:rgb(155,190,235);font-size:13px;'>"
                        + "Sign in with your registered credentials and continue to your dashboard securely."
                        + "</p></html>"
        );
        d.setAlignmentX(Component.LEFT_ALIGNMENT);

        c.add(logo);
        c.add(Box.createVerticalStrut(56));
        c.add(h);
        c.add(Box.createVerticalStrut(16));
        c.add(d);
        c.add(Box.createVerticalGlue());

        JLabel foot = new JLabel("governmentadmin@grievio.com  ·  © 2026 Grievio");
        foot.setForeground(new Color(80, 110, 160));
        foot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(foot);

        left.add(c);
        return left;
    }

    private JPanel buildRight() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(236, 242, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(650, needsLocationFields() ? 760 : 660));

        JPanel accent = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(37, 99, 235), (float) (getWidth() * 0.6), 0, new Color(245, 158, 11)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(0, 4));
        card.add(accent, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28, 52, 28, 52));

        JButton back = linkBtn("← Back");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> {
            if (isAdminRole()) switchTo(new AdminTypeSelectionPage());
            else switchTo(new RoleSelectionPage());
        });
        form.add(back);
        form.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel(pageTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(12, 26, 64));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(govtLoginOnly()
                ? "Government Admin uses a pre-approved email. Enter password and continue."
                : "Use your registered email and password to continue");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(95, 115, 155));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(18));

        emailField = tf();
        passField = pf();
        addField(form, "Email Address", emailField);
        addField(form, "Password", passField);

        if (needsLocationFields()) {
            String[] districts = LocationData.getDistricts();
            if (districts == null || districts.length == 0) districts = new String[]{"Select district"};
            districtCombo = combo(districts, false);
            addField(form, "District", districtCombo);

            sectorField = tf();
            sectorField.setPlaceholder("Enter sector");
            addField(form, "Sector", sectorField);

            if (needsSociety()) {
                societyField = tf();
                societyField.setPlaceholder("Enter society");
                addField(form, "Society", societyField);
            }
        }

        rememberMe = new JCheckBox("Remember me");
        rememberMe.setOpaque(false);
        rememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMe.setForeground(new Color(95, 115, 155));
        rememberMe.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(rememberMe);
        form.add(Box.createVerticalStrut(8));

        JButton forgot = linkBtn("Forgot password?");
        forgot.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgot.addActionListener(e -> switchTo(new ForgotPasswordPage(role)));
        form.add(forgot);
        form.add(Box.createVerticalStrut(20));

        JButton signIn = primaryBtn("Sign In");
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        signIn.addActionListener(e -> doLogin());
        form.add(signIn);
        form.add(Box.createVerticalStrut(16));

        if ("RESIDENT".equals(role)) {
            JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            regRow.setOpaque(false);
            regRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel q = new JLabel("Don't have an account?");
            q.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            q.setForeground(new Color(95, 115, 155));
            JButton regBtn = linkBtn("Register here →");
            regBtn.addActionListener(e -> switchTo(new RegisterPage("RESIDENT")));
            regRow.add(q);
            regRow.add(regBtn);
            form.add(regRow);
        }

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setWheelScrollingEnabled(true);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setBlockIncrement(96);
        scroll.getVerticalScrollBar().putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
        installSmoothScrolling(scroll, form);

        card.add(scroll, BorderLayout.CENTER);
        right.add(card);
        return right;
    }

    private void doLogin() {
        String email = emailField.getText().trim().toLowerCase();
        String pass = new String(passField.getPassword()).trim();
        if (email.isEmpty() || pass.isEmpty()) {
            showErr("Please enter your email and password.");
            return;
        }

        String expected = expectedEmailForRole();
        if (expected != null && !expected.equalsIgnoreCase(email)) {
            showErr("Use the approved login email for this dashboard: " + expected);
            return;
        }

        boolean valid;
        if ("RESIDENT".equals(role)) {
            valid = DatabaseManager.validateResident(email, pass);
        } else if ("PARTNER".equals(role)) {
            String d = selected(districtCombo);
            String s = text(sectorField);
            String soc = text(societyField);
            valid = DatabaseManager.validatePartner(email, pass, d, s, soc);
        } else {
            String d = selected(districtCombo);
            String s = text(sectorField);
            String soc = needsSociety() ? text(societyField) : "";
            valid = DatabaseManager.validateAdmin(email, pass, role, d, s, soc);
        }

        if (!valid) {
            showErr("Invalid credentials. Please check your details and try again.");
            return;
        }

        SessionManager.saveSession(email, email, role, email);
        if (rememberMe.isSelected()) SessionManager.saveLoginForRole(role, email, pass);
        else SessionManager.clearLoginForRole(role);

        JOptionPane.showMessageDialog(this, "Login successful! Your credentials have been saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        PortalLauncher.launchFromSwing(role, email, this);
    }

    private void autoFillRemembered() {
        if (govtLoginOnly()) {
            emailField.setText(GOVT_LOCKED_EMAIL);
            emailField.setEditable(false);
            emailField.setEnabled(false);
        }
        if (prefilledEmail != null && !prefilledEmail.isBlank()) emailField.setText(prefilledEmail);
        if (prefilledPassword != null && !prefilledPassword.isBlank()) passField.setText(prefilledPassword);
        if (SessionManager.hasRememberedLoginForRole(role) && (prefilledEmail == null || prefilledEmail.isBlank())) {
            emailField.setText(SessionManager.getSavedEmailForRole(role));
            passField.setText(SessionManager.getSavedPasswordForRole(role));
            rememberMe.setSelected(true);
        }
        if (govtLoginOnly() && emailField.getText().isBlank()) emailField.setText(GOVT_LOCKED_EMAIL);
    }

    private String expectedEmailForRole() {
        return switch (role) {
            case "SOCIETY_ADMIN" -> SOCIETY_ADMIN_EMAIL;
            case "SECTOR_ADMIN" -> SECTOR_ADMIN_EMAIL;
            case "GOVERNMENT" -> GOVT_LOCKED_EMAIL;
            case "PARTNER" -> PARTNER_EMAIL;
            default -> null;
        };
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
        if (component instanceof Container container) for (Component child : container.getComponents()) bindMouseWheel(child, listener);
    }

    private String selected(JComboBox<String> combo) {
        if (combo == null || combo.getSelectedItem() == null) return "";
        String v = combo.getSelectedItem().toString().trim();
        return v.startsWith("Select ") ? "" : v;
    }

    private String text(RoundedTextField field) {
        return field == null ? "" : field.getText().trim();
    }

    private String pageTitle() {
        return switch (role) {
            case "PARTNER" -> "Partner Login";
            case "SOCIETY_ADMIN" -> "Society Admin Login";
            case "SECTOR_ADMIN" -> "Sector Admin Login";
            case "GOVERNMENT" -> "Government Admin Login";
            default -> "Welcome back";
        };
    }

    private String headlineHtml() {
        return switch (role) {
            case "PARTNER" -> "<span style='color:white;font-size:17pt;font-weight:bold;'>Resolve tasks with <span style='color:rgb(16,200,140)'>precision</span></span>";
            case "SOCIETY_ADMIN" -> "<span style='color:white;font-size:17pt;font-weight:bold;'>Manage your society with <span style='color:rgb(167,120,255)'>clarity</span></span>";
            case "SECTOR_ADMIN" -> "<span style='color:white;font-size:17pt;font-weight:bold;'>Govern your sector with <span style='color:rgb(56,189,248)'>real-time data</span></span>";
            case "GOVERNMENT" -> "<span style='color:white;font-size:17pt;font-weight:bold;'>District governance with <span style='color:rgb(245,158,11)'>transparency</span></span>";
            default -> "<span style='color:white;font-size:17pt;font-weight:bold;'>Resolve grievances with <span style='color:rgb(245,158,11)'>AI power</span></span>";
        };
    }

    private boolean isAdminRole() { return !"RESIDENT".equals(role) && !"PARTNER".equals(role); }
    private boolean needsLocationFields() { return !"RESIDENT".equals(role); }
    private boolean needsSociety() { return "PARTNER".equals(role) || "SOCIETY_ADMIN".equals(role); }
    private boolean govtLoginOnly() { return "GOVERNMENT".equals(role); }

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

    private JComboBox<String> combo(String[] items, boolean editable) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setEditable(editable);
        return c;
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
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(25, 72, 195), getWidth(), 0, new Color(37, 99, 235)));
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

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Error", JOptionPane.ERROR_MESSAGE);
    }
}
