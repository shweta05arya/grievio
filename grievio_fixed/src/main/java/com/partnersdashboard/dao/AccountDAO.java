package com.partnersdashboard.dao;

import com.partnersdashboard.database.DatabaseConnection;
import com.partnersdashboard.model.Account;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AccountDAO {

    public Account login(String email, String plainPassword) {
        String sql = "SELECT id, email, password_hash, role, is_active FROM accounts WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean isActive = rs.getBoolean("is_active");
                if (!isActive) {
                    return null;
                }

                String storedHash = rs.getString("password_hash");

                if (BCrypt.checkpw(plainPassword, storedHash)) {
                    Account acc = new Account();
                    acc.setId(rs.getInt("id"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPasswordHash(storedHash);
                    acc.setRole(rs.getString("role"));
                    acc.setActive(true);
                    return acc;
                }
            }

        } catch (SQLException e) {
            System.err.println("[AccountDAO] Login error: " + e.getMessage());
        }

        return null;
    }

    public int createAccount(String email, String plainPassword, String role) {
        String sql = "INSERT INTO accounts (email, password_hash, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, hashed);
            ps.setString(3, role);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[AccountDAO] Create account error: " + e.getMessage());
        }

        return -1;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT id FROM accounts WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            return ps.executeQuery().next();

        } catch (SQLException e) {
            System.err.println("[AccountDAO] emailExists error: " + e.getMessage());
        }

        return false;
    }

    public boolean resetPassword(int accountId, String newPlainPassword) {
        String sql = "UPDATE accounts SET password_hash = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashed = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt(10));
            ps.setString(1, hashed);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AccountDAO] resetPassword error: " + e.getMessage());
        }

        return false;
    }
}