package org.example.services;

import org.example.models.Admin;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService extends UserService<Admin> {

    @Override
    protected String getUserType() {
        return "Admin";
    }

    @Override
    public void add(Admin admin) {
        admin.setRole("Admin");
        admin.setImage(null);
        int userId = insertBaseUser(admin);
        if (userId != -1) {
            admin.setId(userId);
            System.out.println("Admin ajouté !");
        }
    }

    @Override
    public void update(Admin admin) {
        admin.setRole("Admin");
        updateBaseUser(admin);
        System.out.println("Patient mis à jour !");
    }

    @Override
    public void delete(Admin admin) {
        deleteBaseUser(admin.getId());
        System.out.println("Admin supprimé !");
    }

    @Override
    public List<Admin> getAll() {
        List<Admin> list = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE role = 'Admin'";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Admin admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone")
                );
                admin.setBanned(rs.getBoolean("is_blocked"));
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
        String query = "SELECT * FROM utilisateur WHERE id = ? AND role = 'Admin'";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Admin admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone")
                );
                admin.setBanned(rs.getBoolean("is_blocked"));
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
        String query = "SELECT * FROM utilisateur  WHERE email = ? AND role = 'Admin'";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Admin admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone")
                );
                admin.setBanned(rs.getBoolean("is_blocked"));
                admin.setImage(rs.getString("image"));
                admin.setRole(rs.getString("role"));
                return admin;
            }
        } catch (SQLException e) {
            System.out.println("Erreur findByEmail: " + e.getMessage());
        }
        return null;
    }
    // Add this method to your AdminService class
    public Admin findById(int id) {
        // This is just an alias for getOne to maintain compatibility
        return getOne(id);
    }
}