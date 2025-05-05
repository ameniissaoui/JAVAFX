package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Event {


    private int id;
    private String titre;
    private String dateevent;
    private String lieu;
    private String discription;
    private int nbplace;
    private List<Reservation> reservations = new ArrayList<>();

    public Event(int id, String titre, String dateevent, String lieu, String discription, int nbplace,String image) {
        this.id = id;
        this.titre = titre;
        this.dateevent = dateevent;
        this.lieu = lieu;
        this.discription = discription;
        this.nbplace = nbplace;
        this.image = image;

    }


    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
        // Set the event_id in the reservation
        reservation.setEvent_id(this.id);
    }

    private int latitude;
    private int longitude;
    private String image;
    public Event() {
    }

    // Constructeur
    public Event(int id, String titre, String dateevent, String lieu, String discription,
                 int nbplace, int latitude, int longitude,
                 String image) {
        this.id = id;
        this.titre = titre;
        this.dateevent = dateevent;
        this.lieu = lieu;
        this.discription = discription;
        this.nbplace = nbplace;
        this.latitude = latitude;
        this.longitude = latitude;
        this.image = image;
    }

    public Event(String titre, String dateevent, String lieu, String discription,
                 int nbplace, String image) {
        this.titre = titre;
        this.dateevent = dateevent;
        this.lieu = lieu;
        this.discription = discription;
        this.nbplace = nbplace;
        this.image = image;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", dateevent='" + dateevent + '\'' +
                ", lieu='" + lieu + '\'' +
                ", discription='" + discription + '\'' +
                ", nbplace='" + nbplace + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDateevent() {
        return dateevent;
    }

    public void setDateevent(String dateevent) {
        this.dateevent = dateevent;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    public int getNbplace() {
        return nbplace;
    }

    public void setNbplace(int nbplace) {
        this.nbplace = nbplace;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
