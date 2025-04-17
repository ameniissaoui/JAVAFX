package org.example.models;

public class suivie_medical {
    private int id;
    private String date;
    private String commentaire;

    private historique_traitement historique;

    public suivie_medical() {
    }

    public suivie_medical(int id, String date, String commentaire, historique_traitement historique) {
        this.id = id;
        this.date = date;
        this.commentaire = commentaire;
        this.historique = historique;
    }

    // Constructeur utilisé lors de l'ajout d'un nouveau suivi
    public suivie_medical(String date, String commentaire, historique_traitement historique) {
        this.date = date;
        this.commentaire = commentaire;
        this.historique = historique;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public historique_traitement getHistorique() {
        return historique;
    }

    public void setHistorique(historique_traitement historique) {
        this.historique = historique;
    }

    // Pour la rétrocompatibilité avec le code existant qui utilise getHistoriqueId
    public int getHistoriqueId() {
        return historique != null ? historique.getId() : 0;
    }

    @Override
    public String toString() {
        return "suivie_medical{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", historique=" + (historique != null ? historique.getId() : "null") +
                '}';
    }
}