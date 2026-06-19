package com.partnersdashboard.config;

/**
 * AppConfig - Central configuration for the Partner Dashboard demo.
 * Replace DB and email placeholders before running against your own setup.
 */
public class AppConfig {

    // Database
    public static final String DB_URL      = "jdbc:mysql://localhost:3306/gravio_db?useSSL=false&serverTimezone=UTC";
    public static final String DB_USER     = "root";
    public static final String DB_PASSWORD = "1234";

    // Email / SMTP (use Gmail App Password or another SMTP account)
    public static final String ADMIN_EMAIL        = "your-email@gmail.com";
    public static final String ADMIN_APP_PASSWORD = "your-16-char-app-password";
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";

    // App settings
    public static final String APP_NAME    = "Gravio Partner Dashboard";
    public static final String APP_VERSION = "1.1.0";
    public static final int OTP_VALIDITY_MINUTES = 5;

    // Demo launcher partner account id (used when login is removed)
    public static final int DEMO_PARTNER_ACCOUNT_ID = 2;

    // Proof uploads directory (relative to project root)
    public static final String PROOF_UPLOAD_DIR = "proof_uploads";

    private AppConfig() {}
}
