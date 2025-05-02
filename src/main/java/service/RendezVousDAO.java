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
                rdv.setGoogleEventId(rs.getString("google_event_id")); // Ajout de cette ligne

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
                    rdv.setGoogleEventId(rs.getString("google_event_id")); // Ajout de cette ligne

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

    /**
     * Sauvegarde un nouveau rendez-vous et retourne son ID généré
     */
    public int saveRendezVous(RendezVous rendezVous) throws SQLException {
        String query = "INSERT INTO rendez_vous (planning_id, dateheure, statut, description, google_event_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, rendezVous.getPlanning_id());
            stmt.setTimestamp(2, Timestamp.valueOf(rendezVous.getDateheure()));
            stmt.setString(3, rendezVous.getStatut());
            stmt.setString(4, rendezVous.getDescription());
            stmt.setString(5, rendezVous.getGoogleEventId());

            stmt.executeUpdate();

            // Récupérer l'ID généré
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("La création du rendez-vous a échoué, aucun ID n'a été retourné.");
            }
        }
    }

    public void updateRendezVous(RendezVous rdv) throws SQLException {
        String query = "UPDATE rendez_vous SET planning_id = ?, dateheure = ?, statut = ?, description = ?, google_event_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, rdv.getPlanning_id());
            ps.setTimestamp(2, Timestamp.valueOf(rdv.getDateheure()));
            ps.setString(3, rdv.getStatut());
            ps.setString(4, rdv.getDescription());
            ps.setString(5, rdv.getGoogleEventId()); // Correction de l'ordre et du nom de variable
            ps.setInt(6, rdv.getId());                // Correction de l'ordre et du nom de variable

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

    /**
     * Vérifie si un rendez-vous existe déjà à la date et heure spécifiées
     */
    public boolean rendezVousExisteDejaADateHeure(LocalDateTime dateTime, int planning_id, int rendezVousId) throws SQLException {
        String query = "SELECT COUNT(*) FROM rendez_vous WHERE dateheure = ? AND planning_id = ? AND id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(dateTime));
            stmt.setInt(2, planning_id);
            stmt.setInt(3, rendezVousId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;
        }
    }

    /**
     * Récupère tous les rendez-vous associés à un planning spécifique
     */
    public List<RendezVous> getRendezVousByPlanningId(int planningId) throws SQLException {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String query = "SELECT r.*, p.jour, p.heuredebut, p.heurefin FROM rendez_vous r " +
                "JOIN planning p ON r.planning_id = p.id " +
                "WHERE r.planning_id = ? " +
                "ORDER BY r.dateheure";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, planningId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RendezVous rendezVous = new RendezVous();
                rendezVous.setId(rs.getInt("id"));
                rendezVous.setPlanning_id(rs.getInt("planning_id"));
                rendezVous.setDateheure(rs.getTimestamp("dateheure").toLocalDateTime());
                rendezVous.setStatut(rs.getString("statut"));
                rendezVous.setDescription(rs.getString("description"));
                rendezVous.setGoogleEventId(rs.getString("google_event_id")); // Ajout de cette ligne

                // Créer et associer l'objet Planning
                Planning planning = new Planning();
                planning.setId(rs.getInt("planning_id"));
                planning.setJour(rs.getString("jour"));
                planning.setHeuredebut(rs.getTime("heuredebut").toLocalTime());
                planning.setHeurefin(rs.getTime("heurefin").toLocalTime());

                rendezVous.setPlanning(planning);

                rendezVousList.add(rendezVous);
            }
        }

        return rendezVousList;
    }
}