package org.example.services;

import org.example.models.Demande;
import java.util.List;

public interface IDemandeDAO {
    // Create
    boolean insert(Demande demande);
    
    // Read
    Demande getById(int id);
    List<Demande> getAll();
    List<Demande> getByPatientId(int patientId);
    
    // Update
    boolean update(Demande demande);
    
    // Delete
    boolean delete(int id);
} 