package com.partnersdashboard.model;


/**
 * Account - represents a login account for any role in Gravio.
 * Roles: RESIDENT, PARTNER, SOCIETY_ADMIN, MAIN_ADMIN
 */
public class Account {
    private int id;
    private String email;
    private String passwordHash;
    private String role;
    private boolean isActive;

    public Account() {}

    public Account(int id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Account{id=" + id + ", email='" + email + "', role='" + role + "'}";
    }
}