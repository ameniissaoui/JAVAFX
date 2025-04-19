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
        String query = "UPDATE user SET nom = ?, prenom = ?, email = ?, motDePasse = ?, dateNaissance = ?, telephone = ?, banned = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            String hashedPassword = user.getMotDePasse() != null && !user.getMotDePasse().startsWith("$2a$")
                    ? BCrypt.hashpw(user.getMotDePasse(), BCrypt.gensalt())
                    : user.getMotDePasse();
            pst.setString(4, hashedPassword);
            if (user.getDateNaissance() != null) {
                pst.setDate(5, new java.sql.Date(user.getDateNaissance().getTime()));
            } else {
                pst.setNull(5, Types.DATE);
            }

            pst.setString(6, user.getTelephone());
            pst.setBoolean(7, user.isBanned());
            pst.setInt(8, user.getId());
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

    public void banUser(int userId) {
        String query = "UPDATE user SET banned = true WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur ban user: " + e.getMessage());
        }
    }

    public void unbanUser(int userId) {
        String query = "UPDATE user SET banned = false WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur unban user: " + e.getMessage());
        }
    }
    protected int insertBaseUser(User user) {
        String query = "INSERT INTO user (nom, prenom, email, motDePasse, dateNaissance, telephone, banned) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            String hashedPassword = BCrypt.hashpw(user.getMotDePasse(), BCrypt.gensalt());
            pst.setString(4, hashedPassword);
            if (user.getDateNaissance() != null) {
                pst.setDate(5, new java.sql.Date(user.getDateNaissance().getTime()));
            } else {
                pst.setNull(5, Types.DATE);
            }

            pst.setString(6, user.getTelephone());
            pst.setBoolean(7, user.isBanned());
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
    public boolean verifyPassword(int userId, String password) {
        String query = "SELECT motDePasse FROM user WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("motDePasse");
                if (storedHash == null || !storedHash.startsWith("$2a$")) {
                    System.out.println("Invalid hash format for user ID: " + userId);
                    return false; // Treat invalid hash as failed verification
                }
                return BCrypt.checkpw(password, storedHash);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification du mot de passe: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid salt version for user ID: " + userId + ": " + e.getMessage());
            return false; // Handle BCrypt-specific errors
        }
        return false;
    }

    public void updatePassword(int userId, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String query = "UPDATE user SET motDePasse = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, hashedPassword);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
            throw new RuntimeException("Failed to update password: " + e.getMessage(), e);
        }
    }
}