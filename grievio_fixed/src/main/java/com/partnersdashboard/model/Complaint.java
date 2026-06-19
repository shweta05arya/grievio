package com.partnersdashboard.model;

import java.sql.Timestamp;

/** Complaint raised by a resident. Core entity of the system. */
public class Complaint {

    // ── Status constants ────────────────────────────────────────────────────
    public static final String STATUS_SUBMITTED           = "SUBMITTED";
    public static final String STATUS_ASSIGNED            = "ASSIGNED";
    public static final String STATUS_ACCEPTED            = "ACCEPTED";
    public static final String STATUS_ON_THE_WAY          = "ON_THE_WAY";
    public static final String STATUS_IN_PROGRESS         = "IN_PROGRESS";
    public static final String STATUS_VERIFICATION_PENDING = "VERIFICATION_PENDING";
    public static final String STATUS_COMPLETED           = "COMPLETED";
    public static final String STATUS_REJECTED            = "REJECTED";

    private int id;
    private int residentId;
    private int societyId;
    private String category;
    private String title;
    private String description;
    private String priority;     // LOW | MEDIUM | HIGH
    private String status;
    private String imagePath;
    private Timestamp submittedAt;
    private Timestamp updatedAt;

    // Joined display fields
    private String residentName;
    private String residentEmail;
    private String residentBlock;
    private String residentFlat;
    private String societyName;
    private String partnerName;
    private String visitSlot;

    public Complaint() {}

    // ── Getters & Setters ───────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public int getSocietyId() { return societyId; }
    public void setSocietyId(int societyId) { this.societyId = societyId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Joined fields
    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }

    public String getResidentBlock() { return residentBlock; }
    public void setResidentBlock(String residentBlock) { this.residentBlock = residentBlock; }

    public String getResidentFlat() { return residentFlat; }
    public void setResidentFlat(String residentFlat) { this.residentFlat = residentFlat; }

    public String getSocietyName() { return societyName; }
    public void setSocietyName(String societyName) { this.societyName = societyName; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getVisitSlot() { return visitSlot; }
    public void setVisitSlot(String visitSlot) { this.visitSlot = visitSlot; }

    @Override
    public String toString() {
        return "Complaint{id=" + id + ", title='" + title + "', status='" + status + "'}";
    }
}