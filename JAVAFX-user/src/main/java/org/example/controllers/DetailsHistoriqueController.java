package org.example.controllers;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.util.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    private Button btnfermer;
    @FXML
    private Button Historique;
    @FXML
    private ListView<suivie_medical> listViewSuivis;

    @FXML
    private VBox suivisMedicauxContainer;

    private historique_traitement historique;
    private SuivServices suiviService;
    private ObservableList<suivie_medical> suivisList;
    private final String storagePath = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suiviService = new SuivServices();
        setupListView();
        listViewSuivis.setPlaceholder(new Label("Aucun suivi disponible"));
        addButtonHoverAnimation(btnfermer);
    }

    private void setupListView() {
        listViewSuivis.setCellFactory(listView -> new ProfessionalTableLikeListCell());
    }

    @FXML
    public void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }

    @FXML
    public void redirectToHistorique(ActionEvent event) {
        SceneManager.loadScene("/fxml/ajouter_historique.fxml", event);
    }

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper method to show messages (this might need to be adapted based on your application's message system)
    private void showMessage(String message, String type) {
        System.out.println("[" + type + "] " + message);
        // Implement your message display logic here
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        SceneManager.loadScene("/fxml/demande.fxml", event);

    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {

    }
    @FXML
    public void viewDoctors(ActionEvent event) {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Use SceneManager to load the DoctorList.fxml in full screen
            SceneManager.loadScene("/fxml/DoctorList.fxml", event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des médecins: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void redirectProduit(ActionEvent actionEvent) {
    }

    public void handleHistoRedirect(ActionEvent actionEvent) {
    }


    private class ProfessionalTableLikeListCell extends ListCell<suivie_medical> {
        private final GridPane gridPane = new GridPane();
        private final Label dateLabel = new Label();
        private final Label commentaireLabel = new Label();
        private final ParallelTransition hoverAnimation;

        public ProfessionalTableLikeListCell() {
            gridPane.setHgap(15);
            gridPane.setVgap(5);
            gridPane.setPrefWidth(850);
            gridPane.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 15;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 1;"
            );

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(48, 180, 180, 0.3));
            shadow.setRadius(10);
            gridPane.setEffect(shadow);

            for (int i = 0; i < 2; i++) {
                ColumnConstraints column = new ColumnConstraints();
                if (i == 0) column.setPrefWidth(150); // Date
                else column.setPrefWidth(650); // Commentaire
                gridPane.getColumnConstraints().add(column);
            }

            dateLabel.setWrapText(true);
            dateLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #3fbbc0; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            commentaireLabel.setWrapText(true);
            commentaireLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-family: 'Segoe UI';");

            gridPane.add(dateLabel, 0, 0);
            gridPane.add(commentaireLabel, 1, 0);

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(400), gridPane);
            rotateTransition.setByAngle(5);
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
                        "-fx-background-color: #f8f9fa;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 15;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: #3fbbc0;" +
                                "-fx-border-width: 2;"
                );
                shadow.setColor(Color.rgb(48, 180, 180, 0.5));
            });

            gridPane.setOnMouseExited(event -> {
                gridPane.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 15;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: #e0e0e0;" +
                                "-fx-border-width: 1;"
                );
                shadow.setColor(Color.rgb(48, 180, 180, 0.3));
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
                setGraphic(gridPane);
            }
        }
    }

    private void addButtonHoverAnimation(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> scaleIn.play());
        button.setOnMouseExited(e -> scaleOut.play());
    }

    public void setHistoriqueTraitement(historique_traitement historique) {
        this.historique = historique;

        labelMaladie.setText(historique.getMaladies());
        labelDescription.setText(historique.getDescription());
        labelTypeTraitement.setText(historique.getType_traitement());
        labelNom.setText(historique.getNom());
        labelPrenom.setText(historique.getPrenom());

        if (historique.getBilan() != null && !historique.getBilan().isEmpty()) {
            afficherBilan(historique.getBilan());
        } else {
            Label noBilanLabel = new Label("Aucun bilan disponible");
            bilanContainer.getChildren().add(noBilanLabel);
        }

        loadSuivis();
    }

    private void afficherBilan(String bilanFileName) {
        File bilanFile = new File(storagePath + bilanFileName);
        if (bilanFile.exists()) {
            String extension = getFileExtension(bilanFile).toLowerCase();

            if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                    extension.equals("gif") || extension.equals("bmp")) {
                try {
                    ImageView thumbnail = new ImageView(new Image(bilanFile.toURI().toString()));
                    thumbnail.setFitHeight(100);
                    thumbnail.setFitWidth(100);
                    thumbnail.setPreserveRatio(true);

                    Button viewButton = new Button("Voir en grand");
                    viewButton.setStyle(
                            "-fx-background-color: #3fbbc0;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-padding: 8 15;" +
                                    "-fx-font-family: 'Segoe UI';"
                    );
                    addButtonHoverAnimation(viewButton);
                    viewButton.setOnAction(event -> viewBilanFile(bilanFileName));

                    bilanContainer.getChildren().addAll(thumbnail, viewButton);
                } catch (Exception e) {
                    Label errorLabel = new Label("Erreur de chargement de l'image: " + e.getMessage());
                    bilanContainer.getChildren().add(errorLabel);
                }
            } else {
                Button openButton = new Button("Ouvrir " + bilanFileName);
                openButton.setStyle(
                        "-fx-background-color: #3fbbc0;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 8 15;" +
                                "-fx-font-family: 'Segoe UI';"
                );
                addButtonHoverAnimation(openButton);
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
                    closeButton.setStyle(
                            "-fx-background-color: #3fbbc0;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-padding: 8 15;" +
                                    "-fx-font-family: 'Segoe UI';"
                    );
                    addButtonHoverAnimation(closeButton);
                    closeButton.setOnAction(e -> imageStage.close());

                    VBox root = new VBox(10);
                    root.getChildren().addAll(scrollPane, closeButton);
                    root.setPadding(new Insets(10));

                    Scene scene = new Scene(root);
                    imageStage.setScene(scene);
                    imageStage.show();
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

    private void loadSuivis() {
        try {
            List<suivie_medical> suivis = suiviService.getSuiviByHistoriqueId(historique.getId());

            if (suivis == null || suivis.isEmpty()) {
                suivis = historique.getSuivisMedicaux();
            }

            if (suivis != null && !suivis.isEmpty()) {
                suivisList = FXCollections.observableArrayList(suivis);
                listViewSuivis.setItems(suivisList);
                listViewSuivis.refresh();
            } else {
                suivisList = FXCollections.observableArrayList();
                listViewSuivis.setItems(suivisList);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des suivis: " + e.getMessage());
            e.printStackTrace();
            if (historique.getSuivisMedicaux() != null) {
                suivisList = FXCollections.observableArrayList(historique.getSuivisMedicaux());
                listViewSuivis.setItems(suivisList);
            } else {
                suivisList = FXCollections.observableArrayList();
                listViewSuivis.setItems(suivisList);
            }
        }
    }

    public void retourProfil(ActionEvent event) {
        // Use the SceneManager to load the patient profile scene
        // This will automatically handle setting the window to full screen
        SceneManager.loadScene("/fxml/historique_patient.fxml", event);

        // Get the stage and update the title
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Profil Patient");
    }
    @FXML
    private void fermer(ActionEvent event) {
        try {
            // Log the attempt to load the FXML
            System.out.println("Attempting to load /fxml/historiques_patient.fxml");

            // Use SceneManager to load the scene
            SceneManager.loadScene("/fxml/historiques_patient.fxml", event);

            System.out.println("Successfully loaded historiques_patient.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger historiques_patient.fxml");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void ajouterSuivi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_suivi.fxml"));
            Parent root = loader.load();
            AjouterSuivController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);
            controller.setOnSuiviAddedCallback(this::loadSuivis);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Suivi Médical");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
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
    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }
}