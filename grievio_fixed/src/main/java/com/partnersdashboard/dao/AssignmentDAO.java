package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;
import com.partnersdashboard.model.Society;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** AssignmentDAO - Handles complaint-to-partner assignment data. */
public class AssignmentDAO {

    public boolean assign(int complaintId, int partnerId, int adminAccountId,
                          String visitDate, String visitSlot, String notes) {
        String deleteSql = "DELETE FROM assignments WHERE complaint_id = ?";
        String insertSql = """
            INSERT INTO assignments (complaint_id, partner_id, assigned_by, visit_date, visit_time_slot, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(deleteSql);
                 PreparedStatement ins = conn.prepareStatement(insertSql)) {
                del.setInt(1, complaintId);
                del.executeUpdate();

                ins.setInt(1, complaintId);
                ins.setInt(2, partnerId);
                ins.setInt(3, adminAccountId);
                ins.setString(4, visitDate);
                ins.setString(5, visitSlot);
                ins.setString(6, notes);
                ins.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] assign error: " + e.getMessage());
            return false;
        }
    }

    public int getPartnerIdForComplaint(int complaintId) {
        String sql = "SELECT partner_id FROM assignments WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("partner_id") : -1;
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] getPartnerIdForComplaint error: " + e.getMessage());
            return -1;
        }
    }

    public boolean saveProofImage(int complaintId, String imagePath) {
        String sql = "UPDATE assignments SET proof_image_path = ? WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imagePath);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] saveProofImage error: " + e.getMessage());
            return false;
        }
    }

    public boolean saveCompletionTime(int complaintId) {
        String sql = "UPDATE assignments SET completed_at = NOW() WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] saveCompletionTime error: " + e.getMessage());
            return false;
        }
    }

    public List<Society> getAllSocieties() {
        List<Society> list = new ArrayList<>();
        String sql = "SELECT id, name, address, city, pincode FROM societies ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Society s = new Society();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setAddress(rs.getString("address"));
                s.setCity(rs.getString("city"));
                s.setPincode(rs.getString("pincode"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] getAllSocieties error: " + e.getMessage());
        }
        return list;
    }

    public Society getSocietyById(int id) {
        String sql = "SELECT id, name, address, city, pincode FROM societies WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Society s = new Society();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setAddress(rs.getString("address"));
                s.setCity(rs.getString("city"));
                s.setPincode(rs.getString("pincode"));
                return s;
            }
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] getSocietyById error: " + e.getMessage());
        }
        return null;
    }
}
