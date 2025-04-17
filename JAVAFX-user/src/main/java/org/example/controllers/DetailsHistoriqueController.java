package org.example.controllers;

import org.example.models.historique_traitement;
import org.example.models.suivie_medical;
import org.example.services.SuivServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DetailsHistoriqueController implements Initializable {

    @FXML
    private Label labelMaladie;

    @FXML
    private Label labelDescription;

    @FXML
    private Label labelTypeTraitement;

    @FXML
    private Label labelNom;

    @FXML
    private Label labelPrenom;

    @FXML
    private HBox bilanContainer;

    @FXML
    private ListView<suivie_medical> listViewSuivis;

    private historique_traitement historique;
    private SuivServices suiviService;
    private ObservableList<suivie_medical> suivisList;
    private final String storagePath = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suiviService = new SuivServices();

        // Set custom cell factory for ListView to display like a table
        listViewSuivis.setCellFactory(new Callback<ListView<suivie_medical>, ListCell<suivie_medical>>() {
            @Override
            public ListCell<suivie_medical> call(ListView<suivie_medical> param) {
                return new ListCell<suivie_medical>() {
                    @Override
                    protected void updateItem(suivie_medical item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Create a horizontal box for layout similar to table row
                            HBox hbox = new HBox();
                            hbox.setSpacing(5);

                            // Create labels for each "column"
                            Label dateLabel = new Label(item.getDate());
                            dateLabel.setPrefWidth(150);
                            dateLabel.setWrapText(true);

                            Label commentaireLabel = new Label(item.getCommentaire());
                            commentaireLabel.setPrefWidth(450);
                            commentaireLabel.setWrapText(true);

                            // Add labels to hbox
                            hbox.getChildren().addAll(dateLabel, commentaireLabel);

                            // Style for alternating rows
                            if (getIndex() % 2 == 0) {
                                hbox.setStyle("-fx-background-color: #f5f5f5;");
                            } else {
                                hbox.setStyle("-fx-background-color: white;");
                            }

                            // Set the hbox as the graphic for this cell
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });

        // Set placeholder message when list is empty
        listViewSuivis.setPlaceholder(new Label("Aucun suivi disponible"));

        // Add CSS to make it look more like a TableView
        listViewSuivis.getStyleClass().add("table-view-style");
    }

    public void setHistoriqueTraitement(historique_traitement historique) {
        this.historique = historique;

        // Remplir les informations du traitement
        labelMaladie.setText(historique.getMaladies());
        labelDescription.setText(historique.getDescription());
        labelTypeTraitement.setText(historique.getType_traitement());
        labelNom.setText(historique.getNom());
        labelPrenom.setText(historique.getPrenom());

        // Afficher le bilan si disponible
        if (historique.getBilan() != null && !historique.getBilan().isEmpty()) {
            afficherBilan(historique.getBilan());
        } else {
            Label noBilanLabel = new Label("Aucun bilan disponible");
            bilanContainer.getChildren().add(noBilanLabel);
        }

        // Charger les suivis médicaux pour cet historique
        loadSuivis();
    }

    private void afficherBilan(String bilanFileName) {
        File bilanFile = new File(storagePath + bilanFileName);
        if (bilanFile.exists()) {
            String extension = getFileExtension(bilanFile).toLowerCase();

            // Si c'est une image, afficher une miniature
            if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                    extension.equals("gif") || extension.equals("bmp")) {
                try {
                    ImageView thumbnail = new ImageView(new Image(bilanFile.toURI().toString()));
                    thumbnail.setFitHeight(100);
                    thumbnail.setFitWidth(100);
                    thumbnail.setPreserveRatio(true);

                    Button viewButton = new Button("Voir en grand");
                    viewButton.setOnAction(event -> viewBilanFile(bilanFileName));

                    bilanContainer.getChildren().addAll(thumbnail, viewButton);
                } catch (Exception e) {
                    Label errorLabel = new Label("Erreur de chargement de l'image: " + e.getMessage());
                    bilanContainer.getChildren().add(errorLabel);
                }
            } else {
                // Pour d'autres types de fichiers, afficher juste un bouton pour l'ouvrir
                Button openButton = new Button("Ouvrir " + bilanFileName);
                openButton.setOnAction(event -> viewBilanFile(bilanFileName));
                bilanContainer.getChildren().add(openButton);
            }
        } else {
            Label notFoundLabel = new Label(bilanFileName + " (introuvable)");
            bilanContainer.getChildren().add(notFoundLabel);
        }
    }

    private void viewBilanFile(String bilanFileName) {
        try {
            File bilanFile = new File(storagePath + bilanFileName);
            if (bilanFile.exists()) {
                String extension = getFileExtension(bilanFile).toLowerCase();

                // Si c'est une image, afficher dans une fenêtre
                if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                        extension.equals("gif") || extension.equals("bmp")) {
                    // Créer une fenêtre pour afficher l'image
                    Stage imageStage = new Stage();
                    imageStage.setTitle("Bilan: " + bilanFileName);
                    imageStage.initModality(Modality.APPLICATION_MODAL);

                    ImageView imageView = new ImageView(new Image(bilanFile.toURI().toString()));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(800);

                    ScrollPane scrollPane = new ScrollPane(imageView);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setPrefHeight(600);

                    Button closeButton = new Button("Fermer");
                    closeButton.setOnAction(e -> imageStage.close());

                    VBox root = new VBox(10);
                    root.getChildren().addAll(scrollPane, closeButton);
                    root.setPadding(new javafx.geometry.Insets(10));

                    Scene scene = new Scene(root);
                    imageStage.setScene(scene);
                    imageStage.show();
                } else {
                    // Pour les autres types de fichiers, essayer d'ouvrir avec le programme par défaut
                    try {
                        java.awt.Desktop.getDesktop().open(bilanFile);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Impossible d'ouvrir le fichier. Vérifiez que vous avez une application associée pour ce type de fichier.");
                    }
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Fichier introuvable",
                        "Le fichier " + bilanFileName + " n'existe pas dans le dossier d'uploads.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du fichier: " + e.getMessage());
        }
    }

    private void loadSuivis() {
        try {
            // Get follow-ups by historique ID from the service
            List<suivie_medical> suivis = suiviService.getSuiviByHistoriqueId(historique.getId());

            // If the direct service call fails, try getting them from the historique object
            if (suivis == null || suivis.isEmpty()) {
                suivis = historique.getSuivisMedicaux();
            }

            // Set data to ListView
            if (suivis != null && !suivis.isEmpty()) {
                suivisList = FXCollections.observableArrayList(suivis);
                listViewSuivis.setItems(suivisList);
                listViewSuivis.refresh();
            } else {
                // If no follow-ups, create an empty list
                suivisList = FXCollections.observableArrayList();
                listViewSuivis.setItems(suivisList);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des suivis: " + e.getMessage());
            e.printStackTrace();
            // If there's an error, still try to display any follow-ups from the historique object
            if (historique.getSuivisMedicaux() != null) {
                suivisList = FXCollections.observableArrayList(historique.getSuivisMedicaux());
                listViewSuivis.setItems(suivisList);
            } else {
                suivisList = FXCollections.observableArrayList();
                listViewSuivis.setItems(suivisList);
            }
        }
    }

    @FXML
    private void fermer() {
        // Obtenir la fenêtre actuelle et la fermer
        Stage stage = (Stage) labelNom.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void ajouterSuivi() {
        try {
            // Load the view for adding a medical follow-up
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_suivi.fxml"));
            Parent root = loader.load();

            // Configure the controller
            AjouterSuivController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);

            // Set a callback to refresh the data after adding
            controller.setOnSuiviAddedCallback(this::loadSuivis);

            // Display in a new window
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Suivi Médical");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload data after closing the add window
            loadSuivis();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue d'ajout de suivi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    private void afficherDetailsHistorique(historique_traitement historique) {
        try {
            // Load the details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/details_historique.fxml"));
            Parent root = loader.load();

            // Configure the controller
            DetailsHistoriqueController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);

            // Display in a new window
            Stage stage = new Stage();
            stage.setTitle("Détails du Traitement - " + historique.getNom() + " " + historique.getPrenom());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue des détails: " + e.getMessage());
            e.printStackTrace();
        }
    }
}