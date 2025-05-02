package org.example.services;

import org.example.models.Event;
import org.example.models.Reservation;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements iService<Reservation>{

    private Connection cnx = MaConnexion.getInstance().getCnx();

    @Override
    public void ajouter(Reservation reservation) {
        String query = "INSERT INTO reservation (event_id, nomreserv, mail, nbrpersonne,user_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, reservation.getEvent_id());
            ps.setString(2, reservation.getNomreserv());
            ps.setString(3, reservation.getMail());
            ps.setString(4, reservation.getNbrpersonne());
            ps.setInt(5, reservation.getUser_id());

            ps.executeUpdate();
            System.out.println("Réservation ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Reservation reservation) {
        String query = "UPDATE reservation SET event_id = ?, nomreserv = ?, mail = ?, nbrpersonne = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, reservation.getEvent_id());
            ps.setString(2, reservation.getNomreserv());
            ps.setString(3, reservation.getMail());
            ps.setString(4, reservation.getNbrpersonne());
            ps.setInt(5, reservation.getId());

            ps.executeUpdate();
            System.out.println("Réservation mise à jour avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Reservation reservation) {
        String query = "DELETE FROM reservation WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, reservation.getId());
            ps.executeUpdate();
            System.out.println("Réservation supprimée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Reservation> afficher() {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation";

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setEvent_id(rs.getInt("event_id"));
                reservation.setNomreserv(rs.getString("nomreserv"));
                reservation.setMail(rs.getString("mail"));
                reservation.setNbrpersonne(rs.getString("nbrpersonne"));

                reservations.add(reservation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;

    }
    public List<Reservation> getReservationByUser(int id) {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE user_id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setId(rs.getInt("id"));
                    reservation.setEvent_id(rs.getInt("event_id"));
                    reservation.setNomreserv(rs.getString("nomreserv"));
                    reservation.setMail(rs.getString("mail"));
                    reservation.setNbrpersonne(rs.getString("nbrpersonne"));

                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }

    public int getTotalReservedForEvent(int eventId) {
        int total = 0;
        String req = "SELECT SUM(CAST(nbrpersonne AS INTEGER)) FROM reservation WHERE event_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }


}
