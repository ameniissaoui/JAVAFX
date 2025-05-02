package org.example.services;

import org.example.models.Event;
import org.example.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventService implements iService<Event>{


    private Connection cnx = MaConnexion.getInstance().getCnx();

    @Override
    public void ajouter(Event event) {
        String req = "INSERT INTO event (titre, dateevent, lieu, discription , nbplace,image) VALUES (?, ?, ?, ?, ?,?)";
        try {

            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, event.getTitre());
            pst.setString(2, event.getDateevent());
            pst.setString(3, event.getLieu());
            pst.setString(4, event.getDiscription());
            pst.setInt(5, event.getNbplace());
            pst.setString(6, event.getImage());
            pst.executeUpdate();
            System.out.println("Événement ajouté avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Event event) {
        String req = "UPDATE event SET titre=?, dateevent=?, lieu=?, discription=?, nbplace=?, image=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, event.getTitre());
            pst.setString(2, event.getDateevent());
            pst.setString(3, event.getLieu());
            pst.setString(4, event.getDiscription());
            pst.setInt(5, event.getNbplace());
            pst.setString(6, event.getImage());
            pst.setInt(7, event.getId()); // Added missing parameter for id
            pst.executeUpdate();
            System.out.println("Événement modifié avec succes");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Event event) {
        String req = "DELETE FROM event WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) { // Try-with-resources
            pst.setInt(1, event.getId());

            int rowsAffected = pst.executeUpdate(); // Vérification du nombre de lignes affectées
            if (rowsAffected > 0) {
                System.out.println("Événement supprimé avec succès !");
            } else {
                System.out.println("Aucun événement trouvé avec cet ID.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'événement : " + e.getMessage());
            e.printStackTrace(); // Pour plus de détails en cas d'erreur
        }
    }

    @Override
    public List<Event> afficher() {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM event";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Event ev = new Event(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("dateevent"),
                        rs.getString("lieu"),
                        rs.getString("discription"),
                        rs.getInt("nbplace"),
                        rs.getString("image")
                );
                events.add(ev);
            }
            System.out.println("✅ Liste des événements récupérée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'affichage des événements : " + e.getMessage());
        }
        return events;
    }

    // New method to get an event by its ID
    public Event getById(int id) {
        String req = "SELECT * FROM event WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Event(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("dateevent"),
                        rs.getString("lieu"),
                        rs.getString("discription"),
                        rs.getInt("nbplace"),
                        rs.getString("image")
                );
            } else {
                System.out.println("Aucun événement trouvé avec l'ID: " + id);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'événement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void update(Event event) {
        String req = "UPDATE event SET titre = ?, dateevent = ?, lieu = ?, discription = ?, nbplace = ?, latitude = ?, longitude = ?, image = ? WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            // Définir les paramètres de la requête
            pst.setString(1, event.getTitre());
            pst.setString(2, event.getDateevent());
            pst.setString(3, event.getLieu());
            pst.setString(4, event.getDiscription());
            pst.setInt(5, event.getNbplace());
            pst.setInt(6, event.getLatitude());
            pst.setInt(7, event.getLongitude());
            pst.setString(8, event.getImage());
            pst.setInt(9, event.getId()); // Le critère pour l'UPDATE est l'ID

            int rowsAffected = pst.executeUpdate(); // Exécuter la mise à jour
            if (rowsAffected > 0) {
                System.out.println("Événement mis à jour avec succès !");
            } else {
                System.out.println("Aucun événement trouvé avec cet ID pour la mise à jour.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'événement : " + e.getMessage());
            e.printStackTrace(); // Log de l'erreur
        }
    }

    public Map<String, Long> getNombreEvenementsParLieu() {
        List<Event> events = afficher(); // ou toute méthode qui retourne la liste des événements
        return events.stream()
                .collect(Collectors.groupingBy(Event::getLieu, Collectors.counting()));
    }
}