package org.example.services;

import org.example.models.Recommandation;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommandationDAO implements IRecommandationDAO {
    private Connection cnx;
    
    public RecommandationDAO() {
        cnx = MaConnexion.getInstance().getCnx();
    }
    
    /**
     * Insert a new recommendation into the database
     * @param recommandation The recommendation to insert
     * @return true if the operation was successful
     */
    public boolean insert(Recommandation recommandation) {
        String query = "INSERT INTO recommandation (demande_id, petit_dejeuner, dejeuner, diner, activity, calories, duree, supplements) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, recommandation.getDemande_id());
            pst.setString(2, recommandation.getPetit_dejeuner());
            pst.setString(3, recommandation.getDejeuner());
            pst.setString(4, recommandation.getDiner());
            pst.setString(5, recommandation.getActivity());
            
            if (recommandation.getCalories() != null) {
                pst.setFloat(6, recommandation.getCalories());
            } else {
                pst.setNull(6, Types.FLOAT);
            }
            
            if (recommandation.getDuree() != null) {
                pst.setFloat(7, recommandation.getDuree());
            } else {
                pst.setNull(7, Types.FLOAT);
            }
            
            pst.setString(8, recommandation.getSupplements());
            
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    recommandation.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error inserting recommendation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update an existing recommendation in the database
     * @param recommandation The recommendation to update
     * @return true if the operation was successful
     */
    public boolean update(Recommandation recommandation) {
        String query = "UPDATE recommandation SET petit_dejeuner = ?, dejeuner = ?, diner = ?, " +
                "activity = ?, calories = ?, duree = ?, supplements = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, recommandation.getPetit_dejeuner());
            pst.setString(2, recommandation.getDejeuner());
            pst.setString(3, recommandation.getDiner());
            pst.setString(4, recommandation.getActivity());
            
            if (recommandation.getCalories() != null) {
                pst.setFloat(5, recommandation.getCalories());
            } else {
                pst.setNull(5, Types.FLOAT);
            }
            
            if (recommandation.getDuree() != null) {
                pst.setFloat(6, recommandation.getDuree());
            } else {
                pst.setNull(6, Types.FLOAT);
            }
            
            pst.setString(7, recommandation.getSupplements());
            pst.setInt(8, recommandation.getId());
            
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating recommendation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete a recommendation from the database
     * @param id The ID of the recommendation to delete
     * @return true if the operation was successful
     */
    public boolean delete(int id) {
        String query = "DELETE FROM recommandation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting recommendation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get a recommendation by its ID
     * @param id The ID of the recommendation
     * @return The recommendation, or null if not found
     */
    public Recommandation getById(int id) {
        String query = "SELECT * FROM recommandation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return extractRecommandationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving recommendation: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get a recommendation by demand ID
     * @param demandeId The ID of the related demand
     * @return The recommendation, or null if not found
     */
    public Recommandation getByDemandeId(int demandeId) {
        String query = "SELECT * FROM recommandation WHERE demande_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, demandeId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return extractRecommandationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving recommendation by demand ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all recommendations
     * @return List of all recommendations
     */
    public List<Recommandation> getAll() {
        List<Recommandation> recommendations = new ArrayList<>();
        String query = "SELECT * FROM recommandation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                recommendations.add(extractRecommandationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving all recommendations: " + e.getMessage());
            e.printStackTrace();
        }
        return recommendations;
    }
    
    /**
     * Helper method to extract a Recommandation object from a ResultSet
     * @param rs The ResultSet to extract from
     * @return The extracted Recommandation
     * @throws SQLException if an error occurs
     */
    private Recommandation extractRecommandationFromResultSet(ResultSet rs) throws SQLException {
        Recommandation recommandation = new Recommandation();
        recommandation.setId(rs.getInt("id"));
        recommandation.setDemande_id(rs.getInt("demande_id"));
        recommandation.setPetit_dejeuner(rs.getString("petit_dejeuner"));
        recommandation.setDejeuner(rs.getString("dejeuner"));
        recommandation.setDiner(rs.getString("diner"));
        recommandation.setActivity(rs.getString("activity"));
        
        float calories = rs.getFloat("calories");
        if (!rs.wasNull()) {
            recommandation.setCalories(calories);
        }
        
        float duree = rs.getFloat("duree");
        if (!rs.wasNull()) {
            recommandation.setDuree(duree);
        }
        
        recommandation.setSupplements(rs.getString("supplements"));
        return recommandation;
    }
} 