package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;
import com.partnersdashboard.model.Resident;

import java.sql.*;

/** ResidentDAO - CRUD for residents table. */
public class ResidentDAO {

    /** Get resident by their account_id (used after login). */
    public Resident getByAccountId(int accountId) {
        String sql = """
            SELECT r.*, a.email, s.name AS society_name
            FROM residents r
            JOIN accounts a ON r.account_id = a.id
            JOIN societies s ON r.society_id = s.id
            WHERE r.account_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[ResidentDAO] getByAccountId error: " + e.getMessage());
        }
        return null;
    }

    /** Create a resident profile after account is created during registration. */
    public boolean createResident(Resident r) {
        String sql = """
            INSERT INTO residents (account_id, society_id, first_name, last_name, phone, block_no, flat_no)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getAccountId());
            ps.setInt(2, r.getSocietyId());
            ps.setString(3, r.getFirstName());
            ps.setString(4, r.getLastName());
            ps.setString(5, r.getPhone());
            ps.setString(6, r.getBlockNo());
            ps.setString(7, r.getFlatNo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResidentDAO] createResident error: " + e.getMessage());
        }
        return false;
    }

    /** Get resident by resident ID (not account ID). */
    public Resident getById(int residentId) {
        String sql = """
            SELECT r.*, a.email, s.name AS society_name
            FROM residents r
            JOIN accounts a ON r.account_id = a.id
            JOIN societies s ON r.society_id = s.id
            WHERE r.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[ResidentDAO] getById error: " + e.getMessage());
        }
        return null;
    }

    private Resident mapRow(ResultSet rs) throws SQLException {
        Resident r = new Resident();
        r.setId(rs.getInt("id"));
        r.setAccountId(rs.getInt("account_id"));
        r.setSocietyId(rs.getInt("society_id"));
        r.setSocietyName(rs.getString("society_name"));
        r.setFirstName(rs.getString("first_name"));
        r.setLastName(rs.getString("last_name"));
        r.setEmail(rs.getString("email"));
        r.setPhone(rs.getString("phone"));
        r.setBlockNo(rs.getString("block_no"));
        r.setFlatNo(rs.getString("flat_no"));
        return r;
    }
}