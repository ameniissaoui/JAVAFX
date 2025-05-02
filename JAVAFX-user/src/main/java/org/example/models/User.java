package org.example.models;

import java.util.Date;

public abstract class User {
    protected int id;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String motDePasse;
    protected Date dateNaissance;
    protected String telephone;
    protected boolean banned;
    protected String image;
    protected String role;

    public User() {
        this.banned = false;
        this.image = null;
        this.role = null;
    }

    public User(int id, String nom, String prenom, String email, String motDePasse,
                Date dateNaissance, String telephone, String image, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateNaissance = dateNaissance;
        this.telephone = telephone;
        this.banned = false;
        this.image = image;
        this.role = role;
    }

    public User(String nom, String prenom, String email, String motDePasse,
                Date dateNaissance, String telephone, String image, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateNaissance = dateNaissance;
        this.telephone = telephone;
        this.banned = false;
        this.image = image;
        this.role = role;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public Date getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" +
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