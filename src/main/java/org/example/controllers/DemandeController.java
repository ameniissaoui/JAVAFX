package org.example.controllers;

import org.example.models.Demande;
import org.example.services.DemandeDAO;
import org.example.services.IDemandeDAO;

import java.time.LocalDateTime;
import java.util.List;

public class DemandeController {
    
    private final IDemandeDAO demandeDAO;
    
    public DemandeController() {
        this.demandeDAO = new DemandeDAO();
    }
    
    /**
     * Create a new lifestyle data request (Demande)
     */
    public boolean createDemande(float eau, int nbr_repas, boolean snacks, Float calories,
                                String activity, String sommeil, float duree_activite, int patient_id) {
        
        // Data validation
        if (eau < 0 || nbr_repas < 0 || duree_activite < 0) {
            System.err.println("Invalid input values: numeric values cannot be negative");
            return false;
        }
        
        if (calories != null && calories < 0) {
            System.err.println("Invalid input values: calories cannot be negative");
            return false;
        }
        
        // Create a new Demande object
        Demande demande = new Demande(
            LocalDateTime.now(),  // Current date and time
            eau,
            nbr_repas,
            snacks,
            calories,
            activity,
            sommeil,
            duree_activite,
            patient_id
        );
        
        // Store in database
        return demandeDAO.insert(demande);
    }
    
    /**
     * Retrieve a Demande by its ID
     */
    public Demande getDemande(int id) {
        return demandeDAO.getById(id);
    }
    
    /**
     * Retrieve all Demandes
     */
    public List<Demande> getAllDemandes() {
        return demandeDAO.getAll();
    }
    
    /**
     * Retrieve all Demandes for a specific patient
     */
    public List<Demande> getDemandesByPatient(int patientId) {
        return demandeDAO.getByPatientId(patientId);
    }
    
    /**
     * Update an existing Demande
     */
    public boolean updateDemande(int id, LocalDateTime date, float eau, int nbr_repas, boolean snacks,
                                Float calories, String activity, String sommeil, 
                                float duree_activite, int patient_id) {
        
        // Data validation
        if (eau < 0 || nbr_repas < 0 || duree_activite < 0) {
            System.err.println("Invalid input values: numeric values cannot be negative");
            return false;
        }
        
        if (calories != null && calories < 0) {
            System.err.println("Invalid input values: calories cannot be negative");
            return false;
        }
        
        // Check if the Demande exists
        Demande existing = demandeDAO.getById(id);
        if (existing == null) {
            System.err.println("Demande not found with ID: " + id);
            return false;
        }
        
        // Update the Demande
        Demande demande = new Demande(
            id,
            date,
            eau,
            nbr_repas,
            snacks,
            calories,
            activity,
            sommeil,
            duree_activite,
            patient_id
        );
        
        return demandeDAO.update(demande);
    }
    
    /**
     * Delete a Demande
     */
    public boolean deleteDemande(int id) {
        // Check if the Demande exists
        Demande existing = demandeDAO.getById(id);
        if (existing == null) {
            System.err.println("Demande not found with ID: " + id);
            return false;
        }
        
        return demandeDAO.delete(id);
    }
} 