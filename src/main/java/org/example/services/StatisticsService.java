package org.example.services;

import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.models.User;
import org.example.util.MaConnexion;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

public class StatisticsService {

    private Connection cnx = MaConnexion.getInstance().getCnx();

    // Get count of users by role
    public Map<String, Integer> getUserCountByRole() {
        Map<String, Integer> roleCounts = new HashMap<>();
        String query = "SELECT role, COUNT(*) as count FROM user GROUP BY role";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");
                roleCounts.put(role, count);
            }
        } catch (SQLException e) {
            System.out.println("Error getting user count by role: " + e.getMessage());
        }

        return roleCounts;
    }

    // Get verification statistics for doctors
    public Map<String, Integer> getDoctorVerificationStats() {
        Map<String, Integer> verificationStats = new HashMap<>();
        String query = "SELECT is_verified, COUNT(*) as count FROM medecin GROUP BY is_verified";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            // Initialize with zeros in case no results are found
            verificationStats.put("verified", 0);
            verificationStats.put("unverified", 0);

            while (rs.next()) {
                boolean isVerified = rs.getBoolean("is_verified");
                int count = rs.getInt("count");

                if (isVerified) {
                    verificationStats.put("verified", count);
                } else {
                    verificationStats.put("unverified", count);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting doctor verification stats: " + e.getMessage());
        }

        return verificationStats;
    }

    // Get banned user statistics
    public Map<String, Integer> getBannedUserStats() {
        Map<String, Integer> bannedStats = new HashMap<>();
        String query = "SELECT banned, COUNT(*) as count FROM user GROUP BY banned";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            // Initialize with zeros in case no results are found
            bannedStats.put("banned", 0);
            bannedStats.put("active", 0);

            while (rs.next()) {
                boolean isBanned = rs.getBoolean("banned");
                int count = rs.getInt("count");

                if (isBanned) {
                    bannedStats.put("banned", count);
                } else {
                    bannedStats.put("active", count);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting banned user stats: " + e.getMessage());
        }

        return bannedStats;
    }

    // Get registration statistics by month
    public Map<String, Integer> getRegistrationByMonth() {
        Map<String, Integer> monthlyStats = new LinkedHashMap<>(); // LinkedHashMap to preserve order

        // Initialize all months with 0
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String month : months) {
            monthlyStats.put(month, 0);
        }

        // Using the creation date in the user table
        // For MySQL, you can use MONTHNAME() function
        String query = "SELECT MONTHNAME(created_at) as month, COUNT(*) as count " +
                "FROM user WHERE created_at IS NOT NULL " +
                "GROUP BY MONTHNAME(created_at), MONTH(created_at) " +
                "ORDER BY MONTH(created_at)";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                String month = rs.getString("month");
                int count = rs.getInt("count");
                monthlyStats.put(month, count);
            }
        } catch (SQLException e) {
            System.out.println("Error getting registration by month stats: " + e.getMessage());
            // If 'created_at' column doesn't exist, we'll catch the error here
            System.out.println("Note: You may need to add a 'created_at' column to your user table");
        }

        return monthlyStats;
    }

    // Get age distribution of users
    public Map<String, Integer> getAgeDistribution() {
        Map<String, Integer> ageGroups = new LinkedHashMap<>();

        // Define age groups
        ageGroups.put("Under 18", 0);
        ageGroups.put("18-24", 0);
        ageGroups.put("25-34", 0);
        ageGroups.put("35-44", 0);
        ageGroups.put("45-54", 0);
        ageGroups.put("55-64", 0);
        ageGroups.put("65+", 0);

        String query = "SELECT dateNaissance FROM user WHERE dateNaissance IS NOT NULL";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Date birthDate = rs.getDate("dateNaissance");
                if (birthDate != null) {
                    int age = calculateAge(birthDate);

                    // Increment appropriate age group
                    if (age < 18) ageGroups.put("Under 18", ageGroups.get("Under 18") + 1);
                    else if (age < 25) ageGroups.put("18-24", ageGroups.get("18-24") + 1);
                    else if (age < 35) ageGroups.put("25-34", ageGroups.get("25-34") + 1);
                    else if (age < 45) ageGroups.put("35-44", ageGroups.get("35-44") + 1);
                    else if (age < 55) ageGroups.put("45-54", ageGroups.get("45-54") + 1);
                    else if (age < 65) ageGroups.put("55-64", ageGroups.get("55-64") + 1);
                    else ageGroups.put("65+", ageGroups.get("65+") + 1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting age distribution: " + e.getMessage());
        }

        return ageGroups;
    }

    // Calculate age from birth date
    private int calculateAge(Date birthDate) {
        if (birthDate == null) {
            return 0;
        }

        LocalDate birth = birthDate.toLocalDate();
        LocalDate now = LocalDate.now();
        return Period.between(birth, now).getYears();
    }

    // Get doctor specialties distribution
    public Map<String, Integer> getSpecialtyDistribution() {
        Map<String, Integer> specialties = new HashMap<>();
        String query = "SELECT specialite, COUNT(*) as count FROM medecin GROUP BY specialite";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                String specialty = rs.getString("specialite");
                int count = rs.getInt("count");
                specialties.put(specialty != null ? specialty : "Unspecified", count);
            }
        } catch (SQLException e) {
            System.out.println("Error getting specialty distribution: " + e.getMessage());
        }

        return specialties;
    }

    // Get report statistics
    public Map<String, Integer> getReportStats() {
        Map<String, Integer> reportStats = new HashMap<>();

        // Get total reports
        String totalQuery = "SELECT COUNT(*) as count FROM reports";
        // Get reports by status
        String statusQuery = "SELECT status, COUNT(*) as count FROM reports GROUP BY status";

        try (Statement st = cnx.createStatement()) {
            try (ResultSet rs = st.executeQuery(totalQuery)) {
                if (rs.next()) {
                    reportStats.put("total", rs.getInt("count"));
                }
            }

            try (ResultSet rs = st.executeQuery(statusQuery)) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    reportStats.put(status, count);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting report statistics: " + e.getMessage());
        }

        return reportStats;
    }

    // Get most reported doctors
    public List<Map<String, Object>> getMostReportedDoctors(int limit) {
        List<Map<String, Object>> doctorsList = new ArrayList<>();
        String query = "SELECT d.user_id, u.nom, u.prenom, COUNT(*) as report_count " +
                "FROM reports r " +
                "JOIN medecin d ON r.doctor_id = d.user_id " +
                "JOIN user u ON d.user_id = u.id " +
                "GROUP BY d.user_id, u.nom, u.prenom " +
                "ORDER BY report_count DESC " +
                "LIMIT ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> doctor = new HashMap<>();
                    doctor.put("id", rs.getInt("user_id"));
                    doctor.put("nom", rs.getString("nom"));
                    doctor.put("prenom", rs.getString("prenom"));
                    doctor.put("reportCount", rs.getInt("report_count"));
                    doctorsList.add(doctor);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting most reported doctors: " + e.getMessage());
        }

        return doctorsList;
    }
}