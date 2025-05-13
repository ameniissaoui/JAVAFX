package org.example.services;

import org.example.models.Panier;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierServices implements IServices<Panier> {
    private Connection cnx;

    public PanierServices() {
        MaConnexion dbInstance = MaConnexion.getInstance();
        if (dbInstance.isConnected()) {
            cnx = dbInstance.getCnx();
            System.out.println("Database connection established in PanierServices");
        } else {
            System.err.println("Error: Database connection is null in PanierServices!");
        }
    }

    @Override
    public void addProduit(Panier panier) {
        if (cnx == null) {
            throw new RuntimeException("Database connection is null");
        }

        String sql = "INSERT INTO finale_panier (nom, prenom, adresse, quantite) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, panier.getNom());
            pstmt.setString(2, panier.getPrenom());
            pstmt.setString(3, panier.getAdresse());
            pstmt.setInt(4, panier.getQuantite());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                panier.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding panier: " + e.getMessage());
        }
    }

    @Override
    public void removeProduit(Panier panier) {
        if (cnx == null) {
            throw new RuntimeException("Database connection is null");
        }

        String sql = "DELETE FROM finale_panier WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setInt(1, panier.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error removing panier: " + e.getMessage());
        }
    }

    @Override
    public void editProduit(Panier panier) {
        if (cnx == null) {
            throw new RuntimeException("Database connection is null");
        }

        String sql = "UPDATE finale_panier SET nom = ?, prenom = ?, adresse = ?, quantite = ? WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setString(1, panier.getNom());
            pstmt.setString(2, panier.getPrenom());
            pstmt.setString(3, panier.getAdresse());
            pstmt.setInt(4, panier.getQuantite());
            pstmt.setInt(5, panier.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating panier: " + e.getMessage());
        }
    }

    @Override
    public List<Panier> showProduit() {
        List<Panier> panierList = new ArrayList<>();
        if (cnx == null) {
            throw new RuntimeException("Database connection is null");
        }

        String sql = "SELECT * FROM finale_panier";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Panier panier = new Panier();
                panier.setId(rs.getInt("id"));
                panier.setNom(rs.getString("nom"));
                panier.setPrenom(rs.getString("prenom"));
                panier.setAdresse(rs.getString("adresse"));
                panier.setQuantite(rs.getInt("quantite"));
                panierList.add(panier);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching panier items: " + e.getMessage());
        }
        return panierList;
    }

    @Override
    public Panier getoneProduit(int id) {
        if (cnx == null) {
            throw new RuntimeException("Database connection is null");
        }

        String sql = "SELECT * FROM finale_panier WHERE id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Panier panier = new Panier();
                panier.setId(rs.getInt("id"));
                panier.setNom(rs.getString("nom"));
                panier.setPrenom(rs.getString("prenom"));
                panier.setAdresse(rs.getString("adresse"));
                panier.setQuantite(rs.getInt("quantite"));
                return panier;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching panier item: " + e.getMessage());
        }
        return null;
    }
}