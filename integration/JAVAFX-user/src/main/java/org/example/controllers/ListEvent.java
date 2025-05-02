package org.example.controllers;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.example.models.Event;
import org.example.services.EmailService;
import org.example.services.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.util.SessionManager;

import java.io.IOException;

import static org.example.util.NotificationUtil.showAlert;

public class ListEvent {

    @FXML private FlowPane eventCardsContainer;
    @FXML private AnchorPane mainContent;
    @FXML private Button eventButton;
    @FXML private Button rereservationButton;
    @FXML private Button statistiqueButton;
    @FXML private Button addEventButton;

    private final EventService eventService = new EventService();
    private Event selectedEvent;
    @FXML
    private void navigateToReportDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminReportDashboard.fxml", event);
    }
    @FXML
    private void handleStatisticsRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/statistics-view.fxml", event);
    }

    @FXML
    private void handleStatisticsCommandeRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/back/statistics.fxml", event);
    }

    @FXML
    void initialize() {
        // Configuration des boutons de navigation
        eventButton.setOnAction(event -> {
            try {
                handleeventRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        rereservationButton.setOnAction(event -> {
            try {
                handlereservationRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        statistiqueButton.setOnAction(event -> {
            try {
                handlestatRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Configuration du bouton Ajouter
        if (addEventButton != null) {
            try {
                ImageView addIcon = new ImageView(new Image("file:src/main/resources/images/addm.png"));
                addIcon.setFitWidth(14);
                addIcon.setFitHeight(14);
                addEventButton.setGraphic(addIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        refreshEvents();
    }

    private void handleeventRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listevent.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handlestatRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistique.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handlereservationRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listreservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void refreshEvents() {
        ObservableList<Event> eventList = FXCollections.observableArrayList(eventService.afficher());
        eventCardsContainer.getChildren().clear();

        for (Event event : eventList) {
            VBox card = createEventCard(event);
            eventCardsContainer.getChildren().add(card);
        }
    }

    private VBox createEventCard(Event event) {
        // Création de la carte principale
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setPrefHeight(330);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Image de l'événement
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Chargement de l'image
        try {
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                String path = "file:src/main/resources/com/example/sante/images/" + event.getImage();
                Image image = new Image(path, true);
                imageView.setImage(image);
            } else {
                // Image par défaut si aucune image n'est disponible
                Image defaultImage = new Image("file:src/main/resources/images/event-default.png", true);
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            // Image par défaut en cas d'erreur
            Image defaultImage = new Image("file:src/main/resources/images/event-default.png", true);
            imageView.setImage(defaultImage);
            e.printStackTrace();
        }

        // Conteneur pour l'image avec un fond bleu en haut
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(130);
        imageContainer.setStyle("-fx-background-color: #0077cc; -fx-background-radius: 8 8 0 0;");
        imageContainer.getChildren().add(imageView);

        // Titre de l'événement
        Label titleLabel = new Label(event.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #0077cc;");
        titleLabel.setWrapText(true);
        titleLabel.setPrefWidth(220);

        // Date et lieu avec des icônes
        HBox dateBox = createInfoBox("M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zM5 6v2h14V6H5zm2 4h5v5H7v-5z", event.getDateevent());
        HBox lieuBox = createInfoBox("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z", event.getLieu());

        // Description avec limitation
        Text descriptionText = new Text(event.getDiscription());
        descriptionText.setWrappingWidth(220);
        descriptionText.setStyle("-fx-fill: #666666;");

        ScrollPane descriptionScroll = new ScrollPane(descriptionText);
        descriptionScroll.setPrefHeight(60);
        descriptionScroll.setFitToWidth(true);
        descriptionScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Nombre de places disponibles
        HBox placesBox = createInfoBox("M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3z", "Places: " + event.getNbplace());

        // Boutons d'action avec icônes
        HBox actionButtons = new HBox();
        actionButtons.setSpacing(10);
        actionButtons.setAlignment(Pos.CENTER);

        // Bouton Modifier avec icône
        Button updateBtn = new Button("Modifier");
        updateBtn.setStyle("-fx-background-color: #0077cc; -fx-text-fill: white; -fx-background-radius: 4;");
        updateBtn.setPrefWidth(100);
        try {
            ImageView updateIcon = new ImageView(new Image("file:src/main/resources/images/modifier.png"));
            updateIcon.setFitWidth(16);
            updateIcon.setFitHeight(16);
            updateBtn.setGraphic(updateIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Bouton Supprimer avec icône
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 4;");
        deleteBtn.setPrefWidth(100);
        try {
            ImageView deleteIcon = new ImageView(new Image("file:src/main/resources/images/deletem.png"));
            deleteIcon.setFitWidth(16);
            deleteIcon.setFitHeight(16);
            deleteBtn.setGraphic(deleteIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateBtn.setOnAction(e -> {
            selectedEvent = event;
            onModifierClick();
        });

        deleteBtn.setOnAction(e -> {
            selectedEvent = event;
            delete();
        });

        actionButtons.getChildren().addAll(updateBtn, deleteBtn);

        // Ajout de tous les éléments à la carte
        card.getChildren().addAll(
                imageContainer,
                titleLabel,
                dateBox,
                lieuBox,
                descriptionScroll,
                placesBox,
                actionButtons
        );

        return card;
    }

    private HBox createInfoBox(String svgPath, String text) {
        HBox hbox = new HBox();
        hbox.setSpacing(8);
        hbox.setAlignment(Pos.CENTER_LEFT);

        // Création de l'icône SVG
        Region icon = new Region();
        icon.setPrefSize(16, 16);
        icon.setStyle("-fx-background-color: #0077cc; -fx-shape: '" + svgPath + "';");

        // Texte
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #555555;");
        label.setWrapText(true);

        hbox.getChildren().addAll(icon, label);
        return hbox;
    }

    @FXML
    void delete() {
        if (selectedEvent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Event");
            alert.setContentText("Are you sure you want to delete the selected event?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        EmailService emailService = new EmailService();
                        String subject = "votre event est annule";
                        String body = "Bonjour " +  ",\n\n" +
                                "votre event est annule\"" +
                                "merci pour votre comprehension:\n" +
                                "Merci de votre réservation!\n\n" +
                                "Cordialement,\nL'équipe de réservation";

                        envoyerSms("+21699573310", "Votre réservation a été annulee !");
                        eventService.supprimer(selectedEvent);
                        refreshEvents(); // Rafraîchir la vue après suppression

                    } catch (Exception e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Deletion Failed");
                        errorAlert.setContentText("An error occurred while deleting the event.");
                        errorAlert.showAndWait();
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Event Selected");
            alert.setContentText("Please select an event to delete.");
            alert.showAndWait();
        }
    }

    @FXML
    public void onModifierClick() {
        if (selectedEvent != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/updateEvent.fxml"));
                Parent root = loader.load();
                AddEvent modifierEventController = loader.getController();
                modifierEventController.loadEvent(selectedEvent);
                mainContent.getChildren().setAll(root);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Erreur lors de l'ouverture du formulaire de modification.");
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un événement à modifier.");
            alert.show();
        }
    }

    @FXML
    void gotoevent(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listEvent.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void gotofront(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventFront.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void gotoreservation(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listReservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void gotoAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addevent.fxml"));
            BorderPane addEventContent = loader.load();
            mainContent.getChildren().setAll(addEventContent);
        } catch (IOException e) {
            e.printStackTrace();
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
            System.out.println("Erreur lors de l'envoi du SMS.");
        }
    }
}