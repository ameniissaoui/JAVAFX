package org.example.services;

import org.example.models.Admin;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService extends UserService<Admin> {

    @Override
    protected String getUserType() {
        return "admin";
    }

    @Override
    public void add(Admin admin) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // Set role and image
            admin.setRole("admin");
            admin.setImage(null);

            // Insert into user table
            int userId = insertBaseUser(admin);
            if (userId == -1) {
                conn.rollback();
                return;
            }
            admin.setId(userId);

            // Insert into admin table
            String query = "INSERT INTO admin (user_id) VALUES (?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.executeUpdate();
                conn.commit();
                System.out.println("Admin ajouté !");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erreur ajout admin: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erreur de transaction: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Erreur pour rétablir autoCommit: " + e.getMessage());
            }
        }
    }

    @Override
    public void update(Admin admin) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // Ensure role is correct
            admin.setRole("admin");

            // Update base user information
            updateBaseUser(admin);

            // Admin table doesn't have specific attributes to update
            conn.commit();
            System.out.println("Admin mis à jour !");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Erreur de rollback: " + ex.getMessage());
            }
            System.out.println("Erreur de transaction: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Erreur pour rétablir autoCommit: " + e.getMessage());
            }
        }
    }

    @Override
    public void delete(Admin admin) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // Delete from admin table
            String query = "DELETE FROM admin WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, admin.getId());
                pst.executeUpdate();

                // Delete from user table
                deleteBaseUser(admin.getId());

                conn.commit();
                System.out.println("Admin supprimé !");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erreur delete admin: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erreur de transaction: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Erreur pour rétablir autoCommit: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Admin> getAll() {
        List<Admin> list = new ArrayList<>();
        String query = "SELECT u.* FROM user u JOIN admin a ON u.id = a.user_id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Admin admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone")
                );
                admin.setBanned(rs.getBoolean("banned"));
                admin.setImage(rs.getString("image"));
                admin.setRole(rs.getString("role"));
                list.add(admin);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll admins: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Admin getOne(int id) {
        String query = "SELECT u.* FROM user u JOIN admin a ON u.id = a.user_id WHERE u.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Admin admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone")
                );
                admin.setBanned(rs.getBoolean("banned"));
                admin.setImage(rs.getString("image"));
                admin.setRole(rs.getString("role"));
                return admin;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getOne admin: " + e.getMessage());
        }
        return null;
    }

    public Admin findByEmail(String email) {
        String query = "SELECT u.* FROM user u JOIN admin a ON u.id = a.user_id WHERE u.email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Admin admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone")
                );
                admin.setBanned(rs.getBoolean("banned"));
                admin.setImage(rs.getString("image"));
                admin.setRole(rs.getString("role"));
                return admin;
            }
        } catch (SQLException e) {
            System.out.println("Erreur findByEmail: " + e.getMessage());
        }
        return null;
    }
}