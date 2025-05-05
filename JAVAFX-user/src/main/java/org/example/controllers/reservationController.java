package org.example.controllers;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.stage.Screen;
import org.example.models.*;
import org.example.services.EmailService;
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
import org.example.util.MaConnexion;
import org.example.util.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            // Retrieve the current admin from SessionManager
            SessionManager session = SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            int createdById;
            if (currentUser instanceof Patient) {
                // Si l'utilisateur est un Patient
                createdById = ((Patient) currentUser).getId();
            } else if (currentUser instanceof Medecin) {
                // Si l'utilisateur est un Médecin
                createdById = ((Medecin) currentUser).getId();
            } else {
                // Si ce n'est ni un Patient ni un Médecin, on ne fait rien ou on peut lancer une exception
                return;
            }

                Reservation reservation = new Reservation();
                reservation.setEvent_id(eventId);
                reservation.setNomreserv(name);
                reservation.setMail(email);
                reservation.setNbrpersonne(peopleCount);
                reservation.setUser_id(createdById);
                reservationService.ajouter(reservation);

                showSuccess("Réservation effectuée avec succès pour l'événement: " +
                        (currentEvent != null ? currentEvent.getTitre() : ""));

                // Envoi de l'email de confirmation avec PDF
                EmailService emailService = new EmailService();
                String subject = "Confirmation de réservation";
                String body = "Bonjour " + name + ",\n\n" +
                        "Votre réservation pour l'événement \"" + (currentEvent != null ? currentEvent.getTitre() : "") + "\" a été confirmée.\n\n" +
                        "Détails de la réservation:\n" +
                        "Nom: " + name + "\n" +
                        "Email: " + email + "\n" +
                        "Nombre de personnes: " + peopleCount + "\n\n" +
                        "Merci de votre réservation!\n\n" +
                        "Cordialement,\nL'équipe de réservation";

                // Utiliser la méthode modifiée qui envoie l'email avec les détails de l'événement dans le QR code
                emailService.sendEmailWithPDF(email, subject, body, reservation, currentEvent);

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
    public static final String ACCOUNT_SID = "ACc14792eaf6284c78e43de3ecfbae72bf";
    public static final String AUTH_TOKEN = "b9cf5f8ade3e29e51e64a6132ec85770";
    private void envoyerSms(String destinataire, String messageTexte) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            Message message = Message.creator(
                    new PhoneNumber(destinataire),
                    new PhoneNumber("+18602482399"), // Ton numéro Twilio
                    messageTexte
            ).create();

            System.out.println("SMS envoyé ! SID : " + message.getSid());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l’envoi du SMS.");
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
    // Navigation methods from SuccessController
    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }

    @FXML
    void navigateToHome() {
        navigateTo("/fxml/front/home.fxml");
    }

    @FXML
    void navigateToHistoriques() {
        navigateTo("/fxml/historiques.fxml");
    }

    @FXML
    void redirectToDemande() {
        navigateTo("/fxml/DemandeDashboard.fxml");
    }

    @FXML
    void redirectToRendezVous() {
        navigateTo("/fxml/rendez-vous-view.fxml");
    }

    @FXML
    void redirectProduit() {
        navigateTo("/fxml/front/showProduit.fxml");
    }

    @FXML
    void navigateToTraitement() {
        navigateTo("/fxml/traitement.fxml");
    }

    @FXML
    void viewDoctors() {
        navigateTo("/fxml/DoctorList.fxml");
    }

    @FXML
    void navigateToContact() {
        navigateTo("/fxml/front/contact.fxml");
    }

    @FXML
    void navigateToProfile() {
        navigateTo("/fxml/front/profile.fxml");
    }

    @FXML
    void navigateToFavorites() {
        navigateTo("/fxml/front/favoris.fxml");
    }

    @FXML
    void commande() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToCommandes() {
        navigateTo("/fxml/front/ShowCommande.fxml");
    }

    @FXML
    void navigateToShop() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToEvent() {
        navigateTo("/fxml/eventFront.fxml");
    }
    @FXML
    void navigateToReservation() {
        navigateTo("/fxml/reservationFront.fxml");
    }
    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            System.out.println("Attempting to navigate to " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException(fxmlPath + " resource not found");
            }
            Parent root = loader.load();

            // Get the current stage
            Stage stage = null;
            if (submitButton != null && submitButton.getScene() != null) {
                stage = (Stage) submitButton.getScene().getWindow();
            } else if (submitButton != null && submitButton.getScene() != null) {
                stage = (Stage) submitButton.getScene().getWindow();
            }

            if (stage == null) {
                throw new RuntimeException("Cannot get current stage");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
            System.out.println("Successfully navigated to " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("Unexpected error during navigation: " + e.getMessage());
            e.printStackTrace();

        }
    }
}