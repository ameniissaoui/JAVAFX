package org.example.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Patient;
import org.example.models.User;
import org.example.models.historique_traitement;
import org.example.services.HisServices;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class HistoriquePatientController implements Initializable {

    @FXML
    private ListView<historique_traitement> listViewHistoriques;

    @FXML
    private VBox emptyHistoryPlaceholder;

    private User currentUser;
    private HisServices hisServices;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if the user is logged in and is a patient
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isPatient()) {
            showMessage("Erreur: Accès non autorisé", "danger");
            return;
        }

        // Get the current user from session
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialize services
        hisServices = new HisServices();

        // Load historiques
        rafraichirHistoriques();
    }

    @FXML
    public void rafraichirHistoriques() {
        if (currentUser instanceof Patient) {
            try {
                System.out.println("Recherche des historiques pour le patient ID: " + currentUser.getId());

                // Initialize service if needed
                if (hisServices == null) {
                    hisServices = new HisServices();
                }

                // Get patient's history records
                List<historique_traitement> historiques = hisServices.getHistoriquesByPatientId(currentUser.getId());
                System.out.println("Nombre d'historiques trouvés: " + historiques.size());

                if (listViewHistoriques != null) {
                    listViewHistoriques.getItems().clear();

                    // Convert to observable list
                    ObservableList<historique_traitement> observableHistoriques = FXCollections.observableArrayList(historiques);

                    // Set custom cell factory
                    listViewHistoriques.setCellFactory(param -> new HistoriqueListCell());

                    // Set items to the list
                    listViewHistoriques.setItems(observableHistoriques);

                    // Show/hide empty state
                    if (emptyHistoryPlaceholder != null) {
                        boolean isEmpty = historiques.isEmpty();
                        emptyHistoryPlaceholder.setVisible(isEmpty);
                        emptyHistoryPlaceholder.setManaged(isEmpty);
                        listViewHistoriques.setVisible(!isEmpty);
                        listViewHistoriques.setManaged(!isEmpty);

                        if (isEmpty) {
                            // Add fade-in animation for empty state
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), emptyHistoryPlaceholder);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);
                            fadeIn.play();
                        }
                    }

                    if (!historiques.isEmpty()) {
                        showMessage("Historiques chargés avec succès", "success");
                    }
                } else {
                    System.err.println("ERREUR: listViewHistoriques est null");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des historiques",
                        "Une erreur s'est produite: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Methods to navigate to other screens
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
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de demande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rendez-vous-view.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
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




    @FXML
    public void redirectToHistoriques(ActionEvent event) {
        try {
            if (currentUser instanceof Patient) {
                // Make sure the current user is set in the session
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");

                // Use SceneManager to load the view that displays historiques
                SceneManager.loadScene("/fxml/historiques_patient.fxml", event);

                System.out.println("Redirecting to historiques for patient ID: " + currentUser.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté en tant que patient pour accéder à cette page.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void navigateToReservations(ActionEvent actionEvent) {
    }

    public void redirectProduit(ActionEvent actionEvent) {
    }

    public void handleHistoRedirect(ActionEvent actionEvent) {
    }

    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }

    // Custom cell for displaying historique_traitement items
    private class HistoriqueListCell extends ListCell<historique_traitement> {
        private final VBox contentBox = new VBox(8);
        private final HBox headerBox = new HBox(10);
        private final HBox footerBox = new HBox(10);
        private final VBox mainContainer = new VBox(12);
        private final HBox actionsBox = new HBox(8);
        private final Label titleLabel = new Label();
        private final Label descLabel = new Label();
        private final Label userInfoLabel = new Label(); // Replace date with user info
        private final Button btnModifier = new Button("Modifier");
        private final Button btnSupprimer = new Button("Supprimer");
        private final Button btnDetails = new Button("Détails");
        private final HBox bilanBox = new HBox(5);
        private final Label statusBadge = new Label();

        public HistoriqueListCell() {
            // Configure UI components
            mainContainer.getStyleClass().add("history-card");
            titleLabel.getStyleClass().add("history-title");
            descLabel.getStyleClass().add("history-description");
            userInfoLabel.getStyleClass().add("history-user-info"); // Updated class name
            statusBadge.getStyleClass().addAll("status-badge", "status-active");

            // Configure action buttons
            btnModifier.getStyleClass().addAll("action-button", "edit-button");
            btnSupprimer.getStyleClass().addAll("action-button", "delete-button");
            btnDetails.getStyleClass().addAll("action-button", "details-button");

            // Add icons to buttons
            btnModifier.setGraphic(new Label("✏️"));
            btnSupprimer.setGraphic(new Label("🗑️"));
            btnDetails.setGraphic(new Label("👁️"));

            // Set up bilan indicator
            bilanBox.getStyleClass().add("bilan-container");
            Label bilanIcon = new Label("📄");
            Label bilanText = new Label("Bilan disponible");
            bilanIcon.getStyleClass().add("bilan-icon");
            bilanText.getStyleClass().add("bilan-text");
            bilanBox.getChildren().addAll(bilanIcon, bilanText);
            bilanBox.setAlignment(Pos.CENTER_LEFT);

            // Set up header with title and status
            headerBox.getChildren().addAll(titleLabel, statusBadge);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);
            headerBox.getChildren().add(1, headerSpacer);

            // Set up content area
            contentBox.getChildren().addAll(headerBox, descLabel);

            // Set up footer with user info and bilan indicator
            footerBox.getChildren().addAll(userInfoLabel);
            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);
            footerBox.getChildren().add(footerSpacer);

            // Set up actions
            actionsBox.setAlignment(Pos.CENTER_RIGHT);
            actionsBox.getChildren().addAll(btnDetails, btnModifier, btnSupprimer);

            // Add all components to main container
            mainContainer.getChildren().addAll(contentBox, footerBox, actionsBox);

            // Configure button actions
            btnModifier.setOnAction(e -> modifierHistorique(getItem()));
            btnSupprimer.setOnAction(e -> supprimerHistorique(getItem()));
            btnDetails.setOnAction(e -> afficherDetailsHistorique(getItem()));

            // Add animations
            setUpAnimations();
        }

        private void setUpAnimations() {
            // Create hover effect
            mainContainer.setOnMouseEntered(e -> {
                FadeTransition fade = new FadeTransition(Duration.millis(150), mainContainer);
                fade.setFromValue(0.95);
                fade.setToValue(1.0);
                fade.play();
            });
        }

        @Override
        protected void updateItem(historique_traitement item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Update UI with item data
                titleLabel.setText(item.getType_traitement() + " - " + item.getMaladies());
                descLabel.setText(item.getDescription());

                // Display user info instead of date
                userInfoLabel.setText("Patient: " + item.getNom() + " " + item.getPrenom());

                // Update bilan indicator
                if (item.getBilan() != null && !item.getBilan().isEmpty()) {
                    if (!contentBox.getChildren().contains(bilanBox)) {
                        contentBox.getChildren().add(bilanBox);
                    }
                } else {
                    contentBox.getChildren().remove(bilanBox);
                }

                setGraphic(mainContainer);
                setText(null);

                // Apply slight fade-in effect when loading
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainContainer);
                fadeIn.setFromValue(0.8);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        }
    }

    // Methods for historique actions
    private void modifierHistorique(historique_traitement historique) {
        try {
            // Charger la vue de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_historique.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            ModifierHistController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);
            controller.setRefreshCallback(() -> rafraichirHistoriques());

            // Obtenir la fenêtre actuelle à partir d'un élément de l'interface
            Stage stage = (Stage) listViewHistoriques.getScene().getWindow();

            // Configurer la nouvelle scène avec les dimensions de l'écran
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            stage.setScene(scene);
            stage.setTitle("Modifier l'historique");
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de modification",
                    "Une erreur s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerHistorique(historique_traitement historique) {
        // Demander confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer cet historique ?");
        confirmAlert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprimer le fichier bilan associé s'il existe
                if (historique.getBilan() != null && !historique.getBilan().isEmpty()) {
                    try {
                        Files.deleteIfExists(Paths.get("src/main/resources/uploads/" + historique.getBilan()));
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
                    }
                }

                // Supprimer l'historique de la base de données
                if (hisServices == null) {
                    hisServices = new HisServices();
                }
                hisServices.remove(historique);  // Appel correct sur l'instance

                showMessage("L'historique a été supprimé avec succès", "success");
                rafraichirHistoriques(); // Mettre à jour la liste
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de suppression",
                        "Une erreur s'est produite: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void afficherDetailsHistorique(historique_traitement historique) {
        try {
            // Charger la vue de détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/details_historique.fxml"));
            Parent detailsView = loader.load();

            // Configurer le contrôleur
            DetailsHistoriqueController controller = loader.getController();
            controller.setHistoriqueTraitement(historique);

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) listViewHistoriques.getScene().getWindow();

            // Remplacer le contenu dans la fenêtre actuelle avec taille maximale
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(detailsView, screenBounds.getWidth(), screenBounds.getHeight());

            stage.setScene(scene);
            stage.setTitle("Détails du Traitement - " + historique.getNom() + " " + historique.getPrenom());
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails",
                    "Une erreur s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }
}