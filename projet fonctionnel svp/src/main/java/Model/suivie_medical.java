package Model;

public class suivie_medical {
    private int id;
    private String date;
    private String commentaire;
    private int id_historique_id;

    public suivie_medical() {
    }

    public suivie_medical(int id, String date, String commentaire, int historiqueId) {
        this.id = id;
        this.date = date;
        this.commentaire = commentaire;
        this.id_historique_id = historiqueId;
    }

    // Constructeur utilisé lors de l'ajout d'un nouveau suivi
    public suivie_medical(String date, String commentaire, historique_traitement historique) {
        this.date = date;
        this.commentaire = commentaire;
        this.id_historique_id = historique.getId();
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

    public int getHistoriqueId() {
        return id_historique_id;
    }

    public void setHistoriqueId(int historiqueId) {
        this.id_historique_id = historiqueId;
    }

    @Override
    public String toString() {
        return "suivie_medical{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", historiqueId=" + id_historique_id +
                '}';
    }


    public void setHistorique(historique_traitement historique) {
        if (historique == null) {
            this.id_historique_id = 0;  // Ou une autre valeur par défaut/invalide
        } else {
            this.id_historique_id = historique.getId();
        }
    }
}