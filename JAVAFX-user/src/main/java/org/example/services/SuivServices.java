package org.example.services;
import org.example.interfaces.Iservices;

import org.example.util.MaConnexion;
import org.example.models.historique_traitement;
import org.example.models.suivie_medical;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuivServices implements Iservices<suivie_medical> {
    private final Connection cnx;
    private final HisServices hisServices;

    public SuivServices() {
        cnx = MaConnexion.getInstance().getCnx();
        hisServices = new HisServices();
    }

    @Override
    public void add(suivie_medical suivi) {
        String req = "INSERT INTO chrono.suivie_medical (date, commentaire, id_historique_id) VALUES (?,?,?)";
        try {
            validateHistorique(suivi);
            PreparedStatement stm = cnx.prepareStatement(req);
            setStatementParameters(stm, suivi);
            stm.executeUpdate();
            System.out.println("Suivi médical ajouté avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(suivie_medical suivi) {
        String req = "UPDATE chrono.suivie_medical SET date=?, commentaire=?, id_historique_id=? WHERE id=?";
        try {
            validateHistorique(suivi);
            PreparedStatement stm = cnx.prepareStatement(req);
            setStatementParameters(stm, suivi);
            stm.setInt(4, suivi.getId());
            stm.executeUpdate();
            System.out.println("Suivi médical mis à jour avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(suivie_medical suivi) {
        String req = "DELETE FROM chrono.suivie_medical WHERE id=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, suivi.getId());
            stm.executeUpdate();
            System.out.println("Suivi médical supprimé avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public suivie_medical find(int id) {
        String req = "SELECT * FROM chrono.suivie_medical WHERE id=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return mapResultSetToSuiviMedical(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<suivie_medical> afficher() {
        List<suivie_medical> suivisMedicaux = new ArrayList<>();
        String req = "SELECT * FROM chrono.suivie_medical";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                suivisMedicaux.add(mapResultSetToSuiviMedical(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return suivisMedicaux;
    }

    public List<suivie_medical> getSuiviByHistoriqueId(int historiqueId) {
        List<suivie_medical> suivisMedicaux = new ArrayList<>();
        String req = "SELECT * FROM chrono.suivie_medical WHERE id_historique_id=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, historiqueId);
            ResultSet rs = stm.executeQuery();

            // Récupérer l'objet historique une seule fois pour tous les suivis
            historique_traitement historique = hisServices.find(historiqueId);

            while (rs.next()) {
                suivie_medical suivi = mapResultSetToSuiviMedical(rs, historique);
                suivisMedicaux.add(suivi);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return suivisMedicaux;
    }

    @Override
    public suivie_medical getone() {
        String req = "SELECT * FROM chrono.suivie_medical ORDER BY id DESC LIMIT 1";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            if (rs.next()) {
                return mapResultSetToSuiviMedical(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Helper methods to reduce code duplication

    /**
     * Sets parameters for PreparedStatement with data from suivie_medical object
     */
    private void setStatementParameters(PreparedStatement stm, suivie_medical suivi) throws SQLException {
        stm.setString(1, suivi.getDate());
        stm.setString(2, suivi.getCommentaire());
        stm.setInt(3, suivi.getHistorique().getId());
    }

    /**
     * Validates if historique object is not null
     */
    private void validateHistorique(suivie_medical suivi) {
        if (suivi.getHistorique() == null) {
            throw new RuntimeException("L'historique de traitement associé ne peut pas être null");
        }
    }

    /**
     * Maps ResultSet to suivie_medical object
     */
    private suivie_medical mapResultSetToSuiviMedical(ResultSet rs) throws SQLException {
        suivie_medical suivi = new suivie_medical();
        suivi.setId(rs.getInt("id"));
        suivi.setDate(rs.getString("date"));
        suivi.setCommentaire(rs.getString("commentaire"));

        // Récupérer l'objet historique complet
        int historiqueId = rs.getInt("id_historique_id");
        historique_traitement historique = hisServices.find(historiqueId);
        suivi.setHistorique(historique);

        return suivi;
    }

    /**
     * Maps ResultSet to suivie_medical object with a provided historique
     */
    private suivie_medical mapResultSetToSuiviMedical(ResultSet rs, historique_traitement historique) throws SQLException {
        suivie_medical suivi = new suivie_medical();
        suivi.setId(rs.getInt("id"));
        suivi.setDate(rs.getString("date"));
        suivi.setCommentaire(rs.getString("commentaire"));
        suivi.setHistorique(historique);

        return suivi;
    }
}