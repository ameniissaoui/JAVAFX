package org.example.services;
import org.example.interfaces.Iservices;
import org.example.util.MaConnexion;
import org.example.models.historique_traitement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HisServices implements Iservices<historique_traitement> {
    Connection cnx;

    public HisServices() {
        // S'assurer que l'instance est créée avant d'accéder à cnx
        cnx = MaConnexion.getInstance().getCnx();
    }

    @Override
    public void add(historique_traitement historiqueTraitement) {
        String req="INSERT INTO chrono.historique_traitement (nom, prenom, maladie, description, type_traitement, bilan) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement stm=cnx.prepareStatement(req);
            stm.setString(1, historiqueTraitement.getNom());
            stm.setString(2, historiqueTraitement.getPrenom());
            stm.setString(3,historiqueTraitement.getMaladies());
            stm.setString(4, historiqueTraitement.getDescription());
            stm.setString(5, historiqueTraitement.getType_traitement());
            stm.setString(6, historiqueTraitement.getBilan());
            stm.executeUpdate();
        } catch (SQLException e) {
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
        String req = "SELECT * FROM chrono.historique_traitement WHERE id = ?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                historique_traitement h = new historique_traitement();
                h.setId(rs.getInt("id"));
                h.setNom(rs.getString("nom"));
                h.setPrenom(rs.getString("prenom"));
                h.setMaladies(rs.getString("maladie"));
                h.setDescription(rs.getString("description"));
                h.setType_traitement(rs.getString("type_traitement"));
                h.setBilan(rs.getString("bilan"));
                return h;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<historique_traitement> afficher() {
        List<historique_traitement> historiqueTraitements = new ArrayList<>();
        String req = "SELECT * FROM chrono.historique_traitement";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                historique_traitement historiqueTraitement = new historique_traitement();
                historiqueTraitement.setId(rs.getInt(1));
                historiqueTraitement.setNom(rs.getString("nom"));
                historiqueTraitement.setPrenom(rs.getString("prenom"));
                historiqueTraitement.setMaladies(rs.getString("maladie"));
                historiqueTraitement.setDescription(rs.getString("description"));
                historiqueTraitement.setType_traitement(rs.getString("type_traitement"));
                historiqueTraitement.setBilan(rs.getString("bilan"));
                historiqueTraitements.add(historiqueTraitement);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return historiqueTraitements;
    }

    @Override
    public historique_traitement getone() {
        String req = "SELECT * FROM chrono.historique_traitement ORDER BY id DESC LIMIT 1";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            if (rs.next()) {
                historique_traitement ht = new historique_traitement();
                ht.setId(rs.getInt("id"));
                ht.setNom(rs.getString("nom"));
                ht.setPrenom(rs.getString("prenom"));
                ht.setMaladies(rs.getString("maladie"));
                ht.setDescription(rs.getString("description"));
                ht.setType_traitement(rs.getString("type_traitement"));
                ht.setBilan(rs.getString("bilan"));
                return ht;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}