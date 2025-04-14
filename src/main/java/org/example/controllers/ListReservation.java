package org.example.controllers;

import org.example.models.Reservation;
import org.example.services.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ListReservation {

    @FXML
    private ListView<Reservation> reservationListView;

    @FXML
    private AnchorPane mainContent;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        // Ajouter un en-tête de tableau au-dessus du ListView
        addTableHeader();

        // Configuration du ListView avec une cellule personnalisée en forme de tableau
        reservationListView.setCellFactory(param -> new ListCell<Reservation>() {
            private final Label nomLabel = new Label();
            private final Label emailLabel = new Label();
            private final Label personnesLabel = new Label();
            private final Button deleteBtn = new Button();
            private final HBox actionButtons = new HBox(10, deleteBtn);
            private final GridPane grid = new GridPane();

            {
                // Configuration du GridPane comme un tableau
                grid.setHgap(10);
                grid.setPadding(new Insets(5));
                grid.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");

                // Style et largeur pour les éléments du tableau
                nomLabel.setPrefWidth(250);
                emailLabel.setPrefWidth(380);
                personnesLabel.setPrefWidth(100);

                // Configuration du bouton de suppression avec une icône
                ImageView deleteIcon = new ImageView(new Image("file:src/main/resources/images/deleteM.png"));
                deleteIcon.setFitHeight(18);
                deleteIcon.setFitWidth(18);
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setStyle("-fx-background-color: transparent;");

                // Événement du bouton de suppression
                deleteBtn.setOnAction(event -> {
                    Reservation selectedReservation = getItem();
                    reservationListView.getSelectionModel().select(selectedReservation);
                    delete();
                });
            }

            @Override
            protected void updateItem(Reservation reservation, boolean empty) {
                super.updateItem(reservation, empty);

                if (empty || reservation == null) {
                    setGraphic(null);
                } else {
                    // Configurer le GridPane pour cette ligne
                    grid.getChildren().clear();

                    // Afficher les données dans les étiquettes
                    nomLabel.setText(reservation.getNomreserv());
                    emailLabel.setText(reservation.getMail());
                    personnesLabel.setText(String.valueOf(reservation.getNbrpersonne()));

                    // Ajouter tous les éléments au GridPane comme un tableau
                    grid.add(nomLabel, 0, 0);
                    grid.add(emailLabel, 1, 0);
                    grid.add(personnesLabel, 2, 0);
                    grid.add(actionButtons, 3, 0);

                    setGraphic(grid);
                }
            }
        });

        // Charger les données
        loadReservations();
    }

    private void addTableHeader() {
        // Créer un en-tête de tableau
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(10);
        headerGrid.setPadding(new Insets(10, 5, 10, 5));
        headerGrid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-font-weight: bold;");

        // Créer les étiquettes d'en-tête
        Label nomHeader = new Label("Nom");
        Label emailHeader = new Label("Email");
        Label personnesHeader = new Label("Nombre de personnes");
        Label actionsHeader = new Label("Actions");

        // Définir la largeur des colonnes
        nomHeader.setPrefWidth(250);
        emailHeader.setPrefWidth(380);
        personnesHeader.setPrefWidth(100);
        actionsHeader.setPrefWidth(100);

        // Ajouter les en-têtes au GridPane
        headerGrid.add(nomHeader, 0, 0);
        headerGrid.add(emailHeader, 1, 0);
        headerGrid.add(personnesHeader, 2, 0);
        headerGrid.add(actionsHeader, 3, 0);

        // Ajouter l'en-tête au-dessus du ListView
        AnchorPane parent = (AnchorPane) reservationListView.getParent();
        parent.getChildren().add(headerGrid);

        // Positionner l'en-tête au-dessus du ListView
        AnchorPane.setTopAnchor(headerGrid, 50.0); // Ajuster selon votre mise en page
        AnchorPane.setLeftAnchor(headerGrid, 30.0);
        AnchorPane.setRightAnchor(headerGrid, 30.0);

        // Repositionner le ListView pour faire de la place à l'en-tête
        AnchorPane.setTopAnchor(reservationListView, 90.0); // Ajuster cette valeur
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationService.afficher();
        ObservableList<Reservation> reservationsList = FXCollections.observableArrayList(reservations);
        reservationListView.setItems(reservationsList);
    }

    @FXML
    void delete() {
        Reservation selectedReservation = reservationListView.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmer la suppression");
            alert.setHeaderText("Supprimer la réservation");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        reservationService.supprimer(selectedReservation);
                        reservationListView.getItems().remove(selectedReservation);
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
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Aucune réservation sélectionnée");
            alert.setContentText("Veuillez sélectionner une réservation à supprimer.");
            alert.showAndWait();
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
    void gotoreservation(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listReservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}