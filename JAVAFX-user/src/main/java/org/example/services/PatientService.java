package org.example.services;

import org.example.models.Patient;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientService extends UserService<Patient> {

    @Override
    protected String getUserType() {
        return "patient";
    }

    @Override
    public void add(Patient patient) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // First insert into user table
            int userId = insertBaseUser(patient);
            if (userId == -1) {
                conn.rollback();
                return;
            }
            patient.setId(userId);

            // Then insert into patient table
            String query = "INSERT INTO patient (user_id) VALUES (?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.executeUpdate();
                conn.commit();
                System.out.println("Patient ajouté !");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erreur ajout patient: " + e.getMessage());
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
    public void update(Patient patient) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);
            updateBaseUser(patient);
            conn.commit();
            System.out.println("Patient mis à jour !");

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
    public void delete(Patient patient) {
        Connection conn = MaConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // First delete from patient table
            String query = "DELETE FROM patient WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, patient.getId());
                pst.executeUpdate();

                // Then delete from user table
                deleteBaseUser(patient.getId());

                conn.commit();
                System.out.println("Patient supprimé !");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erreur delete patient: " + e.getMessage());
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
    public List<Patient> getAll() {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT u.* FROM user u " +
                "JOIN patient p ON u.id = p.user_id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone")
                );
                list.add(patient);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll patients: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Patient getOne(int id) {
        String query = "SELECT u.* FROM user u " +
                "JOIN patient p ON u.id = p.user_id " +
                "WHERE u.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur getOne patient: " + e.getMessage());
        }
        return null;
    }
    public Patient findByEmail(String email) {
        String query = "SELECT u.* FROM user u JOIN patient p ON u.id = p.user_id WHERE u.email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateNaissance"),
                        rs.getString("telephone")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }}