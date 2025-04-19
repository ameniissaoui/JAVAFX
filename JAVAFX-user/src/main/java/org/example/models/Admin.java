package org.example.models;

import java.util.Date;

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(int id, String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(id, nom, prenom, email, motDePasse, dateNaissance, telephone);
    }

    public Admin(String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(nom, prenom, email, motDePasse, dateNaissance, telephone);
    }


    @Override
    public String toString() {
        return "Admin{" + super.toString() + "}";
    }
}
