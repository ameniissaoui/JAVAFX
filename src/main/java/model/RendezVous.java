package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RendezVous {
    private int id;
    private int planning_id;
    private LocalDateTime dateheure;
    private String statut;
    private String description;
    private Planning planning; // Pour stocker les informations du planning associé

    public RendezVous() {
    }

    public RendezVous(int id, int planning_id, LocalDateTime dateheure, String statut, String description) {
        this.id = id;
        this.planning_id = planning_id;
        this.dateheure = dateheure;
        this.statut = statut;
        this.description = description;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlanning_id() {
        return planning_id;
    }

    public void setPlanning_id(int planning_id) {
        this.planning_id = planning_id;
    }

    public LocalDateTime getDateheure() {
        return dateheure;
    }

    public void setDateheure(LocalDateTime dateheure) {
        this.dateheure = dateheure;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Planning getPlanning() {
        return planning;
    }

    public void setPlanning(Planning planning) {
        this.planning = planning;
    }

    // Méthodes utilitaires
    public String getFormattedDateTime() {
        if (dateheure == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateheure.format(formatter);
    }

    public String getFormattedDate() {
        if (dateheure == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateheure.format(formatter);
    }

    public String getFormattedTime() {
        if (dateheure == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateheure.format(formatter);
    }

    // Méthode pour obtenir les informations du planning associé
    public String getPlanningInfo() {
        if (planning == null) return "";
        return planning.getJour() + "\n" + planning.getHeuredebut().format(DateTimeFormatter.ofPattern("HH:mm"))
                + " - " + planning.getHeurefin().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String googleEventId;

    public String getGoogleEventId() {
        return googleEventId;
    }

    public void setGoogleEventId(String googleEventId) {
        this.googleEventId = googleEventId;
    }
}