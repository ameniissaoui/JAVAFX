package org.example.controllers;

import org.example.models.Recommandation;
import org.example.services.IRecommandationDAO;
import org.example.services.RecommandationDAO;

import java.util.List;

public class RecommandationController {
    
    private final IRecommandationDAO recommandationDAO;
    private final DemandeController demandeController;
    
    public RecommandationController() {
        this.recommandationDAO = new RecommandationDAO();
        this.demandeController = new DemandeController();
    }
    
    /**
     * Create a new recommendation
     */
    public boolean createRecommandation(int demandeId, String petit_dejeuner, String dejeuner, String diner,
                                     String activity, Float calories, Float duree, String supplements) {
        
        // Check if the Demande exists
        if (demandeController.getDemande(demandeId) == null) {
            System.err.println("Cannot create recommendation: Demande not found with ID: " + demandeId);
            return false;
        }
        
        // Check if a recommendation already exists for this demande
        if (recommandationDAO.getByDemandeId(demandeId) != null) {
            System.err.println("A recommendation already exists for Demande ID: " + demandeId);
            return false;
        }
        
        // Data validation
        if (calories != null && calories < 0) {
            System.err.println("Invalid input values: calories cannot be negative");
            return false;
        }
        
        if (duree != null && duree < 0) {
            System.err.println("Invalid input values: duration cannot be negative");
            return false;
        }
        
        // Create a new Recommandation object
        Recommandation recommandation = new Recommandation(
            demandeId,
            petit_dejeuner,
            dejeuner,
            diner,
            activity,
            calories,
            duree,
            supplements
        );
        
        // Store in database
        return recommandationDAO.insert(recommandation);
    }
    
    /**
     * Retrieve a Recommandation by its ID
     */
    public Recommandation getRecommandation(int id) {
        return recommandationDAO.getById(id);
    }
    
    /**
     * Retrieve all Recommandations
     */
    public List<Recommandation> getAllRecommandations() {
        return recommandationDAO.getAll();
    }
    
    /**
     * Retrieve a Recommandation by Demande ID
     */
    public Recommandation getRecommandationByDemandeId(int demandeId) {
        return recommandationDAO.getByDemandeId(demandeId);
    }
    
    /**
     * Update an existing Recommandation
     */
    public boolean updateRecommandation(int id, int demandeId, String petit_dejeuner, String dejeuner, String diner,
                                     String activity, Float calories, Float duree, String supplements) {
        
        // Check if the Recommandation exists
        Recommandation existing = recommandationDAO.getById(id);
        if (existing == null) {
            System.err.println("Recommandation not found with ID: " + id);
            return false;
        }
        
        // Check if the Demande exists
        if (demandeController.getDemande(demandeId) == null) {
            System.err.println("Cannot update recommendation: Demande not found with ID: " + demandeId);
            return false;
        }
        
        // Data validation
        if (calories != null && calories < 0) {
            System.err.println("Invalid input values: calories cannot be negative");
            return false;
        }
        
        if (duree != null && duree < 0) {
            System.err.println("Invalid input values: duration cannot be negative");
            return false;
        }
        
        // Update the Recommandation
        Recommandation recommandation = new Recommandation(
            id,
            demandeId,
            petit_dejeuner,
            dejeuner,
            diner,
            activity,
            calories,
            duree,
            supplements
        );
        
        return recommandationDAO.update(recommandation);
    }
    
    /**
     * Delete a Recommandation
     */
    public boolean deleteRecommandation(int id) {
        // Check if the Recommandation exists
        Recommandation existing = recommandationDAO.getById(id);
        if (existing == null) {
            System.err.println("Recommandation not found with ID: " + id);
            return false;
        }
        
        return recommandationDAO.delete(id);
    }
} 