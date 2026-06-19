package com.grievio.model;

public class Complaint {
    private int id;
    private String complaintId, title, societyName, location, complaintType, description;
    private String priority, category, status, assignedHead, assignedWorker, assignedPartner;
    private double latitude, longitude;
    private int userId, predictedDays, voteCount;
    private boolean isPublic;
    private String createdAt, updatedAt;

    public Complaint() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String v) { this.complaintId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getSocietyName() { return societyName; }
    public void setSocietyName(String v) { this.societyName = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String v) { this.complaintType = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getPriority() { return priority; }
    public void setPriority(String v) { this.priority = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getAssignedHead() { return assignedHead; }
    public void setAssignedHead(String v) { this.assignedHead = v; }
    public String getAssignedWorker() { return assignedWorker; }
    public void setAssignedWorker(String v) { this.assignedWorker = v; }
    public String getAssignedPartner() { return assignedPartner; }
    public void setAssignedPartner(String v) { this.assignedPartner = v; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double v) { this.latitude = v; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double v) { this.longitude = v; }
    public int getUserId() { return userId; }
    public void setUserId(int v) { this.userId = v; }
    public int getPredictedDays() { return predictedDays; }
    public void setPredictedDays(int v) { this.predictedDays = v; }
    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int v) { this.voteCount = v; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean v) { this.isPublic = v; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String v) { this.createdAt = v; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String v) { this.updatedAt = v; }
}
