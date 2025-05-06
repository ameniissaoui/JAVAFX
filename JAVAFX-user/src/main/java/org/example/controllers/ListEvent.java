package org.example.controllers;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
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
import javafx.stage.Stage;
import org.example.models.Event;
import org.example.services.EventService;
import java.io.IOException;

public class ListEvent {

    @FXML private FlowPane eventCardsContainer;
    @FXML private AnchorPane mainContent;
    @FXML private Button addEventButton;
    @FXML private Button eventButton, rereservationButton, statistiqueButton;
    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button buttoncommande;
    @FXML private Button tablesButton;

    private final EventService eventService = new EventService();
    private Event selectedEvent;

    @FXML
    void initialize() {
        eventButton.setOnAction(event -> navigate("/fxml/listevent.fxml", event));
        rereservationButton.setOnAction(event -> navigate("/fxml/listreservation.fxml", event));
        statistiqueButton.setOnAction(event -> navigate("/fxml/statistique.fxml", event));
        addEventButton.setOnAction(e -> gotoAddEvent());
        tablesButton.setOnAction(event -> navigate("/fxml/back/showProduit.fxml", event));
        profileButton.setOnAction(event -> navigate("/fxml/admin_profile.fxml", event));
        historique.setOnAction(event -> navigate("/fxml/liste_historique_back.fxml", event));
        suivi.setOnAction(event -> navigate("/fxml/liste_suivi_back.fxml", event));
        buttoncommande.setOnAction(event -> navigate("/fxml/back/showCommande.fxml", event));
        eventCardsContainer.prefWrapLengthProperty().bind(mainContent.widthProperty().subtract(40));
        refreshEvents();
    }

    private void navigate(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page: " + e.getMessage());
        }
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
        VBox card = new VBox(7);
        card.setPadding(Insets.EMPTY);
        card.setPrefWidth(293);
        card.setPrefHeight(340);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        try {
            String imagePath = (event.getImage() != null && !event.getImage().isEmpty())
                    ? "file:src/main/resources/com/example/sante/images/" + event.getImage()
                    : "file:src/main/resources/images/event-default.png";
            imageView.setImage(new Image(imagePath));
        } catch (Exception e) {
            imageView.setImage(new Image("file:src/main/resources/images/event-default.png"));
        }

        Label titleLabel = new Label(event.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#1d4ed8"));
        titleLabel.setPadding(new Insets(5, 10, 0, 10));

        Label dateLabel = new Label("📅 " + event.getDateevent());
        dateLabel.setPadding(new Insets(0, 10, 0, 10));

        Label locationLabel = new Label("📍 " + event.getLieu());
        locationLabel.setPadding(new Insets(0, 10, 0, 10));

        Label descriptionLabel = new Label(event.getDiscription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(240);
        descriptionLabel.setPadding(new Insets(0, 10, 0, 10));
        descriptionLabel.setTextFill(Color.GRAY);
        descriptionLabel.setStyle("-fx-font-size: 12px;");

        Label placesLabel = new Label("👥 Places: " + event.getNbplace());
        placesLabel.setPadding(new Insets(0, 10, 0, 10));

        Button updateBtn = new Button("Modifier");
        updateBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> {
            selectedEvent = event;
            onModifierClick();
        });

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            selectedEvent = event;
            delete();
        });

        HBox buttonsBox = new HBox(10, updateBtn, deleteBtn);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 10, 0));

        card.getChildren().addAll(imageView, titleLabel, dateLabel, locationLabel, descriptionLabel, placesLabel, buttonsBox);
        return card;
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void delete() {
        if (selectedEvent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setContentText("Voulez-vous vraiment supprimer cet événement ?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        eventService.supprimer(selectedEvent);
                        envoyerSms("+21699573310", "Votre réservation a été annulée !");
                        refreshEvents();
                    } catch (Exception e) {
                        showErrorDialog("Erreur", "Suppression échouée.");
                    }
                }
            });
        }
    }

    @FXML
    public void onModifierClick() {
        if (selectedEvent != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/updateEvent.fxml"));
                Parent root = loader.load();
                AddEvent controller = loader.getController();
                controller.loadEvent(selectedEvent);
                mainContent.getChildren().setAll(root);
            } catch (IOException e) {
                showErrorDialog("Erreur", "Impossible d’ouvrir le formulaire de modification.");
            }
        }
    }

    @FXML
    public void gotoAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addevent.fxml"));
            BorderPane addEventContent = loader.load();
            mainContent.getChildren().setAll(addEventContent);
        } catch (IOException e) {
            showErrorDialog("Erreur", "Ajout impossible.");
        }
    }

    public static final String ACCOUNT_SID = "ACc14792eaf6284c78e43de3ecfbae72bf";
    public static final String AUTH_TOKEN = "b9cf5f8ade3e29e51e64a6132ec85770";

    private void envoyerSms(String to, String message) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message.creator(new PhoneNumber(to), new PhoneNumber("+18602482399"), message).create();
        } catch (Exception e) {
            System.err.println("Erreur SMS: " + e.getMessage());
        }
    }
    @FXML
    private void handleStatisticsRedirect(ActionEvent event) {
        navigate("/fxml/statistics-view.fxml", event);
    }

    @FXML
    private void handleStatisticsCommandeRedirect(ActionEvent event) {
        navigate("/fxml/back/statistics.fxml", event);
    }

    @FXML
    private void navigateToReportDashboard(ActionEvent event) {
        navigate("/fxml/AdminDashboard.fxml", event);
    }

    @FXML
    private void handleProfileRedirect(ActionEvent event) {
        navigate("/fxml/admin_profile.fxml", event);
    }


}
