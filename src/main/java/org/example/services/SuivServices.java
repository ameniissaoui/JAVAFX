package org.example.services;

import org.example.interfaces.Iservices;
import org.example.util.MaConnexion;
import org.example.models.historique_traitement;
import org.example.models.suivie_medical;
import org.example.models.Medecin; // Importer Medecin

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
        Connection localCnx = null;
        PreparedStatement stm = null;

        try {
            // Obtenir une nouvelle connexion ou utiliser la connexion existante
            localCnx = cnx != null ? cnx : MaConnexion.getInstance().getCnx();

            // Désactiver auto-commit pour gérer la transaction manuellement
            localCnx.setAutoCommit(false);

            // Valider les données avant insertion
            if (suivi.getHistorique() == null || suivi.getHistorique().getId() <= 0) {
                throw new RuntimeException("L'ID de l'historique de traitement est invalide: " +
                        (suivi.getHistorique() != null ? suivi.getHistorique().getId() : "null"));
            }

            if (suivi.getUser() == null || suivi.getUser().getId() <= 0) {
                throw new RuntimeException("L'ID de l'utilisateur (médecin) est invalide: " +
                        (suivi.getUser() != null ? suivi.getUser().getId() : "null"));
            }

            // IMPORTANT: Changed column name from 'id_historique' to 'id_historique_id'
            String req = "INSERT INTO finalee.suivie_medical (date, commentaire, id_historique_id, utilisateur_id) VALUES (?, ?, ?, ?)";
            stm = localCnx.prepareStatement(req);

            // Logger les valeurs pour le débogage
            System.out.println("Insertion du suivi: " +
                    "date=" + suivi.getDate() +
                    ", commentaire=" + suivi.getCommentaire() +
                    ", id_historique_id=" + suivi.getHistorique().getId() +
                    ", utilisateur_id=" + suivi.getUser().getId());

            // Paramétrer la requête
            stm.setString(1, suivi.getDate());
            stm.setString(2, suivi.getCommentaire());

            // Using correct column name 'id_historique_id'
            stm.setInt(3, suivi.getHistorique().getId());
            stm.setInt(4, suivi.getUser().getId());

            // Exécuter la requête
            int rowsAffected = stm.executeUpdate();
            System.out.println("Nombre de lignes affectées: " + rowsAffected);

            if (rowsAffected > 0) {
                // Valider la transaction
                localCnx.commit();
                System.out.println("Suivi médical ajouté avec succès dans la base de données.");
            } else {
                // Annuler la transaction si aucune ligne n'est affectée
                localCnx.rollback();
                throw new RuntimeException("Échec de l'insertion du suivi médical : aucune ligne modifiée.");
            }

        } catch (SQLException e) {
            try {
                // Annuler la transaction en cas d'erreur
                if (localCnx != null) {
                    localCnx.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }

            System.err.println("Erreur SQL lors de l'ajout du suivi: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur SQL lors de l'ajout du suivi: " + e.getMessage(), e);
        } finally {
            try {
                // Rétablir l'auto-commit et fermer les ressources
                if (localCnx != null && localCnx != cnx) {
                    localCnx.setAutoCommit(true);
                    localCnx.close();
                }
                if (stm != null) {
                    stm.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
            }
        }
    }

    @Override
    public void update(suivie_medical suivi) {
        String req = "UPDATE finalee.suivie_medical SET date=?, commentaire=?, id_historique_id=?, utilisateur_id=? WHERE id=?";
        try {
            validateHistorique(suivi);
            validateUser(suivi);
            PreparedStatement stm = cnx.prepareStatement(req);
            setStatementParameters(stm, suivi);
            stm.setInt(5, suivi.getId());
            stm.executeUpdate();
            System.out.println("Suivi médical mis à jour avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(suivie_medical suivi) {
        String req = "DELETE FROM finalee.suivie_medical WHERE id=?";
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
        String req = "SELECT sm.*, u.*, m.* FROM finalee.suivie_medical sm " +
                "JOIN finalee.utilisateur u ON sm.utilisateur_id = u.id " +
                "LEFT JOIN finalee.medecin m ON u.id = m.utilisateur_id " +
                "WHERE sm.id = ? AND u.role = 'MEDECIN'";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                int historiqueId = rs.getInt("id_historique_id");
                historique_traitement historique = hisServices.find(historiqueId);
                return mapResultSetToSuiviMedical(rs, historique);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<suivie_medical> afficher() {
        List<suivie_medical> suivisMedicaux = new ArrayList<>();
        String req = "SELECT sm.*, u.*, m.* FROM finalee.suivie_medical sm " +
                "JOIN finalee.utilisateur u ON sm.utilisateur_id = u.id " +
                "LEFT JOIN finalee.medecin m ON u.id = m.utilisateur_id " +
                "WHERE u.role = 'MEDECIN'";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                int historiqueId = rs.getInt("id_historique_id");
                historique_traitement historique = hisServices.find(historiqueId);
                suivisMedicaux.add(mapResultSetToSuiviMedical(rs, historique));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return suivisMedicaux;
    }

    public List<suivie_medical> getSuiviByHistoriqueId(int historiqueId) {
        List<suivie_medical> suivisMedicaux = new ArrayList<>();
        String req = "SELECT sm.*, u.*, m.* FROM finalee.suivie_medical sm " +
                "JOIN finalee.utilisateur u ON sm.utilisateur_id = u.id " +
                "LEFT JOIN finalee.medecin m ON u.id = m.utilisateur_id " +
                "WHERE sm.id_historique_id = ? AND u.role = 'MEDECIN'";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, historiqueId);
            ResultSet rs = stm.executeQuery();

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
        String req = "SELECT sm.*, u.*, m.* FROM finalee.suivie_medical sm " +
                "JOIN finalee.utilisateur u ON sm.utilisateur_id = u.id " +
                "LEFT JOIN finalee.medecin m ON u.id = m.utilisateur_id " +
                "WHERE u.role = 'MEDECIN' ORDER BY sm.id DESC LIMIT 1";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            if (rs.next()) {
                int historiqueId = rs.getInt("id_historique_id");
                historique_traitement historique = hisServices.find(historiqueId);
                return mapResultSetToSuiviMedical(rs, historique);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Méthodes helper
    private void setStatementParameters(PreparedStatement stm, suivie_medical suivi) throws SQLException {
        stm.setString(1, suivi.getDate());
        stm.setString(2, suivi.getCommentaire());

        // Handle id_historique_id more explicitly
        if (suivi.getHistorique() == null || suivi.getHistorique().getId() <= 0) {
            throw new SQLException("L'historique n'a pas un ID valide");
        }
        stm.setInt(3, suivi.getHistorique().getId());

        // Handle utilisateur_id more explicitly
        if (suivi.getUser() == null || suivi.getUser().getId() <= 0) {
            throw new SQLException("L'utilisateur n'a pas un ID valide");
        }
        stm.setInt(4, suivi.getUser().getId());

        // Debug output
        System.out.println("Parameters set: date=" + suivi.getDate() +
                ", commentaire=" + (suivi.getCommentaire() != null ?
                (suivi.getCommentaire().length() > 20 ?
                        suivi.getCommentaire().substring(0, 20) + "..." :
                        suivi.getCommentaire()) :
                "null") +
                ", historique_id=" + suivi.getHistorique().getId() +
                ", utilisateur_id=" + suivi.getUser().getId());
    }
    private void validateHistorique(suivie_medical suivi) {
        if (suivi.getHistorique() == null) {
            throw new RuntimeException("L'historique de traitement associé ne peut pas être null");
        }
    }

    private void validateUser(suivie_medical suivi) {
        if (suivi.getUser() == null) {
            throw new RuntimeException("L'utilisateur (médecin) associé ne peut pas être null");
        }
        if (!"MEDECIN".equalsIgnoreCase(suivi.getUser().getRole())) {
            throw new RuntimeException("L'utilisateur associé doit être un médecin");
        }
    }

    private suivie_medical mapResultSetToSuiviMedical(ResultSet rs, historique_traitement historique) throws SQLException {
        suivie_medical suivi = new suivie_medical();
        suivi.setId(rs.getInt("id"));
        suivi.setDate(rs.getString("date"));
        suivi.setCommentaire(rs.getString("commentaire"));
        suivi.setHistorique(historique);

        // Charger l'utilisateur (médecin) en utilisant la classe Medecin
        Medecin medecin = new Medecin();
        medecin.setId(rs.getInt("utilisateur_id"));
        medecin.setNom(rs.getString("utilisateur.nom"));
        medecin.setPrenom(rs.getString("utilisateur.prenom"));
        medecin.setEmail(rs.getString("utilisateur.email"));
        medecin.setMotDePasse(rs.getString("utilisateur.motDePasse"));
        medecin.setDateNaissance(rs.getDate("utilisateur.dateNaissance"));
        medecin.setTelephone(rs.getString("utilisateur.telephone"));
        medecin.setBanned(rs.getBoolean("utilisateur.banned"));
        medecin.setImage(rs.getString("utilisateur.image"));
        medecin.setRole(rs.getString("utilisateur.role"));
        // Charger les champs spécifiques à Medecin depuis finalee.medecin
        medecin.setSpecialite(rs.getString("utilisateur.specialite"));
        medecin.setDiploma(rs.getString("utilisateur.diploma"));
        medecin.setIs_verified(rs.getBoolean("utilisateur.is_verified"));
        suivi.setUser(medecin);

        return suivi;
    }

    // Surcharge pour la compatibilité avec l'existant
    private suivie_medical mapResultSetToSuiviMedical(ResultSet rs) throws SQLException {
        int historiqueId = rs.getInt("id_historique_id");
        historique_traitement historique = hisServices.find(historiqueId);
        return mapResultSetToSuiviMedical(rs, historique);
    }

}