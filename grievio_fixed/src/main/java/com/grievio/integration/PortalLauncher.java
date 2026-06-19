package com.grievio.integration;

import com.grievio.MainApp;
import com.grievio.database.DatabaseHelper;
import com.grievio.model.User;
import com.grievio.session.SessionManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PortalLauncher {
    private static final AtomicBoolean FX_STARTED = new AtomicBoolean(false);

    private PortalLauncher() {}

    public static void launchFromSwing(String role, String email, JFrame currentFrame) {
        DatabaseHelper.initializeDatabase();
        initFxToolkit();
        User user = buildUser(role, email);
        SessionManager.getInstance().login(user);
        if (currentFrame != null) currentFrame.dispose();

        Platform.runLater(() -> {
            // Partner has its own dashboard with fallback to grievio DB
            if ("PARTNER".equals(role)) {
                launchPartnerDashboard(user);
                return;
            }

            if (MainApp.primaryStage == null) {
                MainApp.primaryStage = new Stage();
                MainApp.primaryStage.setTitle("Grievio — AI Smart Complaint System");
                MainApp.primaryStage.setMinWidth(1100);
                MainApp.primaryStage.setMinHeight(700);
            }

            switch (role) {
                case "SOCIETY_ADMIN" -> MainApp.showSocietyDashboard();
                case "SECTOR_ADMIN"  -> MainApp.showSectorDashboard();
                case "GOVERNMENT"    -> MainApp.showGovDashboard();
                case "ADMIN"         -> MainApp.showAdminDashboard();
                default              -> MainApp.showUserDashboard();
            }
            MainApp.primaryStage.show();
        });
    }

    private static void initFxToolkit() {
        if (FX_STARTED.compareAndSet(false, true)) {
            new JFXPanel();
            Platform.setImplicitExit(false);
        }
    }

    private static User buildUser(String role, String email) {
        User user = new User();
        user.setEmail(email != null ? email : "");
        user.setPhone("");
        user.setSector("Sector-4");
        user.setSociety("Green Valley Society");
        switch (role) {
            case "SOCIETY_ADMIN" -> { user.setId(2); user.setName("Society Admin"); user.setRole("society_admin"); }
            case "SECTOR_ADMIN"  -> { user.setId(3); user.setName("Sector Head");   user.setRole("sector_head"); }
            case "GOVERNMENT"    -> {
                user.setId(4); user.setName("Gov Admin"); user.setRole("gov_admin");
                user.setDepartment("Municipal Corporation");
            }
            case "ADMIN"         -> { user.setId(5); user.setName("System Admin");  user.setRole("admin"); }
            case "PARTNER"       -> { user.setId(6); user.setName("Partner Raj");   user.setRole("partner"); }
            default              -> {
                user.setId(1);
                user.setName(email == null || email.isBlank() ? "Resident User" : email);
                user.setRole("user");
            }
        }
        return user;
    }

    /**
     * Partner dashboard: try MySQL partner DB first; if unavailable, fall back to
     * the grievio JavaFX PartnerDashboard which uses SQLite.
     */
    private static void launchPartnerDashboard(User user) {
        // Try the full partner dashboard (MySQL) first
        boolean mysqlLaunched = false;
        try {
            com.partnersdashboard.util.SessionManager ps =
                com.partnersdashboard.util.SessionManager.getInstance();
            if (ps.getPartner() == null) {
                com.partnersdashboard.dao.PartnerDAO dao = new com.partnersdashboard.dao.PartnerDAO();
                com.partnersdashboard.model.Partner partner =
                    dao.getByAccountId(com.partnersdashboard.config.AppConfig.DEMO_PARTNER_ACCOUNT_ID);
                if (partner != null) {
                    com.partnersdashboard.model.Account account = new com.partnersdashboard.model.Account();
                    account.setId(partner.getAccountId());
                    account.setEmail(partner.getEmail());
                    account.setRole("PARTNER");
                    account.setActive(true);
                    ps.setAccount(account);
                    ps.setPartner(partner);
                }
            }
            Stage stage = new Stage();
            new com.partnersdashboard.ui.PartnerDashboard(stage);
            stage.show();
            mysqlLaunched = true;
        } catch (Exception ex) {
            System.err.println("[PortalLauncher] MySQL partner DB unavailable, falling back to SQLite partner view: " + ex.getMessage());
        }

        // Fallback: open the grievio JavaFX PartnerDashboard (uses SQLite grievio.db)
        if (!mysqlLaunched) {
            if (MainApp.primaryStage == null) {
                MainApp.primaryStage = new Stage();
                MainApp.primaryStage.setTitle("Grievio — AI Smart Complaint System");
                MainApp.primaryStage.setMinWidth(1100);
                MainApp.primaryStage.setMinHeight(700);
            }
            MainApp.showPartnerDashboard();
            MainApp.primaryStage.show();
        }
    }
}
