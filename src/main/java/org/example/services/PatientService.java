package org.example.services;

import org.example.models.Patient;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientService extends UserService<Patient> {

    @Override
    protected String getUserType() {
        return "Patient";
    }

    @Override
    public void add(Patient patient) {
        // Set role before inserting
        patient.setRole("Patient");
        patient.setImage(null); // Ensure image is null initially

        // Insert into user table
        int userId = insertBaseUser(patient);
        if (userId != -1) {
            patient.setId(userId);
            System.out.println("Patient ajouté !");
        }
    }
    @Override
    public void update(Patient patient) {
        // Ensure role is correct
        patient.setRole("Patient");

        // Update base user information
        updateBaseUser(patient);
        System.out.println("Patient mis à jour !");
    }
    @Override
    public void delete(Patient patient) {
        // Delete directly from user table
        deleteBaseUser(patient.getId());
        System.out.println("Patient supprimé !");
    }
    @Override
    public List<Patient> getAll() {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE role = 'Patient'";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone")
                );
                patient.setBanned(rs.getBoolean("is_blocked"));
                patient.setImage(rs.getString("image"));
                patient.setRole(rs.getString("role"));
                list.add(patient);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll patients: " + e.getMessage());
        }
        return list;
    }
    @Override
    public Patient getOne(int id) {
        String query = "SELECT * FROM utilisateur WHERE id = ? AND role = 'Patient'";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone")
                );
                patient.setBanned(rs.getBoolean("is_blocked"));
                patient.setImage(rs.getString("image"));
                patient.setRole(rs.getString("role"));
                return patient;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getOne patient: " + e.getMessage());
        }
        return null;
    }
    public Patient findByEmail(String email) {
        String query = "SELECT * FROM utilisateur WHERE email = ? AND role = 'Patient'";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getDate("date_naissance"),
                        rs.getString("telephone")
                );
                patient.setBanned(rs.getBoolean("is_blocked"));
                patient.setImage(rs.getString("image"));
                patient.setRole(rs.getString("role"));
                return patient;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }}