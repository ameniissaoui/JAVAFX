package org.example.models;

public class suivie_medical {
    private int id;
    private String date;
    private String commentaire;

    // Relations ManyToOne
    private historique_traitement historique;  // Cette relation est correcte
    private User user;  // Plus spécifiquement un Medecin qui réalise le suivi

    public suivie_medical() {
    }

    public suivie_medical(int id, String date, String commentaire, historique_traitement historique) {
        this.id = id;
        this.date = date;
        this.commentaire = commentaire;
        this.historique = historique;
    }

    // Constructeur utilisé lors de l'ajout d'un nouveau suivi
    public suivie_medical(String date, String commentaire, historique_traitement historique, User user) {
        this.date = date;
        this.commentaire = commentaire;
        this.historique = historique;
        this.user = user;
    }

    // Getters et Setters existants
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

    // Getters et setters pour la relation avec User (Medecin)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Pour la rétrocompatibilité avec le code existant
    public int getUserId() {
        return user != null ? user.getId() : 0;
    }

    @Override
    public String toString() {
        return "suivie_medical{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", commentaire='" + (commentaire != null ? commentaire.substring(0, Math.min(20, commentaire.length())) + "..." : "null") + '\'' +
                ", historique=" + (historique != null ? historique.getId() : "null") +
                ", user=" + (user != null ? user.getId() : "null") +
                '}';
    }
}