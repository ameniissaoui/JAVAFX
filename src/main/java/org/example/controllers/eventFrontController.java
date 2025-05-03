package org.example.controllers;

import org.example.models.Event;
import org.example.services.EventService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class eventFrontController {
    @FXML
    private GridPane eventGridPane;
    @FXML
    private ScrollPane scrollPane;
    private final EventService eventService = new EventService();

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

    @FXML
    void initialize() {
        List<Event> events = eventService.afficher(); // Récupérer les événements

        eventGridPane.getChildren().clear();
        eventGridPane.setAlignment(Pos.CENTER);
        eventGridPane.setHgap(20); // Espacement horizontal
        eventGridPane.setVgap(20); // Espacement vertical

        int column = 0;
        int row = 0;

        for (Event event : events) {
            VBox card = createEventCard(event);
            eventGridPane.add(card, column, row);
            column++;

            if (column == 3) { // 3 cartes par ligne
                column = 0;
                row++;
            }
        }

        // Assurer un scroll fluide et adapter à la largeur
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(eventGridPane);
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox();
        card.setPadding(new Insets(10));
        card.setSpacing(8);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setAlignment(Pos.CENTER);

        // Charger l'image
        String imagePath = "file:src/main/resources/com/example/sante/images/" + event.getImage();
        ImageView imageView = new ImageView(new Image(imagePath));
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // ---- Badge de la date en bleu ----
        String formattedDate = formatDate(event.getDateevent()); // Convertir la date
        Label dateBadge = new Label(formattedDate);
        dateBadge.setStyle("-fx-background-color: #3fbbc0; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 8px; -fx-background-radius: 10px;");

        StackPane badgeContainer = new StackPane();
        badgeContainer.getChildren().add(dateBadge);
        StackPane.setAlignment(dateBadge, Pos.TOP_RIGHT);  // Alignement à droite

        // Labels
        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3fbbc0;");

        Label location = new Label(event.getLieu());
        location.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        Label description = new Label(event.getDiscription().length() > 80 ? event.getDiscription().substring(0, 80) + "..." : event.getDiscription());
        description.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        description.setWrapText(true);

        // Boutons avec icônes
        ImageView detailsIcon = new ImageView(new Image("file:src/main/resources/com/example/sante/images/details.png"));
        detailsIcon.setFitWidth(16);
        detailsIcon.setFitHeight(16);

        Button detailsButton = new Button("Voir Détails", detailsIcon);
        detailsButton.setStyle("-fx-background-color: #3fbbc0; -fx-text-fill: white; -fx-font-size: 12px;");
        detailsButton.setOnAction(e -> showEventDetails(event));

        ImageView reserveIcon = new ImageView(new Image("file:src/main/resources/com/example/sante/images/reservation.png"));
        reserveIcon.setFitWidth(16);
        reserveIcon.setFitHeight(16);

        Button reserveButton = new Button("Réserver", reserveIcon);
        reserveButton.setStyle("-fx-background-color: #3fbbc0; -fx-text-fill: white; -fx-font-size: 12px;");
        reserveButton.setOnAction(e -> redirectToAddReservation(event));

        // Regrouper les boutons
        HBox buttonBox = new HBox(10, detailsButton, reserveButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Ajouter les éléments à la carte
        StackPane imageContainer = new StackPane(imageView, badgeContainer);
        StackPane.setAlignment(badgeContainer, Pos.TOP_RIGHT);  // Positionner le badge à droite

        card.getChildren().addAll(imageContainer, title, location, description, buttonBox);

        // Effet au survol
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #3fbbc0; -fx-effect: dropshadow(three-pass-box, rgba(63,187,192,0.2), 10, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));

        return card;
    }

    // ---- Fonction pour formater la date en "14Feb" ----
    private String formatDate(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            String day = parts[2]; // Jour
            String month = parts[1]; // Mois (numérique)

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int monthIndex = Integer.parseInt(month) - 1; // Convertir en index

            return day + months[monthIndex]; // Exemple : "14Feb"
        } catch (Exception e) {
            return dateStr; // En cas d'erreur, afficher la date originale
        }
    }

    private void showEventDetails(Event event) {
        System.out.println("Afficher les détails de : " + event.getTitre());
        // Implement a detailed view if needed
    }

    private void redirectToAddReservation(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addReservation.fxml"));
            Parent root = loader.load();

            // Get the controller and set the event ID
            reservationController controller = loader.getController();
            controller.setEventId(event.getId());

            // Update the scene
            Scene scene = new Scene(root, 1200, 700);
            Stage stage = (Stage) eventGridPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}