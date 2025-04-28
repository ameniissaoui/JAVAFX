package org.example.controllers;

import org.example.models.Event;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ListEvent {

    @FXML private ListView<Event> eventListView;
    @FXML private AnchorPane mainContent;

    private final EventService eventService = new EventService();

    @FXML
    void initialize() {
        // Ajouter un en-tête de tableau au-dessus du ListView
        addTableHeader();

        // Configuration du ListView avec une cellule personnalisée en forme de tableau
        eventListView.setCellFactory(param -> new ListCell<Event>() {
            private final Label titreLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label lieuLabel = new Label();
            private final Label descriptionLabel = new Label();
            private final Label placesLabel = new Label();
            private final ImageView imageView = new ImageView();
            private final Button updateBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox actionButtons = new HBox(10, updateBtn, deleteBtn);
            private final GridPane grid = new GridPane();

            {
                // Configuration du GridPane comme un tableau
                grid.setHgap(10);
                grid.setPadding(new Insets(5));
                grid.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");

                // Configuration des éléments
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);

                // Boutons d'action
                ImageView editIcon = new ImageView(new Image("file:src/main/resources/images/modifier.png"));
                editIcon.setFitHeight(18);
                editIcon.setFitWidth(18);
                updateBtn.setGraphic(editIcon);
                updateBtn.setStyle("-fx-background-color: transparent;");

                ImageView deleteIcon = new ImageView(new Image("file:src/main/resources/images/deleteM.png"));
                deleteIcon.setFitHeight(18);
                deleteIcon.setFitWidth(18);
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setStyle("-fx-background-color: transparent;");

                // Style et largeur pour les éléments du tableau
                titreLabel.setPrefWidth(100);
                dateLabel.setPrefWidth(100);
                lieuLabel.setPrefWidth(100);
                descriptionLabel.setPrefWidth(150);
                placesLabel.setPrefWidth(100);

                // Événements des boutons
                updateBtn.setOnAction(event -> {
                    Event selectedEvent = getItem();
                    eventListView.getSelectionModel().select(selectedEvent);
                    onModifierClick();
                });

                deleteBtn.setOnAction(event -> {
                    Event selectedEvent = getItem();
                    eventListView.getSelectionModel().select(selectedEvent);
                    delete();
                });
            }

            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);

                if (empty || event == null) {
                    setGraphic(null);
                } else {
                    // Configurer le GridPane pour cette ligne
                    grid.getChildren().clear();

                    // Afficher les données dans les étiquettes
                    titreLabel.setText(event.getTitre());
                    dateLabel.setText(event.getDateevent());
                    lieuLabel.setText(event.getLieu());
                    descriptionLabel.setText(event.getDiscription());
                    placesLabel.setText(String.valueOf(event.getNbplace()));

                    // Chargement de l'image
                    if (event.getImage() != null && !event.getImage().isEmpty()) {
                        String path = "file:src/main/resources/com/example/sante/images/" + event.getImage();
                        Image image = new Image(path);
                        imageView.setImage(image);
                    }

                    // Ajouter tous les éléments au GridPane comme un tableau
                    grid.add(titreLabel, 0, 0);
                    grid.add(dateLabel, 1, 0);
                    grid.add(lieuLabel, 2, 0);
                    grid.add(descriptionLabel, 3, 0);
                    grid.add(placesLabel, 4, 0);
                    grid.add(imageView, 5, 0);
                    grid.add(actionButtons, 6, 0);

                    setGraphic(grid);
                }
            }
        });

        // Charger les données
        refreshEvents();
    }

    private void addTableHeader() {
        // Créer un en-tête de tableau
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(10);
        headerGrid.setPadding(new Insets(10, 5, 10, 5));
        headerGrid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-font-weight: bold;");

        // Créer les étiquettes d'en-tête
        Label titreHeader = new Label("Titre");
        Label dateHeader = new Label("Date");
        Label lieuHeader = new Label("Lieu");
        Label descriptionHeader = new Label("Description");
        Label placesHeader = new Label("Places");
        Label imageHeader = new Label("Image");
        Label actionsHeader = new Label("Actions");

        // Définir la largeur des colonnes
        titreHeader.setPrefWidth(100);
        dateHeader.setPrefWidth(100);
        lieuHeader.setPrefWidth(100);
        descriptionHeader.setPrefWidth(150);
        placesHeader.setPrefWidth(100);
        imageHeader.setPrefWidth(50);
        actionsHeader.setPrefWidth(100);

        // Ajouter les en-têtes au GridPane
        headerGrid.add(titreHeader, 0, 0);
        headerGrid.add(dateHeader, 1, 0);
        headerGrid.add(lieuHeader, 2, 0);
        headerGrid.add(descriptionHeader, 3, 0);
        headerGrid.add(placesHeader, 4, 0);
        headerGrid.add(imageHeader, 5, 0);
        headerGrid.add(actionsHeader, 6, 0);

        // Ajouter l'en-tête au-dessus du ListView
        AnchorPane parent = (AnchorPane) eventListView.getParent();
        parent.getChildren().add(headerGrid);

        // Positionner l'en-tête au-dessus du ListView
        AnchorPane.setTopAnchor(headerGrid, 50.0); // Ajuster selon votre mise en page
        AnchorPane.setLeftAnchor(headerGrid, 30.0);
        AnchorPane.setRightAnchor(headerGrid, 30.0);

        // Repositionner le ListView pour faire de la place à l'en-tête
        AnchorPane.setTopAnchor(eventListView, 90.0); // Ajuster cette valeur
    }

    public void refreshEvents() {
        ObservableList<Event> eventList = FXCollections.observableArrayList(eventService.afficher());
        eventListView.setItems(eventList);
    }

    // Le reste du code reste identique...
    @FXML
    void delete() {
        Event selectedEvent = eventListView.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Event");
            alert.setContentText("Are you sure you want to delete the selected event?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        eventService.supprimer(selectedEvent);
                        eventListView.getItems().remove(selectedEvent);
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
        Event selectedEvent = eventListView.getSelectionModel().getSelectedItem();
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

    public void gotoAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addevent.fxml"));
            BorderPane addEventContent = loader.load();
            mainContent.getChildren().setAll(addEventContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}