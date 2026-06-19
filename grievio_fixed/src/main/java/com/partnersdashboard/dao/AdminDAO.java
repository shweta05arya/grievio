package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;

import java.sql.*;

/** AdminDAO - Queries for the admins table. */
public class AdminDAO {

    /**
     * Get the society_id associated with an admin's account.
     * Returns -1 if no society is linked (main admin).
     */
    public int getSocietyIdByAccountId(int accountId) {
        String sql = "SELECT society_id FROM admins WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int sid = rs.getInt("society_id");
                return rs.wasNull() ? -1 : sid;
            }
        } catch (SQLException e) {
            System.err.println("[AdminDAO] getSocietyIdByAccountId error: " + e.getMessage());
        }
        return -1;
    }

    /** Get admin's display name. */
    public String getAdminName(int accountId) {
        String sql = "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM admins WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("full_name");
        } catch (SQLException e) {
            System.err.println("[AdminDAO] getAdminName error: " + e.getMessage());
        }
        return "Admin";
    }

    /** Create a new society admin (used by main admin). */
    public boolean createAdmin(int accountId, Integer societyId, String firstName, String lastName, String phone) {
        String sql = "INSERT INTO admins (account_id, society_id, first_name, last_name, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            if (societyId != null) ps.setInt(2, societyId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, phone);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AdminDAO] createAdmin error: " + e.getMessage());
        }
        return false;
    }
}