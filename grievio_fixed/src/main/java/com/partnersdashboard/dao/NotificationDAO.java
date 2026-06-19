package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;
import com.partnersdashboard.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** NotificationDAO - in-app notification management. */
public class NotificationDAO {

    /** Create a new notification for a user. */
    public boolean create(int accountId, String title, String message) {
        String sql = "INSERT INTO notifications (account_id, title, message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, title);
            ps.setString(3, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] create error: " + e.getMessage());
        }
        return false;
    }

    /** Get all unread notifications for an account, newest first. */
    public List<Notification> getUnread(int accountId) {
        return query(accountId, "WHERE n.account_id = ? AND n.is_read = FALSE ORDER BY n.created_at DESC");
    }

    /** Get all notifications (read + unread) for an account. */
    public List<Notification> getAll(int accountId) {
        return query(accountId, "WHERE n.account_id = ? ORDER BY n.created_at DESC LIMIT 50");
    }

    /** Mark a notification as read. */
    public boolean markRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] markRead error: " + e.getMessage());
        }
        return false;
    }

    /** Mark all notifications as read for an account. */
    public boolean markAllRead(int accountId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] markAllRead error: " + e.getMessage());
        }
        return false;
    }

    /** Count unread notifications (for badge display). */
    public int countUnread(int accountId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE account_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] countUnread error: " + e.getMessage());
        }
        return 0;
    }

    private List<Notification> query(int accountId, String whereClause) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.* FROM notifications n " + whereClause;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setAccountId(rs.getInt("account_id"));
                n.setTitle(rs.getString("title"));
                n.setMessage(rs.getString("message"));
                n.setRead(rs.getBoolean("is_read"));
                n.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(n);
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] query error: " + e.getMessage());
        }
        return list;
    }
}