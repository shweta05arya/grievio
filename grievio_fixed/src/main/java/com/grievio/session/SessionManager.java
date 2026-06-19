package com.grievio.session;

import com.grievio.model.User;

import java.io.*;
import java.util.Properties;

public final class SessionManager {
    private static final String SESSION_FILE = System.getProperty("user.home") + File.separator + ".grievio_session.properties";
    private static SessionManager instance;
    private User currentUser;
    private SessionManager() {}
    public static SessionManager getInstance() { if (instance == null) instance = new SessionManager(); return instance; }
    public void login(User user) { this.currentUser = user; if (user != null) saveSession(String.valueOf(user.getId()), user.getEmail(), user.getRole(), user.getName()); }
    public void logout() { this.currentUser = null; clearSession(); }
    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    private static Properties loadProps() { Properties p = new Properties(); File f = new File(SESSION_FILE); if (!f.exists()) return p; try (FileInputStream fis = new FileInputStream(f)) { p.load(fis); } catch (IOException ignored) {} return p; }
    private static void saveProps(Properties p) { try (FileOutputStream fos = new FileOutputStream(SESSION_FILE)) { p.store(fos, "Grievio Session"); } catch (IOException e) { e.printStackTrace(); } }
    public static void saveSession(String accountId, String email, String role, String name) { Properties p = loadProps(); p.setProperty("isLoggedIn", "true"); p.setProperty("accountId", accountId != null ? accountId : ""); p.setProperty("email", email != null ? email : ""); p.setProperty("role", role != null ? role : ""); p.setProperty("name", name != null ? name : ""); saveProps(p); }
    public static void saveLogin(String email, String password) { saveLoginForRole("GLOBAL", email, password); }
    public static void saveLoginForRole(String role, String email, String password) { Properties p = loadProps(); String key = normalizeRole(role); p.setProperty("saved.email." + key, email != null ? email : ""); p.setProperty("saved.password." + key, password != null ? password : ""); p.setProperty("remember.me." + key, "true"); saveProps(p); }
    public static void clearLogin() { clearLoginForRole("GLOBAL"); }
    public static void clearLoginForRole(String role) { Properties p = loadProps(); String key = normalizeRole(role); p.remove("saved.email." + key); p.remove("saved.password." + key); p.setProperty("remember.me." + key, "false"); saveProps(p); }
    public static boolean hasRememberedLogin() { return hasRememberedLoginForRole("GLOBAL"); }
    public static boolean hasRememberedLoginForRole(String role) { return "true".equalsIgnoreCase(loadProps().getProperty("remember.me." + normalizeRole(role), "false")); }
    public static String getSavedEmail() { return getSavedEmailForRole("GLOBAL"); }
    public static String getSavedEmailForRole(String role) { return loadProps().getProperty("saved.email." + normalizeRole(role), ""); }
    public static String getSavedPassword() { return getSavedPasswordForRole("GLOBAL"); }
    public static String getSavedPasswordForRole(String role) { return loadProps().getProperty("saved.password." + normalizeRole(role), ""); }
    private static String normalizeRole(String role) { return (role == null || role.isBlank()) ? "GLOBAL" : role.trim().toUpperCase(); }
    public static boolean hasSession() { return "true".equalsIgnoreCase(loadProps().getProperty("isLoggedIn", "false")); }
    public static String getRole() { return loadProps().getProperty("role", ""); }
    public static String getAccountId() { return loadProps().getProperty("accountId", ""); }
    public static String getEmail() { return loadProps().getProperty("email", ""); }
    public static String getName() { return loadProps().getProperty("name", ""); }
    public static void clearSession() { new File(SESSION_FILE).delete(); }
}
