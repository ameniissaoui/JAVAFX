package org.example.services;

import org.example.models.Planning;
import org.example.models.RendezVous;
import org.example.util.MaConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour manipuler les entités RendezVous depuis la base de données.
 */
public class RendezVousDAO {

    private final PlanningDAO planningDAO = new PlanningDAO();

    public List<RendezVous> getAllRendezVous() throws SQLException {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String query = "SELECT * FROM rendez_vous ORDER BY dateheure DESC";

        try (Connection conn = MaConnexion.getInstance().getCnx();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                RendezVous rdv = parseResultSet(rs);
                rendezVousList.add(rdv);
            }
        }

        // Associer les objets Planning
        for (RendezVous rdv : rendezVousList) {
            if (rdv.getPlanning_id() > 0) {
                try {
                    Planning planning = planningDAO.getPlanningById(rdv.getPlanning_id());
                    rdv.setPlanning(planning);
                } catch (SQLException e) {
                    System.err.println("Erreur lors du chargement du planning : " + e.getMessage());
                }
            }
        }

        return rendezVousList;
    }

    public RendezVous getRendezVousById(int id) throws SQLException {
        String query = "SELECT * FROM rendez_vous WHERE id = ?";

        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                RendezVous rdv = parseResultSet(rs);

                if (rdv.getPlanning_id() > 0) {
                    Planning planning = planningDAO.getPlanningById(rdv.getPlanning_id());
                    rdv.setPlanning(planning);
                }

                return rdv;
            }
        }

        return null;
    }

    public int saveRendezVous(RendezVous rdv) throws SQLException {
        String query = "INSERT INTO rendez_vous (planning_id, dateheure, statut, description, google_event_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, rdv.getPlanning_id());
            stmt.setTimestamp(2, Timestamp.valueOf(rdv.getDateheure()));
            stmt.setString(3, rdv.getStatut());
            stmt.setString(4, rdv.getDescription());
            stmt.setString(5, rdv.getGoogleEventId());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1); // ID généré
            } else {
                throw new SQLException("Échec de création du rendez-vous, aucun ID généré.");
            }
        }
    }

    public void updateRendezVous(RendezVous rdv) throws SQLException {
        String query = "UPDATE rendez_vous SET planning_id = ?, dateheure = ?, statut = ?, description = ?, google_event_id = ? WHERE id = ?";

        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, rdv.getPlanning_id());
            ps.setTimestamp(2, Timestamp.valueOf(rdv.getDateheure()));
            ps.setString(3, rdv.getStatut());
            ps.setString(4, rdv.getDescription());
            ps.setString(5, rdv.getGoogleEventId());
            ps.setInt(6, rdv.getId());

            ps.executeUpdate();
        }
    }

    public void deleteRendezVous(int id) throws SQLException {
        String query = "DELETE FROM rendez_vous WHERE id = ?";

        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<RendezVous> getRendezVousByPlanningId(int planningId) throws SQLException {
        List<RendezVous> list = new ArrayList<>();
        String query = "SELECT * FROM rendez_vous WHERE planning_id = ? ORDER BY dateheure";

        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, planningId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RendezVous rdv = parseResultSet(rs);
                list.add(rdv);
            }
        }

        // Associer les objets Planning après avoir fermé le ResultSet
        for (RendezVous rdv : list) {
            Planning planning = planningDAO.getPlanningById(planningId);
            rdv.setPlanning(planning);
        }

        return list;
    }

    public boolean rendezVousExisteDejaADateHeure(LocalDateTime dateTime, int planning_id, int rendezVousId) throws SQLException {
        String query = "SELECT COUNT(*) FROM rendez_vous WHERE dateheure = ? AND planning_id = ? AND id != ?";

        try (Connection conn = MaConnexion.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(dateTime));
            stmt.setInt(2, planning_id);
            stmt.setInt(3, rendezVousId);

            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Convertit un ResultSet SQL vers un objet RendezVous
     */
    private RendezVous parseResultSet(ResultSet rs) throws SQLException {
        RendezVous rdv = new RendezVous();
        rdv.setId(rs.getInt("id"));
        rdv.setPlanning_id(rs.getInt("planning_id"));

        Timestamp ts = rs.getTimestamp("dateheure");
        if (ts != null) {
            rdv.setDateheure(ts.toLocalDateTime());
        }

        rdv.setStatut(rs.getString("statut"));
        rdv.setDescription(rs.getString("description"));
        rdv.setGoogleEventId(rs.getString("google_event_id"));

        return rdv;
    }
}