import model.Planning;
import service.PlanningDAO;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class TestConnection {
    public static void main(String[] args) {
        // Test de la connexion et des opérations CRUD
        PlanningDAO planningDAO = new PlanningDAO();

        try {
            // Tester la connexion
            boolean connexionOK = planningDAO.testConnection();
            System.out.println("Test de connexion: " + (connexionOK ? "RÉUSSI" : "ÉCHEC"));

            if (connexionOK) {
                // Lister tous les plannings
                System.out.println("\nListe des plannings:");
                List<Planning> plannings = planningDAO.getAllPlannings();
                for (Planning p : plannings) {
                    System.out.println(p.getId() + " - " + p);
                }

                // Exemples d'autres opérations à tester
                /*
                // Ajouter un nouveau planning
                Planning nouveauPlanning = new Planning();
                nouveauPlanning.setJour("Vendredi");
                nouveauPlanning.setHeuredebut(LocalTime.of(9, 0));
                nouveauPlanning.setHeurefin(LocalTime.of(17, 0));
                planningDAO.savePlanning(nouveauPlanning);

                // Mettre à jour un planning
                Planning planningAModifier = planningDAO.getPlanningById(1);
                if (planningAModifier != null) {
                    planningAModifier.setJour("Lundi modifié");
                    planningDAO.updatePlanning(planningAModifier);
                }

                // Supprimer un planning (décommenter avec précaution)
                // planningDAO.deletePlanning(5);
                */
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}