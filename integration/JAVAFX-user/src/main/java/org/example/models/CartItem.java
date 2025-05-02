package org.example.models;

public class CartItem {
    private int id;
    private int produitId;
    private int commandeId;
    private int panierId;
    private int quantite;
    private int utilisateurId; // Added for user ID
    private Produit produit;
    private Commande commande;
    private Panier panier;

    // Constructors
    public CartItem() {}

    public CartItem(int produitId, int commandeId, int quantite, int utilisateurId) {
        this.produitId = produitId;
        this.commandeId = commandeId;
        this.quantite = quantite;
        this.utilisateurId = utilisateurId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public int getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(int commandeId) {
        this.commandeId = commandeId;
    }

    public int getPanierId() {
        return panierId;
    }

    public void setPanierId(int panierId) {
        this.panierId = panierId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Panier getPanier() {
        return panier;
    }

    public void setPanier(Panier panier) {
        this.panier = panier;
        this.panierId = (panier != null) ? panier.getId() : 0;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", produitId=" + produitId +
                ", commandeId=" + commandeId +
                ", panierId=" + panierId +
                ", quantite=" + quantite +
                ", utilisateurId=" + utilisateurId +
                '}';
    }
}