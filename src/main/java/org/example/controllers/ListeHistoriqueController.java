package org.example.controllers;

import org.example.models.historique_traitement;
import org.example.services.HisServices;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.control.Tooltip;
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
    private final String storagePath = "src/main/resources/images/";

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_historique.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_historique.fxml"));
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
                // Tentative de suppression
                hisServices.remove(historique);
                loadData(); // Actualiser la liste après suppression

                // Message de succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText("Suppression réussie");
                successAlert.setContentText("L'historique de traitement a été supprimé avec succès.");
                successAlert.showAndWait();

            } catch (Exception e) {
                String errorMessage = e.getMessage();

                // Vérifier si c'est une erreur de contrainte d'intégrité
                if (errorMessage != null && errorMessage.contains("foreign key constraint fails")
                        || errorMessage.contains("CONSTRAINT") || errorMessage.contains("FOREIGN KEY")) {

                    // Alerte spécifique pour les contraintes de clé étrangère
                    Alert foreignKeyAlert = new Alert(Alert.AlertType.WARNING);
                    foreignKeyAlert.setTitle("Suppression impossible");
                    foreignKeyAlert.setHeaderText("Action non autorisée");

                    // Construction d'un message détaillé
                    StringBuilder contentBuilder = new StringBuilder();
                    contentBuilder.append("Impossible de supprimer cet historique de traitement car il possède des suivis médicaux associés.\n\n");
                    contentBuilder.append("Patient: ").append(historique.getNom()).append(" ").append(historique.getPrenom()).append("\n\n");
                    contentBuilder.append("Vous devez d'abord supprimer tous les suivis médicaux associés à ce patient avant de pouvoir supprimer son historique de traitement.\n\n");
                    contentBuilder.append("Actions recommandées:\n");
                    contentBuilder.append("  1. Accédez à la liste des suivis médicaux de ce patient\n");
                    contentBuilder.append("  2. Supprimez tous les suivis médicaux existants\n");
                    contentBuilder.append("  3. Tentez à nouveau la suppression de l'historique");

                    foreignKeyAlert.setContentText(contentBuilder.toString());
                    foreignKeyAlert.getDialogPane().setPrefWidth(500); // Largeur personnalisée pour une meilleure lisibilité

                    foreignKeyAlert.showAndWait();
                } else {
                    // Alerte générique pour les autres types d'erreurs
                    Alert genericErrorAlert = new Alert(Alert.AlertType.ERROR);
                    genericErrorAlert.setTitle("Erreur");
                    genericErrorAlert.setHeaderText("Erreur de suppression");
                    genericErrorAlert.setContentText("Une erreur est survenue lors de la suppression:\n" + e.getMessage());
                    genericErrorAlert.showAndWait();
                }

                e.printStackTrace();
            }
        }
    }
    private void afficherSuivisMedicaux(historique_traitement historique) {
        try {
            // Charger la vue d'ajout de suivi médical
            URL location = getClass().getResource("/fxml/ajouter_suivie_medical.fxml");
            if (location == null) {
                System.err.println("Could not find FXML file!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
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

        private final Button btnModifier = new Button();
        private final Button btnSupprimer = new Button();
        private final Button btnAjouterSuivi = new Button();
        private final Button btnAfficher = new Button();
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

            // Edit button
            FontIcon editIcon = new FontIcon(BootstrapIcons.PENCIL_SQUARE);
            editIcon.setIconSize(18);
            editIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            btnModifier.setGraphic(editIcon);
            btnModifier.setStyle("-fx-background-color: #FFA500; -fx-padding: 5px;");
            btnModifier.setTooltip(new Tooltip("Modifier"));
            btnModifier.setPrefSize(32, 32);
            btnModifier.setMinSize(32, 32);
            btnModifier.setMaxSize(32, 32);

            // Delete button
            FontIcon deleteIcon = new FontIcon(BootstrapIcons.TRASH);
            deleteIcon.setIconSize(18);
            deleteIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            btnSupprimer.setGraphic(deleteIcon);
            btnSupprimer.setStyle("-fx-background-color: #FF0000; -fx-padding: 5px;");
            btnSupprimer.setTooltip(new Tooltip("Supprimer"));
            btnSupprimer.setPrefSize(32, 32);
            btnSupprimer.setMinSize(32, 32);
            btnSupprimer.setMaxSize(32, 32);

            // Medical follow-up button
            FontIcon followUpIcon = new FontIcon(BootstrapIcons.CLIPBOARD_PLUS);
            followUpIcon.setIconSize(18);
            followUpIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            btnAjouterSuivi.setGraphic(followUpIcon);
            btnAjouterSuivi.setStyle("-fx-background-color: #9400D3; -fx-padding: 5px;");
            btnAjouterSuivi.setTooltip(new Tooltip("Suivis Médicaux"));
            btnAjouterSuivi.setPrefSize(32, 32);
            btnAjouterSuivi.setMinSize(32, 32);
            btnAjouterSuivi.setMaxSize(32, 32);

            // Details button
            FontIcon detailsIcon = new FontIcon(BootstrapIcons.EYE);
            detailsIcon.setIconSize(18);
            detailsIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            btnAfficher.setGraphic(detailsIcon);
            btnAfficher.setStyle("-fx-background-color: #3498db; -fx-padding: 5px;");
            btnAfficher.setTooltip(new Tooltip("Afficher"));
            btnAfficher.setPrefSize(32, 32);
            btnAfficher.setMinSize(32, 32);
            btnAfficher.setMaxSize(32, 32);
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