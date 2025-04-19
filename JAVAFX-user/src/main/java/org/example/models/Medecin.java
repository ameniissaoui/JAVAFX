package org.example.models;

import java.util.Date;

public class
Medecin extends User{
    String specialite;
    String diploma;
    private boolean is_verified = false;

    public Medecin() {
        super();
    }

    public Medecin(int userId, String specialite, String diploma) {
        super();
        setId(userId);  // Set the existing user ID
        this.specialite = specialite;
        this.diploma = diploma;
    }
    public Medecin(String specialite, String diploma) {
        this.specialite = specialite;
        this.diploma = diploma;
    }

    public Medecin(int id, String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone, String specialite, String diploma) {
        super(id, nom, prenom, email, motDePasse, dateNaissance, telephone);
        this.specialite = specialite;
        this.diploma = diploma;
    }

    public Medecin(String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone, String specialite, String diploma) {
        super(nom, prenom, email, motDePasse, dateNaissance, telephone);
        this.specialite = specialite;
        this.diploma = diploma;
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
                ", id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", motDePasse='" + motDePasse + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
