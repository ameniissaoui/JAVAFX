package org.example.services;

import org.example.models.Medecin;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedecinService extends UserService<Medecin> {

    private static final int REPORT_THRESHOLD = 3;

    @Override
    protected String getUserType() {
        return "Médecin";
    }

    @Override
    public void add(Medecin medecin) {
        // Set role before inserting
        medecin.setRole("Médecin");
        medecin.setImage(null); // Ensure image is null initially

        // Insert into user table with all fields
        int userId = insertBaseUser(medecin);
        if (userId != -1) {
            medecin.setId(userId);
            System.out.println("Medecin ajouté !");
        }
    }
    @Override
    public void update(Medecin medecin) {
        // Ensure role is correct
        medecin.setRole("Médecin");

        // Update all fields in the user table
        updateBaseUser(medecin);
        System.out.println("Medecin mis à jour !");
    }

    @Override
    public void delete(Medecin medecin) {
        // Delete directly from user table
        deleteBaseUser(medecin.getId());
        System.out.println("Medecin supprimé !");
    }
    @Override
    public List<Medecin> getAll() {
        List<Medecin> list = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE role = 'Médecin' AND is_blocked = FALSE";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Medecin medecin = new Medecin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone"),
                        rs.getString("image"),
                        rs.getString("role"),
                        rs.getString("specialite"),
                        rs.getString("diploma")
                );
                medecin.setBanned(rs.getBoolean("is_blocked"));
                medecin.setIs_verified(rs.getBoolean("is_verified"));
                list.add(medecin);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll medecins: " + e.getMessage());
        }
        return list;
    }
    @Override
    public Medecin getOne(int id) {
        String query = "SELECT * FROM utilisateur WHERE id = ? AND role = 'Médecin'";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Medecin medecin = new Medecin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone"),
                        rs.getString("image"),
                        rs.getString("role"),
                        rs.getString("specialite"),
                        rs.getString("diploma")
                );
                medecin.setBanned(rs.getBoolean("is_blocked"));
                medecin.setIs_verified(rs.getBoolean("is_verified"));
                return medecin;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getOne medecin: " + e.getMessage());
        }
        return null;
    }

    public Medecin findByEmail(String email) {
        String query = "SELECT * FROM utilisateur WHERE email = ? AND role = 'Médecin'";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Medecin medecin = new Medecin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone"),
                        rs.getString("image"),
                        rs.getString("role"),
                        rs.getString("specialite"),
                        rs.getString("diploma")
                );
                medecin.setBanned(rs.getBoolean("is_blocked"));
                medecin.setIs_verified(rs.getBoolean("is_verified"));
                return medecin;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void verifyDoctor(int id) {
        Medecin medecin = getOne(id);
        if (medecin != null) {
            medecin.setIs_verified(true);
            update(medecin);
        }
    }

    public void unverifyDoctor(int id) {
        Medecin medecin = getOne(id);
        if (medecin != null) {
            medecin.setIs_verified(false);
            update(medecin);
        }
    }

    public boolean submitReport(int doctorId, int patientId, String reason) {
        // Verify the doctor exists and is a medecin
        String checkQuery = "SELECT id FROM utilisateur WHERE id = ? AND role = 'Médecin'";
        try (PreparedStatement checkStmt = cnx.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, doctorId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                return false; // Doctor doesn't exist or isn't a medecin
            }
        } catch (SQLException e) {
            System.err.println("Error checking doctor: " + e.getMessage());
            return false;
        }

        // Continue with report submission
        String query = "INSERT INTO reports (doctor_id, patient_id, reason, status, created_at) VALUES (?, ?, ?, 'PENDING', NOW())";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, patientId);
            stmt.setString(3, reason);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Check and apply threshold
                checkAndApplyThreshold(doctorId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error submitting report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public void checkAndApplyThreshold(int doctorId) {
        // Count total reports for this doctor
        int reportCount = getReportCount(doctorId);

        if (reportCount >= 3) {
            // Get the doctor
            Medecin medecin = getOne(doctorId);
            if (medecin != null) {
                // Ban the doctor
                medecin.setBanned(true);
                update(medecin);
                System.out.println("Doctor ID " + doctorId + " auto-banned due to " + reportCount + " reports");

                // Log the action
                logAutomaticBan(doctorId, reportCount);
            }
        }
    }
    private void logAutomaticBan(int doctorId, int reportCount) {
        String query = "INSERT INTO report_actions (report_id, admin_id, action_type, comments) " +
                "VALUES (NULL, 0, 'AUTO_BAN', ?)";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "Doctor automatically banned due to reaching " + reportCount + " reports threshold");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging automatic ban: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getReportCount(int doctorId) {
        String countQuery = "SELECT COUNT(*) FROM reports WHERE doctor_id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(countQuery)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des signalements: " + e.getMessage());
        }
        return 0;
    }
    public boolean hasReported(int doctorId, int patientId) {
        String query = "SELECT COUNT(*) FROM reports WHERE doctor_id = ? AND patient_id = ?";
        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking previous reports: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public Medecin findById(int id) {
        // This is just an alias for getOne to maintain compatibility
        return getOne(id);
    }
}