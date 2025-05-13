package org.example.services;

import org.example.util.MaConnexion;
import org.example.models.Demande;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeDAO implements IDemandeDAO {
    
    private Connection connection;
    
    public DemandeDAO() {
        connection = MaConnexion.getInstance().getCnx(); 
    }
    
    @Override
    public boolean insert(Demande demande) {
        String query = "INSERT INTO demande (date, eau, nbr_repas, snacks, calories, activity, sommeil, duree_activite, patient_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(demande.getDate()));
            ps.setFloat(2, demande.getEau());
            ps.setInt(3, demande.getNbr_repas());
            ps.setBoolean(4, demande.isSnacks());
            
            if (demande.getCalories() != null) {
                ps.setFloat(5, demande.getCalories());
            } else {
                ps.setNull(5, Types.FLOAT);
            }
            
            ps.setString(6, demande.getActivity());
            
            if (demande.getSommeil() != null) {
                ps.setString(7, demande.getSommeil());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            
            ps.setFloat(8, demande.getDuree_activite());
            ps.setInt(9, demande.getPatient_id());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    demande.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error inserting demande: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Demande getById(int id) {
        String query = "SELECT * FROM demande WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return extractDemandeFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving demande by ID: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Demande> getAll() {
        List<Demande> demandes = new ArrayList<>();
        String query = "SELECT * FROM demande";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Demande demande = extractDemandeFromResultSet(rs);
                demandes.add(demande);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all demandes: " + e.getMessage());
        }
        
        return demandes;
    }
    
    @Override
    public List<Demande> getByPatientId(int patientId) {
        List<Demande> demandes = new ArrayList<>();
        String query = "SELECT * FROM demande WHERE patient_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Demande demande = extractDemandeFromResultSet(rs);
                demandes.add(demande);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving demandes by patient ID: " + e.getMessage());
        }
        
        return demandes;
    }
    
    @Override
    public boolean update(Demande demande) {
        String query = "UPDATE demande SET date = ?, eau = ?, nbr_repas = ?, snacks = ?, calories = ?, " +
                      "activity = ?, sommeil = ?, duree_activite = ?, patient_id = ? WHERE id = ?";
                      
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setTimestamp(1, Timestamp.valueOf(demande.getDate()));
            ps.setFloat(2, demande.getEau());
            ps.setInt(3, demande.getNbr_repas());
            ps.setBoolean(4, demande.isSnacks());
            
            if (demande.getCalories() != null) {
                ps.setFloat(5, demande.getCalories());
            } else {
                ps.setNull(5, Types.FLOAT);
            }
            
            ps.setString(6, demande.getActivity());
            
            if (demande.getSommeil() != null) {
                ps.setString(7, demande.getSommeil());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            
            ps.setFloat(8, demande.getDuree_activite());
            ps.setInt(9, demande.getPatient_id());
            ps.setInt(10, demande.getId());
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating demande: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM demande WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting demande: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to extract Demande object from ResultSet
    private Demande extractDemandeFromResultSet(ResultSet rs) throws SQLException {
        Demande demande = new Demande();
        
        demande.setId(rs.getInt("id"));
        
        Timestamp timestamp = rs.getTimestamp("date");
        if (timestamp != null) {
            demande.setDate(timestamp.toLocalDateTime());
        }
        
        demande.setEau(rs.getFloat("eau"));
        demande.setNbr_repas(rs.getInt("nbr_repas"));
        demande.setSnacks(rs.getBoolean("snacks"));
        
        Float calories = rs.getFloat("calories");
        if (!rs.wasNull()) {
            demande.setCalories(calories);
        }
        
        demande.setActivity(rs.getString("activity"));
        
        String sommeil = rs.getString("sommeil");
        if (!rs.wasNull()) {
            demande.setSommeil(sommeil);
        }
        
        demande.setDuree_activite(rs.getFloat("duree_activite"));
        demande.setPatient_id(rs.getInt("patient_id"));
        
        return demande;
    }
} 