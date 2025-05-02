package org.example.models;

import java.util.Date;

public class Patient extends User {
    public Patient() {
        super();
        this.role = "patient"; // Set default role
        this.image = null; // Image is null by default
    }

    public Patient(int id, String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(id, nom, prenom, email, motDePasse, dateNaissance, telephone, null, "patient");
    }

    public Patient(String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(nom, prenom, email, motDePasse, dateNaissance, telephone, null, "patient");
    }
    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", telephone='" + telephone + '\'' +
                ", banned=" + banned +
                ", image='" + image + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}