package org.example.models;

public class Favoris {
    private int id;
    private Produit produit;
    private int utilisateurId;

    public Favoris() {
    }

    public Favoris(int id, Produit produit) {
        this.id = id;
        this.produit = produit;
    }

    public Favoris(Produit produit) {
        this.produit = produit;
    }

    public Favoris(Produit produit, int utilisateurId) {
        this.produit = produit;
        this.utilisateurId = utilisateurId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    @Override
    public String toString() {
        return "Favoris{" +
                "id=" + id +
                ", produit=" + (produit != null ? produit.getNom() : "null") +
                ", utilisateurId=" + utilisateurId +
                '}';
    }
}