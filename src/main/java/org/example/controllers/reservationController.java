package org.example.controllers;

import org.example.models.Event;
import org.example.models.Reservation;
import org.example.services.EventService;
import org.example.services.ReservationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class reservationController {

    @FXML
    private TextField nameField; // Champ pour le nom du réservant
    @FXML
    private TextField emailField; // Champ pour l'email
    @FXML
    private TextField peopleCountField; // Champ pour le nombre de personnes
    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;

    // New FXML elements for event information
    @FXML
    private VBox eventInfoBox;
    @FXML
    private Label eventTitleLabel;
    @FXML
    private Label eventDateLabel;
    @FXML
    private Label eventLocationLabel;

    private Reservation currentReservation; // Référence à la réservation à modifier
    private int eventId; // Variable pour stocker l'ID de l'événement
    private Event currentEvent; // Variable pour stocker l'événement actuel
    private boolean isEditMode = false; // Variable pour savoir si on est en mode modification
    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        // Initialize empty form
        if (eventInfoBox != null) {
            eventInfoBox.setVisible(false); // Hide until we have event data
        }
    }

    @FXML
    void eventfront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventFront.fxml")); // Remplace par le bon fichier
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void reservationfront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservationFront.fxml")); // Remplace par le bon fichier
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour initialiser le formulaire avec une réservation (si mode modification)
    public void initialize(Reservation reservation, boolean isEditMode) {
        this.isEditMode = isEditMode;

        if (isEditMode) {
            // Si c'est en mode édition, on charge les données de la réservation
            this.currentReservation = reservation;
            nameField.setText(reservation.getNomreserv());
            emailField.setText(reservation.getMail());
            peopleCountField.setText(String.valueOf(reservation.getNbrpersonne()));
            submitButton.setText("Modifier");

            // Get event information
            loadEventData(reservation.getEvent_id());
        } else {
            // Si c'est en mode ajout, on réinitialise les champs
            submitButton.setText("Ajouter");
        }
    }

    // Méthode pour passer l'event_id et charger les informations de l'événement
    public void setEventId(int eventId) {
        this.eventId = eventId;
        loadEventData(eventId);
    }

    // Méthode pour charger les données de l'événement
    private void loadEventData(int eventId) {
        try {
            // Récupérer les détails de l'événement
            currentEvent = eventService.getById(eventId);

            if (currentEvent != null && eventTitleLabel != null) {
                // Afficher les informations de l'événement
                eventTitleLabel.setText(currentEvent.getTitre());
                eventDateLabel.setText("Date: " + formatDate(currentEvent.getDateevent()));
                eventLocationLabel.setText("Lieu: " + currentEvent.getLieu());
                eventInfoBox.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des informations de l'événement.");
        }
    }

    // Fonction pour formater la date (réutilisée de eventFrontController)
    private String formatDate(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            String day = parts[2]; // Jour
            String month = parts[1]; // Mois (numérique)

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int monthIndex = Integer.parseInt(month) - 1; // Convertir en index

            return day + " " + months[monthIndex] + " " + parts[0]; // Format plus lisible: "14 Feb 2023"
        } catch (Exception e) {
            return dateStr; // En cas d'erreur, afficher la date originale
        }
    }

    private boolean validateFields() {
        String name = nameField.getText();
        String email = emailField.getText();
        String peopleCount = peopleCountField.getText();

        if (name.isEmpty() || email.isEmpty() || peopleCount.isEmpty()) {
            showAlert("Tous les champs doivent être remplis.");
            return false;
        }

        try {
            Integer.parseInt(peopleCount); // Vérification du nombre de personnes
        } catch (NumberFormatException e) {
            showAlert("Le nombre de personnes doit être un nombre valide.");
            return false;
        }

        // Validation de l'email avec une expression régulière
        if (!isValidEmail(email)) {
            showAlert("L'email n'est pas valide. Veuillez entrer un email valide.");
            return false;
        }

        return true;
    }

    // Méthode pour valider le format de l'email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    // Méthode pour afficher une alerte d'erreur
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    // Méthode pour afficher un message de succès
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    // Méthode pour gérer la soumission de la réservation
    @FXML
    private void handleReservation() {
        if (!validateFields()) {
            return; // Ne pas continuer si la validation échoue
        }

        String name = nameField.getText();
        String email = emailField.getText();
        String peopleCount = peopleCountField.getText();

        ReservationService reservationService = new ReservationService();

        if (isEditMode) {
            Reservation reservation = new Reservation();
            reservation.setEvent_id(currentReservation.getEvent_id());
            reservation.setNomreserv(name);
            reservation.setMail(email);
            reservation.setNbrpersonne(peopleCount);
            // Si on est en mode modification, on met à jour la réservation
            reservation.setId(currentReservation.getId()); // On conserve l'ID pour la mise à jour
            reservationService.modifier(reservation);

            showSuccess("Réservation modifiée avec succès!");

            try {
                // Charger le FXML de la fenêtre des réservations
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservationFront.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root, 1200, 700);
                Stage stage = (Stage) submitButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Reservation reservation = new Reservation();
            reservation.setEvent_id(eventId);
            reservation.setNomreserv(name);
            reservation.setMail(email);
            reservation.setNbrpersonne(peopleCount);

            // Si on est en mode ajout, on crée une nouvelle réservation
            reservationService.ajouter(reservation);

            showSuccess("Réservation effectuée avec succès pour l'événement: " +
                    (currentEvent != null ? currentEvent.getTitre() : ""));

            try {
                // Charger le FXML de la fenêtre des réservations
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservationFront.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1200, 700);
                Stage stage = (Stage) submitButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour annuler l'ajout ou la modification
    @FXML
    public void cancelAction() {
        try {
            // Retourner à la liste des événements
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventFront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 700);
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}