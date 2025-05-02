package org.example.services;

import org.example.interfaces.Iservices;
import org.example.util.MaConnexion;
import org.example.models.historique_traitement;
import org.example.models.Patient; // Importer Patient

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HisServices implements Iservices<historique_traitement> {
    Connection cnx;

    public HisServices() {
        cnx = MaConnexion.getInstance().getCnx();
    }

    public List<historique_traitement> getHistoriquesByPatientId(int patientId) {
        List<historique_traitement> list = new ArrayList<>();
        String req = "SELECT ht.*, u.* FROM chrono.historique_traitement ht " +
                "JOIN chrono.user u ON ht.user_id = u.id " +
                "WHERE ht.user_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, patientId);
            System.out.println("Exécution de la requête: " + req + " avec patientId=" + patientId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                historique_traitement h = mapResultSetToHistoriqueTraitement(rs);
                list.add(h);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    @Override
    public void add(historique_traitement historiqueTraitement) {
        String req = "INSERT INTO chrono.historique_traitement (nom, prenom, maladie, description, type_traitement, bilan, user_id) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, historiqueTraitement.getNom());
            stm.setString(2, historiqueTraitement.getPrenom());
            stm.setString(3, historiqueTraitement.getMaladies());
            stm.setString(4, historiqueTraitement.getDescription());
            stm.setString(5, historiqueTraitement.getType_traitement());
            stm.setString(6, historiqueTraitement.getBilan());

            if (historiqueTraitement.getUser() != null) {
                System.out.println("Setting user_id: " + historiqueTraitement.getUser().getId());
                stm.setInt(7, historiqueTraitement.getUser().getId());
            } else {
                System.out.println("User is null, setting null for user_id");
                stm.setNull(7, java.sql.Types.INTEGER);
            }

            int result = stm.executeUpdate();
            System.out.println("Rows affected: " + result);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(historique_traitement historiqueTraitement) {
        String req = "UPDATE chrono.historique_traitement SET nom=?, prenom=?, maladie=?, description=?, type_traitement=?, bilan=? WHERE id=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, historiqueTraitement.getNom());
            stm.setString(2, historiqueTraitement.getPrenom());
            stm.setString(3, historiqueTraitement.getMaladies());
            stm.setString(4, historiqueTraitement.getDescription());
            stm.setString(5, historiqueTraitement.getType_traitement());
            stm.setString(6, historiqueTraitement.getBilan());
            stm.setInt(7, historiqueTraitement.getId());
            stm.executeUpdate();
            System.out.println("Historique mis à jour avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(historique_traitement historiqueTraitement) {
        String req = "DELETE FROM chrono.historique_traitement WHERE id=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, historiqueTraitement.getId());
            stm.executeUpdate();
            System.out.println("Historique supprimé avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public historique_traitement find(int id) {
        String req = "SELECT ht.*, u.* FROM chrono.historique_traitement ht " +
                "JOIN chrono.user u ON ht.user_id = u.id " +
                "WHERE ht.id = ? AND u.role = 'PATIENT'";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return mapResultSetToHistoriqueTraitement(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<historique_traitement> afficher() {
        List<historique_traitement> historiqueTraitements = new ArrayList<>();
        String req = "SELECT ht.*, u.* FROM chrono.historique_traitement ht " +
                "JOIN chrono.user u ON ht.user_id = u.id " +
                "WHERE u.role = 'PATIENT'";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                historiqueTraitements.add(mapResultSetToHistoriqueTraitement(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return historiqueTraitements;
    }

    @Override
    public historique_traitement getone() {
        String req = "SELECT ht.*, u.* FROM chrono.historique_traitement ht " +
                "JOIN chrono.user u ON ht.user_id = u.id " +
                "WHERE u.role = 'PATIENT' ORDER BY ht.id DESC LIMIT 1";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            if (rs.next()) {
                return mapResultSetToHistoriqueTraitement(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Méthode helper pour mapper ResultSet à historique_traitement
    private historique_traitement mapResultSetToHistoriqueTraitement(ResultSet rs) throws SQLException {
        historique_traitement h = new historique_traitement();
        h.setId(rs.getInt("id"));
        h.setNom(rs.getString("nom"));
        h.setPrenom(rs.getString("prenom"));
        h.setMaladies(rs.getString("maladie"));
        h.setDescription(rs.getString("description"));
        h.setType_traitement(rs.getString("type_traitement"));
        h.setBilan(rs.getString("bilan"));

        // Charger l'utilisateur (patient) en utilisant la classe Patient
        Patient patient = new Patient();
        patient.setId(rs.getInt("user_id"));
        patient.setNom(rs.getString("u.nom"));
        patient.setPrenom(rs.getString("u.prenom"));
        patient.setEmail(rs.getString("u.email"));
        patient.setMotDePasse(rs.getString("u.motDePasse"));
        patient.setDateNaissance(rs.getDate("u.dateNaissance"));
        patient.setTelephone(rs.getString("u.telephone"));
        patient.setBanned(rs.getBoolean("u.banned"));
        patient.setImage(rs.getString("u.image"));
        patient.setRole(rs.getString("u.role"));
        h.setUser(patient);

        return h;
    }
}