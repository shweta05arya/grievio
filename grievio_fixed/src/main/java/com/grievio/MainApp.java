package com.grievio;

import com.grievio.database.DatabaseHelper;
import com.grievio.ui.RoleSelectionPage;
import com.grievio.ui.user.UserDashboard;
import com.grievio.ui.admin.AdminDashboard;
import com.grievio.ui.partner.PartnerDashboard;
import com.grievio.ui.sector.SectorDashboard;
import com.grievio.ui.society.SocietyDashboard;
import com.grievio.ui.gov.GovDashboard;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.SwingUtilities;

public class MainApp extends Application {
    public static Stage primaryStage;
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Grievio — AI Smart Complaint System");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        DatabaseHelper.initializeDatabase();
        showLogin();
    }
    public static void showLogin() {
        if (primaryStage != null) primaryStage.hide();
        SwingUtilities.invokeLater(() -> new RoleSelectionPage().setVisible(true));
    }
    public static void showUserDashboard() { UserDashboard d = new UserDashboard(); Scene scene = new Scene(d.getRoot(), 1280, 800); applyCSS(scene); primaryStage.setScene(scene); primaryStage.setMaximized(true); }
    public static void showAdminDashboard() { AdminDashboard d = new AdminDashboard(); Scene scene = new Scene(d.getRoot(), 1380, 860); applyCSS(scene); primaryStage.setScene(scene); primaryStage.setMaximized(true); }
    public static void showPartnerDashboard() { PartnerDashboard d = new PartnerDashboard(); Scene scene = new Scene(d.getRoot(), 1280, 800); applyCSS(scene); primaryStage.setScene(scene); primaryStage.setMaximized(true); }
    public static void showSectorDashboard() { SectorDashboard d = new SectorDashboard(); Scene scene = new Scene(d.getRoot(), 1280, 800); applyCSS(scene); primaryStage.setScene(scene); primaryStage.setMaximized(true); }
    public static void showSocietyDashboard() { SocietyDashboard d = new SocietyDashboard(); Scene scene = new Scene(d.getRoot(), 1280, 800); applyCSS(scene); primaryStage.setScene(scene); primaryStage.setMaximized(true); }
    public static void showGovDashboard() { GovDashboard d = new GovDashboard(); Scene scene = new Scene(d.getRoot(), 1280, 800); applyCSS(scene); primaryStage.setScene(scene); primaryStage.setMaximized(true); }
    private static void applyCSS(Scene scene) { try { String css = MainApp.class.getResource("/com/grievio/css/styles.css").toExternalForm(); scene.getStylesheets().add(css); } catch (Exception e) { System.err.println("CSS not found: " + e.getMessage()); } }
    public static void main(String[] args) { launch(args); }
}
