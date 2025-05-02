package org.example.services;

import org.example.models.CartItem;
import org.example.models.Panier;
import org.example.models.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartItemServices implements IServices<CartItem> {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/chrono";
    private static final String USER = "root";
    private static final String PASS = "";
    private ProduitServices produitServices;
    private PanierServices panierServices;

    public CartItemServices() {
        produitServices = new ProduitServices();
        panierServices = new PanierServices();
    }

    @Override
    public void addProduit(CartItem cartItem) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO cart_item (produit_id, commande_id, panier_id, quantite, utilisateur_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, cartItem.getProduitId());
            if (cartItem.getCommandeId() == 0) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, cartItem.getCommandeId());
            }
            if (cartItem.getPanierId() == 0) {
                pstmt.setNull(3, Types.INTEGER);
            } else {
                pstmt.setInt(3, cartItem.getPanierId());
            }
            pstmt.setInt(4, cartItem.getQuantite());
            if (cartItem.getUtilisateurId() == 0) {
                pstmt.setNull(5, Types.INTEGER);
            } else {
                pstmt.setInt(5, cartItem.getUtilisateurId());
            }
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                cartItem.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding cart item: " + e.getMessage());
        }
    }

    @Override
    public void removeProduit(CartItem cartItem) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "DELETE FROM cart_item WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartItem.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error removing cart item: " + e.getMessage());
        }
    }

    @Override
    public void editProduit(CartItem cartItem) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "UPDATE cart_item SET produit_id = ?, commande_id = ?, panier_id = ?, quantite = ?, utilisateur_id = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartItem.getProduitId());
            if (cartItem.getCommandeId() == 0) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, cartItem.getCommandeId());
            }
            if (cartItem.getPanierId() == 0) {
                pstmt.setNull(3, Types.INTEGER);
            } else {
                pstmt.setInt(3, cartItem.getPanierId());
            }
            pstmt.setInt(4, cartItem.getQuantite());
            if (cartItem.getUtilisateurId() == 0) {
                pstmt.setNull(5, Types.INTEGER);
            } else {
                pstmt.setInt(5, cartItem.getUtilisateurId());
            }
            pstmt.setInt(6, cartItem.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating cart item: " + e.getMessage());
        }
    }

    public void updateCommandeIdForCartItems(int newCommandeId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "UPDATE cart_item SET commande_id = ? WHERE commande_id IS NULL";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newCommandeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating commande_id for cart items: " + e.getMessage());
        }
    }

    @Override
    public List<CartItem> showProduit() {
        List<CartItem> cartItems = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT * FROM cart_item";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                CartItem cartItem = new CartItem();
                cartItem.setId(rs.getInt("id"));
                cartItem.setProduitId(rs.getInt("produit_id"));
                int commandeId = rs.getInt("commande_id");
                if (rs.wasNull()) {
                    cartItem.setCommandeId(0);
                } else {
                    cartItem.setCommandeId(commandeId);
                }
                int panierId = rs.getInt("panier_id");
                if (rs.wasNull()) {
                    cartItem.setPanierId(0);
                } else {
                    cartItem.setPanierId(panierId);
                    Panier panier = panierServices.getoneProduit(panierId);
                    cartItem.setPanier(panier);
                }
                cartItem.setQuantite(rs.getInt("quantite"));
                int utilisateurId = rs.getInt("utilisateur_id");
                if (rs.wasNull()) {
                    cartItem.setUtilisateurId(0);
                } else {
                    cartItem.setUtilisateurId(utilisateurId);
                }

                Produit produit = produitServices.getoneProduit(cartItem.getProduitId());
                cartItem.setProduit(produit);

                cartItems.add(cartItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching cart items: " + e.getMessage());
        }
        return cartItems;
    }

    @Override
    public CartItem getoneProduit(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT * FROM cart_item WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                CartItem cartItem = new CartItem();
                cartItem.setId(rs.getInt("id"));
                cartItem.setProduitId(rs.getInt("produit_id"));
                int commandeId = rs.getInt("commande_id");
                if (rs.wasNull()) {
                    cartItem.setCommandeId(0);
                } else {
                    cartItem.setCommandeId(commandeId);
                }
                int panierId = rs.getInt("panier_id");
                if (rs.wasNull()) {
                    cartItem.setPanierId(0);
                } else {
                    cartItem.setPanierId(panierId);
                    Panier panier = panierServices.getoneProduit(panierId);
                    cartItem.setPanier(panier);
                }
                cartItem.setQuantite(rs.getInt("quantite"));
                int utilisateurId = rs.getInt("utilisateur_id");
                if (rs.wasNull()) {
                    cartItem.setUtilisateurId(0);
                } else {
                    cartItem.setUtilisateurId(utilisateurId);
                }

                Produit produit = produitServices.getoneProduit(cartItem.getProduitId());
                cartItem.setProduit(produit);

                return cartItem;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching cart item: " + e.getMessage());
        }
        return null;
    }
}