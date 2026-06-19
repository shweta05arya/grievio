package com.grievio.email;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Real Gmail SMTP OTP sender using the app-password credentials
 * already configured in the project.
 *
 * SETUP:
 *   1. Replace SENDER_EMAIL with your Gmail address.
 *   2. Replace SENDER_APP_PASSWORD with your 16-char Gmail App Password
 *      (Google Account → Security → 2-Step Verification → App Passwords).
 *   This is the existing email/app-password configuration from the project.
 */
public class EmailService {

    // ── CONFIGURE THESE TWO FIELDS with your existing credentials ────────────
    private static final String SENDER_EMAIL       = "official.gravio@gmail.com";
    private static final String SENDER_APP_PASSWORD = "ggkwqxotocsvulsd";
    // ─────────────────────────────────────────────────────────────────────────

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;

    private EmailService() {}

    /**
     * Sends a real OTP email to the given recipient via Gmail SMTP.
     * Throws MessagingException on failure (caller shows error to user).
     */
    public static void sendOTP(String recipientEmail, String otp, String purpose) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols",   "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        String subject = buildSubject(purpose);
        String body    = buildBody(otp, purpose);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL, "Grievio"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private static String buildSubject(String purpose) {
        return switch (purpose) {
            case "REGISTER"        -> "Grievio — Verify Your Email";
            case "FORGOT_PASSWORD" -> "Grievio — Password Reset OTP";
            default                -> "Grievio — OTP Verification";
        };
    }

    private static String buildBody(String otp, String purpose) {
        String action = switch (purpose) {
            case "REGISTER"        -> "complete your registration";
            case "FORGOT_PASSWORD" -> "reset your password";
            default                -> "verify your identity";
        };
        return """
            <div style="font-family:Segoe UI,sans-serif;background:#0a1628;color:#ffffff;padding:40px;border-radius:12px;max-width:480px;margin:auto;">
              <div style="text-align:center;margin-bottom:28px;">
                <span style="font-size:32px;">⚖️</span>
                <h2 style="color:#ffffff;margin:8px 0 4px;">Grievio</h2>
                <p style="color:#8baad4;font-size:13px;margin:0;">AI Complaint Management System</p>
              </div>
              <div style="background:#132044;border-radius:10px;padding:28px;text-align:center;">
                <p style="color:#c0d4f0;font-size:15px;margin:0 0 20px;">Use the following OTP to %s:</p>
                <div style="background:#1a2d5a;border:2px solid #3b82f6;border-radius:10px;padding:20px;display:inline-block;min-width:160px;">
                  <span style="font-size:36px;font-weight:bold;letter-spacing:8px;color:#60a5fa;">%s</span>
                </div>
                <p style="color:#8baad4;font-size:13px;margin:20px 0 0;">This OTP is valid for <strong style="color:#f59e0b;">10 minutes</strong>.<br>Do not share it with anyone.</p>
              </div>
              <p style="color:#4a6a9a;font-size:11px;text-align:center;margin-top:24px;">© 2026 Grievio. All rights reserved.</p>
            </div>
            """.formatted(action, otp);
    }
}
