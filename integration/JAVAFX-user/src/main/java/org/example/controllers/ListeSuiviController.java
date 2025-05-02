package org.example.controllers;

import javafx.scene.layout.*;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.control.Tooltip;
import org.example.models.historique_traitement;
import org.example.models.suivie_medical;
import org.example.services.HisServices;
import org.example.services.SuivServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeSuiviController implements Initializable {

    @FXML
    private ListView<suivie_medical> listViewSuivi;

    @FXML
    private Label patientInfoLabel;

    @FXML
    private Button retourButton;

    @FXML
    private Button acceuil;

    @FXML
    private Button profileButton;

    private SuivServices suivServices;
    private HisServices hisServices;
    private ObservableList<suivie_medical> suiviList;
    private historique_traitement historiqueTraitement;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suivServices = new SuivServices();
        hisServices = new HisServices();
        setFullScreen();
        setupListView();
        acceuil.setOnAction(event -> handleAcceuilRedirect());
        listViewSuivi.setPlaceholder(new Label("Aucun suivi disponible"));
    }

    private void handleAcceuilRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liste_historique.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) acceuil.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupListView() {
        listViewSuivi.setCellFactory(listView -> new ProfessionalTableLikeListCell());
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setHistoriqueTraitement(historique_traitement historiqueTraitement) {
        this.historiqueTraitement = historiqueTraitement;
        if (historiqueTraitement != null) {
            loadData();
        } else {
            System.err.println("Erreur: historiqueTraitement est null");
        }
    }

    public void loadData() {
        if (historiqueTraitement != null && listViewSuivi != null) {
            try {
                List<suivie_medical> suivis = suivServices.getSuiviByHistoriqueId(historiqueTraitement.getId());
                suiviList = FXCollections.observableArrayList(suivis);
                listViewSuivi.getItems().clear();
                listViewSuivi.setItems(suiviList);
                listViewSuivi.refresh();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                        "Impossible de charger les suivis médicaux: " + e.getMessage());
            }
        }
    }

    @FXML
    private BorderPane mainContainer;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void afficherSuivi(suivie_medical suivi) {
        try {
            // Similar to modifierSuivi, this is using a BorderPane content swap
            // rather than a full scene change
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/afficher_suivi.fxml"));
            Parent detailsView = loader.load();
            AfficherSuiviController controller = loader.getController();
            controller.setSuiviMedical(suivi);
            controller.setHistoriqueTraitement(historiqueTraitement);

            // Get the stage and ensure it's maximized
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setMaximized(true);

            controller.setRetourCallback(() -> {
                loadData();
                if (mainContainer != null) {
                    VBox listContent = new VBox(15);
                    listContent.setStyle("-fx-padding: 20;");
                    listContent.getChildren().add(listViewSuivi);
                    mainContainer.setCenter(listContent);
                    // Ensure the stage stays maximized when returning
                    stage.setMaximized(true);
                }
            });

            if (mainContainer != null) {
                mainContainer.setCenter(detailsView);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue des détails: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterNouveauSuivi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_suivi.fxml"));
            Parent root = loader.load();
            AjouterSuivController controller = loader.getController();
            controller.setHistoriqueTraitement(historiqueTraitement);
            controller.setOnSuiviAddedCallback(this::loadData);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Suivi Médical");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void retourVersList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liste_historique.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setTitle("Liste des Historiques de Traitement");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de retourner à la liste des historiques: " + e.getMessage());
        }
    }

    @FXML
    private void backToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profil: " + e.getMessage());
        }
    }

    private void modifierSuivi(suivie_medical suivi) {
        try {
            // Since this is using a BorderPane approach rather than full scene replacement,
            // we'll keep most of the logic but ensure fullscreen is properly handled
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_suivi.fxml"));
            Parent modifierView = loader.load();
            ModifierSuiviController controller = loader.getController();
            controller.setSuiviMedical(suivi);
            controller.setHistoriqueTraitement(historiqueTraitement);
            controller.setRefreshCallback(this::loadData);

            // Get the stage from the SceneManager approach
            Stage stage = (Stage) mainContainer.getScene().getWindow();

            // Make sure window is maximized
            stage.setMaximized(true);

            controller.setRetourCallback(() -> {
                loadData();
                if (mainContainer != null) {
                    VBox listContent = new VBox(15);
                    listContent.setStyle("-fx-padding: 20;");
                    listContent.getChildren().add(listViewSuivi);
                    mainContainer.setCenter(listContent);
                    // Ensure the stage stays maximized when returning
                    stage.setMaximized(true);
                }
            });

            if (mainContainer != null) {
                mainContainer.setCenter(modifierView);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue de modification: " + e.getMessage());
        }
    }
    private void supprimerSuivi(suivie_medical suivi) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer ce suivi médical ?");
        confirmDialog.setContentText("Date: " + suivi.getDate() + "\nCommentaire: " + suivi.getCommentaire());
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                suivServices.remove(suivi);
                loadData();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Suppression réussie");
                alert.setContentText("Le suivi médical a été supprimé avec succès.");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur de suppression");
                alert.setContentText("Une erreur est survenue lors de la suppression: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class ProfessionalTableLikeListCell extends ListCell<suivie_medical> {
        private final GridPane gridPane = new GridPane();
        private final Label dateLabel = new Label();
        private final Label commentaireLabel = new Label();
        private final HBox actionsBox = new HBox(5);
        private final Button btnModifier = new Button();
        private final Button btnSupprimer = new Button();
        private final Button btnAfficher = new Button();
        private final ParallelTransition hoverAnimation;

        public ProfessionalTableLikeListCell() {
            // Configure grid pane with professional styling
            gridPane.setHgap(15);
            gridPane.setVgap(5);
            gridPane.setPrefWidth(780);
            gridPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffffff, #e0f4f4);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 15;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-color: #30b4b4;" +
                            "-fx-border-width: 1;"
            );

            // Add shadow effect for depth
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(48, 180, 180, 0.3));
            shadow.setRadius(10);
            gridPane.setEffect(shadow);

            // Add column constraints
            for (int i = 0; i < 3; i++) {
                ColumnConstraints column = new ColumnConstraints();
                if (i == 0) column.setPrefWidth(150); // Date
                else if (i == 1) column.setPrefWidth(430); // Commentaire
                else column.setPrefWidth(180); // Actions
                gridPane.getColumnConstraints().add(column);
            }

            // Configure text elements with professional typography
            dateLabel.setWrapText(true);
            dateLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #30b4b4; -fx-font-weight: bold; -fx-font-family: 'Arial';");
            commentaireLabel.setWrapText(true);
            commentaireLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-family: 'Arial';");

            // Add elements to grid
            gridPane.add(dateLabel, 0, 0);
            gridPane.add(commentaireLabel, 1, 0);
            gridPane.add(actionsBox, 2, 0);

            // Configure button icons with modern styling
            FontIcon viewIcon = new FontIcon(BootstrapIcons.EYE);
            viewIcon.setIconSize(20);
            viewIcon.setIconColor(Color.WHITE);
            btnAfficher.setGraphic(viewIcon);
            btnAfficher.setStyle(
                    "-fx-background-color: #4285F4;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"
            );
            btnAfficher.setTooltip(new Tooltip("Afficher"));
            btnAfficher.setPrefSize(40, 40);

            FontIcon editIcon = new FontIcon(BootstrapIcons.PENCIL_SQUARE);
            editIcon.setIconSize(20);
            editIcon.setIconColor(Color.WHITE);
            btnModifier.setGraphic(editIcon);
            btnModifier.setStyle(
                    "-fx-background-color: #FFA500;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"
            );
            btnModifier.setTooltip(new Tooltip("Modifier"));
            btnModifier.setPrefSize(40, 40);

            FontIcon deleteIcon = new FontIcon(BootstrapIcons.TRASH);
            deleteIcon.setIconSize(20);
            deleteIcon.setIconColor(Color.WHITE);
            btnSupprimer.setGraphic(deleteIcon);
            btnSupprimer.setStyle(
                    "-fx-background-color: #FF0000;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"
            );
            btnSupprimer.setTooltip(new Tooltip("Supprimer"));
            btnSupprimer.setPrefSize(40, 40);

            // Configure buttons
            configureButtons();

            // Add combined 3D rotation and scale animation on hover
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(400), gridPane);
            rotateTransition.setByAngle(15);
            rotateTransition.setCycleCount(2);
            rotateTransition.setAutoReverse(true);

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), gridPane);
            scaleTransition.setToX(1.02);
            scaleTransition.setToY(1.02);
            scaleTransition.setCycleCount(2);
            scaleTransition.setAutoReverse(true);

            hoverAnimation = new ParallelTransition(rotateTransition, scaleTransition);

            gridPane.setOnMouseEntered(event -> {
                hoverAnimation.play();
                gridPane.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #e0f4f4, #ffffff);" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 15;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: #30b4b4;" +
                                "-fx-border-width: 2;"
                );
                shadow.setColor(Color.rgb(48, 180, 180, 0.5));
            });

            gridPane.setOnMouseExited(event -> {
                gridPane.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #ffffff, #e0f4f4);" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 15;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: #30b4b4;" +
                                "-fx-border-width: 1;"
                );
                shadow.setColor(Color.rgb(48, 180, 180, 0.3));
            });
        }

        private void configureButtons() {
            btnModifier.setOnAction(event -> {
                suivie_medical suivi = getItem();
                if (suivi != null) {
                    modifierSuivi(suivi);
                }
            });

            btnSupprimer.setOnAction(event -> {
                suivie_medical suivi = getItem();
                if (suivi != null) {
                    supprimerSuivi(suivi);
                }
            });

            btnAfficher.setOnAction(event -> {
                suivie_medical suivi = getItem();
                if (suivi != null) {
                    afficherSuivi(suivi);
                }
            });
        }

        @Override
        protected void updateItem(suivie_medical suivi, boolean empty) {
            super.updateItem(suivi, empty);
            if (empty || suivi == null) {
                setGraphic(null);
            } else {
                dateLabel.setText(suivi.getDate());
                commentaireLabel.setText(suivi.getCommentaire());
                actionsBox.getChildren().clear();
                actionsBox.setSpacing(10);
                actionsBox.getChildren().addAll(btnAfficher, btnModifier, btnSupprimer);
                setGraphic(gridPane);
            }
        }
    }

    private void setFullScreen() {
        anchorPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.setMaximized(true);
                VBox mainVBox = (VBox) anchorPane.getChildren().stream()
                        .filter(node -> node instanceof VBox && ((VBox) node).getLayoutY() > 120)
                        .findFirst()
                        .orElse(null);
                if (mainVBox != null) {
                    AnchorPane.setBottomAnchor(mainVBox, 0.0);
                    AnchorPane.setLeftAnchor(mainVBox, 0.0);
                    AnchorPane.setRightAnchor(mainVBox, 0.0);
                }
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
            System.err.println("Erreur lors du chargement de la vue medecin_profile.fxml: " + e.getMessage());
        }
    }
}