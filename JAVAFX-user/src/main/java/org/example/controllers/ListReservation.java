package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.models.Reservation;
import org.example.services.ReservationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.io.IOException;
import java.util.List;

public class ListReservation {

    @FXML private FlowPane reservationsCardContainer;
    @FXML private AnchorPane mainContent;
    @FXML private Button eventButton, rereservationButton, statistiqueButton;
    @FXML private Button suivi, historique, buttoncommande, tablesButton, profileButton;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        setupNavigationHandlers();
        setupCardContainer();
        loadReservations();
    }

    private void setupNavigationHandlers() {
        eventButton.setOnAction(event -> navigate("/fxml/listevent.fxml", event));
        rereservationButton.setOnAction(event -> navigate("/fxml/listreservation.fxml", event));
        statistiqueButton.setOnAction(event -> navigate("/fxml/statistique.fxml", event));
        suivi.setOnAction(event -> navigate("/fxml/liste_suivi_back.fxml", event));
        historique.setOnAction(event -> navigate("/fxml/liste_historique_back.fxml", event));
        buttoncommande.setOnAction(event -> navigate("/fxml/back/showCommande.fxml", event));
        tablesButton.setOnAction(event -> navigate("/fxml/back/showProduit.fxml", event));
        profileButton.setOnAction(event -> navigate("/fxml/admin_profile.fxml", event));
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

    private void setupCardContainer() {
        if (reservationsCardContainer != null) {
            reservationsCardContainer.setHgap(15);
            reservationsCardContainer.setVgap(15);
            reservationsCardContainer.setPadding(new Insets(15));
            reservationsCardContainer.setPrefWrapLength(900);
            reservationsCardContainer.setColumnHalignment(javafx.geometry.HPos.CENTER);
        }
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationService.afficher();
        reservationsCardContainer.getChildren().clear();
        reservations.forEach(reservation -> reservationsCardContainer.getChildren().add(createReservationCard(reservation)));
    }

    private VBox createReservationCard(Reservation reservation) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(285);
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                "-fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        HBox header = createInfoBox("user_icon.png", reservation.getNomreserv(), 16, true);
        HBox email = createInfoBox("email_icon.png", reservation.getMail(), 14, false);
        HBox people = createInfoBox("people_icon.png", "Nombre de personnes: " + reservation.getNbrpersonne(), 14, false);

        Separator separator = new Separator();
        HBox actions = createActionButtons(reservation);

        card.getChildren().addAll(header, email, people, separator, actions);
        addCardHoverEffect(card);
        return card;
    }

    private HBox createInfoBox(String iconPath, String text, int fontSize, boolean bold) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        ImageView icon = new ImageView(new Image("file:src/main/resources/images/" + iconPath));
        icon.setFitHeight(18);
        icon.setFitWidth(18);

        Label label = new Label(text);
        label.setStyle("-fx-font-size: " + fontSize + "px; -fx-text-fill: #444444;" + (bold ? " -fx-font-weight: bold; color: #0077cc;" : ""));
        box.getChildren().addAll(icon, label);
        return box;
    }

    private HBox createActionButtons(Reservation reservation) {
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button deleteBtn = createIconButton("deleteM.png");
        deleteBtn.setOnAction(e -> deleteReservation(reservation));

        Button editBtn = createIconButton("edit_icon.png");
        Button viewBtn = createIconButton("view_icon.png");

        actionBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        return actionBox;
    }

    private Button createIconButton(String iconFile) {
        Button btn = new Button();
        ImageView icon = new ImageView(new Image("file:src/main/resources/images/" + iconFile));
        icon.setFitHeight(18);
        icon.setFitWidth(18);
        btn.setGraphic(icon);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        return btn;
    }

    private void addCardHoverEffect(VBox card) {
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 12, 0, 0, 6);" +
                "-fx-background-radius: 8; -fx-border-color: #dbeafe; -fx-border-radius: 8;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                "-fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;"));
    }

    private void deleteReservation(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Supprimer la réservation ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationService.supprimer(reservation);
                    loadReservations();
                } catch (Exception e) {
                    showErrorDialog("Erreur", "Suppression échouée: " + e.getMessage());
                }
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
