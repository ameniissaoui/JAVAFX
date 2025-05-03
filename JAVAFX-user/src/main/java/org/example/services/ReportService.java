package org.example.services;

import javafx.application.Platform;
import org.example.models.Medecin;
import org.example.models.Report;
import org.example.models.ReportAction;
import org.example.util.MaConnexion;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private Connection cnx = MaConnexion.getInstance().getCnx();
    private MedecinService medecinService;

    // Twilio configuration
    private static final String TWILIO_ACCOUNT_SID = "";
    private static final String TWILIO_AUTH_TOKEN = "";
    private static final String TWILIO_WHATSAPP_NUMBER = "";
    private static boolean twilioInitialized = false;
    public ReportService() {
        this.medecinService = new MedecinService();
        initializeTwilio();

    }
    private void initializeTwilio() {
        if (!twilioInitialized) {
            try {
                System.out.println("Initializing Twilio with SID: " + TWILIO_ACCOUNT_SID.substring(0, 5) + "...");
                Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
                twilioInitialized = true;
                System.out.println("Twilio initialized successfully");
            } catch (Exception e) {
                System.err.println("Failed to initialize Twilio: " + e.getMessage());
                e.printStackTrace();
            }
        }
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
        // First, get the doctor ID from the report
        int doctorId = -1;
        try (Connection conn = MaConnexion.getInstance().getCnx()) {
            String doctorQuery = "SELECT r.doctor_id, d.nom, d.prenom, d.telephone FROM reports r " +
                    "JOIN user d ON r.doctor_id = d.id " +
                    "WHERE r.id = ?";
            try (PreparedStatement doctorStmt = conn.prepareStatement(doctorQuery)) {
                doctorStmt.setInt(1, reportId);
                ResultSet rs = doctorStmt.executeQuery();
                if (rs.next()) {
                    doctorId = rs.getInt("doctor_id");

                    // If this is a WARNING action, handle it immediately to avoid connection issues
                    if ("WARNING".equals(actionType)) {
                        String doctorName = rs.getString("nom") + " " + rs.getString("prenom");
                        String phoneNumber = rs.getString("telephone");
                        System.out.println("WARNING action for doctor: " + doctorName + " (ID: " + doctorId + "), phone: " + phoneNumber);

                        // Send WhatsApp directly with the retrieved information and comments
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            sendWhatsAppDirectly(doctorName, phoneNumber, comments);
                        } else {
                            System.err.println("No phone number available for doctor: " + doctorName);
                            showErrorDialog("Erreur", "Numéro de téléphone manquant pour le médecin.");
                        }
                    }
                } else {
                    System.err.println("Report not found: " + reportId);
                    return false;
                }
            }

            // Now log the action
            String query = "INSERT INTO report_actions (report_id, admin_id, action_type, comments, created_at) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, reportId);
                stmt.setInt(2, adminId);
                stmt.setString(3, actionType);
                stmt.setString(4, comments);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Handle different action types
                    switch (actionType) {
                        case "BANNED":
                            // Ban the doctor
                            if (doctorId > 0) {
                                banDoctorUser(doctorId);
                                System.out.println("Doctor ID " + doctorId + " has been banned.");
                            }
                            break;

                        case "UNBANNED":
                            // Unban the doctor
                            if (doctorId > 0) {
                                unbanDoctorUser(doctorId);
                                System.out.println("Doctor ID " + doctorId + " has been unbanned.");
                            }
                            break;

                        case "CLARIFIED":
                            // Delete the report
                            deleteReport(reportId);
                            System.out.println("Report ID " + reportId + " has been deleted (clarified).");
                            break;

                        case "WARNING":
                            // WhatsApp notification is already handled above
                            System.out.println("WARNING action logged for report ID " + reportId);
                            break;
                    }
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private boolean deleteReport(int reportId) {
        String query = "DELETE FROM reports WHERE id = ?";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reportId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void banDoctorUser(int doctorId) {
        // First, get the user ID associated with this doctor
        String query = "SELECT user_id FROM medecin WHERE user_id = ?";
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
            } else {
                // Try direct update if user_id is the same as doctorId
                String directUpdateQuery = "UPDATE user SET banned = true WHERE id = ?";
                try (PreparedStatement directUpdateStmt = conn.prepareStatement(directUpdateQuery)) {
                    directUpdateStmt.setInt(1, doctorId);
                    int affected = directUpdateStmt.executeUpdate();
                    if (affected > 0) {
                        System.out.println("User ID " + doctorId + " has been banned directly.");
                    } else {
                        System.err.println("Could not find user to ban for doctor ID: " + doctorId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error banning doctor user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void unbanDoctorUser(int doctorId) {
        // First, get the user ID associated with this doctor
        String query = "SELECT user_id FROM medecin WHERE user_id = ?";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");

                // Now unban the user in the user table
                String updateQuery = "UPDATE user SET banned = false WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, userId);
                    updateStmt.executeUpdate();
                    System.out.println("User ID " + userId + " associated with Doctor ID " + doctorId + " has been unbanned.");
                }
            } else {
                // Try direct update if user_id is the same as doctorId
                String directUpdateQuery = "UPDATE user SET banned = false WHERE id = ?";
                try (PreparedStatement directUpdateStmt = conn.prepareStatement(directUpdateQuery)) {
                    directUpdateStmt.setInt(1, doctorId);
                    int affected = directUpdateStmt.executeUpdate();
                    if (affected > 0) {
                        System.out.println("User ID " + doctorId + " has been unbanned directly.");
                    } else {
                        System.err.println("Could not find user to unban for doctor ID: " + doctorId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error unbanning doctor user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void sendWhatsAppDirectly(String doctorName, String phoneNumber, String customMessage) {
        try {
            // Ensure Twilio is initialized
            initializeTwilio();

            // Format phone number
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+216" + phoneNumber; // Prepend +216 for Tunisia
            }

            // Validate phone number format
            if (phoneNumber.length() != 12 || !phoneNumber.matches("\\+216\\d{8}")) {
                System.err.println("Invalid phone number format: " + phoneNumber);
                showErrorDialog("Erreur", "Format de numéro de téléphone invalide: " + phoneNumber + ". Attendu: +216 suivi de 8 chiffres.");
                return;
            }

            System.out.println("Preparing to send WhatsApp message to: " + phoneNumber);

            // Create message body with custom message if provided
            String messageBody;
            if (customMessage != null && !customMessage.trim().isEmpty()) {
                messageBody = "Bonjour Dr. " + doctorName + ",\n\n" +
                        customMessage + "\n\n" +
                        "Merci,\nL'équipe d'administration";
            } else {
                messageBody = "Bonjour Dr. " + doctorName + ",\n\n" +
                        "Nous avons reçu un signalement concernant votre compte. Veuillez contacter l'administration pour plus d'informations.\n\n" +
                        "Merci,\nL'équipe d'administration";
            }

            // Create and send the message
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + phoneNumber),
                    new PhoneNumber(TWILIO_WHATSAPP_NUMBER),
                    messageBody
            ).create();

            System.out.println("WhatsApp message sent successfully: " + message.getSid());

            // Show success dialog
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Notification envoyée");
                alert.setHeaderText(null);
                alert.setContentText("La notification WhatsApp a été envoyée avec succès au Dr. " + doctorName);
                alert.showAndWait();
            });

        } catch (com.twilio.exception.ApiException e) {
            System.err.println("Twilio API Error: " + e.getMessage());
            showErrorDialog("Erreur Twilio API", "Erreur lors de l'envoi du message: " + e.getMessage());
        } catch (com.twilio.exception.AuthenticationException e) {
            System.err.println("Twilio Authentication Error: " + e.getMessage());
            showErrorDialog("Erreur d'authentification Twilio", "Vos identifiants Twilio sont incorrects ou ont expiré.");
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp notification: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur", "Échec de l'envoi de la notification WhatsApp: " + e.getMessage());
        }
    }
    private void sendWhatsAppNotification(Medecin medecin) {
        if (medecin != null) {
            sendWhatsAppDirectly(medecin.getNom() + " " + medecin.getPrenom(), medecin.getTelephone(), null);
        } else {
            System.err.println("Cannot send WhatsApp notification: Medecin object is null");
            showErrorDialog("Erreur", "Impossible d'envoyer la notification: Informations du médecin manquantes.");
        }
    }
    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void checkAndApplyThreshold(int doctorId) {
        // Count ALL reports for this doctor, not just unresolved ones
        int totalReports = countTotalReports(doctorId);

        if (totalReports >= 3) {
            try {
                // Ban the doctor directly
                banDoctorUser(doctorId);
                System.out.println("Doctor ID " + doctorId + " BANNED due to reaching " + totalReports + " reports threshold.");

                // Use a valid admin ID for the automatic ban action
                // This is to avoid foreign key constraint issues
                int adminId = getValidAdminId();
                if (adminId > 0) {
                    // Get a report ID for this doctor to associate with the action
                    int reportId = getReportIdForDoctor(doctorId);
                    if (reportId > 0) {
                        // Log the automatic ban with a valid admin ID and report ID
                        String query = "INSERT INTO report_actions (report_id, admin_id, action_type, comments, created_at) " +
                                "VALUES (?, ?, 'AUTO_BAN', ?, NOW())";
                        try (Connection conn = MaConnexion.getInstance().getCnx();
                             PreparedStatement stmt = conn.prepareStatement(query)) {
                            stmt.setInt(1, reportId);
                            stmt.setInt(2, adminId);
                            stmt.setString(3, "Doctor automatically banned due to reaching " + totalReports + " reports threshold");
                            stmt.executeUpdate();
                            System.out.println("Automatic ban action logged successfully");
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error in automatic ban process: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private int getValidAdminId() {
        // Get a valid admin ID from the database to use for automatic actions
        String query = "SELECT user_id FROM admin LIMIT 1";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting valid admin ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Return -1 if no valid admin ID found
    }

    private int getReportIdForDoctor(int doctorId) {
        // Get a report ID for this doctor to associate with the action
        String query = "SELECT id FROM reports WHERE doctor_id = ? LIMIT 1";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting report ID for doctor: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Return -1 if no report found
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

}