package org.example.controllers;

import org.example.models.Reservation;
import org.example.services.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class reservationFrontController {
    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ListView<Reservation> reservationListView;

    private final ReservationService reservationService = new ReservationService();
    private ObservableList<Reservation> reservationsList;

    @FXML
    void eventfront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventFront.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservationFront.fxml"));
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
    public void initialize() {
        // Configuration du ListView avec un factory personnalisé
        reservationListView.setCellFactory(lv -> new ReservationListCell());

        // Chargement des données
        loadReservations();

        // Configuration de la recherche
        searchButton.setOnAction(event -> filterReservations());
    }

    private void openReservationForm(Reservation reservation, boolean isEditMode) {
        try {
            // Charger le FXML de la fenêtre d'ajout/modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/updateReservation.fxml"));
            Parent root = loader.load();

            // Passer la réservation et l'état (ajout ou modification)
            reservationController controller = loader.getController();
            controller.initialize(reservation, isEditMode);

            // Récupérer la fenêtre (stage) actuelle et mettre à jour la scène
            Scene scene = new Scene(root, 1200, 700);
            Stage stage = (Stage) reservationListView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationService.afficher();
        reservationsList = FXCollections.observableArrayList(reservations);
        reservationListView.setItems(reservationsList);
    }

    private void filterReservations() {
        String keyword = searchField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            reservationListView.setItems(reservationsList);
        } else {
            ObservableList<Reservation> filteredList = FXCollections.observableArrayList();
            for (Reservation res : reservationsList) {
                if (res.getNomreserv().toLowerCase().contains(keyword) ||
                        res.getMail().toLowerCase().contains(keyword)) {
                    filteredList.add(res);
                }
            }
            reservationListView.setItems(filteredList);
        }
    }

    // Classe personnalisée pour afficher chaque réservation comme une ligne de tableau
    private class ReservationListCell extends ListCell<Reservation> {
        private final GridPane grid = new GridPane();
        private final Label nomLabel = new Label();
        private final Label emailLabel = new Label();
        private final Label personnesLabel = new Label();
        private final HBox actionsBox = new HBox(10);
        private final Button btnModifier = new Button();
        private final Button btnSupprimer = new Button();

        public ReservationListCell() {
            // Configuration du GridPane
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setPrefWidth(800);
            grid.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

            // Configuration des colonnes
            for (int i = 0; i < 4; i++) {
                grid.getColumnConstraints().add(new javafx.scene.layout.ColumnConstraints());
                grid.getColumnConstraints().get(i).setPercentWidth(25);
            }

            // Configurer les labels
            nomLabel.setMaxWidth(Double.MAX_VALUE);
            emailLabel.setMaxWidth(Double.MAX_VALUE);
            personnesLabel.setMaxWidth(Double.MAX_VALUE);

            // Configurer les boutons
            ImageView deleteIcon = new ImageView(new Image("file:src/main/resources/images/deleteM.png"));
            ImageView editIcon = new ImageView(new Image("file:src/main/resources/images/modifier.png"));

            deleteIcon.setFitWidth(16);
            deleteIcon.setFitHeight(16);
            editIcon.setFitWidth(16);
            editIcon.setFitHeight(16);

            btnModifier.setGraphic(editIcon);
            btnSupprimer.setGraphic(deleteIcon);

            btnModifier.setStyle("-fx-background-color: white; -fx-border-color: #43ACD1; -fx-border-radius: 5px; -fx-padding: 5px;");
            btnSupprimer.setStyle("-fx-background-color: white; -fx-border-color: #43ACD1; -fx-border-radius: 5px; -fx-padding: 5px;");

            btnModifier.setText("Modifier");
            btnModifier.setContentDisplay(ContentDisplay.LEFT);

            btnSupprimer.setText("Supprimer");
            btnSupprimer.setContentDisplay(ContentDisplay.LEFT);

            actionsBox.getChildren().addAll(btnModifier, btnSupprimer);
            actionsBox.setAlignment(Pos.CENTER_LEFT);

            // Ajouter les éléments au GridPane
            grid.add(nomLabel, 0, 0);
            grid.add(emailLabel, 1, 0);
            grid.add(personnesLabel, 2, 0);
            grid.add(actionsBox, 3, 0);

            // Définir le contenu de la cellule
            setText(null);
        }

        @Override
        protected void updateItem(Reservation reservation, boolean empty) {
            super.updateItem(reservation, empty);

            if (empty || reservation == null) {
                setGraphic(null);
            } else {
                // Mettre à jour les valeurs
                nomLabel.setText(reservation.getNomreserv());
                emailLabel.setText(reservation.getMail());
                personnesLabel.setText(String.valueOf(reservation.getNbrpersonne()));

                // Configurer les actions des boutons
                btnModifier.setOnAction(event -> {
                    openReservationForm(reservation, true);
                });

                btnSupprimer.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation de suppression");
                    alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette réservation ?");
                    alert.setContentText("Cette action est irréversible.");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            reservationService.supprimer(reservation);
                            getListView().getItems().remove(reservation);
                            System.out.println("Supprimé : " + reservation);
                        }
                    });
                });

                setGraphic(grid);
            }
        }
    }
}