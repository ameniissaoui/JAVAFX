package org.example.services;

import org.example.util.MaConnexion;
import org.example.models.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitServices implements IServices <Produit> {
    private Connection cnx;

    public ProduitServices() {
        try {
            // Get database connection
            initializeConnection();

            // Test connection immediately
            if (cnx == null || cnx.isClosed()) {
                System.err.println("Database connection is null or closed after initialization");
                // Try to reconnect once
                initializeConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeConnection() {
        try {
            MaConnexion dbInstance = MaConnexion.getInstance();
            if (dbInstance != null && dbInstance.isConnected()) {
                cnx = dbInstance.getCnx();
                if (cnx == null) {
                    System.err.println("Database connection is null despite dbInstance.isConnected() returning true");
                }
            } else {
                System.err.println("Error: Database instance is null or not connected!");
            }
        } catch (Exception e) {
            System.err.println("Exception during connection initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Ensure connection is valid before each operation
    private boolean ensureConnection() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("Connection is null or closed, attempting to reconnect...");
                initializeConnection();
                return cnx != null && !cnx.isClosed();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void addProduit(Produit produit) {
        if (!ensureConnection()) {
            System.err.println("Cannot add product: Database connection failed");
            return;
        }

        String req = "INSERT INTO chrono.produit(nom, description, prix, stock_quantite, date, image) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setFloat(3, produit.getPrix());
            stmt.setInt(4, produit.getStock_quantite());
            stmt.setDate(5, produit.getDate());
            stmt.setString(6, produit.getImage());

            int result = stmt.executeUpdate();
            System.out.println("Product added successfully! Rows affected: " + result);
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void removeProduit(Produit produit) {
        if (!ensureConnection()) {
            System.err.println("Cannot remove product: Database connection failed");
            return;
        }

        String req = "DELETE FROM chrono.produit WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setInt(1, produit.getId());
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("No product found with ID: " + produit.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void editProduit(Produit produit) {
        if (!ensureConnection()) {
            System.err.println("Cannot edit product: Database connection failed");
            return;
        }

        String req = "UPDATE chrono.produit SET nom=?, description=?, prix=?, stock_quantite=?, date=?, image=? WHERE id=?";

        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setFloat(3, produit.getPrix());
            stmt.setInt(4, produit.getStock_quantite());
            stmt.setDate(5, produit.getDate());
            stmt.setString(6, produit.getImage());
            stmt.setInt(7, produit.getId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Product updated successfully!");
            } else {
                System.out.println("No product found with ID: " + produit.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Produit> showProduit() {
        List<Produit> produits = new ArrayList<>();

        if (!ensureConnection()) {
            System.err.println("Cannot show products: Database connection failed");
            return produits; // Return empty list
        }

        String req = "SELECT * FROM chrono.produit";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(req)) {

            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setDescription(rs.getString("description"));
                p.setPrix(rs.getFloat("prix"));
                p.setStock_quantite(rs.getInt("stock_quantite"));
                p.setDate(rs.getDate("date"));
                p.setImage(rs.getString("image"));
                produits.add(p);
            }

            System.out.println("Retrieved " + produits.size() + " products from database");
        } catch (SQLException e) {
            System.err.println("Error retrieving products: " + e.getMessage());
            e.printStackTrace();
        }

        return produits;
    }

    @Override
    public Produit getoneProduit(int id) {
        if (!ensureConnection()) {
            System.err.println("Cannot get product: Database connection failed");
            return null;
        }

        String req = "SELECT * FROM chrono.produit WHERE id = ?";
        Produit p = null;

        try (PreparedStatement stmt = cnx.prepareStatement(req)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    p = new Produit();
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setDescription(rs.getString("description"));
                    p.setPrix(rs.getFloat("prix"));
                    p.setStock_quantite(rs.getInt("stock_quantite"));
                    p.setDate(rs.getDate("date"));
                    p.setImage(rs.getString("image"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving product: " + e.getMessage());
            e.printStackTrace();
        }

        return p;
    }

    // Check if connection is working and print connection info
    public boolean testConnection() {
        try {
            if (cnx == null) {
                System.err.println("Connection is null");
                return false;
            }

            if (cnx.isClosed()) {
                System.err.println("Connection is closed");
                return false;
            }

            // Test with a simple query
            try (Statement stmt = cnx.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    System.out.println("Database connection test successful");

                    // Print connection metadata
                    DatabaseMetaData metaData = cnx.getMetaData();
                    System.out.println("Database: " + metaData.getDatabaseProductName() + " " +
                            metaData.getDatabaseProductVersion());
                    System.out.println("Driver: " + metaData.getDriverName() + " " +
                            metaData.getDriverVersion());
                    System.out.println("Connection is valid and working");
                    return true;
                }
            }

            System.err.println("Connection query returned no results");
            return false;
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}