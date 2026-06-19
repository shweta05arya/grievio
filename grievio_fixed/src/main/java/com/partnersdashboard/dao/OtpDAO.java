package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * OtpDAO - Manages OTP records for work completion verification.
 * Each complaint gets one OTP record that is updated on each generation.
 */
public class OtpDAO {

    /** Save/replace OTP for a complaint. Called when partner clicks "Complete Work". */
    public boolean saveOtp(int complaintId, String otpCode) {
        String deleteSql = "DELETE FROM otp_verification WHERE complaint_id = ?";
        String insertSql = """
            INSERT INTO otp_verification (complaint_id, otp_code, otp_status)
            VALUES (?, ?, 'PENDING')
            """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(deleteSql);
                 PreparedStatement ins = conn.prepareStatement(insertSql)) {
                del.setInt(1, complaintId);
                del.executeUpdate();

                ins.setInt(1, complaintId);
                ins.setString(2, otpCode);
                ins.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[OtpDAO] saveOtp error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verify the OTP entered by the partner.
     * Checks code match and that OTP was generated within AppConfig.OTP_VALIDITY_MINUTES.
     * Returns true if valid; marks as VERIFIED in DB.
     */
    public boolean verifyOtp(int complaintId, String enteredOtp, int validityMinutes) {
        String sql = """
            SELECT otp_code, otp_status, otp_generated_at
            FROM otp_verification
            WHERE complaint_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedOtp    = rs.getString("otp_code");
                String status       = rs.getString("otp_status");
                Timestamp generated = rs.getTimestamp("otp_generated_at");

                // Already verified
                if ("VERIFIED".equals(status)) return false;

                // Check expiry
                LocalDateTime generatedTime = generated.toLocalDateTime();
                if (generatedTime.plusMinutes(validityMinutes).isBefore(LocalDateTime.now())) {
                    markExpired(complaintId, conn);
                    return false;
                }

                // Check code match
                if (storedOtp != null && storedOtp.equals(enteredOtp)) {
                    markVerified(complaintId, conn);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[OtpDAO] verifyOtp error: " + e.getMessage());
        }
        return false;
    }

    private void markVerified(int complaintId, Connection conn) throws SQLException {
        String sql = "UPDATE otp_verification SET otp_status='VERIFIED', otp_verified_at=NOW() WHERE complaint_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.executeUpdate();
        }
    }

    private void markExpired(int complaintId, Connection conn) throws SQLException {
        String sql = "UPDATE otp_verification SET otp_status='EXPIRED' WHERE complaint_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.executeUpdate();
        }
    }
}