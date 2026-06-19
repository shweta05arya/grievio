package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;
import com.partnersdashboard.model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ComplaintDAO - CRUD and query operations for complaints.
 * Includes joins to resident, society, and partner tables for rich display.
 */
public class ComplaintDAO {

    // ── Base SQL with all joined fields ─────────────────────────────────────
    private static final String BASE_SQL = """
        SELECT c.*,
               CONCAT(r.first_name, ' ', r.last_name) AS resident_name,
               a_res.email AS resident_email,
               r.block_no, r.flat_no,
               s.name AS society_name,
               CONCAT(p.first_name, ' ', p.last_name) AS partner_name,
               asgn.visit_time_slot AS visit_slot
        FROM complaints c
        JOIN residents r ON c.resident_id = r.id
        JOIN accounts a_res ON r.account_id = a_res.id
        JOIN societies s ON c.society_id = s.id
        LEFT JOIN assignments asgn ON c.id = asgn.complaint_id
        LEFT JOIN partners p ON asgn.partner_id = p.id
        """;

    /** Get all complaints for a specific resident. */
    public List<Complaint> getByResidentId(int residentId) {
        return query(BASE_SQL + " WHERE c.resident_id = ? ORDER BY c.submitted_at DESC",
                ps -> ps.setInt(1, residentId));
    }

    /** Get all complaints for a society (used by society admin). */
    public List<Complaint> getBySocietyId(int societyId) {
        return query(BASE_SQL + " WHERE c.society_id = ? ORDER BY c.submitted_at DESC",
                ps -> ps.setInt(1, societyId));
    }

    /** Get all complaints (main admin). */
    public List<Complaint> getAll() {
        return query(BASE_SQL + " ORDER BY c.submitted_at DESC", ps -> {});
    }

    /** Get complaints assigned to a specific partner. */
    public List<Complaint> getByPartnerId(int partnerId) {
        return query(BASE_SQL + " WHERE asgn.partner_id = ? ORDER BY c.submitted_at DESC",
                ps -> ps.setInt(1, partnerId));
    }

    /** Get a single complaint by ID. */
    public Complaint getById(int id) {
        List<Complaint> list = query(BASE_SQL + " WHERE c.id = ?", ps -> ps.setInt(1, id));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Submit a new complaint from resident.
     * Returns generated complaint ID.
     */
    public int submitComplaint(Complaint c) {
        String sql = """
            INSERT INTO complaints (resident_id, society_id, category, title, description, priority, image_path)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getResidentId());
            ps.setInt(2, c.getSocietyId());
            ps.setString(3, c.getCategory());
            ps.setString(4, c.getTitle());
            ps.setString(5, c.getDescription());
            ps.setString(6, c.getPriority() != null ? c.getPriority() : "MEDIUM");
            ps.setString(7, c.getImagePath());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] submitComplaint error: " + e.getMessage());
        }
        return -1;
    }

    /** Update complaint status + log it in complaint_updates. */
    public boolean updateStatus(int complaintId, String newStatus, int updatedByAccountId, String notes) {
        String currentStatus = null;
        try {
            Complaint existing = getById(complaintId);
            if (existing != null) currentStatus = existing.getStatus();
        } catch (Exception ignored) {}

        String updateSql = "UPDATE complaints SET status = ? WHERE id = ?";
        String logSql = """
            INSERT INTO complaint_updates (complaint_id, updated_by, old_status, new_status, notes)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(updateSql);
                 PreparedStatement ps2 = conn.prepareStatement(logSql)) {

                ps1.setString(1, newStatus);
                ps1.setInt(2, complaintId);
                ps1.executeUpdate();

                ps2.setInt(1, complaintId);
                ps2.setInt(2, updatedByAccountId);
                ps2.setString(3, currentStatus);
                ps2.setString(4, newStatus);
                ps2.setString(5, notes);
                ps2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] updateStatus error: " + e.getMessage());
        }
        return false;
    }

    /** Update priority (admin action). */
    public boolean updatePriority(int complaintId, String priority) {
        String sql = "UPDATE complaints SET priority = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, priority);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] updatePriority error: " + e.getMessage());
        }
        return false;
    }

    /** Count complaints by status for a society (for dashboard metrics). */
    public int countByStatus(int societyId, String status) {
        String sql = "SELECT COUNT(*) FROM complaints WHERE society_id = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, societyId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] countByStatus error: " + e.getMessage());
        }
        return 0;
    }

    // ── Private helper ───────────────────────────────────────────────────────
    @FunctionalInterface
    interface PSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<Complaint> query(String sql, PSetter setter) {
        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] query error: " + e.getMessage());
        }
        return list;
    }

    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setResidentId(rs.getInt("resident_id"));
        c.setSocietyId(rs.getInt("society_id"));
        c.setCategory(rs.getString("category"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setPriority(rs.getString("priority"));
        c.setStatus(rs.getString("status"));
        c.setImagePath(rs.getString("image_path"));
        c.setSubmittedAt(rs.getTimestamp("submitted_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        c.setResidentName(rs.getString("resident_name"));
        c.setResidentEmail(rs.getString("resident_email"));
        c.setResidentBlock(rs.getString("block_no"));
        c.setResidentFlat(rs.getString("flat_no"));
        c.setSocietyName(rs.getString("society_name"));
        c.setPartnerName(rs.getString("partner_name"));
        c.setVisitSlot(rs.getString("visit_slot"));
        return c;
    }
}
