package org.example.models;

public class Reservation {

    private int id;
    private int event_id;
    private String nomreserv;
    private String mail;
    private String nbrpersonne;
    // We can add an Event reference
    private Event event;
    private int user_id;
    public int getUser_id() {
        return user_id;
    }

    public Reservation(int event_id, String nomreserv, String mail, String nbrpersonne, int user_id) {
        this.event_id = event_id;
        this.nomreserv = nomreserv;
        this.mail = mail;
        this.nbrpersonne = nbrpersonne;
        this.event = event;
        this.user_id = user_id;
    }

    public Reservation(int id, int event_id, String nomreserv, String mail, String nbrpersonne, int user_id) {
        this.id = id;
        this.event_id = event_id;
        this.nomreserv = nomreserv;
        this.mail = mail;
        this.nbrpersonne = nbrpersonne;
        this.event = event;
        this.user_id = user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }


    public Reservation() {
    }

    public Reservation(int id, int event_id, String mail, String nomreserv, String nbrpersonne) {
        this.id = id;
        this.event_id = event_id;
        this.nomreserv = nomreserv;
        this.mail = mail;
        this.nbrpersonne = nbrpersonne;
    }

    @Override
    public String toString() {
        return "reservation{" +
                "id=" + id +
                ", event_id=" + event_id +
                ", nomreserv='" + nomreserv + '\'' +
                ", mail='" + mail + '\'' +
                ", nbrpersonne='" + nbrpersonne + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNomreserv() {
        return nomreserv;
    }

    public void setNomreserv(String nomreserv) {
        this.nomreserv = nomreserv;
    }

    public String getNbrpersonne() {
        return nbrpersonne;
    }

    public void setNbrpersonne(String nbrpersonne) {
        this.nbrpersonne = nbrpersonne;
    }

    // New methods for Event relationship
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
        if (event != null) {
            this.event_id = event.getId();
        }
    }
}