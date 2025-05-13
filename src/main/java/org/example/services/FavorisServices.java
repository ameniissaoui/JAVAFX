package org.example.services;

import org.example.util.MaConnexion;
import org.example.models.Favoris;
import org.example.models.Produit;
import org.example.models.User;
import org.example.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FavorisServices implements IServices<Favoris> {
    private static final Logger LOGGER = Logger.getLogger(FavorisServices.class.getName());
    private Connection cnx;

    public FavorisServices() {
        try {
            initializeConnection();
            if (cnx == null || cnx.isClosed()) {
                System.err.println("Database connection is null or closed after initialization");
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
    public void addProduit(Favoris favoris) {
        if (!ensureConnection()) {
            System.err.println("Cannot add favorite: Database connection failed");
            throw new RuntimeException("Failed to add favorite: Database connection unavailable");
        }

        // Ensure utilisateurId is set
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        if (!(currentUser instanceof User)) {
            LOGGER.severe("Cannot add favorite: No user logged in or invalid user type");
            throw new RuntimeException("Cannot add favorite: User not authenticated");
        }
        int utilisateurId = ((User) currentUser).getId();
        favoris.setUtilisateurId(utilisateurId);

        String sql = "INSERT INTO favoris (produit_id, utilisateur_id) VALUES (?, ?)";
        try (PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, favoris.getProduit().getId());
            stmt.setInt(2, favoris.getUtilisateurId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to add favorite, no rows affected.");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    favoris.setId(rs.getInt(1));
                }
            }
            LOGGER.info("Added favorite for produit_id: " + favoris.getProduit().getId() + " and utilisateur_id: " + favoris.getUtilisateurId());
        } catch (SQLException e) {
            LOGGER.severe("Error adding favorite: " + e.getMessage());
            throw new RuntimeException("Failed to add favorite to database", e);
        }
    }

    @Override
    public void removeProduit(Favoris favoris) {
        if (!ensureConnection()) {
            System.err.println("Cannot remove favorite: Database connection failed");
            throw new RuntimeException("Failed to remove favorite: Database connection unavailable");
        }

        String sql = "DELETE FROM favoris WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, favoris.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("No favorite found to delete for id: " + favoris.getId());
            } else {
                LOGGER.info("Removed favorite with id: " + favoris.getId());
            }
        } catch (SQLException e) {
            LOGGER.severe("Error removing favorite: " + e.getMessage());
            throw new RuntimeException("Failed to remove favorite from database", e);
        }
    }

    @Override
    public void editProduit(Favoris favoris) {
        if (!ensureConnection()) {
            System.err.println("Cannot edit favorite: Database connection failed");
            throw new RuntimeException("Failed to edit favorite: Database connection unavailable");
        }

        String sql = "UPDATE favoris SET produit_id = ?, utilisateur_id = ? WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, favoris.getProduit().getId());
            stmt.setInt(2, favoris.getUtilisateurId());
            stmt.setInt(3, favoris.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("No favorite found to update for id: " + favoris.getId());
            } else {
                LOGGER.info("Updated favorite with id: " + favoris.getId());
            }
        } catch (SQLException e) {
            LOGGER.severe("Error updating favorite: " + e.getMessage());
            throw new RuntimeException("Failed to update favorite in database", e);
        }
    }

    @Override
    public List<Favoris> showProduit() {
        List<Favoris> favorisList = new ArrayList<>();
        if (!ensureConnection()) {
            System.err.println("Cannot show favorites: Database connection failed");
            return favorisList;
        }

        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int utilisateurId = 0;

        if (currentUser instanceof User) {
            utilisateurId = ((User) currentUser).getId();
        } else {
            LOGGER.warning("No user logged in or invalid user type in SessionManager");
            return favorisList;
        }

        String sql = "SELECT f.id, f.produit_id, f.utilisateur_id, p.nom, p.description, p.prix, p.stock_quantite, p.date, p.image, p.created_by_id " +
                "FROM favoris f JOIN produit p ON f.produit_id = p.id WHERE f.utilisateur_id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, utilisateurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produit produit = new Produit(
                            rs.getInt("produit_id"),
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getFloat("prix"),
                            rs.getInt("stock_quantite"),
                            rs.getDate("date"),
                            rs.getString("image"),
                            rs.getInt("created_by_id")
                    );
                    Favoris favoris = new Favoris(rs.getInt("id"), produit);
                    favoris.setUtilisateurId(rs.getInt("utilisateur_id"));
                    favorisList.add(favoris);
                }
            }
            LOGGER.info("Retrieved " + favorisList.size() + " favorites for utilisateur_id: " + utilisateurId);
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving favorites: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve favorites from database", e);
        }
        return favorisList;
    }

    @Override
    public Favoris getoneProduit(int id) {
        if (!ensureConnection()) {
            System.err.println("Cannot get favorite: Database connection failed");
            return null;
        }

        String sql = "SELECT f.id, f.produit_id, f.utilisateur_id, p.nom, p.description, p.prix, p.stock_quantite, p.date, p.image, p.created_by_id " +
                "FROM favoris f JOIN produit p ON f.produit_id = p.id WHERE f.id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produit produit = new Produit(
                            rs.getInt("produit_id"),
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getFloat("prix"),
                            rs.getInt("stock_quantite"),
                            rs.getDate("date"),
                            rs.getString("image"),
                            rs.getInt("created_by_id")
                    );
                    Favoris favoris = new Favoris(rs.getInt("id"), produit);
                    favoris.setUtilisateurId(rs.getInt("utilisateur_id"));
                    LOGGER.info("Retrieved favorite with id: " + id);
                    return favoris;
                }
            }
            LOGGER.warning("No favorite found for id: " + id);
            return null;
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving favorite: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve favorite from database", e);
        }
    }

    public boolean isFavorited(Produit produit) {
        if (!ensureConnection()) {
            System.err.println("Cannot check favorite status: Database connection failed");
            throw new RuntimeException("Failed to check favorite status: Database connection unavailable");
        }

        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int utilisateurId = 0;

        if (currentUser instanceof User) {
            utilisateurId = ((User) currentUser).getId();
        } else {
            LOGGER.warning("No user logged in or invalid user type in SessionManager");
            return false;
        }

        String sql = "SELECT COUNT(*) FROM favoris WHERE produit_id = ? AND utilisateur_id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, produit.getId());
            stmt.setInt(2, utilisateurId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean isFavorited = rs.getInt(1) > 0;
                    LOGGER.info("Checked favorite status for produit_id: " + produit.getId() + " and utilisateur_id: " + utilisateurId + ", isFavorited: " + isFavorited);
                    return isFavorited;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking favorite status: " + e.getMessage());
            throw new RuntimeException("Failed to check favorite status in database", e);
        }
        return false;
    }

    public void removeByProduit(Produit produit) {
        if (!ensureConnection()) {
            System.err.println("Cannot remove favorite by product: Database connection failed");
            throw new RuntimeException("Failed to remove favorite: Database connection unavailable");
        }

        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int utilisateurId = 0;

        if (currentUser instanceof User) {
            utilisateurId = ((User) currentUser).getId();
        } else {
            LOGGER.warning("No user logged in or invalid user type in SessionManager");
            return;
        }

        String sql = "DELETE FROM favoris WHERE produit_id = ? AND utilisateur_id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, produit.getId());
            stmt.setInt(2, utilisateurId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("No favorite found to delete for produit_id: " + produit.getId() + " and utilisateur_id: " + utilisateurId);
            } else {
                LOGGER.info("Removed favorite for produit_id: " + produit.getId() + " and utilisateur_id: " + utilisateurId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error removing favorite by produit: " + e.getMessage());
            throw new RuntimeException("Failed to remove favorite from database", e);
        }
    }

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

            try (Statement stmt = cnx.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    System.out.println("Database connection test successful");
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