package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Planning {
    private int id;
    private String jour;
    private LocalTime heuredebut;
    private LocalTime heurefin;

    // Constructeur par défaut
    public Planning() {
    }

    // Constructeur avec paramètres
    public Planning(int id, String jour, LocalTime heuredebut, LocalTime heurefin) {
        this.id = id;
        this.jour = jour;
        this.heuredebut = heuredebut;
        this.heurefin = heurefin;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJour() {
        return jour;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public LocalTime getHeuredebut() {
        return heuredebut;
    }

    public void setHeuredebut(LocalTime heuredebut) {
        this.heuredebut = heuredebut;
    }

    public LocalTime getHeurefin() {
        return heurefin;
    }

    public void setHeurefin(LocalTime heurefin) {
        this.heurefin = heurefin;
    }

    // Méthode toString pour l'affichage
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return jour + " (" + heuredebut.format(formatter) + " - " + heurefin.format(formatter) + ")";
    }
}