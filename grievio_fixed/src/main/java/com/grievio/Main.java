package com.grievio;

import com.grievio.database.DatabaseHelper;
import com.grievio.db.DatabaseManager;
import com.grievio.ui.IntroScreen;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        DatabaseManager.getConnection();
        DatabaseHelper.initializeDatabase();
        SwingUtilities.invokeLater(() -> new IntroScreen().setVisible(true));
    }
}
