package com.partnersdashboard.ui.common;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * NavigationManager - Singleton that holds the primary Stage reference
 * and handles screen/scene switching throughout the app.
 */
public class NavigationManager {

    private static NavigationManager instance;
    private Stage primaryStage;

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) instance = new NavigationManager();
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /** Navigate to a new scene, keeping the same window. */
    public void navigateTo(Scene scene, String title) {
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gravio – " + title);
        primaryStage.centerOnScreen();
    }

    /** Navigate to a scene with default title. */
    public void navigateTo(Scene scene) {
        navigateTo(scene, "Society Service & Complaint Management");
    }
}
