package org.example.models;

import java.time.LocalDateTime;

public class Report {
    private int id;
    private int doctorId;
    private int patientId;
    private String reason;
    private LocalDateTime createdAt;
    private String status;
    private String adminComments;
    private Integer adminId;
    private String doctorNom;
    private String doctorPrenom;
    private String doctorImage; // New field
    private String patientNom;
    private String patientPrenom;

    // Constructor
    public Report(int id, int doctorId, int patientId, String reason, LocalDateTime createdAt,
                  String status, String adminComments, Integer adminId) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.reason = reason;
        this.createdAt = createdAt;
        this.status = status;
        this.adminComments = adminComments;
        this.adminId = adminId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }
    public String getDoctorImage() { return doctorImage; }
    public void setDoctorImage(String doctorImage) { this.doctorImage = doctorImage; }
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public String getDoctorNom() { return doctorNom; }
    public void setDoctorNom(String doctorNom) { this.doctorNom = doctorNom; }

    public String getDoctorPrenom() { return doctorPrenom; }
    public void setDoctorPrenom(String doctorPrenom) { this.doctorPrenom = doctorPrenom; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getPatientPrenom() { return patientPrenom; }
    public void setPatientPrenom(String patientPrenom) { this.patientPrenom = patientPrenom; }
    public Report() {
    }
}