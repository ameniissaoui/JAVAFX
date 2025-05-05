package org.example.models;

import java.time.LocalDateTime;

public class ReportAction {
    private int id;
    private int reportId;
    private int adminId;
    private String actionType;
    private String comments;
    private LocalDateTime createdAt;

    // Constructor
    public ReportAction(int id, int reportId, int adminId, String actionType, String comments, LocalDateTime createdAt) {
        this.id = id;
        this.reportId = reportId;
        this.adminId = adminId;
        this.actionType = actionType;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}