package org.example.models;

public class Panier {
    private int id;
    private String nom;
    private String prenom;
    private String adresse;
    private int quantite;

    // Default constructor
    public Panier() {}

    // Parameterized constructor
    public Panier(String nom, String prenom, String adresse, int quantite) {
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.quantite = quantite;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "Panier{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", quantite=" + quantite +
                '}';
    }
}