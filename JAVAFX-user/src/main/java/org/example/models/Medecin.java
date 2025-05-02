package org.example.models;

import java.util.Date;

public class Medecin extends User {
    private String specialite;
    private String diploma;
    private boolean is_verified = false;

    public Medecin() {
        super();
        this.role = "medecin"; // Set default role
        this.image = null; // Image is null by default
    }

    public Medecin(int userId, String specialite, String diploma) {
        super();
        setId(userId);
        this.specialite = specialite;
        this.diploma = diploma;
        this.role = "medecin";
        this.image = null;
    }

    public Medecin(String specialite, String diploma) {
        super();
        this.specialite = specialite;
        this.diploma = diploma;
        this.role = "medecin";
        this.image = null;
    }

    public Medecin(int id, String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone, String image, String role, String specialite, String diploma) {
        super(id, nom, prenom, email, motDePasse, dateNaissance, telephone, image, role);
        this.specialite = specialite;
        this.diploma = diploma;
        this.role = "medecin"; // Enforce role
    }

    public Medecin(String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone, String image, String role, String specialite, String diploma) {
        super(nom, prenom, email, motDePasse, dateNaissance, telephone, image, role);
        this.specialite = specialite;
        this.diploma = diploma;
        this.role = "medecin"; // Enforce role
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getDiploma() {
        return diploma;
    }

    public void setDiploma(String diploma) {
        this.diploma = diploma;
    }

    public boolean isIs_verified() {
        return is_verified;
    }

    public void setIs_verified(boolean is_verified) {
        this.is_verified = is_verified;
    }

    @Override
    public String toString() {
        return "Medecin{" +
                "specialite='" + specialite + '\'' +
                ", diploma='" + diploma + '\'' +
                ", is_verified=" + is_verified +
                ", id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", motDePasse='" + motDePasse + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", telephone='" + telephone + '\'' +
                ", banned=" + banned +
                ", image='" + image + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}