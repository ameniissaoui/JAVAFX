package org.example.services;

import org.example.interfaces.IService;
import org.example.models.User;
import org.example.util.MaConnexion;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.List;

public abstract class UserService<T extends User> implements IService<T> {

    protected Connection cnx = MaConnexion.getInstance().getCnx();

    // Common operations for all user types
    protected abstract String getUserType();

    // Common methods can be implemented here, but specific implementations will be in child classes
    protected void updateBaseUser(User user) {
        String query = "UPDATE user SET nom = ?, prenom = ?, email = ?, motDePasse = ?, dateNaissance = ?, telephone = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMotDePasse());

            if (user.getDateNaissance() != null) {
                pst.setDate(5, new java.sql.Date(user.getDateNaissance().getTime()));
            } else {
                pst.setNull(5, Types.DATE);
            }

            pst.setString(6, user.getTelephone());
            pst.setInt(7, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur update user: " + e.getMessage());
        }
    }

    protected void deleteBaseUser(int userId) {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur delete user: " + e.getMessage());
        }
    }

    protected int insertBaseUser(User user) {
        String query = "INSERT INTO user (nom, prenom, email, motDePasse, dateNaissance, telephone) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMotDePasse());

            if (user.getDateNaissance() != null) {
                pst.setDate(5, new java.sql.Date(user.getDateNaissance().getTime()));
            } else {
                pst.setNull(5, Types.DATE);
            }

            pst.setString(6, user.getTelephone());
            pst.executeUpdate();

            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur ajout user: " + e.getMessage());
        }
        return -1;
    }
    public boolean verifyPassword(int adminId, String password) {
        String query = "SELECT motDePasse FROM user WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, adminId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("motDePasse");
                return BCrypt.checkpw(password, storedHash);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification du mot de passe: " + e.getMessage());
        }
        return false;
    }
    public void updatePassword(int adminId, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String query = "UPDATE user SET motDePasse = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, hashedPassword);
            pst.setInt(2, adminId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
        }
    }}