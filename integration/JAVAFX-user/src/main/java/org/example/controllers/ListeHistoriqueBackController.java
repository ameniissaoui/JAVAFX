package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.models.historique_traitement;
import org.example.services.HisServices;
import org.example.util.AnimationUtils;
import org.example.util.NotificationManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ListeHistoriqueBackController implements Initializable {

    @FXML private VBox cardContainer; // Changed from ListView to VBox for card layout
    @FXML private Button suivi;
    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private Button historique;
    @FXML private Button tablesButton;
    @FXML private Button eventButton;
    @FXML private Button profileButton;
    @FXML private Button acceuil;
    @FXML private Button statsButton;

    // Nouveaux éléments du FXML
    @FXML private Button userProfileButton;
    @FXML private Button manageReportsButton;
    @FXML private Button buttoncommande;
    @FXML private Button reservationButton;
    @FXML private Button statistiqueButton;
    @FXML private Button statisticsButton;
    @FXML private Button statisticsButton1;

    private HisServices hisServices;
    private ObservableList<historique_traitement> historiqueList;
    private FilteredList<historique_traitement> filteredData;

    // Chemin de stockage des fichiers de bilan
    private final String storagePath = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hisServices = new HisServices();

        // Styling active navigation
        historique.getStyleClass().add("active-nav-button");
        historique.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6;");

        // Initialisation de la liste
        setupCardContainer();
        // Removed addHeaderToListView() as headers are not needed for cards

        // Chargement des données
        loadData();

        // Configuration des boutons et événements
        setupEventHandlers();

        // Configuration de la recherche
        setupFiltering();

        // Animations de démarrage
        animateOnInitialize();

        // Configuration des nouveaux boutons
        if (statsButton != null) {
            statsButton.setOnAction(event -> handleStatsView());
        }
        suivi.setOnAction(event -> handleSuiviRedirect());
        acceuil.setOnAction(event -> handleAcceuilRedirect());

        // Configuration des nouveaux boutons du FXML
        setupNewButtons();
        buttoncommande.setOnAction(event -> handleCommandeRedirect());
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        reservationButton.setOnAction(event -> handleReservationRedirect());
        statistiqueButton.setOnAction(event -> handleStatistiqueRedirect());
        profileButton.setOnAction(event -> handleProfileRedirect());

        // Boutons qui étaient référencés dans le FXML mais manquants dans le contrôleur initial
        if (acceuil != null) {
            acceuil.setOnAction(event -> handleAcceuilRedirect());
        }
    }

    private void setupNewButtons() {
        // Gestionnaires d'événements pour les nouveaux boutons
        if (manageReportsButton != null) {
            manageReportsButton.setOnAction(event -> handleRedirect("/fxml/reports_management.fxml"));
        }

        if (buttoncommande != null) {
            buttoncommande.setOnAction(event -> handleRedirect("/fxml/commande_back.fxml"));
        }

        if (reservationButton != null) {
            reservationButton.setOnAction(event -> handleRedirect("/fxml/reservation_back.fxml"));
        }

        if (statistiqueButton != null) {
            statistiqueButton.setOnAction(event -> handleRedirect("/fxml/event_statistics.fxml"));
        }

        if (statisticsButton != null) {
            statisticsButton.setOnAction(event -> handleRedirect("/fxml/user_statistics.fxml"));
        }

        if (statisticsButton1 != null) {
            statisticsButton1.setOnAction(event -> handleRedirect("/fxml/commande_statistics.fxml"));
        }

        if (userProfileButton != null) {
            userProfileButton.setOnAction(event -> handleRedirect("/fxml/user_profile.fxml"));
        }
    }

    private void handleStatsView() {
        try {
            Parent statsRoot = FXMLLoader.load(getClass().getResource("/fxml/stats_suivi.fxml"));
            Stage stage = (Stage) statsButton.getScene().getWindow();
            Scene statsScene = new Scene(statsRoot);
            stage.setScene(statsScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void animateOnInitialize() {
        Platform.runLater(() -> {
            AnimationUtils.fadeIn(cardContainer, 300);
            AnimationUtils.fadeIn(titleLabel, 200);
        });
    }

    private void setupEventHandlers() {
        // Gestion des redirections
        suivi.setOnAction(event -> handleSuiviRedirect());
        tablesButton.setOnAction(event -> handleRedirect("/fxml/produit_back.fxml"));
        eventButton.setOnAction(event -> handleRedirect("/fxml/evenement_back.fxml"));
        profileButton.setOnAction(event -> handleRedirect("/fxml/profile_back.fxml"));

        // Gestion du bouton rafraîchir avec animation améliorée
        refreshButton.setOnAction(event -> {
            AnimationUtils.rotateTransition(refreshButton, 360, 500).play();

            // Effet visuel temporaire sur le cardContainer
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#3b82f6"));
            glow.setWidth(20);
            glow.setHeight(20);
            cardContainer.setEffect(glow);

            loadData();

            // Retirer l'effet après un délai
            Platform.runLater(() -> {
                try {
                    Thread.sleep(300);
                    cardContainer.setEffect(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            NotificationManager.showNotification("Données rechargées",
                    "Les données ont été rafraîchies avec succès",
                    NotificationManager.NotificationType.SUCCESS);
        });

        // Ajouter un effet visuel au survol du bouton rafraîchir
        refreshButton.setOnMouseEntered(e ->
                refreshButton.setStyle("-fx-background-color: #e9a208; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;"));
        refreshButton.setOnMouseExited(e ->
                refreshButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;"));
    }

    private void handleRedirect(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) suivi.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFiltering() {
        // Configuration de la recherche avec animation
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDataBySearch(newValue);
        });

        // Amélioration visuelle du champ de recherche
        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-prompt-text-fill: #3b82f6;");
                if (searchField.getParent() instanceof HBox) {
                    HBox parent = (HBox) searchField.getParent();
                    parent.setStyle("-fx-background-color: white; -fx-border-color: #3b82f6; -fx-border-radius: 5; -fx-padding: 5 10;");
                }
            } else {
                searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                if (searchField.getParent() instanceof HBox) {
                    HBox parent = (HBox) searchField.getParent();
                    parent.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 5; -fx-padding: 5 10;");
                }
            }
        });

        // Update card display when filtered data changes
        filteredData.predicateProperty().addListener((observable, oldValue, newValue) -> {
            updateCardDisplay();
        });
    }

    private void filterDataBySearch(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredData.setPredicate(historique -> true);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();

            filteredData.setPredicate(historique -> {
                boolean matchesSearch =
                        (historique.getNom() != null && historique.getNom().toLowerCase().contains(lowerCaseSearch)) ||
                                (historique.getPrenom() != null && historique.getPrenom().toLowerCase().contains(lowerCaseSearch)) ||
                                (historique.getMaladies() != null && historique.getMaladies().toLowerCase().contains(lowerCaseSearch)) ||
                                (historique.getDescription() != null && historique.getDescription().toLowerCase().contains(lowerCaseSearch));

                return matchesSearch;
            });
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleAcceuilRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Get the stage from the acceuil button
            Stage stage = (Stage) acceuil.getScene().getWindow();

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSuiviRedirect() {
        try {
            // Use SceneManager to load the scene and ensure it displays maximized
            SceneManager.loadScene("/fxml/liste_suivi_back.fxml", new ActionEvent(suivi, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des suivis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCardContainer() {
        cardContainer.setSpacing(15);
        cardContainer.setPadding(new Insets(10));
        cardContainer.setStyle(
                "-fx-background-color: transparent;"
        );
    }

    public void loadData() {
        List<historique_traitement> histories = hisServices.afficher();
        historiqueList = FXCollections.observableArrayList(histories);

        // Création d'une FilteredList à partir des données d'origine
        filteredData = new FilteredList<>(historiqueList, p -> true);

        // Update card display
        updateCardDisplay();
    }

    private void updateCardDisplay() {
        cardContainer.getChildren().clear();
        for (historique_traitement historique : filteredData) {
            cardContainer.getChildren().add(createCard(historique));
        }
    }

    private VBox createCard(historique_traitement historique) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f9fafb; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        ));

        // Patient Info
        Label patientLabel = new Label("Patient: " + (historique.getNom() != null ? historique.getNom() : "") + " " +
                (historique.getPrenom() != null ? historique.getPrenom() : ""));
        patientLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2a44; -fx-font-size: 16px;");

        // Description
        Label descriptionLabel = new Label(historique.getDescription() != null ? historique.getDescription() : "");
        descriptionLabel.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 14px;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(600);

        // Action Button (Voir)
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button viewButton = new Button("Voir");
        viewButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 15; " +
                        "-fx-background-radius: 8;"
        );

        // Hover effect for button
        viewButton.setOnMouseEntered(e ->
                viewButton.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 8;"));
        viewButton.setOnMouseExited(e ->
                viewButton.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 8;"));

        viewButton.setOnAction(e -> viewBilanFile(historique.getBilan()));
        buttonBox.getChildren().add(viewButton);

        card.getChildren().addAll(patientLabel, descriptionLabel, buttonBox);
        return card;
    }
    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
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

                    ImageView imageView = new ImageView(new javafx.scene.image.Image(bilanFile.toURI().toString()));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(800);

                    ScrollPane scrollPane = new ScrollPane(imageView);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setPrefHeight(600);
                    scrollPane.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

                    Button closeButton = new Button("Fermer");
                    closeButton.setStyle(
                            "-fx-background-color: #3b82f6; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-padding: 8 20; " +
                                    "-fx-background-radius: 5;"
                    );
                    closeButton.setOnAction(e -> imageStage.close());

                    // Effet de survol pour le bouton
                    closeButton.setOnMouseEntered(e ->
                            closeButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;"));
                    closeButton.setOnMouseExited(e ->
                            closeButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;"));

                    VBox root = new VBox(15);
                    root.getChildren().addAll(scrollPane, closeButton);
                    root.setPadding(new Insets(15));
                    root.setAlignment(Pos.CENTER);
                    root.setStyle("-fx-background-color: #f8fafc;");

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
                        "Le fichier " + bilanFileName + " n'existe pas dans le dossier d'Uploads.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du fichier: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Customizing alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.getStyleClass().add("modern-alert");

        alert.showAndWait();
    }

    @FXML
    private void navigateToReportDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminReportDashboard.fxml", event);
    }

    @FXML
    private void handleStatisticsRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/statistics-view.fxml", event);
    }

    @FXML
    private void handleStatisticsCommandeRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/back/statistics.fxml", event);
    }
    @FXML
    private void handleCommandeRedirect() {
        try {
            SceneManager.loadScene("/fxml/back/showCommande.fxml", new ActionEvent(buttoncommande, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des commandes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTablesRedirect() {
        try {
            SceneManager.loadScene("/fxml/back/showProduit.fxml", new ActionEvent(tablesButton, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleeventRedirect() {
        try {
            SceneManager.loadScene("/fxml/listevent.fxml", new ActionEvent(eventButton, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleHistoriqueRedirect() {
        try {
            // Use SceneManager to load the scene and ensure it displays maximized
            SceneManager.loadScene("/fxml/liste_historique_back.fxml", new ActionEvent(historique, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleReservationRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ReservationList.fxml"));
            Stage stage = (Stage) reservationButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des réservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleStatistiqueRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/EventStatistics.fxml"));
            Stage stage = (Stage) statistiqueButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger les statistiques des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleProfileRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Profile.fxml"));
            Stage stage = (Stage) profileButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profil: " + e.getMessage());
            e.printStackTrace();
        }
    }
}