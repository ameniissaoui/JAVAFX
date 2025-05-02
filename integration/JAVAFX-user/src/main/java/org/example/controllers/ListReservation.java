package org.example.controllers;

import org.example.models.Reservation;
import org.example.services.ReservationService;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ListReservation {

    @FXML
    private FlowPane reservationsCardContainer;

    @FXML
    private AnchorPane mainContent;
    @FXML private Button eventButton;
    @FXML private Button rereservationButton;
    @FXML private Button statistiqueButton;
    @FXML private Button suivi;
    @FXML private Button historique;
    @FXML private Button buttoncommande;
    @FXML private Button tablesButton;
    @FXML private Button profileButton;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        // Initialize the FlowPane for card layout
        if (reservationsCardContainer != null) {
            reservationsCardContainer.setHgap(15);
            reservationsCardContainer.setVgap(15);
            reservationsCardContainer.setPadding(new Insets(15));
            reservationsCardContainer.setPrefWrapLength(900); // Set to accommodate 3 cards per row
            reservationsCardContainer.setColumnHalignment(javafx.geometry.HPos.CENTER);
        }

        // Load reservations
        loadReservations();

        // Set event handlers for navigation buttons
        setupNavigationHandlers();
    }

    private void setupNavigationHandlers() {
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

        suivi.setOnAction(event -> {
            try {
                handleSuiviRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        historique.setOnAction(event -> {
            try {
                handleHistoriqueRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        buttoncommande.setOnAction(event -> {
            try {
                handleCommandeRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        tablesButton.setOnAction(event -> {
            try {
                handleProduitRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        profileButton.setOnAction(event -> {
            try {
                handleProfileRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationService.afficher();
        reservationsCardContainer.getChildren().clear();

        for (Reservation reservation : reservations) {
            reservationsCardContainer.getChildren().add(createReservationCard(reservation));
        }
    }

    private VBox createReservationCard(Reservation reservation) {
        // Card container
        VBox card = new VBox();
        card.setPadding(new Insets(15));
        card.setSpacing(10);
        card.setPrefWidth(285);
        card.setMinWidth(285);
        card.setMaxWidth(285);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        // Icon and name in HBox
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);

        // User icon
        ImageView userIcon = new ImageView(new Image("file:src/main/resources/images/user_icon.png"));
        userIcon.setFitHeight(24);
        userIcon.setFitWidth(24);

        // Reservation name with styled label
        Label nameLabel = new Label(reservation.getNomreserv());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0077cc;");

        headerBox.getChildren().addAll(userIcon, nameLabel);

        // Email section
        HBox emailBox = new HBox();
        emailBox.setAlignment(Pos.CENTER_LEFT);
        emailBox.setSpacing(10);

        // Email icon
        ImageView emailIcon = new ImageView(new Image("file:src/main/resources/images/email_icon.png"));
        emailIcon.setFitHeight(16);
        emailIcon.setFitWidth(16);

        // Email label
        Label emailLabel = new Label(reservation.getMail());
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444;");

        emailBox.getChildren().addAll(emailIcon, emailLabel);

        // Persons count section
        HBox personBox = new HBox();
        personBox.setAlignment(Pos.CENTER_LEFT);
        personBox.setSpacing(10);

        // Person icon
        ImageView personIcon = new ImageView(new Image("file:src/main/resources/images/people_icon.png"));
        personIcon.setFitHeight(16);
        personIcon.setFitWidth(16);

        // Persons count label
        Label personCountLabel = new Label("Nombre de personnes: " + reservation.getNbrpersonne());
        personCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444;");

        personBox.getChildren().addAll(personIcon, personCountLabel);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));

        // Action buttons
        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(5, 0, 0, 0));

        // Delete button with maintained icon
        Button deleteBtn = new Button();
        ImageView deleteIcon = new ImageView(new Image("file:src/main/resources/images/deleteM.png"));
        deleteIcon.setFitHeight(18);
        deleteIcon.setFitWidth(18);
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        // Delete action
        deleteBtn.setOnAction(event -> {
            deleteReservation(reservation);
        });

        // Edit button (optional)
        Button editBtn = new Button();
        ImageView editIcon = new ImageView(new Image("file:src/main/resources/images/edit_icon.png"));
        editIcon.setFitHeight(18);
        editIcon.setFitWidth(18);
        editBtn.setGraphic(editIcon);
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        // View details button (optional)
        Button viewBtn = new Button();
        ImageView viewIcon = new ImageView(new Image("file:src/main/resources/images/view_icon.png"));
        viewIcon.setFitHeight(18);
        viewIcon.setFitWidth(18);
        viewBtn.setGraphic(viewIcon);
        viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        // Add buttons to action box
        actionBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        actionBox.setSpacing(15);

        // Add all elements to card
        card.getChildren().addAll(headerBox, emailBox, personBox, separator, actionBox);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 12, 0, 0, 6); " +
                    "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dbeafe; -fx-border-width: 1;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                    "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        });

        return card;
    }

    private void deleteReservation(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la réservation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationService.supprimer(reservation);
                    loadReservations(); // Reload all cards after deletion
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText("Échec de la suppression");
                    errorAlert.setContentText("Une erreur s'est produite lors de la suppression de la réservation.");
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }

    private void handlereservationRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listreservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
    void gotoreservation(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listReservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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

    private void handleSuiviRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/suivi.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handleHistoriqueRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/historique.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handleCommandeRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/commande.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handleProduitRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/produit.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handleProfileRedirect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
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

}