package com.partnersdashboard.model;


/** Partner/Worker profile. Pre-registered by admin, login-only. */
public class Partner {
    private int id;
    private int accountId;
    private int societyId;
    private String societyName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String skill;
    private String availability;  // AVAILABLE | BUSY | OFF_DUTY
    private int totalTasks;
    private int completedTasks;
    private double avgRating;

    public Partner() {}

    // ── Getters & Setters ───────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public int getSocietyId() { return societyId; }
    public void setSocietyId(int societyId) { this.societyId = societyId; }

    public String getSocietyName() { return societyName; }
    public void setSocietyName(String societyName) { this.societyName = societyName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }

    /** On-time completion percentage */
    public double getCompletionRate() {
        if (totalTasks == 0) return 0.0;
        return (completedTasks * 100.0) / totalTasks;
    }
}
