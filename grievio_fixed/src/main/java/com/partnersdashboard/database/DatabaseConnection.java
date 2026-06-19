package com.partnersdashboard.database;

import com.partnersdashboard.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Singleton JDBC connection manager.
 * Provides a single reusable connection to the MySQL database.
 * Use getConnection() wherever DB access is needed.
 */
public class DatabaseConnection {

    private static Connection connection = null;

    /** Returns the active database connection, creating it if necessary. */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                        AppConfig.DB_URL,
                        AppConfig.DB_USER,
                        AppConfig.DB_PASSWORD
                );
                System.out.println("[DB] Connected to MySQL: gravio_db");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Check pom.xml dependency.", e);
            }
        }
        return connection;
    }

    /** Closes the connection cleanly on app shutdown. */
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}