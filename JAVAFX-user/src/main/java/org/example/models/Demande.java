package org.example.models;

import java.time.LocalDateTime;

public class Demande {
    private int id;
    private LocalDateTime date;
    private float eau;
    private int nbr_repas;
    private boolean snacks;
    private Float calories;
    private String activity;
    private String sommeil;
    private float duree_activite;
    private int patient_id;

    // Default constructor
    public Demande() {
    }

    // Parameterized constructor
    public Demande(int id, LocalDateTime date, float eau, int nbr_repas, boolean snacks, 
                  Float calories, String activity, String sommeil, 
                  float duree_activite, int patient_id) {
        this.id = id;
        this.date = date;
        this.eau = eau;
        this.nbr_repas = nbr_repas;
        this.snacks = snacks;
        this.calories = calories;
        this.activity = activity;
        this.sommeil = sommeil;
        this.duree_activite = duree_activite;
        this.patient_id = patient_id;
    }

    // Constructor without ID for new records
    public Demande(LocalDateTime date, float eau, int nbr_repas, boolean snacks, 
                  Float calories, String activity, String sommeil, 
                  float duree_activite, int patient_id) {
        this.date = date;
        this.eau = eau;
        this.nbr_repas = nbr_repas;
        this.snacks = snacks;
        this.calories = calories;
        this.activity = activity;
        this.sommeil = sommeil;
        this.duree_activite = duree_activite;
        this.patient_id = patient_id;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public float getEau() {
        return eau;
    }

    public void setEau(float eau) {
        this.eau = eau;
    }

    public int getNbr_repas() {
        return nbr_repas;
    }

    public void setNbr_repas(int nbr_repas) {
        this.nbr_repas = nbr_repas;
    }

    public boolean isSnacks() {
        return snacks;
    }

    public void setSnacks(boolean snacks) {
        this.snacks = snacks;
    }

    public Float getCalories() {
        return calories;
    }

    public void setCalories(Float calories) {
        this.calories = calories;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getSommeil() {
        return sommeil;
    }

    public void setSommeil(String sommeil) {
        this.sommeil = sommeil;
    }

    public float getDuree_activite() {
        return duree_activite;
    }

    public void setDuree_activite(float duree_activite) {
        this.duree_activite = duree_activite;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }

    @Override
    public String toString() {
        return "Demande{" +
                "id=" + id +
                ", date=" + date +
                ", eau=" + eau +
                ", nbr_repas=" + nbr_repas +
                ", snacks=" + snacks +
                ", calories=" + calories +
                ", activity='" + activity + '\'' +
                ", sommeil='" + sommeil + '\'' +
                ", duree_activite=" + duree_activite +
                ", patient_id=" + patient_id +
                '}';
    }
} 