package org.example.services;

import org.example.models.Recommandation;
import java.util.List;

public interface IRecommandationDAO {
    // Create
    boolean insert(Recommandation recommandation);
    
    // Read
    Recommandation getById(int id);
    List<Recommandation> getAll();
    Recommandation getByDemandeId(int demandeId);
    
    // Update
    boolean update(Recommandation recommandation);
    
    // Delete
    boolean delete(int id);
} 