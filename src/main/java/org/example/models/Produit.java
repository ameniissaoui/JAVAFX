package org.example.models;

import java.sql.Date;

public class Produit {

    private int id;
    private String nom;
    private String description;
    private float prix;
    private int stock_quantite;
    private Date date;
    private String image;
    private int createdById;

    public Produit() {
    }

    public Produit(String nom, String description, float prix, int stock_quantite, Date date, String image, int createdById) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock_quantite = stock_quantite;
        this.date = date;
        this.image = image;
        this.createdById = createdById;
    }

    public Produit(int id, String nom, String description, float prix, int stock_quantite, Date date, String image, int createdById) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock_quantite = stock_quantite;
        this.date = date;
        this.image = image;
        this.createdById = createdById;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public int getStock_quantite() {
        return stock_quantite;
    }

    public void setStock_quantite(int stock_quantite) {
        this.stock_quantite = stock_quantite;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCreatedById() {
        return createdById;
    }

    public void setCreatedById(int createdById) {
        this.createdById = createdById;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", stock_quantite=" + stock_quantite +
                ", date=" + date +
                ", image='" + image + '\'' +
                ", createdById=" + createdById +
                '}';
    }
}