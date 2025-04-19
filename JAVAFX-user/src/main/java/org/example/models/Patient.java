package org.example.models;

import java.util.Date;

public class Patient extends User {
    public Patient() {
        super();
    }

    public Patient(int id, String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(id, nom, prenom, email, motDePasse, dateNaissance, telephone);
    }

    public Patient(String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(nom, prenom, email, motDePasse, dateNaissance, telephone);
    }

    public Patient(int id, String nom, String prenom, String email, String motDePasse, java.sql.Date dateNaissance, String telephone, String assurance) {
    }


    @Override
    public String toString() {
        return "Patient{" + super.toString() + "}";
    }
}
