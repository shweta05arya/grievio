package com.partnersdashboard.model;


/** Resident/User profile linked to an Account. */
public class Resident {
    private int id;
    private int accountId;
    private int societyId;
    private String societyName;   // joined field for display
    private String firstName;
    private String lastName;
    private String email;         // from accounts table
    private String phone;
    private String blockNo;
    private String flatNo;

    public Resident() {}

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

    public String getBlockNo() { return blockNo; }
    public void setBlockNo(String blockNo) { this.blockNo = blockNo; }

    public String getFlatNo() { return flatNo; }
    public void setFlatNo(String flatNo) { this.flatNo = flatNo; }

    public String getAddress() { return "Block " + blockNo + ", Flat " + flatNo; }
}