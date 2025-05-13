package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class historique_traitement {
    private int id;
    private String nom;
    private String prenom;
    private String maladies;
    private String description;
    private String type_traitement;
    private String bilan;

    // Relation ManyToOne avec User (plus spécifiquement Patient)
    private User user;  // Cette relation est correcte

    // Liste pour la relation OneToMany avec suivie_medical
    private List<suivie_medical> suivisMedicaux = new ArrayList<>();

    public historique_traitement() {}

    public historique_traitement(String description, String maladies, String prenom, String nom, int id, String type_traitement, String bilan) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.maladies = maladies;
        this.description = description;
        this.type_traitement = type_traitement;
        this.bilan = bilan;
    }

    public historique_traitement(String nom, String prenom, String maladies, String description, String type_traitement, String bilan) {
        this.nom = nom;
        this.prenom = prenom;
        this.maladies = maladies;
        this.description = description;
        this.type_traitement = type_traitement;
        this.bilan = bilan;
    }

    // Les getters et setters existants...
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

    public String getMaladies() {
        return maladies;
    }

    public void setMaladies(String maladies) {
        this.maladies = maladies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType_traitement() {
        return type_traitement;
    }

    public void setType_traitement(String type_traitement) {
        this.type_traitement = type_traitement;
    }

    public String getBilan() {
        return bilan;
    }

    public void setBilan(String bilan) {
        this.bilan = bilan;
    }

    // Getters et setters pour la relation avec User (Patient)
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

    // Getters et setters pour la relation OneToMany avec suivie_medical
    public List<suivie_medical> getSuivisMedicaux() {
        return suivisMedicaux;
    }

    public void setSuivisMedicaux(List<suivie_medical> suivisMedicaux) {
        this.suivisMedicaux = suivisMedicaux;
    }

    // Méthode pratique pour ajouter un suivi médical
    public void addSuiviMedical(suivie_medical suivi) {
        suivisMedicaux.add(suivi);
        suivi.setHistorique(this);
    }

    // Méthode pratique pour supprimer un suivi médical
    public void removeSuiviMedical(suivie_medical suivi) {
        suivisMedicaux.remove(suivi);
        suivi.setHistorique(null);
    }

    @Override
    public String toString() {
        return "historique_traitement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", maladies='" + maladies + '\'' +
                ", description='" + description + '\'' +
                ", type_traitement='" + type_traitement + '\'' +
                ", bilan='" + bilan + '\'' +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", nombre de suivis=" + (suivisMedicaux != null ? suivisMedicaux.size() : 0) +
                '}';
    }
}