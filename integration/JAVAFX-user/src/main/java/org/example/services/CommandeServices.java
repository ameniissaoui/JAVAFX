package org.example.services;

import org.example.util.MaConnexion;
import org.example.models.Commande;
import org.example.models.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeServices implements IServices<Commande> {
    Connection cnx;

    public CommandeServices() {
        MaConnexion dbInstance = MaConnexion.getInstance();
        if (dbInstance.isConnected()) {
            cnx = dbInstance.getCnx();
            System.out.println("Database connection established in CommandeServices");
        } else {
            System.err.println("Error: Database connection is null in CommandeServices!");
        }
    }

    @Override
    public void addProduit(Commande commande) {
        if (cnx == null) {
            System.err.println("Cannot add commande: Database connection is null");
            throw new RuntimeException("Database connection is null");
        }

        System.out.println("Adding commande: " + commande.getNom() + " " + commande.getPrenom());

        String reqCommande = "INSERT INTO chrono.commande(nom, prenom, email, adresse, phone_number, stripe_session_id, payment_status, total_amount, utilisateur_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmtCommande = cnx.prepareStatement(reqCommande, Statement.RETURN_GENERATED_KEYS)) {
            stmtCommande.setString(1, commande.getNom());
            stmtCommande.setString(2, commande.getPrenom());
            stmtCommande.setString(3, commande.getEmail());
            stmtCommande.setString(4, commande.getAdresse());
            stmtCommande.setInt(5, commande.getPhone_number());
            stmtCommande.setString(6, commande.getStripeSessionId() != null ? commande.getStripeSessionId() : null);
            stmtCommande.setString(7, commande.getPaymentStatus());
            stmtCommande.setDouble(8, commande.getTotalAmount());
            stmtCommande.setInt(9, commande.getUtilisateurId());

            int rowsInserted = stmtCommande.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Commande inserted successfully into database");
            } else {
                throw new SQLException("Failed to insert commande");
            }

            ResultSet generatedKeys = stmtCommande.getGeneratedKeys();
            if (generatedKeys.next()) {
                int commandeId = generatedKeys.getInt(1);
                commande.setId(commandeId);
                System.out.println("Commande created with ID: " + commandeId);

                if (commande.getProduit() != null) {
                    associateProductWithCommande(commande.getProduit(), commandeId);
                } else {
                    System.out.println("No product associated with commande ID=" + commandeId);
                }
            } else {
                throw new SQLException("Failed to get commande ID after insertion");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error adding commande: " + e.getMessage());
            throw new RuntimeException("Error adding commande: " + e.getMessage());
        }
    }

    private void associateProductWithCommande(Produit produit, int commandeId) throws SQLException {
        String checkStockQuery = "SELECT stock_quantite FROM chrono.produit WHERE id = ?";
        try (PreparedStatement checkStockStmt = cnx.prepareStatement(checkStockQuery)) {
            checkStockStmt.setInt(1, produit.getId());
            ResultSet rs = checkStockStmt.executeQuery();

            if (rs.next()) {
                int stockQuantite = rs.getInt("stock_quantite");
                if (stockQuantite <= 0) {
                    throw new RuntimeException("Product is out of stock: " + produit.getNom());
                }
            } else {
                throw new RuntimeException("Product not found in database: ID=" + produit.getId());
            }
        }

        String reqCartItem = "INSERT INTO chrono.cart_item(produit_id, commande_id, quantite) VALUES (?, ?, ?)";
        try (PreparedStatement stmtCartItem = cnx.prepareStatement(reqCartItem)) {
            stmtCartItem.setInt(1, produit.getId());
            stmtCartItem.setInt(2, commandeId);
            stmtCartItem.setInt(3, 1);

            int rowsInserted = stmtCartItem.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("CartItem added successfully: commande_id=" + commandeId + ", produit_id=" + produit.getId());
                updateProductStock(produit.getId());
            } else {
                System.out.println("Failed to add cart_item entry");
            }
        }
    }

    private void updateProductStock(int productId) throws SQLException {
        String reqUpdateStock = "UPDATE chrono.produit SET stock_quantite = stock_quantite - 1 WHERE id = ?";
        try (PreparedStatement stmtUpdateStock = cnx.prepareStatement(reqUpdateStock)) {
            stmtUpdateStock.setInt(1, productId);
            int rowsUpdated = stmtUpdateStock.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Product stock updated for product ID=" + productId);
            } else {
                System.out.println("Failed to update product stock for product ID=" + productId);
            }
        }
    }

    @Override
    public void removeProduit(Commande commande) {
        if (cnx == null) {
            System.err.println("Cannot remove commande: Database connection is null");
            throw new RuntimeException("Database connection is null");
        }

        String req = "DELETE FROM chrono.commande WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setInt(1, commande.getId());
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Commande deleted successfully: ID=" + commande.getId());
            } else {
                System.out.println("No commande found with ID: " + commande.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error deleting commande: " + e.getMessage());
            throw new RuntimeException("Error deleting commande: " + e.getMessage());
        }
    }

    @Override
    public void editProduit(Commande commande) {
        if (cnx == null) {
            System.err.println("Cannot edit commande: Database connection is null");
            throw new RuntimeException("Database connection is null");
        }

        String req = "UPDATE chrono.commande SET nom=?, prenom=?, email=?, adresse=?, phone_number=?, stripe_session_id=?, payment_status=?, total_amount=?, utilisateur_id=? WHERE id=?";
        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setString(1, commande.getNom());
            stmt.setString(2, commande.getPrenom());
            stmt.setString(3, commande.getEmail());
            stmt.setString(4, commande.getAdresse());
            stmt.setInt(5, commande.getPhone_number());
            stmt.setString(6, commande.getStripeSessionId());
            stmt.setString(7, commande.getPaymentStatus());
            stmt.setDouble(8, commande.getTotalAmount());
            stmt.setInt(9, commande.getUtilisateurId());
            stmt.setInt(10, commande.getId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Commande updated successfully: ID=" + commande.getId());
            } else {
                System.out.println("No commande found with ID: " + commande.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error updating commande: " + e.getMessage());
            throw new RuntimeException("Error updating commande: " + e.getMessage());
        }
    }

    @Override
    public List<Commande> showProduit() {
        if (cnx == null) {
            System.err.println("Cannot fetch commandes: Database connection is null");
            throw new RuntimeException("Database connection is null");
        }

        List<Commande> commandes = new ArrayList<>();
        String req = "SELECT * FROM chrono.commande";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(req)) {
            while (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setAdresse(rs.getString("adresse"));
                c.setPhone_number(rs.getInt("phone_number"));
                c.setStripeSessionId(rs.getString("stripe_session_id"));
                c.setPaymentStatus(rs.getString("payment_status"));
                c.setTotalAmount(rs.getDouble("total_amount"));
                c.setUtilisateurId(rs.getInt("utilisateur_id"));
                commandes.add(c);
            }
            System.out.println("Fetched " + commandes.size() + " commandes from database");
        } catch (SQLException e) {
            System.err.println("Error fetching commandes: " + e.getMessage());
            throw new RuntimeException("Error fetching commandes: " + e.getMessage());
        }
        return commandes;
    }

    @Override
    public Commande getoneProduit(int id) {
        if (cnx == null) {
            System.err.println("Cannot fetch commande: Database connection is null");
            throw new RuntimeException("Database connection is null");
        }

        String req = "SELECT * FROM chrono.commande WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setAdresse(rs.getString("adresse"));
                c.setPhone_number(rs.getInt("phone_number"));
                c.setStripeSessionId(rs.getString("stripe_session_id"));
                c.setPaymentStatus(rs.getString("payment_status"));
                c.setTotalAmount(rs.getDouble("total_amount"));
                c.setUtilisateurId(rs.getInt("utilisateur_id"));
                System.out.println("Fetched commande with ID: " + id);
                return c;
            } else {
                System.out.println("No commande found with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving commande: " + e.getMessage());
            throw new RuntimeException("Error retrieving commande: " + e.getMessage());
        }
        return null;
    }

    public Commande getCommandeByStripeSessionId(String stripeSessionId) {
        if (cnx == null) {
            System.err.println("Cannot fetch commande by stripe_session_id: Database connection is null");
            throw new RuntimeException("Database connection is null");
        }

        String req = "SELECT * FROM chrono.commande WHERE stripe_session_id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setString(1, stripeSessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setAdresse(rs.getString("adresse"));
                c.setPhone_number(rs.getInt("phone_number"));
                c.setStripeSessionId(rs.getString("stripe_session_id"));
                c.setPaymentStatus(rs.getString("payment_status"));
                c.setTotalAmount(rs.getDouble("total_amount"));
                c.setUtilisateurId(rs.getInt("utilisateur_id"));
                System.out.println("Fetched commande with stripe_session_id: " + stripeSessionId);
                return c;
            } else {
                System.out.println("No commande found with stripe_session_id: " + stripeSessionId);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving commande by stripe_session_id: " + e.getMessage());
            throw new RuntimeException("Error retrieving commande by stripe_session_id: " + e.getMessage());
        }
        return null;
    }
}