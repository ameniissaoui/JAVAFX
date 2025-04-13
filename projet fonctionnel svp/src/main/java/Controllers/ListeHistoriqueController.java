package Controllers;

import Services.HisServices;
import Model.historique_traitement;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeHistoriqueController implements Initializable {

    @FXML
    private ListView<historique_traitement> listViewHistorique;

    private HisServices hisServices;
    private ObservableList<historique_traitement> historiqueList;
    private final String storagePath = "src/main/resources/uploads/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hisServices = new HisServices();

        setupListView();
        loadData();
    }

    private void setupListView() {
        listViewHistorique.setCellFactory(lv -> new TableLikeListCell());
    }

    public void loadData() {
        List<historique_traitement> histories = hisServices.afficher();
        historiqueList = FXCollections.observableArrayList(histories);
        listViewHistorique.setItems(historiqueList);
    }

    @FXML
    private void onAjouterClick() {
        try {
            // Charger la vue d'ajout dans une nouvelle fenêtre
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ajouter_historique.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter Historique");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir après fermeture
            loadData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void rafraichir() {
        loadData();
    }

    private void modifierHistorique(historique_traitement historique) {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modifier_historique.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et initialiser les données
            ModifierHistController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);

            // Définir un callback pour rafraîchir les données après modification
            controller.setRefreshCallback(() -> loadData());

            // Créer et afficher la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Modifier Historique");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue de modification: " + e.getMessage());
        }
    }

    private void supprimerHistorique(historique_traitement historique) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cet historique ?");
        confirmDialog.setContentText("Nom: " + historique.getNom() + "\nPrénom: " + historique.getPrenom());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprimer le fichier bilan associé
                if (historique.getBilan() != null && !historique.getBilan().isEmpty()) {
                    try {
                        File bilanFile = new File(storagePath + historique.getBilan());
                        if (bilanFile.exists()) {
                            bilanFile.delete();
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
                    }
                }

                // Supprimer l'enregistrement de la base de données
                hisServices.remove(historique);
                loadData(); // Refresh list after deletion

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Suppression réussie");
                alert.setContentText("L'historique de traitement a été supprimé avec succès.");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur de suppression");
                alert.setContentText("Une erreur est survenue lors de la suppression: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    private void afficherSuivisMedicaux(historique_traitement historique) {
        try {
            // Charger la vue d'ajout de suivi médical
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/ajouter_suivie_medical.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            AjouterSuivController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);

            // Afficher dans une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Ajouter Suivi Médical - " + historique.getNom() + " " + historique.getPrenom());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue d'ajout de suivi médical: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherDetailsHistorique(historique_traitement historique) {
        try {
            // Load the details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/details_historique.fxml"));
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

    // Custom ListCell to display items in a table-like format
    private class TableLikeListCell extends ListCell<historique_traitement> {
        private final GridPane gridPane = new GridPane();
        private final Label nomLabel = new Label();
        private final Label prenomLabel = new Label();
        private final Label maladieLabel = new Label();
        private final Label descriptionLabel = new Label();
        private final Label typeLabel = new Label();
        private final HBox bilanBox = new HBox(5);
        private final HBox actionsBox = new HBox(5);

        private final Button btnModifier = new Button("Modifier");
        private final Button btnSupprimer = new Button("Supprimer");
        private final Button btnAjouterSuivi = new Button("Suivis Médicaux");
        private final Button btnAfficher = new Button("Afficher");

        public TableLikeListCell() {
            // Configure grid pane
            gridPane.setHgap(10);
            gridPane.setPrefWidth(980);
            gridPane.getStyleClass().add("table-row");
            gridPane.setStyle("-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

            // Add column constraints
            for (int i = 0; i < 7; i++) {
                javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();

                // Set different widths for columns
                if (i == 0 || i == 1) {
                    column.setPrefWidth(100); // Nom, Prenom
                } else if (i == 2) {
                    column.setPrefWidth(120); // Maladie
                } else if (i == 3) {
                    column.setPrefWidth(180); // Description
                } else if (i == 4) {
                    column.setPrefWidth(120); // Type
                } else if (i == 5) {
                    column.setPrefWidth(130); // Bilan
                } else if (i == 6) {
                    column.setPrefWidth(200); // Actions
                }

                gridPane.getColumnConstraints().add(column);
            }

            // Configure text elements
            nomLabel.setWrapText(true);
            prenomLabel.setWrapText(true);
            maladieLabel.setWrapText(true);
            descriptionLabel.setWrapText(true);
            typeLabel.setWrapText(true);

            // Add elements to grid
            gridPane.add(nomLabel, 0, 0);
            gridPane.add(prenomLabel, 1, 0);
            gridPane.add(maladieLabel, 2, 0);
            gridPane.add(descriptionLabel, 3, 0);
            gridPane.add(typeLabel, 4, 0);
            gridPane.add(bilanBox, 5, 0);
            gridPane.add(actionsBox, 6, 0);

            // Style buttons
            btnModifier.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
            btnSupprimer.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white;");
            btnAjouterSuivi.setStyle("-fx-background-color: #9400D3; -fx-text-fill: white;");
            btnAfficher.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

            // Configure buttons
            configureButtons();

            // Add header row
            if (getIndex() == 0) {
                createHeaderRow();
            }
        }

        private void configureButtons() {
            btnModifier.setOnAction(event -> {
                historique_traitement historique = getItem();
                if (historique != null) {
                    modifierHistorique(historique);
                }
            });

            btnSupprimer.setOnAction(event -> {
                historique_traitement historique = getItem();
                if (historique != null) {
                    supprimerHistorique(historique);
                }
            });

            btnAjouterSuivi.setOnAction(event -> {
                historique_traitement historique = getItem();
                if (historique != null) {
                    afficherSuivisMedicaux(historique);
                }
            });

            btnAfficher.setOnAction(event -> {
                historique_traitement historique = getItem();
                if (historique != null) {
                    afficherDetailsHistorique(historique);
                }
            });
        }

        private void createHeaderRow() {
            // Create header labels
            Label nomHeader = new Label("Nom");
            Label prenomHeader = new Label("Prénom");
            Label maladieHeader = new Label("Maladie");
            Label descriptionHeader = new Label("Description");
            Label typeHeader = new Label("Type");
            Label bilanHeader = new Label("Bilan");
            Label actionsHeader = new Label("Actions");

            // Style headers
            String headerStyle = "-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #f0f0f0;";
            nomHeader.setStyle(headerStyle);
            prenomHeader.setStyle(headerStyle);
            maladieHeader.setStyle(headerStyle);
            descriptionHeader.setStyle(headerStyle);
            typeHeader.setStyle(headerStyle);
            bilanHeader.setStyle(headerStyle);
            actionsHeader.setStyle(headerStyle);

            // Add headers to list view as a header item
            // Note: This would require extending ObservableList to add a header item
            // For simplicity, we'll add it as a cell style
        }

        @Override
        protected void updateItem(historique_traitement historique, boolean empty) {
            super.updateItem(historique, empty);

            if (empty || historique == null) {
                setGraphic(null);
            } else {
                // Set data to labels
                nomLabel.setText(historique.getNom());
                prenomLabel.setText(historique.getPrenom());
                maladieLabel.setText(historique.getMaladies());
                descriptionLabel.setText(historique.getDescription());
                typeLabel.setText(historique.getType_traitement());

                // Configure bilan display
                bilanBox.getChildren().clear();

                String bilan = historique.getBilan();
                if (bilan != null && !bilan.isEmpty()) {
                    File bilanFile = new File(storagePath + bilan);
                    if (bilanFile.exists()) {
                        String extension = getFileExtension(bilanFile).toLowerCase();

                        // Display thumbnail for images
                        if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                                extension.equals("gif") || extension.equals("bmp")) {
                            try {
                                ImageView thumbnail = new ImageView(new Image(bilanFile.toURI().toString()));
                                thumbnail.setFitHeight(30);
                                thumbnail.setFitWidth(30);
                                thumbnail.setPreserveRatio(true);
                                bilanBox.getChildren().add(thumbnail);
                            } catch (Exception e) {
                                // Skip image in case of error
                            }
                        }

                        // Add view button
                        Button viewButton = new Button("Voir");
                        viewButton.setOnAction(event -> viewBilanFile(bilan));
                        bilanBox.getChildren().add(viewButton);
                    } else {
                        Label notFoundLabel = new Label(bilan + " (introuvable)");
                        bilanBox.getChildren().add(notFoundLabel);
                    }
                }

                // Configure actions buttons
                actionsBox.getChildren().clear();
                actionsBox.getChildren().addAll(btnModifier, btnSupprimer, btnAjouterSuivi, btnAfficher);

                setGraphic(gridPane);
            }
        }
    }

    private void viewBilanFile(String bilanFileName) {
        try {
            File bilanFile = new File(storagePath + bilanFileName);
            if (bilanFile.exists()) {
                String extension = getFileExtension(bilanFile).toLowerCase();

                // If it's an image, display in a window
                if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                        extension.equals("gif") || extension.equals("bmp")) {
                    // Create a window to display the image
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
                } else if (extension.equals("pdf")) {
                    // For PDFs, try to open with default program
                    try {
                        java.awt.Desktop.getDesktop().open(bilanFile);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Impossible d'ouvrir le fichier PDF. Vérifiez que vous avez un lecteur PDF installé.");
                    }
                } else {
                    // For other file types, try to open with default program
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
}