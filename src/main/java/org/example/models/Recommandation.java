package org.example.models;

public class Recommandation {
    private int id;
    private int demande_id;
    private String petit_dejeuner;
    private String dejeuner;
    private String diner;
    private String activity;
    private Float calories;
    private Float duree;
    private String supplements;

    // Default constructor
    public Recommandation() {
    }

    // Parameterized constructor with ID
    public Recommandation(int id, int demande_id, String petit_dejeuner, String dejeuner, String diner, 
                         String activity, Float calories, Float duree, String supplements) {
        this.id = id;
        this.demande_id = demande_id;
        this.petit_dejeuner = petit_dejeuner;
        this.dejeuner = dejeuner;
        this.diner = diner;
        this.activity = activity;
        this.calories = calories;
        this.duree = duree;
        this.supplements = supplements;
    }

    // Constructor without ID for new records
    public Recommandation(int demande_id, String petit_dejeuner, String dejeuner, String diner, 
                         String activity, Float calories, Float duree, String supplements) {
        this.demande_id = demande_id;
        this.petit_dejeuner = petit_dejeuner;
        this.dejeuner = dejeuner;
        this.diner = diner;
        this.activity = activity;
        this.calories = calories;
        this.duree = duree;
        this.supplements = supplements;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDemande_id() {
        return demande_id;
    }

    public void setDemande_id(int demande_id) {
        this.demande_id = demande_id;
    }

    public String getPetit_dejeuner() {
        return petit_dejeuner;
    }

    public void setPetit_dejeuner(String petit_dejeuner) {
        this.petit_dejeuner = petit_dejeuner;
    }
    
    // For backward compatibility with existing code
    public String getPetitDejeuner() {
        return petit_dejeuner;
    }
    
    // For backward compatibility with existing code
    public void setPetitDejeuner(String petit_dejeuner) {
        this.petit_dejeuner = petit_dejeuner;
    }

    public String getDejeuner() {
        return dejeuner;
    }

    public void setDejeuner(String dejeuner) {
        this.dejeuner = dejeuner;
    }

    public String getDiner() {
        return diner;
    }

    public void setDiner(String diner) {
        this.diner = diner;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Float getCalories() {
        return calories;
    }

    public void setCalories(Float calories) {
        this.calories = calories;
    }

    public Float getDuree() {
        return duree;
    }

    public void setDuree(Float duree) {
        this.duree = duree;
    }

    public String getSupplements() {
        return supplements;
    }

    public void setSupplements(String supplements) {
        this.supplements = supplements;
    }

    @Override
    public String toString() {
        return "Recommandation{" +
                "id=" + id +
                ", demande_id=" + demande_id +
                ", petit_dejeuner='" + petit_dejeuner + '\'' +
                ", dejeuner='" + dejeuner + '\'' +
                ", diner='" + diner + '\'' +
                ", activity='" + activity + '\'' +
                ", calories=" + calories +
                ", duree=" + duree +
                ", supplements='" + supplements + '\'' +
                '}';
    }
} 