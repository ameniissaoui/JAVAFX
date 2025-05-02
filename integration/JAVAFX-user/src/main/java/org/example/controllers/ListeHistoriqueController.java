package org.example.controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Medecin;
import org.example.models.historique_traitement;
import org.example.services.HisServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import org.example.util.SessionManager;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeHistoriqueController implements Initializable {

    @FXML
    private ListView<historique_traitement> listViewHistorique;
    @FXML
    private AnchorPane anchorPane;
    private HisServices hisServices;
    private ObservableList<historique_traitement> historiqueList;
    private final String storagePath = "src/main/resources/images/";
    private boolean medecinMode = false;
    private Medecin currentMedecin;
    @FXML
    private Button suiviButton;
    @FXML
    private BorderPane mainBorderPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hisServices = new HisServices();

        if (suiviButton != null) {
            suiviButton.setVisible(SessionManager.getInstance().isMedecin());
        }
        setupListView();
        loadData();
        setFullScreen();
    }

    public void loadData() {
        List<historique_traitement> histories = hisServices.afficher();
        historiqueList = FXCollections.observableArrayList(histories);
        listViewHistorique.setItems(historiqueList);
    }

    private void setupListView() {
        listViewHistorique.setCellFactory(lv -> {
            HistoriqueCardCell cell = new HistoriqueCardCell();
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

        listViewHistorique.setFixedCellSize(360);
        listViewHistorique.setMinWidth(1100);
        listViewHistorique.setPrefWidth(1100);

        listViewHistorique.setStyle("-fx-background-color: transparent; " +
                "-fx-padding: 15; " +
                "-fx-background-insets: 0; " +
                "-fx-border-width: 0;");
    }

    @FXML
    private void onAjouterClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_historique.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter Historique");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_historique.fxml"));
            Parent root = loader.load();
            ModifierHistController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);
            controller.setRefreshCallback(() -> loadData());
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
                hisServices.remove(historique);
                loadData();
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText("Suppression réussie");
                successAlert.setContentText("L'historique de traitement a été supprimé avec succès.");
                successAlert.showAndWait();
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains("foreign key constraint fails")) {
                    Alert foreignKeyAlert = new Alert(Alert.AlertType.WARNING);
                    foreignKeyAlert.setTitle("Suppression impossible");
                    foreignKeyAlert.setHeaderText("Action non autorisée");
                    StringBuilder contentBuilder = new StringBuilder();
                    contentBuilder.append("Impossible de supprimer cet historique de traitement car il possède des suivis médicaux associés.\n\n");
                    contentBuilder.append("Patient: ").append(historique.getNom()).append(" ").append(historique.getPrenom()).append("\n\n");
                    contentBuilder.append("Vous devez d'abord supprimer tous les suivis médicaux associés à ce patient avant de pouvoir supprimer son historique de traitement.\n\n");
                    contentBuilder.append("Actions recommandées:\n");
                    contentBuilder.append("  1. Accédez à la liste des suivis médicaux de ce patient\n");
                    contentBuilder.append("  2. Supprimez tous les suivis médicaux existants\n");
                    contentBuilder.append("  3. Tentez à nouveau la suppression de l'historique");
                    foreignKeyAlert.setContentText(contentBuilder.toString());
                    foreignKeyAlert.getDialogPane().setPrefWidth(500);
                    foreignKeyAlert.showAndWait();
                } else {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_suivie_medical.fxml"));
            Parent suiviForm = loader.load();
            AjouterSuivController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);
            controller.setOnSuiviAddedCallback(() -> {
                mainBorderPane.setCenter(listViewHistorique);
                loadData();
            });
            mainBorderPane.setCenter(suiviForm);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue d'ajout de suivi médical: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherDetailsHistorique(historique_traitement historique) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/details_historique.fxml"));
            Parent detailsView = loader.load();
            DetailsHistoriqueController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);
            Node anyExistingNode = listViewHistorique;
            if (anyExistingNode == null) {
                throw new RuntimeException("Cannot access the current scene");
            }
            Stage currentStage = (Stage) anyExistingNode.getScene().getWindow();
            currentStage.setTitle("Détails du Traitement - " + historique.getNom() + " " + historique.getPrenom());
            Scene scene = new Scene(detailsView);
            currentStage.setScene(scene);
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

    private class HistoriqueCardCell extends ListCell<historique_traitement> {
        private final VBox cardContainer = new VBox(15);
        private final HBox headerBox = new HBox(20);
        private final Label patientNameLabel = new Label();
        private final Label maladieLabel = new Label();
        private final Label descriptionLabel = new Label();
        private final Label typeLabel = new Label();
        private final HBox bilanBox = new HBox(20);
        private final HBox actionsBox = new HBox(15);
        private final Button btnAjouterSuivi = new Button("Suivi Médical");
        private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

        public HistoriqueCardCell() {
            // Card container setup with enhanced design
            cardContainer.setPrefWidth(960);
            cardContainer.setMinWidth(960);
            cardContainer.setMaxWidth(960);
            cardContainer.setPrefHeight(320);
            cardContainer.setMinHeight(320);
            cardContainer.setMaxHeight(320);

            // Modern design with gradient background, shadow, and rounded corners
            cardContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #e6f5f5); " +
                    "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 30; " +
                    "-fx-effect: dropshadow(gaussian, rgba(48,180,180,0.3), 20, 0.2, 0, 5);");

            // 3D transformation for animation
            cardContainer.getTransforms().addAll(rotateX, rotateY);

            // Patient icon with a circular gradient and 3D effect
            StackPane patientIcon = new StackPane();
            patientIcon.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #30b4b4, #289b9b); " +
                    "-fx-background-radius: 50%; -fx-min-width: 50px; -fx-min-height: 50px; " +
                    "-fx-max-width: 50px; -fx-max-height: 50px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 2);");

            Label initialsLabel = new Label("P");
            initialsLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
            patientIcon.getChildren().add(initialsLabel);

            // Patient name with a modern font style
            patientNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: #30b4b4; " +
                    "-fx-font-family: 'Segoe UI';");

            // Header layout
            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);
            headerBox.getChildren().addAll(patientIcon, patientNameLabel, headerSpacer, btnAjouterSuivi);
            headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            headerBox.setStyle("-fx-padding: 0 0 15 0;");

            // Maladie label with a subtle underline effect
            maladieLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555; -fx-font-size: 15px; " +
                    "-fx-border-color: #30b4b4; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 5 0;");

            // Description with improved readability
            descriptionLabel.setWrapText(true);
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 5 0; " +
                    "-fx-font-family: 'Segoe UI';");
            descriptionLabel.setMaxHeight(90);

            // Type label with a glowing badge effect
            typeLabel.setStyle("-fx-background-color: linear-gradient(to right, #30b4b4, #289b9b); " +
                    "-fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 30; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-effect: dropshadow(gaussian, #30b4b4, 10, 0.5, 0, 0);");

            // Bilan section with a modern layout
            bilanBox.setStyle("-fx-padding: 10 0;");

            // Suivi Médical button with animation and glow
            FontIcon followUpIcon = new FontIcon(BootstrapIcons.CLIPBOARD_PLUS);
            followUpIcon.setIconSize(18);
            followUpIcon.setIconColor(Color.WHITE);

            btnAjouterSuivi.setGraphic(followUpIcon);
            btnAjouterSuivi.setStyle("-fx-background-color: linear-gradient(to right, #30b4b4, #289b9b); " +
                    "-fx-padding: 8 20; -fx-text-fill: white; -fx-background-radius: 25; " +
                    "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(48,180,180,0.5), 15, 0.3, 0, 2);");

            // Hover animation for the button
            btnAjouterSuivi.setOnMouseEntered(e -> {
                btnAjouterSuivi.setStyle("-fx-background-color: linear-gradient(to right, #289b9b, #30b4b4); " +
                        "-fx-padding: 8 20; -fx-text-fill: white; -fx-background-radius: 25; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(48,180,180,0.8), 20, 0.5, 0, 3);");
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), btnAjouterSuivi);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.play();
            });

            btnAjouterSuivi.setOnMouseExited(e -> {
                btnAjouterSuivi.setStyle("-fx-background-color: linear-gradient(to right, #30b4b4, #289b9b); " +
                        "-fx-padding: 8 20; -fx-text-fill: white; -fx-background-radius: 25; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(48,180,180,0.5), 15, 0.3, 0, 2);");
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), btnAjouterSuivi);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });

            btnAjouterSuivi.setOnAction(event -> {
                historique_traitement historique = getItem();
                if (historique != null) {
                    afficherSuivisMedicaux(historique);
                }
            });

            // Separators with a gradient effect
            Separator topSeparator = new Separator();
            topSeparator.setStyle("-fx-background-color: linear-gradient(to right, #e6f5f5, #30b4b4);");

            Separator bottomSeparator = new Separator();
            bottomSeparator.setStyle("-fx-background-color: linear-gradient(to right, #30b4b4, #e6f5f5);");

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            // Info container with improved spacing and layout
            VBox infoContainer = new VBox(15);
            infoContainer.getChildren().addAll(
                    createInfoRow("Maladie:", maladieLabel),
                    createInfoRow("Description:", descriptionLabel),
                    createInfoRow("Type de traitement:", typeLabel),
                    createInfoRow("Bilan:", bilanBox)
            );

            cardContainer.getChildren().addAll(
                    headerBox,
                    topSeparator,
                    infoContainer,
                    spacer,
                    bottomSeparator,
                    actionsBox
            );

            // 3D hover animation for the card
            cardContainer.setOnMouseEntered(e -> {
                cardContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #d9ecec); " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 30; " +
                        "-fx-effect: dropshadow(gaussian, rgba(48,180,180,0.5), 25, 0.3, 0, 8);");

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(rotateX.angleProperty(), 5),
                                new KeyValue(rotateY.angleProperty(), 5))
                );
                timeline.play();
            });

            cardContainer.setOnMouseExited(e -> {
                cardContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #e6f5f5); " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 30; " +
                        "-fx-effect: dropshadow(gaussian, rgba(48,180,180,0.3), 20, 0.2, 0, 5);");

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(rotateX.angleProperty(), 0),
                                new KeyValue(rotateY.angleProperty(), 0))
                );
                timeline.play();
            });
        }

        private HBox createInfoRow(String label, Node content) {
            HBox row = new HBox(15);
            Label titleLabel = new Label(label);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444; -fx-font-size: 14px; " +
                    "-fx-font-family: 'Segoe UI';");
            titleLabel.setPrefWidth(160);
            row.getChildren().addAll(titleLabel, content);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            return row;
        }

        @Override
        protected void updateItem(historique_traitement historique, boolean empty) {
            super.updateItem(historique, empty);

            if (empty || historique == null) {
                setGraphic(null);
            } else {
                btnAjouterSuivi.setVisible(true);
                btnAjouterSuivi.setManaged(true);

                if (actionsBox.getChildren().contains(btnAjouterSuivi)) {
                    actionsBox.getChildren().remove(btnAjouterSuivi);
                }

                StackPane patientIcon = (StackPane) headerBox.getChildren().get(0);
                Label initialsLabel = (Label) patientIcon.getChildren().get(0);
                String initials = String.valueOf(historique.getNom().charAt(0));
                initialsLabel.setText(initials);

                patientNameLabel.setText(historique.getNom() + " " + historique.getPrenom());
                maladieLabel.setText(historique.getMaladies());
                descriptionLabel.setText(historique.getDescription());
                typeLabel.setText(historique.getType_traitement());

                bilanBox.getChildren().clear();
                String bilan = historique.getBilan();
                if (bilan != null && !bilan.isEmpty()) {
                    File bilanFile = new File(storagePath + bilan);
                    if (bilanFile.exists()) {
                        String extension = getFileExtension(bilanFile).toLowerCase();

                        if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                                extension.equals("gif") || extension.equals("bmp")) {
                            try {
                                StackPane thumbnailContainer = new StackPane();
                                thumbnailContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                                        "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-padding: 5;");

                                ImageView thumbnail = new ImageView(new Image(bilanFile.toURI().toString()));
                                thumbnail.setFitHeight(50);
                                thumbnail.setFitWidth(50);
                                thumbnail.setPreserveRatio(true);

                                thumbnailContainer.getChildren().add(thumbnail);
                                bilanBox.getChildren().add(thumbnailContainer);
                            } catch (Exception e) {
                                // Ignore image errors
                            }
                        }

                        Button viewButton = new Button("Voir");
                        FontIcon viewIcon = new FontIcon(BootstrapIcons.EYE);
                        viewIcon.setIconSize(14);
                        viewIcon.setIconColor(Color.valueOf("#30b4b4"));
                        viewButton.setGraphic(viewIcon);

                        viewButton.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #30b4b4; " +
                                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 13px; " +
                                "-fx-padding: 6 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.2, 0, 1);");

                        viewButton.setOnMouseEntered(e -> {
                            viewButton.setStyle("-fx-background-color: #e6f5f5; -fx-text-fill: #30b4b4; " +
                                    "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 13px; " +
                                    "-fx-padding: 6 15; -fx-effect: dropshadow(gaussian, rgba(48,180,180,0.4), 10, 0.3, 0, 2);");
                        });

                        viewButton.setOnMouseExited(e -> {
                            viewButton.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #30b4b4; " +
                                    "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 13px; " +
                                    "-fx-padding: 6 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.2, 0, 1);");
                        });

                        viewButton.setOnAction(event -> viewBilanFile(bilan));
                        bilanBox.getChildren().add(viewButton);
                    } else {
                        Label notFoundLabel = new Label(bilan + " (introuvable)");
                        notFoundLabel.setStyle("-fx-text-fill: #d9534f; -fx-font-style: italic; -fx-font-size: 13px;");
                        bilanBox.getChildren().add(notFoundLabel);
                    }
                } else {
                    HBox emptyBilanBox = new HBox(5);
                    FontIcon infoIcon = new FontIcon(BootstrapIcons.INFO_CIRCLE);
                    infoIcon.setIconSize(14);
                    infoIcon.setIconColor(Color.valueOf("#999"));

                    Label noBilanLabel = new Label("Aucun bilan disponible");
                    noBilanLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 13px;");

                    emptyBilanBox.getChildren().addAll(infoIcon, noBilanLabel);
                    bilanBox.getChildren().add(emptyBilanBox);
                }

                setGraphic(cardContainer);
            }
        }
    }

    private void viewBilanFile(String bilanFileName) {
        try {
            File bilanFile = new File(storagePath + bilanFileName);
            if (bilanFile.exists()) {
                String extension = getFileExtension(bilanFile).toLowerCase();

                if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                        extension.equals("gif") || extension.equals("bmp")) {
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
                    closeButton.setStyle("-fx-background-color: #30b4b4; -fx-text-fill: white; -fx-background-radius: 20; " +
                            "-fx-padding: 8 20; -fx-font-weight: bold;");
                    closeButton.setOnAction(e -> imageStage.close());

                    VBox root = new VBox(15);
                    root.getChildren().addAll(scrollPane, closeButton);
                    root.setPadding(new javafx.geometry.Insets(15));
                    root.setStyle("-fx-background-color: #f8f9fa;");

                    Scene scene = new Scene(root);
                    imageStage.setScene(scene);
                    imageStage.show();
                } else if (extension.equals("pdf")) {
                    try {
                        java.awt.Desktop.getDesktop().open(bilanFile);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Impossible d'ouvrir le fichier PDF. Vérifiez que vous avez un lecteur PDF installé.");
                    }
                } else {
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

    @FXML
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) listViewHistorique.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de naviguer vers la page d'accueil: " + e.getMessage());
        }
    }

    private void setFullScreen() {
        anchorPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.setMaximized(true);
            }
        });
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/medecin_profile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue medecin_profile.fxml: " + e.getMessage());
        }
    }
}