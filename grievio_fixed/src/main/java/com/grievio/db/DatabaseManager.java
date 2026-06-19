package com.grievio.db;

import com.grievio.util.PasswordUtil;

import java.sql.*;

public final class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/grievio.db";
    private static Connection connection;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            initTables();
            seedData();
        } catch (Exception e) {
            System.err.println("DB init failed: " + e.getMessage());
        }
    }

    private DatabaseManager() {}
    public static Connection getConnection() { return connection; }

    private static void initTables() throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS admins (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE NOT NULL, phone TEXT, password_hash TEXT NOT NULL, role TEXT NOT NULL, district TEXT, sector TEXT, society TEXT, address TEXT)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS partners (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE NOT NULL, phone TEXT, organisation TEXT, password_hash TEXT NOT NULL, district TEXT, sector TEXT, society TEXT, address TEXT)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS residents (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE NOT NULL, phone TEXT, password_hash TEXT NOT NULL, district TEXT, sector TEXT, society TEXT, tower TEXT, flat TEXT, address TEXT)");
            ensureColumn("admins", "name", "TEXT"); ensureColumn("admins", "phone", "TEXT"); ensureColumn("admins", "address", "TEXT");
            ensureColumn("partners", "name", "TEXT"); ensureColumn("partners", "phone", "TEXT"); ensureColumn("partners", "organisation", "TEXT"); ensureColumn("partners", "address", "TEXT");
            ensureColumn("residents", "address", "TEXT");
        }
    }

    private static void ensureColumn(String table, String col, String type) throws SQLException {
        try (Statement s = connection.createStatement()) {
            try { s.executeQuery("SELECT " + col + " FROM " + table + " LIMIT 1").close(); }
            catch (SQLException ex) { s.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + col + " " + type); }
        }
    }

    private static void seedData() throws SQLException {
        String adminPh = PasswordUtil.hashPassword("1234");
        String partnerPh = PasswordUtil.hashPassword("password123");
        String rph = PasswordUtil.hashPassword("Resident@123");
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("INSERT INTO admins(name,email,phone,password_hash,role,district,sector,society,address) VALUES('Society Admin','societyadmin@grievio.com','9999999999','" + adminPh + "','SOCIETY_ADMIN','Delhi','Sector 12','Green Valley','Green Valley, Sector 12, Delhi') ON CONFLICT(email) DO UPDATE SET name=excluded.name, phone=excluded.phone, password_hash=excluded.password_hash, role=excluded.role, district=excluded.district, sector=excluded.sector, society=excluded.society, address=excluded.address");
            s.executeUpdate("INSERT INTO admins(name,email,phone,password_hash,role,district,sector,society,address) VALUES('Sector Admin','sectoradmin@grievio.com','9999999998','" + adminPh + "','SECTOR_ADMIN','Delhi','Sector 12','','Sector 12, Delhi') ON CONFLICT(email) DO UPDATE SET name=excluded.name, phone=excluded.phone, password_hash=excluded.password_hash, role=excluded.role, district=excluded.district, sector=excluded.sector, society=excluded.society, address=excluded.address");
            s.executeUpdate("INSERT INTO admins(name,email,phone,password_hash,role,district,sector,society,address) VALUES('Government Dashboard','governmentadmin@grievio.com','9999999997','" + adminPh + "','GOVERNMENT','Delhi','Sector 12','','Sector 12, Delhi') ON CONFLICT(email) DO UPDATE SET name=excluded.name, phone=excluded.phone, password_hash=excluded.password_hash, role=excluded.role, district=excluded.district, sector=excluded.sector, society=excluded.society, address=excluded.address");
            s.executeUpdate("INSERT INTO partners(name,email,phone,organisation,password_hash,district,sector,society,address) VALUES('Raj Kumar','raj.kumar@gravio.com','9999999996','Civic Partner','" + partnerPh + "','Delhi','Sector 12','Green Valley','Green Valley, Sector 12, Delhi') ON CONFLICT(email) DO UPDATE SET name=excluded.name, phone=excluded.phone, organisation=excluded.organisation, password_hash=excluded.password_hash, district=excluded.district, sector=excluded.sector, society=excluded.society, address=excluded.address");
            s.executeUpdate("INSERT INTO residents(name,email,phone,password_hash,district,sector,society,tower,flat,address) VALUES('Demo Resident','user@grievio.com','9999999995','" + rph + "','Delhi','Sector 12','Green Valley','A','101','A-101, Green Valley, Sector 12, Delhi') ON CONFLICT(email) DO UPDATE SET name=excluded.name, phone=excluded.phone, password_hash=excluded.password_hash, district=excluded.district, sector=excluded.sector, society=excluded.society, tower=excluded.tower, flat=excluded.flat, address=excluded.address");
        }
    }

    public static boolean validateResident(String email, String password) {
        return checkPassword("SELECT password_hash FROM residents WHERE email=?", email, password);
    }

    public static boolean validatePartner(String email, String password, String district, String sector, String society) {
        if (connection == null) return false;
        String normalized = email.trim().toLowerCase();
        try (PreparedStatement exact = connection.prepareStatement("SELECT password_hash FROM partners WHERE email=? AND district=? AND sector=? AND society=?")) {
            exact.setString(1, normalized); exact.setString(2, district.trim()); exact.setString(3, sector.trim()); exact.setString(4, society.trim());
            ResultSet rs = exact.executeQuery();
            if (rs.next() && PasswordUtil.matches(password, rs.getString(1))) return true;
        } catch (SQLException ignored) {}
        try (PreparedStatement fallback = connection.prepareStatement("SELECT password_hash FROM partners WHERE email=?")) {
            fallback.setString(1, normalized);
            ResultSet rs = fallback.executeQuery(); return rs.next() && PasswordUtil.matches(password, rs.getString(1));
        } catch (SQLException e) { return false; }
    }

    public static boolean validateAdmin(String email, String password, String role, String district, String sector, String society) {
        if (connection == null) return false;
        String normalized = email.trim().toLowerCase();
        String sql = "SELECT password_hash FROM admins WHERE email=? AND role=? AND district=? AND sector=?" + ("SOCIETY_ADMIN".equals(role) ? " AND society=?" : "");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalized); ps.setString(2, role); ps.setString(3, district.trim()); ps.setString(4, sector.trim());
            if ("SOCIETY_ADMIN".equals(role)) ps.setString(5, society.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && PasswordUtil.matches(password, rs.getString(1))) return true;
        } catch (SQLException ignored) {}

        try (PreparedStatement fallback = connection.prepareStatement("SELECT password_hash FROM admins WHERE email=? AND role=?")) {
            fallback.setString(1, normalized);
            fallback.setString(2, role);
            ResultSet rs = fallback.executeQuery(); return rs.next() && PasswordUtil.matches(password, rs.getString(1));
        } catch (SQLException e) { return false; }
    }

    private static boolean checkPassword(String sql, String email, String password) {
        if (connection == null) return false;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery(); return rs.next() && PasswordUtil.matches(password, rs.getString(1));
        } catch (SQLException e) { return false; }
    }

    public static boolean registerResident(String name, String email, String phone, String password) {
        return registerResident(name, email, phone, password, "", "", "", "", "", "");
    }

    public static boolean registerResident(String name, String email, String phone, String password, String district, String sector, String society, String tower, String flat, String address) {
        if (connection == null || isEmailRegistered(email)) return false;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO residents(name,email,phone,password_hash,district,sector,society,tower,flat,address) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, name.trim()); ps.setString(2, email.trim().toLowerCase()); ps.setString(3, phone.trim()); ps.setString(4, PasswordUtil.hashPassword(password));
            ps.setString(5, district); ps.setString(6, sector); ps.setString(7, society); ps.setString(8, tower); ps.setString(9, flat); ps.setString(10, address);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("Resident register error: " + e.getMessage()); return false; }
    }

    public static boolean registerAdmin(String name, String email, String phone, String password, String role, String district, String sector, String society, String address) {
        if (connection == null || isEmailRegistered(email)) return false;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO admins(name,email,phone,password_hash,role,district,sector,society,address) VALUES(?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, name.trim()); ps.setString(2, email.trim().toLowerCase()); ps.setString(3, phone.trim()); ps.setString(4, PasswordUtil.hashPassword(password)); ps.setString(5, role);
            ps.setString(6, district); ps.setString(7, sector); ps.setString(8, society); ps.setString(9, address); ps.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("Admin register error: " + e.getMessage()); return false; }
    }

    public static boolean registerPartner(String name, String email, String phone, String organisation, String password, String district, String sector, String society, String address) {
        if (connection == null || isEmailRegistered(email)) return false;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO partners(name,email,phone,organisation,password_hash,district,sector,society,address) VALUES(?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, name.trim()); ps.setString(2, email.trim().toLowerCase()); ps.setString(3, phone.trim()); ps.setString(4, organisation.trim()); ps.setString(5, PasswordUtil.hashPassword(password));
            ps.setString(6, district); ps.setString(7, sector); ps.setString(8, society); ps.setString(9, address); ps.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("Partner register error: " + e.getMessage()); return false; }
    }

    public static boolean isEmailRegistered(String email) {
        return emailExists(email);
    }

    public static boolean emailExists(String email) {
        if (connection == null) return false;
        String normalized = email.trim().toLowerCase();
        return exists("SELECT 1 FROM residents WHERE email=?", normalized) || exists("SELECT 1 FROM admins WHERE email=?", normalized) || exists("SELECT 1 FROM partners WHERE email=?", normalized);
    }

    private static boolean exists(String sql, String email) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) { ps.setString(1, email); return ps.executeQuery().next(); } catch (SQLException e) { return false; }
    }

    public static boolean updatePassword(String email, String newRawPassword) {
        if (connection == null) return false;
        String hashed = PasswordUtil.hashPassword(newRawPassword);
        return update("UPDATE residents SET password_hash=? WHERE email=?", hashed, email) > 0
                || update("UPDATE admins SET password_hash=? WHERE email=?", hashed, email) > 0
                || update("UPDATE partners SET password_hash=? WHERE email=?", hashed, email) > 0;
    }

    private static int update(String sql, String hashed, String email) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) { ps.setString(1, hashed); ps.setString(2, email.trim().toLowerCase()); return ps.executeUpdate(); } catch (SQLException e) { return 0; }
    }
}
