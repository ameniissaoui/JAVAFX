package org.example.services;

import org.example.models.Medecin;
import org.example.models.Report;
import org.example.models.ReportAction;
import org.example.util.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private Connection cnx = MaConnexion.getInstance().getCnx();
    private UserService userService; // Reference to UserService for banUser

    public ReportService() {
        // We'll initialize this in the methods that need it
    }

    public List<Report> getReports(String status, String reason, LocalDate startDate, LocalDate endDate) {
        List<Report> reports = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT r.*, " +
                        "d.nom AS doctor_nom, d.prenom AS doctor_prenom, d.image AS doctor_image, " +
                        "p.nom AS patient_nom, p.prenom AS patient_prenom " +
                        "FROM reports r " +
                        "LEFT JOIN user d ON r.doctor_id = d.id " +
                        "LEFT JOIN user p ON r.patient_id = p.id " +
                        "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();
        if (status != null && !status.isEmpty()) {
            query.append(" AND r.status = ?");
            params.add(status);
        }
        if (reason != null && !reason.isEmpty()) {
            query.append(" AND r.reason LIKE ?");
            params.add("%" + reason + "%");
        }
        if (startDate != null) {
            query.append(" AND r.created_at >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            query.append(" AND r.created_at <= ?");
            params.add(Date.valueOf(endDate));
        }

        try (Connection conn = MaConnexion.getInstance().getCnx(); // Get fresh connection
             PreparedStatement pst = conn.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setDoctorId(rs.getInt("doctor_id"));
                report.setPatientId(rs.getInt("patient_id"));
                report.setDoctorNom(rs.getString("doctor_nom"));
                report.setDoctorPrenom(rs.getString("doctor_prenom"));
                report.setDoctorImage(rs.getString("doctor_image"));
                report.setPatientNom(rs.getString("patient_nom"));
                report.setPatientPrenom(rs.getString("patient_prenom"));
                report.setReason(rs.getString("reason"));
                report.setStatus(rs.getString("status"));
                report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                report.setAdminComments(rs.getString("admin_comments"));
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reports: " + e.getMessage());
            e.printStackTrace();
        }
        return reports;
    }

    public boolean updateReportStatus(int reportId, String newStatus, String adminComments, int adminId) {
        String query = "UPDATE reports SET status = ?, admin_comments = ?, admin_id = ? WHERE id = ?";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, adminComments);
            stmt.setInt(3, adminId);
            stmt.setInt(4, reportId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Check thresholds after updating status
                String checkQuery = "SELECT doctor_id FROM reports WHERE id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, reportId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int doctorId = rs.getInt("doctor_id");
                        checkAndApplyThreshold(doctorId);
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating report status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean logAction(int reportId, int adminId, String actionType, String comments) {
        String query = "INSERT INTO report_actions (report_id, admin_id, action_type, comments) VALUES (?, ?, ?, ?)";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reportId);
            stmt.setInt(2, adminId);
            stmt.setString(3, actionType);
            stmt.setString(4, comments);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Automatically update doctor status based on action type
                String doctorQuery = "SELECT doctor_id FROM reports WHERE id = ?";
                try (PreparedStatement doctorStmt = conn.prepareStatement(doctorQuery)) {
                    doctorStmt.setInt(1, reportId);
                    ResultSet rs = doctorStmt.executeQuery();
                    if (rs.next()) {
                        int doctorId = rs.getInt("doctor_id");
                        if (actionType.equals("SUSPENDED")) {
                            updateDoctorStatus(doctorId, "SUSPENDED");
                        } else if (actionType.equals("BANNED")) {
                            updateDoctorStatus(doctorId, "BANNED");
                            banDoctorUser(doctorId);
                        }
                        checkAndApplyThreshold(doctorId);
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDoctorStatus(int doctorId, String newStatus) {
        String query = "UPDATE medecin SET status = ? WHERE id = ?";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, doctorId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating doctor status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int countUnresolvedReports(int doctorId) {
        String query = "SELECT COUNT(*) FROM reports WHERE doctor_id = ? AND status IN ('PENDING', 'UNDER_REVIEW')";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting unresolved reports: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int countTotalReports(int doctorId) {
        String query = "SELECT COUNT(*) FROM reports WHERE doctor_id = ?";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total reports: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public void checkAndApplyThreshold(int doctorId) {
        // Count total reports instead of just unresolved ones for auto-ban feature
        int totalReports = countTotalReports(doctorId);

        if (totalReports >= 3) {
            // Threshold reached: BAN the doctor (not just suspend)
            updateDoctorStatus(doctorId, "BANNED");
            banDoctorUser(doctorId);

            // Log this automatic action
            logAutomaticBan(doctorId, totalReports);

            System.out.println("Doctor ID " + doctorId + " BANNED due to reaching " + totalReports + " reports threshold.");
        } else if (totalReports >= 2) {
            // Flag for review
            updateDoctorStatus(doctorId, "FLAGGED");
            System.out.println("Doctor ID " + doctorId + " has " + totalReports + " reports. Flagged for review.");
        }
    }

    private void banDoctorUser(int doctorId) {
        // First, get the user ID associated with this doctor
        String query = "SELECT user_id FROM medecin WHERE id = ?";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");

                // Now ban the user in the user table
                String updateQuery = "UPDATE user SET banned = true WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, userId);
                    updateStmt.executeUpdate();
                    System.out.println("User ID " + userId + " associated with Doctor ID " + doctorId + " has been banned.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error banning doctor user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logAutomaticBan(int doctorId, int reportCount) {
        // Log this automatic action in report_actions table
        String query = "INSERT INTO report_actions (report_id, admin_id, action_type, comments) " +
                "VALUES (NULL, 0, 'AUTO_BAN', 'Doctor automatically banned due to " + reportCount + " reports')";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging automatic ban: " + e.getMessage());
            e.printStackTrace();
        }
    }
}