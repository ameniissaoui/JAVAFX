package org.example.services;

import org.example.util.MaConnexion;
import org.example.models.Commande;
import org.example.models.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeServices implements IServices <Commande> {
    Connection cnx;
    public CommandeServices() {
        MaConnexion dbInstance = MaConnexion.getInstance();
        if (dbInstance.isConnected()) {
            cnx = dbInstance.getCnx();
        } else {
            System.err.println("Error: Database connection is null in CommandeServices!");
            // You might want to throw an exception here, or handle it differently
        }
    }

    @Override
    public void addProduit(Commande commande) {
        if (cnx == null) {
            System.err.println("Cannot add commande: Database connection is null");
            return;
        }

        // First insert the commande
        String reqCommande = "INSERT INTO chrono.commande(nom, prenom, email, adresse, phone_number) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmtCommande = cnx.prepareStatement(reqCommande, Statement.RETURN_GENERATED_KEYS)) {
            stmtCommande.setString(1, commande.getNom());
            stmtCommande.setString(2, commande.getPrenom());
            stmtCommande.setString(3, commande.getEmail());
            stmtCommande.setString(4, commande.getAdresse());
            stmtCommande.setInt(5, commande.getPhone_number());

            stmtCommande.executeUpdate();

            // Get the generated commande ID
            ResultSet generatedKeys = stmtCommande.getGeneratedKeys();
            if (generatedKeys.next()) {
                int commandeId = generatedKeys.getInt(1);
                commande.setId(commandeId);

                // Now insert the commande_produit association if a product is assigned
                if (commande.getProduit() != null) {
                    // Assuming you have a commande_produit table to store the many-to-many relationship
                    String reqCommandeProduit = "INSERT INTO chrono.commande_produit(commande_id, produit_id) VALUES (?, ?)";
                    try (PreparedStatement stmtCommandeProduit = cnx.prepareStatement(reqCommandeProduit)) {
                        stmtCommandeProduit.setInt(1, commandeId);
                        stmtCommandeProduit.setInt(2, commande.getProduit().getId());
                        stmtCommandeProduit.executeUpdate();
                    }

                    // Update the product stock quantity
                    String reqUpdateStock = "UPDATE chrono.produit SET stock_quantite = stock_quantite - 1 WHERE id = ?";
                    try (PreparedStatement stmtUpdateStock = cnx.prepareStatement(reqUpdateStock)) {
                        stmtUpdateStock.setInt(1, commande.getProduit().getId());
                        stmtUpdateStock.executeUpdate();
                    }
                }
            }

            System.out.println("Commande added successfully!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeProduit(Commande commande) {
        // First delete associated entries in commande_produit
        String reqDeleteAssociation = "DELETE FROM commande_produit WHERE commande_id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(reqDeleteAssociation)) {
            stmt.setInt(1, commande.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting commande_produit associations: " + e.getMessage());
        }

        // Then delete the commande
        String req = "DELETE FROM commande WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setInt(1, commande.getId());
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Commande deleted successfully!");
            } else {
                System.out.println("No commande found with ID: " + commande.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error deleting commande: " + e.getMessage());
        }
    }

    @Override
    public void editProduit(Commande commande) {
        String req = "UPDATE commande SET nom=?, prenom=?, email=?, adresse=?, phone_number=? WHERE id=?";

        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setString(1, commande.getNom());
            stmt.setString(2, commande.getPrenom());
            stmt.setString(3, commande.getEmail());
            stmt.setString(4, commande.getAdresse());
            stmt.setInt(5, commande.getPhone_number());
            stmt.setInt(6, commande.getId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                // If commande is updated successfully, update the product association
                if (commande.getProduit() != null) {
                    // First delete existing associations
                    String reqDeleteAssociation = "DELETE FROM commande_produit WHERE commande_id = ?";
                    try (PreparedStatement stmtDelete = cnx.prepareStatement(reqDeleteAssociation)) {
                        stmtDelete.setInt(1, commande.getId());
                        stmtDelete.executeUpdate();
                    }

                    // Then insert the new association
                    String reqCommandeProduit = "INSERT INTO commande_produit(commande_id, produit_id) VALUES (?, ?)";
                    try (PreparedStatement stmtCommandeProduit = cnx.prepareStatement(reqCommandeProduit)) {
                        stmtCommandeProduit.setInt(1, commande.getId());
                        stmtCommandeProduit.setInt(2, commande.getProduit().getId());
                        stmtCommandeProduit.executeUpdate();
                    }
                }
                System.out.println("Commande updated successfully!");
            } else {
                System.out.println("No commande found with ID: " + commande.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error updating commande: " + e.getMessage());
        }
    }

    @Override
    public List<Commande> showProduit() {
        List<Commande> commandes = new ArrayList<>();

        String req = "SELECT c.*, cp.produit_id FROM chrono.commande c " +
                "LEFT JOIN chrono.commande_produit cp ON c.id = cp.commande_id";
        try {
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(req);
            while (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setAdresse(rs.getString("adresse"));
                c.setPhone_number(rs.getInt("phone_number"));

                // Get associated product if exists
                int produitId = rs.getInt("produit_id");
                if (!rs.wasNull()) {
                    // Get the product details
                    ProduitServices produitServices = new ProduitServices();
                    Produit produit = produitServices.getoneProduit(produitId);
                    c.setProduit(produit);
                }

                commandes.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(commandes);
        return commandes;
    }

    @Override
    public Commande getoneProduit(int id) {
        String req = "SELECT c.*, cp.produit_id FROM chrono.commande c " +
                "LEFT JOIN chrono.commande_produit cp ON c.id = cp.commande_id " +
                "WHERE c.id = ?";
        Commande c = null;

        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                c = new Commande();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setAdresse(rs.getString("adresse"));
                c.setPhone_number(rs.getInt("phone_number"));

                // Get associated product if exists
                int produitId = rs.getInt("produit_id");
                if (!rs.wasNull()) {
                    // Get the product details
                    ProduitServices produitServices = new ProduitServices();
                    Produit produit = produitServices.getoneProduit(produitId);
                    c.setProduit(produit);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving commande: " + e.getMessage());
        }
        return c;
    }
}