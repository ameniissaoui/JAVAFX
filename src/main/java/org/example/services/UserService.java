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
// Replace your current insertBaseUser and updateBaseUser methods with these updated versions

    protected int insertBaseUser(User user) {
        // Handle password hashing with compatibility
        insertOrUpdateUserWithCompatibleHash(user, false);

        String query = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, date_naissance, telephone, is_blocked, image, role";

        // Add medecin-specific fields if applicable
        if (user instanceof Medecin) {
            query += ", specialite, diploma, is_verified";
        }

        query += ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?";

        // Add placeholders for medecin-specific fields
        if (user instanceof Medecin) {
            query += ", ?, ?, ?";
        }

        query += ")";

        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMotDePasse()); // Pre-processed by insertOrUpdateUserWithCompatibleHash
            if (user.getDateNaissance() != null) {
                pst.setDate(5, new java.sql.Date(user.getDateNaissance().getTime()));
            } else {
                pst.setNull(5, Types.DATE);
            }
            pst.setString(6, user.getTelephone());
            pst.setBoolean(7, user.isBanned());
            pst.setString(8, user.getImage());
            pst.setString(9, user.getRole());

            // Set medecin-specific fields if applicable
            if (user instanceof Medecin) {
                Medecin medecin = (Medecin) user;
                pst.setString(10, medecin.getSpecialite());
                pst.setString(11, medecin.getDiploma());
                pst.setBoolean(12, medecin.isIs_verified());
            }

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
        // Handle password hashing with compatibility
        insertOrUpdateUserWithCompatibleHash(user, true);

        String query = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, date_naissance = ?, telephone = ?, is_blocked = ?, image = ?, role = ?";

        // Add medecin-specific fields if applicable
        if (user instanceof Medecin) {
            query += ", specialite = ?, diploma = ?, is_verified = ?";
        }

        query += " WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMotDePasse()); // Pre-processed by insertOrUpdateUserWithCompatibleHash
            if (user.getDateNaissance() != null) {
                pst.setDate(5, new java.sql.Date(user.getDateNaissance().getTime()));
            } else {
                pst.setNull(5, Types.DATE);
            }
            pst.setString(6, user.getTelephone());
            pst.setBoolean(7, user.isBanned());
            pst.setString(8, user.getImage());
            pst.setString(9, user.getRole());

            int paramIndex = 10;
            // Set medecin-specific fields if applicable
            if (user instanceof Medecin) {
                Medecin medecin = (Medecin) user;
                pst.setString(paramIndex++, medecin.getSpecialite());
                pst.setString(paramIndex++, medecin.getDiploma());
                pst.setBoolean(paramIndex++, medecin.isIs_verified());
            }

            pst.setInt(paramIndex, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur update user: " + e.getMessage());
        }
    }
    protected void deleteBaseUser(int userId) {
        String query = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur delete user: " + e.getMessage());
        }
    }

    public void banUser(int userId) {
        String query = "UPDATE utilisateur SET is_blocked = true WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur ban user: " + e.getMessage());
        }
    }

    public void unbanUser(int userId) {
        String query = "UPDATE utilisateur SET is_blocked = false WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur unban user: " + e.getMessage());
        }
    }
    public boolean verifyPassword(int userId, String password) {
        String query = "SELECT mot_de_passe FROM utilisateur WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("mot_de_passe");
                if (storedHash == null) {
                    System.out.println("No hash found for user ID: " + userId);
                    return false;
                }

                // Check if it's a $2y$ hash (Symfony format)
                if (storedHash.startsWith("$2y$")) {
                    // Convert $2y$ to $2a$ for BCrypt compatibility
                    String adaptedHash = "$2a$" + storedHash.substring(4);
                    try {
                        return BCrypt.checkpw(password, adaptedHash);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error checking converted hash: " + e.getMessage());
                        // As fallback, try direct string comparison if the hash conversion fails
                        return BCrypt.hashpw(password, BCrypt.gensalt()).equals(adaptedHash);
                    }
                }
                // Standard $2a$ BCrypt hash (JavaFX format)
                else if (storedHash.startsWith("$2a$")) {
                    return BCrypt.checkpw(password, storedHash);
                }
                else {
                    System.out.println("Unsupported hash format for user ID: " + userId);
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification du mot de passe: " + e.getMessage());
        }
        return false;
    }
    public void updatePassword(int userId, String newPassword) {
        // Always use JavaFX style ($2a$) for new passwords created within the app
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        String query = "UPDATE utilisateur SET mot_de_passe = ? WHERE id = ?";
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
        String selectQuery = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(selectQuery)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                // Create the appropriate user type based on the role
                String userRole = rs.getString("role");
                User user;
                switch (userRole.toLowerCase()) {
                    case "Admin":
                        user = new Admin();
                        break;
                    case "Médecin":
                        user = new Medecin();
                        if (user instanceof Medecin) {
                            Medecin medecin = (Medecin) user;
                            medecin.setSpecialite(rs.getString("specialite"));
                            medecin.setDiploma(rs.getString("diploma"));
                            medecin.setIs_verified(rs.getBoolean("is_verified"));
                        }
                        break;
                    case "Patient":
                    default:
                        user = new Patient();
                        break;
                }
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRole(userRole);
                user.setBanned(rs.getBoolean("is_blocked"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setMotDePasse(rs.getString("mot_de_passe"));
                user.setTelephone(rs.getString("telephone"));
                user.setImage(rs.getString("image"));
                if (rs.getDate("date_naissance") != null) {
                    user.setDateNaissance(new java.util.Date(rs.getDate("date_naissance").getTime()));
                }
                return user;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche de l'utilisateur: " + e.getMessage());
        }

        // Create new user if not found
        User newUser;
        switch (role.toLowerCase()) {
            case "Admin":
                newUser = new Admin();
                break;
            case "Médecin":
                newUser = new Medecin();
                break;
            case "Patient":
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
    // Add this to your UserService.java file

    /**
     * Adds proper handling for both Symfony ($2y$) and JavaFX ($2a$) password hashes
     * when creating new users.
     */
    protected void insertOrUpdateUserWithCompatibleHash(User user, boolean isUpdate) {
        String hashedPassword;

        // Handle password hashing for new password
        if (user.getMotDePasse() != null && !user.getMotDePasse().isEmpty()) {
            // If it's not already a hash (doesn't start with $2a$ or $2y$)
            if (!user.getMotDePasse().startsWith("$2")) {
                // Use BCrypt to hash it with $2a$ format (JavaFX style)
                hashedPassword = BCrypt.hashpw(user.getMotDePasse(), BCrypt.gensalt());
            } else {
                // It's already a hash, keep it as is
                hashedPassword = user.getMotDePasse();
            }
        } else if (isUpdate) {
            // For updates where password is empty, get the existing hash
            hashedPassword = getExistingPasswordHash(user.getId());
        } else {
            // For new users with empty password (rare case)
            hashedPassword = "";
        }

        // Set the processed password back to the user object
        user.setMotDePasse(hashedPassword);
    }

    /**
     * Helper method to retrieve existing password hash from database
     */
    private String getExistingPasswordHash(int userId) {
        String query = "SELECT mot_de_passe FROM utilisateur WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("mot_de_passe");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving existing password: " + e.getMessage());
        }
        return "";
    }
}
