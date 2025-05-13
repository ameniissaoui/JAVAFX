package org.example.controllers;

import org.example.models.Demande;
import org.example.models.Patient;
import org.example.models.Medecin;
import org.example.services.DemandeDAO;
import org.example.services.IDemandeDAO;
import org.example.util.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

public class DemandeController {

    private final IDemandeDAO demandeDAO;
    private final SessionManager sessionManager;

    public DemandeController() {
        this.demandeDAO = new DemandeDAO();
        this.sessionManager = SessionManager.getInstance();
    }
    public boolean createDemande(float eau, int nbr_repas, boolean snacks, Float calories,
                                 String activity, String sommeil, float duree_activite, int patient_id) {

        System.out.println("Attempting to create demande with patient_id: " + patient_id);

        if (sessionManager.isPatient()) {
            Patient currentPatient = sessionManager.getCurrentPatient();
            System.out.println("Current logged-in patient has ID: " + currentPatient.getId());

            // Check if they match
            if (currentPatient.getId() != patient_id) {
                System.out.println("WARNING: IDs don't match!");
            }
        }if (sessionManager.isMedecin()) {
            // Doctors can create demandes for any patient
        } else {
            System.err.println("Only patients and doctors can create demandes");
            return false;
        }

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
     * For patients: can only view their own
     * For doctors: can view any patient's demandes
     * For admins: can view all demandes
     */
    public Demande getDemande(int id) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            System.err.println("User must be logged in to view a demande");
            return null;
        }

        Demande demande = demandeDAO.getById(id);

        // If demande not found, return null
        if (demande == null) {
            return null;
        }

        // Authorization check
        if (sessionManager.isPatient()) {
            Patient currentPatient = sessionManager.getCurrentPatient();
            if (currentPatient.getId() != demande.getPatient_id()) {
                System.err.println("Patients can only view their own demandes");
                return null;
            }
        }
        // Doctors and admins can view any demande

        return demande;
    }

    /**
     * Retrieve all Demandes
     * For admins: all demandes
     * For doctors: all patient demandes
     * For patients: not authorized
     */
    public List<Demande> getAllDemandes() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            System.err.println("User must be logged in to view demandes");
            return null;
        }

        // Authorization check
        if (sessionManager.isPatient()) {
            System.err.println("Patients cannot view all demandes");
            return null;
        }

        return demandeDAO.getAll();
    }

    /**
     * Retrieve all Demandes for a specific patient
     * For patients: only their own
     * For doctors: any patient
     * For admins: any patient
     */
    public List<Demande> getDemandesByPatient(int patientId) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            System.err.println("User must be logged in to view demandes");
            return null;
        }

        // Authorization check
        if (sessionManager.isPatient()) {
            Patient currentPatient = sessionManager.getCurrentPatient();
            if (currentPatient.getId() != patientId) {
                System.err.println("Patients can only view their own demandes");
                return null;
            }
        }
        // Doctors and admins can view any patient's demandes

        return demandeDAO.getByPatientId(patientId);
    }

    /**
     * Get demandes for the currently logged in patient
     * Only works if current user is a patient
     */
    public List<Demande> getCurrentPatientDemandes() {
        // Check if user is logged in as patient
        if (!sessionManager.isPatient()) {
            System.err.println("Only patients can use this method");
            return null;
        }

        Patient currentPatient = sessionManager.getCurrentPatient();
        return demandeDAO.getByPatientId(currentPatient.getId());
    }

    /**
     * Update an existing Demande
     * For patients: can only update their own
     * For doctors: can update any patient's demande
     * For admins: not allowed (business rule)
     */
    public boolean updateDemande(int id, LocalDateTime date, float eau, int nbr_repas, boolean snacks,
                                 Float calories, String activity, String sommeil,
                                 float duree_activite, int patient_id) {

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            System.err.println("User must be logged in to update a demande");
            return false;
        }

        // Check if the Demande exists
        Demande existing = demandeDAO.getById(id);
        if (existing == null) {
            System.err.println("Demande not found with ID: " + id);
            return false;
        }

        // Authorization check
        if (sessionManager.isPatient()) {
            Patient currentPatient = sessionManager.getCurrentPatient();
            if (currentPatient.getId() != existing.getPatient_id() ||
                    currentPatient.getId() != patient_id) {
                System.err.println("Patients can only update their own demandes");
                return false;
            }
        } else if (sessionManager.isMedecin()) {
            // Doctors can update any patient's demande
            // But they cannot change the patient it belongs to
            if (existing.getPatient_id() != patient_id) {
                System.err.println("Cannot change the patient a demande belongs to");
                return false;
            }
        } else {
            System.err.println("Only patients and doctors can update demandes");
            return false;
        }

        // Data validation
        if (eau < 0 || nbr_repas < 0 || duree_activite < 0) {
            System.err.println("Invalid input values: numeric values cannot be negative");
            return false;
        }

        if (calories != null && calories < 0) {
            System.err.println("Invalid input values: calories cannot be negative");
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
     * For patients: can only delete their own
     * For doctors: can delete any patient's demande
     * For admins: can delete any demande
     */
    public boolean deleteDemande(int id) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            System.err.println("User must be logged in to delete a demande");
            return false;
        }

        // Check if the Demande exists
        Demande existing = demandeDAO.getById(id);
        if (existing == null) {
            System.err.println("Demande not found with ID: " + id);
            return false;
        }

        // Authorization check
        if (sessionManager.isPatient()) {
            Patient currentPatient = sessionManager.getCurrentPatient();
            if (currentPatient.getId() != existing.getPatient_id()) {
                System.err.println("Patients can only delete their own demandes");
                return false;
            }
        }
        // Doctors and admins can delete any demande

        return demandeDAO.delete(id);
    }
}