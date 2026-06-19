package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;
import com.partnersdashboard.model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** FeedbackDAO - CRUD for feedback/ratings table. */
public class FeedbackDAO {

    /** Submit feedback from resident after task completion. */
    public boolean submitFeedback(Feedback f) {
        String sql = """
            INSERT INTO feedback (complaint_id, resident_id, partner_id, rating, comment)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE rating=VALUES(rating), comment=VALUES(comment)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, f.getComplaintId());
            ps.setInt(2, f.getResidentId());
            ps.setInt(3, f.getPartnerId());
            ps.setInt(4, f.getRating());
            ps.setString(5, f.getComment());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] submitFeedback error: " + e.getMessage());
        }
        return false;
    }

    /** Get all feedback for a partner (for partner dashboard). */
    public List<Feedback> getByPartnerId(int partnerId) {
        String sql = """
            SELECT f.*, CONCAT(r.first_name,' ',r.last_name) AS resident_name, c.title AS complaint_title
            FROM feedback f
            JOIN residents r ON f.resident_id = r.id
            JOIN complaints c ON f.complaint_id = c.id
            WHERE f.partner_id = ?
            ORDER BY f.submitted_at DESC
            """;
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partnerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Feedback fb = new Feedback();
                fb.setId(rs.getInt("id"));
                fb.setComplaintId(rs.getInt("complaint_id"));
                fb.setResidentId(rs.getInt("resident_id"));
                fb.setPartnerId(rs.getInt("partner_id"));
                fb.setRating(rs.getInt("rating"));
                fb.setComment(rs.getString("comment"));
                fb.setSubmittedAt(rs.getTimestamp("submitted_at"));
                fb.setResidentName(rs.getString("resident_name"));
                fb.setComplaintTitle(rs.getString("complaint_title"));
                list.add(fb);
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getByPartnerId error: " + e.getMessage());
        }
        return list;
    }

    /** Check if feedback already exists for a complaint. */
    public boolean existsForComplaint(int complaintId) {
        String sql = "SELECT id FROM feedback WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] existsForComplaint error: " + e.getMessage());
        }
        return false;
    }
}