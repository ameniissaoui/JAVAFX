package org.example.models;

import java.util.Date;

public class Admin extends User {
    public Admin() {
        super();
        this.role = "admin"; // Set default role
        this.image = null; // Image is null by default
    }

    public Admin(int id, String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(id, nom, prenom, email, motDePasse, dateNaissance, telephone, null, "admin");
    }

    public Admin(String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        super(nom, prenom, email, motDePasse, dateNaissance, telephone, null, "admin");
    }

    @Override
    public String toString() {
        return "Admin{" +
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