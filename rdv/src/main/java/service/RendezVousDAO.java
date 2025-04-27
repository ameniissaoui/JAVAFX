package service;

import model.Planning;
import model.RendezVous;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    private PlanningDAO planningDAO = new PlanningDAO();

    public List<RendezVous> getAllRendezVous() throws SQLException {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String query = "SELECT * FROM rendez_vous ORDER BY dateheure DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Première étape: récupérer les données de base des rendez-vous
            while (rs.next()) {
                RendezVous rdv = new RendezVous();
                rdv.setId(rs.getInt("id"));
                rdv.setPlanning_id(rs.getInt("planning_id"));

                // Conversion du Timestamp SQL en LocalDateTime
                Timestamp timestamp = rs.getTimestamp("dateheure");
                if (timestamp != null) {
                    rdv.setDateheure(timestamp.toLocalDateTime());
                }

                rdv.setStatut(rs.getString("statut"));
                rdv.setDescription(rs.getString("description"));

                // Important: Ajouter le rendez-vous à la liste sans récupérer le planning ici
                rendezVousList.add(rdv);
            }
        }

        // Deuxième étape: récupérer les plannings dans une boucle séparée
        // après avoir fermé le ResultSet principal
        for (RendezVous rdv : rendezVousList) {
            if (rdv.getPlanning_id() > 0) {
                try {
                    Planning planning = planningDAO.getPlanningById(rdv.getPlanning_id());
                    rdv.setPlanning(planning);
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la récupération du planning " + rdv.getPlanning_id() + ": " + e.getMessage());
                    // Continuez même si un planning n'est pas trouvé
                }
            }
        }

        return rendezVousList;
    }

    public RendezVous getRendezVousById(int id) throws SQLException {
        String query = "SELECT * FROM rendez_vous WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RendezVous rdv = new RendezVous();
                    rdv.setId(rs.getInt("id"));
                    rdv.setPlanning_id(rs.getInt("planning_id"));

                    Timestamp timestamp = rs.getTimestamp("dateheure");
                    if (timestamp != null) {
                        rdv.setDateheure(timestamp.toLocalDateTime());
                    }

                    rdv.setStatut(rs.getString("statut"));
                    rdv.setDescription(rs.getString("description"));

                    // Récupérer le planning associé
                    if (rs.getInt("planning_id") > 0) {
                        Planning planning = planningDAO.getPlanningById(rs.getInt("planning_id"));
                        rdv.setPlanning(planning);
                    }

                    return rdv;
                }
            }
        }

        return null;
    }

    public void saveRendezVous(RendezVous rdv) throws SQLException {
        String query = "INSERT INTO rendez_vous (planning_id, dateheure, statut, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, rdv.getPlanning_id());
            ps.setTimestamp(2, Timestamp.valueOf(rdv.getDateheure()));
            ps.setString(3, rdv.getStatut());
            ps.setString(4, rdv.getDescription());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rdv.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    public void updateRendezVous(RendezVous rdv) throws SQLException {
        String query = "UPDATE rendez_vous SET planning_id = ?, dateheure = ?, statut = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, rdv.getPlanning_id());
            ps.setTimestamp(2, Timestamp.valueOf(rdv.getDateheure()));
            ps.setString(3, rdv.getStatut());
            ps.setString(4, rdv.getDescription());
            ps.setInt(5, rdv.getId());

            ps.executeUpdate();
        }
    }

    public void deleteRendezVous(int id) throws SQLException {
        String query = "DELETE FROM rendez_vous WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Méthodes supplémentaires utiles
    public List<RendezVous> getRendezVousByPlanningId(int planningId) throws SQLException {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String query = "SELECT * FROM rendez_vous WHERE planning_id = ? ORDER BY dateheure";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, planningId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RendezVous rdv = new RendezVous();
                    rdv.setId(rs.getInt("id"));
                    rdv.setPlanning_id(rs.getInt("planning_id"));

                    Timestamp timestamp = rs.getTimestamp("dateheure");
                    if (timestamp != null) {
                        rdv.setDateheure(timestamp.toLocalDateTime());
                    }

                    rdv.setStatut(rs.getString("statut"));
                    rdv.setDescription(rs.getString("description"));

                    // Récupérer le planning associé
                    Planning planning = planningDAO.getPlanningById(planningId);
                    rdv.setPlanning(planning);

                    rendezVousList.add(rdv);
                }
            }
        }

        return rendezVousList;
    }
}