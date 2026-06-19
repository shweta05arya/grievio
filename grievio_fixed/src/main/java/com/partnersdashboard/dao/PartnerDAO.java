package com.partnersdashboard.dao;


import com.partnersdashboard.database.DatabaseConnection;
import com.partnersdashboard.model.Partner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** PartnerDAO - queries for partners table. */
public class PartnerDAO {

    private static final String BASE_SQL = """
        SELECT p.*, a.email, s.name AS society_name
        FROM partners p
        JOIN accounts a ON p.account_id = a.id
        LEFT JOIN societies s ON p.society_id = s.id
        """;

    public Partner getByAccountId(int accountId) {
        List<Partner> list = query(BASE_SQL + " WHERE p.account_id = ?", ps -> ps.setInt(1, accountId));
        return list.isEmpty() ? null : list.get(0);
    }

    public Partner getById(int partnerId) {
        List<Partner> list = query(BASE_SQL + " WHERE p.id = ?", ps -> ps.setInt(1, partnerId));
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Partner> getBySocietyId(int societyId) {
        return query(BASE_SQL + " WHERE p.society_id = ?", ps -> ps.setInt(1, societyId));
    }

    public List<Partner> getAvailableBySociety(int societyId) {
        return query(BASE_SQL + " WHERE p.society_id = ? AND p.availability = 'AVAILABLE'",
                ps -> ps.setInt(1, societyId));
    }

    public List<Partner> getAll() {
        return query(BASE_SQL + " ORDER BY p.first_name", ps -> {});
    }

    /** Update partner availability. */
    public boolean updateAvailability(int partnerId, String availability) {
        String sql = "UPDATE partners SET availability = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, availability);
            ps.setInt(2, partnerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PartnerDAO] updateAvailability error: " + e.getMessage());
        }
        return false;
    }

    /** Update total/completed tasks and avg rating after feedback. */
    public boolean updateStats(int partnerId) {
        String sql = """
            UPDATE partners p SET
                completed_tasks = (SELECT COUNT(*) FROM complaints c
                                   JOIN assignments a ON c.id = a.complaint_id
                                   WHERE a.partner_id = p.id AND c.status = 'COMPLETED'),
                total_tasks     = (SELECT COUNT(*) FROM assignments WHERE partner_id = p.id),
                avg_rating      = (SELECT COALESCE(AVG(rating), 0) FROM feedback WHERE partner_id = p.id)
            WHERE p.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partnerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PartnerDAO] updateStats error: " + e.getMessage());
        }
        return false;
    }

    /** Admin creates a new partner account. Returns partner ID or -1. */
    public int createPartner(Partner p, int accountId) {
        String sql = """
            INSERT INTO partners (account_id, society_id, first_name, last_name, phone, skill)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setInt(2, p.getSocietyId());
            ps.setString(3, p.getFirstName());
            ps.setString(4, p.getLastName());
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getSkill());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("[PartnerDAO] createPartner error: " + e.getMessage());
        }
        return -1;
    }

    // ── Private helper ───────────────────────────────────────────────────────
    @FunctionalInterface interface PSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<Partner> query(String sql, PSetter setter) {
        List<Partner> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PartnerDAO] query error: " + e.getMessage());
        }
        return list;
    }

    private Partner mapRow(ResultSet rs) throws SQLException {
        Partner p = new Partner();
        p.setId(rs.getInt("id"));
        p.setAccountId(rs.getInt("account_id"));
        p.setSocietyId(rs.getInt("society_id"));
        p.setSocietyName(rs.getString("society_name"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setEmail(rs.getString("email"));
        p.setPhone(rs.getString("phone"));
        p.setSkill(rs.getString("skill"));
        p.setAvailability(rs.getString("availability"));
        p.setTotalTasks(rs.getInt("total_tasks"));
        p.setCompletedTasks(rs.getInt("completed_tasks"));
        p.setAvgRating(rs.getDouble("avg_rating"));
        return p;
    }
}
