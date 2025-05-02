package org.example.services;

import org.example.models.Planning;
import org.example.util.MaConnexion;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlanningDAO {

    /**
     * Récupère tous les plannings de la base de données
     */
    public List<Planning> getAllPlannings() throws SQLException {
        List<Planning> plannings = new ArrayList<>();
        String query = "SELECT * FROM planning";
        
        // Get a connection from MaConnexion (will automatically reconnect if closed)
        Connection conn = MaConnexion.getInstance().getCnx();
        
        // Check if connection is valid, reconnect if needed
        if (!validateConnection(conn)) {
            MaConnexion.getInstance().reconnect();
            conn = MaConnexion.getInstance().getCnx();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Planning planning = new Planning();
                planning.setId(rs.getInt("id"));
                planning.setJour(rs.getString("jour"));
                planning.setHeuredebut(rs.getTime("heuredebut").toLocalTime());
                planning.setHeurefin(rs.getTime("heurefin").toLocalTime());

                plannings.add(planning);
            }
        } catch (SQLException e) {
            System.err.println("Error loading plannings: " + e.getMessage());
            throw e;
        }

        return plannings;
    }

    /**
     * Récupère un planning par son ID
     */
    public Planning getPlanningById(int id) throws SQLException {
        String query = "SELECT * FROM planning WHERE id = ?";
        
        // Get a connection from MaConnexion (will automatically reconnect if closed)
        Connection conn = MaConnexion.getInstance().getCnx();
        
        // Check if connection is valid, reconnect if needed
        if (!validateConnection(conn)) {
            MaConnexion.getInstance().reconnect();
            conn = MaConnexion.getInstance().getCnx();
        }

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Planning planning = new Planning();
                    planning.setId(rs.getInt("id"));
                    planning.setJour(rs.getString("jour"));
                    planning.setHeuredebut(rs.getTime("heuredebut").toLocalTime());
                    planning.setHeurefin(rs.getTime("heurefin").toLocalTime());

                    return planning;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting planning by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    /**
     * Ajoute un nouveau planning dans la base de données
     */
    public void savePlanning(Planning planning) throws SQLException {
        String query = "INSERT INTO planning (jour, heuredebut, heurefin) VALUES (?, ?, ?)";
        
        // Get a connection from MaConnexion (will automatically reconnect if closed)
        Connection conn = MaConnexion.getInstance().getCnx();
        
        // Check if connection is valid, reconnect if needed
        if (!validateConnection(conn)) {
            MaConnexion.getInstance().reconnect();
            conn = MaConnexion.getInstance().getCnx();
        }

        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, planning.getJour());
            ps.setTime(2, Time.valueOf(planning.getHeuredebut()));
            ps.setTime(3, Time.valueOf(planning.getHeurefin()));

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        planning.setId(generatedKeys.getInt(1));
                        System.out.println("Planning ajouté avec ID: " + planning.getId());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving planning: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Met à jour un planning existant
     */
    public void updatePlanning(Planning planning) throws SQLException {
        String query = "UPDATE planning SET jour = ?, heuredebut = ?, heurefin = ? WHERE id = ?";
        
        // Get a connection from MaConnexion (will automatically reconnect if closed)
        Connection conn = MaConnexion.getInstance().getCnx();
        
        // Check if connection is valid, reconnect if needed
        if (!validateConnection(conn)) {
            MaConnexion.getInstance().reconnect();
            conn = MaConnexion.getInstance().getCnx();
        }

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, planning.getJour());
            ps.setTime(2, Time.valueOf(planning.getHeuredebut()));
            ps.setTime(3, Time.valueOf(planning.getHeurefin()));
            ps.setInt(4, planning.getId());

            int rowsAffected = ps.executeUpdate();
            System.out.println(rowsAffected + " planning(s) mis à jour.");
        } catch (SQLException e) {
            System.err.println("Error updating planning: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime un planning de la base de données
     */
    public void deletePlanning(int id) throws SQLException {
        String query = "DELETE FROM planning WHERE id = ?";
        
        // Get a connection from MaConnexion (will automatically reconnect if closed)
        Connection conn = MaConnexion.getInstance().getCnx();
        
        // Check if connection is valid, reconnect if needed
        if (!validateConnection(conn)) {
            MaConnexion.getInstance().reconnect();
            conn = MaConnexion.getInstance().getCnx();
        }

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            System.out.println(rowsAffected + " planning(s) supprimé(s).");
        } catch (SQLException e) {
            System.err.println("Error deleting planning: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test simple de la connexion
     */
    public boolean testConnection() {
        try {
            Connection conn = MaConnexion.getInstance().getCnx();
            return validateConnection(conn);
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates that a connection is open and valid
     */
    private boolean validateConnection(Connection conn) throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                return false;
            }
            // Check if connection is valid with a 2-second timeout
            return conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("Error validating connection: " + e.getMessage());
            return false;
        }
    }
}