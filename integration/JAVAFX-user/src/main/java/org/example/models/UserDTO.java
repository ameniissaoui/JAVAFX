package org.example.models;

import java.util.Date;

public class UserDTO {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private Date dateNaissance;
    private String telephone;

    public UserDTO(String nom, String prenom, String email, String motDePasse, Date dateNaissance, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateNaissance = dateNaissance;
        this.telephone = telephone;
    }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getMotDePasse() { return motDePasse; }
    public Date getDateNaissance() { return dateNaissance; }
    public String getTelephone() { return telephone; }
}