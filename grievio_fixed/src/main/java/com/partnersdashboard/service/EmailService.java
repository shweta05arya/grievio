package com.partnersdashboard.service;

import com.partnersdashboard.config.AppConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/** Sends OTP emails using SMTP credentials from AppConfig. */
public class EmailService {

    public boolean sendOtpEmail(String toEmail, String residentName, int complaintId, String otpCode) {
        if (AppConfig.ADMIN_EMAIL.startsWith("your-") || AppConfig.ADMIN_APP_PASSWORD.startsWith("your-")) {
            System.err.println("[EmailService] SMTP placeholders still configured. Skipping real email send.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", AppConfig.SMTP_HOST);
        props.put("mail.smtp.port", AppConfig.SMTP_PORT);
        props.put("mail.smtp.ssl.trust", AppConfig.SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(AppConfig.ADMIN_EMAIL, AppConfig.ADMIN_APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConfig.ADMIN_EMAIL, "Gravio – Partner Verification"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Gravio | Completion OTP – Complaint #" + complaintId);
            message.setContent(buildEmailHtml(residentName, complaintId, otpCode), "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("[EmailService] OTP sent successfully to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send OTP email: " + e.getMessage());
            return false;
        }
    }

    private String buildEmailHtml(String name, int complaintId, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: 'Segoe UI', sans-serif; background:#f4f6fb; padding:30px;">
              <div style="max-width:500px; margin:auto; background:white; border-radius:12px;
                          padding:32px; box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                <div style="text-align:center; margin-bottom:24px;">
                  <h1 style="color:#0B1533; font-size:26px; margin:0;">🏢 Gravio</h1>
                  <p style="color:#8C94A6; margin:4px 0 0;">Partner Work Completion Verification</p>
                </div>
                <p style="color:#1B2235; font-size:16px;">Hello <strong>%s</strong>,</p>
                <p style="color:#444; font-size:14px; line-height:1.6;">
                  Your assigned worker has marked complaint <strong>#%d</strong> as completed.
                  Please share the OTP below with the worker only after checking the work.
                </p>
                <div style="text-align:center; margin:28px 0;">
                  <div style="background:#0B1533; color:white; font-size:34px;
                               font-weight:bold; letter-spacing:8px; padding:20px 32px;
                               border-radius:10px; display:inline-block;">%s</div>
                  <p style="color:#8C94A6; font-size:12px; margin-top:10px;">
                    This OTP is valid for %d minutes.
                  </p>
                </div>
                <p style="color:#e74c3c; font-size:13px;">Do not share this OTP until the work is truly complete.</p>
              </div>
            </body>
            </html>
            """.formatted(name, complaintId, otp, AppConfig.OTP_VALIDITY_MINUTES);
    }
}
