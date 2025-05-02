package org.example.services;

import org.example.interfaces.IService;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.User;
import org.example.util.MaConnexion;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import org.example.models.Patient; // Import Patient class
public abstract class UserService<T extends User> implements IService<T> {

    protected Connection cnx = MaConnexion.getInstance().getCnx();

    protected abstract String getUserType();

    protected int insertBaseUser(User user) {
        String query = "INSERT INTO user (nom, prenom, email, motDePasse, dateNaissance, telephone, banned, image, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            pst.setString(8, user.getImage()); // Can be null
            pst.setString(9, user.getRole()); // Set role based on user type
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

    protected void updateBaseUser(User user) {
        String query = "UPDATE user SET nom = ?, prenom = ?, email = ?, motDePasse = ?, dateNaissance = ?, telephone = ?, banned = ?, image = ?, role = ? WHERE id = ?";
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
            pst.setString(8, user.getImage());
            pst.setString(9, user.getRole());
            pst.setInt(10, user.getId());
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
    public User getOrCreateUser(String email, String role) {
        String selectQuery = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(selectQuery)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                // Create the appropriate user type based on the role
                String userRole = rs.getString("role");
                User user;
                switch (userRole.toLowerCase()) {
                    case "admin":
                        user = new Admin();
                        break;
                    case "medecin":
                        user = new Medecin();
                        break;
                    case "patient":
                    default:
                        user = new Patient();
                        break;
                }
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRole(userRole);
                user.setBanned(rs.getBoolean("banned"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setMotDePasse(rs.getString("motDePasse"));
                user.setTelephone(rs.getString("telephone"));
                user.setImage(rs.getString("image"));
                if (rs.getDate("dateNaissance") != null) {
                    user.setDateNaissance(new java.util.Date(rs.getDate("dateNaissance").getTime()));
                }
                return user;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche de l'utilisateur: " + e.getMessage());
        }

        // Create new user if not found
        User newUser;
        switch (role.toLowerCase()) {
            case "admin":
                newUser = new Admin();
                break;
            case "medecin":
                newUser = new Medecin();
                break;
            case "patient":
            default:
                newUser = new Patient(); // Default to Patient for Google sign-ins
                break;
        }
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setBanned(false);
        newUser.setNom("");
        newUser.setPrenom("");
        newUser.setMotDePasse(""); // No password for Google users
        newUser.setTelephone("");
        newUser.setImage("");

        int userId = insertBaseUser(newUser);
        if (userId != -1) {
            newUser.setId(userId);
            return newUser;
        }
        return null;
    }
}
