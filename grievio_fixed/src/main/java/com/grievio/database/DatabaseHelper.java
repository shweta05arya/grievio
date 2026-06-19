package com.grievio.database;

import java.sql.*;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:grievio.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Users (all roles)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT DEFAULT 'user',
                    society TEXT,
                    sector TEXT,
                    department TEXT,
                    phone TEXT,
                    created_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Complaints
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS complaints (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    complaint_id TEXT UNIQUE NOT NULL,
                    user_id INTEGER,
                    title TEXT,
                    society_name TEXT,
                    location TEXT,
                    latitude REAL,
                    longitude REAL,
                    complaint_type TEXT,
                    description TEXT,
                    priority TEXT,
                    category TEXT,
                    status TEXT DEFAULT 'Pending',
                    assigned_head TEXT,
                    assigned_worker TEXT,
                    assigned_partner TEXT,
                    predicted_days INTEGER,
                    is_public INTEGER DEFAULT 0,
                    vote_count INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT (datetime('now')),
                    updated_at TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY(user_id) REFERENCES users(id)
                )
            """);

            // Comments
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS comments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    complaint_id TEXT,
                    user_id INTEGER,
                    author_name TEXT,
                    comment TEXT,
                    created_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Ratings
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    complaint_id TEXT UNIQUE,
                    user_id INTEGER,
                    stars INTEGER,
                    feedback TEXT,
                    created_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Votes (petitions)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS votes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    complaint_id TEXT,
                    user_id INTEGER,
                    UNIQUE(complaint_id, user_id)
                )
            """);

            // Notifications
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    message TEXT,
                    is_read INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Petitions forwarding tracking table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS petition_forwards (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    complaint_id TEXT NOT NULL,
                    forwarded_by TEXT,
                    forwarded_to TEXT,
                    note TEXT,
                    forwarded_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Insert demo users
            stmt.execute("""
                INSERT OR IGNORE INTO users (name, email, password, role, society, sector, department)
                VALUES
                  ('Demo User',        'user@grievio.com',      'password123', 'user',    'Green Valley Society', 'Sector-4', NULL),
                  ('Society Admin',    'societyadmin@grievio.com','admin123',   'society_admin','Green Valley Society','Sector-4',NULL),
                  ('Sector Head',      'sectoradmin@grievio.com', 'admin123',   'sector_head', NULL,               'Sector-4',NULL),
                  ('Gov Admin',        'governmentadmin@grievio.com',   'admin123',    'gov_admin', NULL,                NULL,'Municipal Corporation'),
                  ('System Admin',     'admin@grievio.com',      'admin123',    'admin',   NULL,                  NULL,NULL),
                  ('Partner Raj',      'raj.kumar@gravio.com',    'password123',  'partner', 'Green Valley Society','Sector-4',NULL)
            """);

            // Sample complaints
            stmt.execute("""
                INSERT OR IGNORE INTO complaints
                    (complaint_id, user_id, title, society_name, location, latitude, longitude,
                     complaint_type, description, priority, category, status, assigned_head,
                     assigned_worker, predicted_days, is_public, vote_count)
                VALUES
                    ('GRV-001',1,'Water Leakage in Block A','Green Valley Society','Block A, Floor 2',
                     28.6139,77.2090,'Plumbing','Continuous water leakage causing damage to walls.',
                     'High','Society-Level','Completed','Mr. Sharma','Plumber Ravi',3,0,0),
                    ('GRV-002',1,'Street Light Not Working','Green Valley Society','Main Gate Road',
                     28.6200,77.2100,'Electrical','Street lights on main road are not functioning.',
                     'Medium','Public-Area','In Progress','Municipal Officer',NULL,7,1,12),
                    ('GRV-003',1,'Garbage Overflow Near Park','Green Valley Society','Community Park',
                     28.6150,77.2080,'Sanitation','Garbage bins overflowing, causing bad smell.',
                     'High','Public-Area','Pending','Sanitation Head',NULL,5,1,8),
                    ('GRV-004',1,'Lift Malfunction Tower B','Green Valley Society','Tower B',
                     28.6140,77.2095,'Lift/Elevator','Lift is stuck frequently on floor 5.',
                     'Urgent','Society-Level','Pending','Maintenance Head',NULL,2,0,0),
                    ('GRV-005',1,'Road Pothole Near School','Green Valley Society','School Road',
                     28.6180,77.2110,'Road','Large pothole near school gate is dangerous.',
                     'High','Public-Area','Pending','PWD Officer',NULL,5,1,25)
            """);

            // Sample comments
            stmt.execute("""
                INSERT OR IGNORE INTO comments (complaint_id, user_id, author_name, comment)
                VALUES
                  ('GRV-001', 1, 'Admin', 'Work order issued. Plumber assigned.'),
                  ('GRV-001', 1, 'Plumber Ravi', 'Pipe replaced successfully. Leakage fixed.'),
                  ('GRV-002', 1, 'Municipal Officer', 'Electrician dispatched to check transformers.')
            """);

            // Sample notifications
            stmt.execute("""
                INSERT OR IGNORE INTO notifications (user_id, message)
                VALUES
                  (1, '✅ Your complaint GRV-001 has been completed!'),
                  (1, '🔄 Your complaint GRV-002 is now In Progress.'),
                  (1, '📋 New update on GRV-003: Sanitation team reviewing.')
            """);

            System.out.println("✅ Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database init error: " + e.getMessage());
        }
    }
}
