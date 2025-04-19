package org.example.services;

import org.example.models.Medecin;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedecinService extends UserService<Medecin> {

    @Override
    protected String getUserType() {
        return "medecin";
    }

    @Override
    public void add(Medecin medecin) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // First insert into user table
            int userId = insertBaseUser(medecin);
            if (userId == -1) {
                conn.rollback();
                return;
            }
            medecin.setId(userId);

            // Then insert into medecin table
            String query = "INSERT INTO medecin (user_id, specialite, diploma, is_verified) VALUES (?, ?, ?, ?)";            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.setString(2, medecin.getSpecialite());
                pst.setString(3, medecin.getDiploma());
                pst.setBoolean(4, medecin.isIs_verified());
                pst.executeUpdate();
                conn.commit();
                System.out.println("Medecin ajouté !");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erreur ajout medecin: " + e.getMessage());
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
    public void update(Medecin medecin) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // Update base user information
            updateBaseUser(medecin);

            // Update medecin-specific information
            String query = "UPDATE medecin SET specialite = ?, diploma = ?, is_verified = ?  WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, medecin.getSpecialite());
                pst.setString(2, medecin.getDiploma());
                pst.setBoolean(3, medecin.isIs_verified());
                pst.setInt(4, medecin.getId());
                pst.executeUpdate();
                conn.commit();
                System.out.println("Medecin mis à jour !");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erreur update medecin: " + e.getMessage());
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
    public void delete(Medecin medecin) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // First delete from medecin table
            String query = "DELETE FROM medecin WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, medecin.getId());
                pst.executeUpdate();

                // Then delete from user table
                deleteBaseUser(medecin.getId());

                conn.commit();
                System.out.println("Medecin supprimé !");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erreur delete medecin: " + e.getMessage());
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
    public List<Medecin> getAll() {
        List<Medecin> list = new ArrayList<>();
        String query = "SELECT u.*, m.specialite, m.diploma, m.is_verified  FROM user u " +
                "JOIN medecin m ON u.id = m.user_id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Medecin medecin = new Medecin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone"),
                        rs.getString("specialite"),
                        rs.getString("diploma")
                );
                medecin.setBanned(rs.getBoolean("banned"));
                medecin.setIs_verified(rs.getBoolean("is_verified"));
                list.add(medecin);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll medecins: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Medecin getOne(int id) {
        String query = "SELECT u .*, m.specialite, m.diploma, m.is_verified  FROM user u " +
                "JOIN medecin m ON u.id = m.user_id " +
                "WHERE u.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Medecin medecin = new Medecin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone"),
                        rs.getString("specialite"),
                        rs.getString("diploma")
                );
                medecin.setBanned(rs.getBoolean("banned"));
                medecin.setIs_verified(rs.getBoolean("is_verified"));
                return medecin;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getOne medecin: " + e.getMessage());
        }
        return null;
    }
    public Medecin findByEmail(String email) {
        String query = "SELECT u.*, m.specialite, m.diploma, m.is_verified  FROM user u JOIN medecin m ON u.id = m.user_id WHERE u.email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Medecin medecin = new Medecin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone"),
                        rs.getString("specialite"),
                        rs.getString("diploma")
                );
                medecin.setBanned(rs.getBoolean("banned"));
                medecin.setIs_verified(rs.getBoolean("is_verified"));
                return medecin;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void verifyDoctor(int id) {
        Medecin medecin = getOne(id);
        if (medecin != null) {
            medecin.setIs_verified(true);
            update(medecin);
        }
    }

    public void unverifyDoctor(int id) {
        Medecin medecin = getOne(id);
        if (medecin != null) {
            medecin.setIs_verified(false);
            update(medecin);
        }
    }
}