package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.models.Patient;
import org.example.models.User;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Demande Dashboard screen.
 * Provides navigation to both Demande and Recommendations views.
 * All navigation methods use SceneManager to ensure full-screen display.
 */
public class DemandeDashboardController implements Initializable {

    @FXML private VBox demandeCard;
    @FXML private Button historique;
    @FXML private Button profileButton;
    @FXML private VBox recommendationCard;
    @FXML private Button profile;
    @FXML private Button chatButton;

    private Stage chatStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Check if a user is logged in
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                    "Vous devez être connecté pour accéder à cette page.");
            redirectToLogin();
        }
    }

    /**
     * Opens the chat dialog in a new window
     * @param event The action event that triggered this method
     */
    @FXML
    public void openChatDialog(ActionEvent event) {
        try {
            // Only create a new stage if one doesn't exist or if the previous one was closed
            if (chatStage == null || !chatStage.isShowing()) {
                // Load the chat dialog FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatDialog.fxml"));
                Parent root = loader.load();

                // Get the controller to initialize it after the stage is shown
                ChatDialogController controller = loader.getController();

                // Create a new stage for the chat dialog
                chatStage = new Stage();
                chatStage.initModality(Modality.NONE); // Allows interaction with main window
                chatStage.initStyle(StageStyle.DECORATED);
                chatStage.setTitle("Assistant ChronoSerena");
                chatStage.setResizable(true);

                // Set scene
                Scene scene = new Scene(root);

                // Apply chat styles
                scene.getStylesheets().add(getClass().getResource("/css/chatbot-styles.css").toExternalForm());

                chatStage.setScene(scene);

                // Position the dialog relative to the main window
                Stage mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                chatStage.setX(mainStage.getX() + mainStage.getWidth() - chatStage.getWidth() - 20);
                chatStage.setY(mainStage.getY() + 100);

                // Show the chat dialog
                chatStage.show();

                // Initialize the chat after the dialog is shown
                controller.initializeChat();
            } else {
                // If dialog exists but is minimized, bring it to front
                chatStage.setIconified(false);
                chatStage.toFront();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'ouverture",
                    "Impossible d'ouvrir l'assistant: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Navigate to the demande view from a button action
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToDemandeAction(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeMyView.fxml", event);
    }

    /**
     * Navigate to the demande view from a mouse click on a card
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToDemandeClick(MouseEvent event) {
        try {
            // Since MouseEvent doesn't work directly with SceneManager's loadScene method,
            // we need to handle this case manually but following same principles
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeMyView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) demandeCard.getScene().getWindow();

            // Create scene with screen dimensions for full screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Preserve stylesheets if any
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the recommendations view from a button action
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToRecommendationsAction(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeRecommendations.fxml", event);
    }

    /**
     * Navigate to the recommendations view from a mouse click on a card
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToRecommendationsClick(MouseEvent event) {
        try {
            // Since MouseEvent doesn't work directly with SceneManager's loadScene method,
            // we need to handle this case manually but following same principles
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeRecommendations.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) recommendationCard.getScene().getWindow();

            // Create scene with screen dimensions for full screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Preserve stylesheets if any
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to Health Analytics Dashboard
     * @param event The event that triggered this action
     */
    @FXML
    public void handleAnalytics(ActionEvent event) {
        SceneManager.loadScene("/fxml/HealthAnalyticsDashboard.fxml", event);
    }

    /**
     * Shows an alert dialog with the specified parameters
     * @param type The type of alert
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Redirects to the login page
     */
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) demandeCard.getScene().getWindow();

            // Handle case where the scene might not be attached yet
            if (stage == null) {
                stage = new Stage();
            }

            // For login, we might not want full-screen, so using regular scene transition
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }

    @FXML
    private void redirectProduit(ActionEvent event) {
        SceneManager.loadScene("/fxml/front/showProduit.fxml", event);
    }

    @FXML
    private void showNotifications(ActionEvent event) {
        // Implement notification display logic here
        // This could be a popup with recent notifications
        Alert notificationsAlert = new Alert(Alert.AlertType.INFORMATION);
        notificationsAlert.setTitle("Notifications");
        notificationsAlert.setHeaderText("Vos notifications");

        // Here you would load actual notifications from a service
        // This is just a placeholder
        VBox notificationsContent = new VBox(10);
        notificationsContent.getChildren().addAll(
                new Label("Rappel: Prendre médicament à 14:00"),
                new Label("Rendez-vous demain à 10:30")
        );

        notificationsAlert.getDialogPane().setContent(notificationsContent);
        notificationsAlert.showAndWait();
    }

    @FXML
    public void redirectToHistorique(ActionEvent event) {
        try {
            // Enhanced logging
            System.out.println("Starting history redirect...");
            System.out.println("SessionManager.isLoggedIn(): " + SessionManager.getInstance().isLoggedIn());
            System.out.println("SessionManager.isPatient(): " + SessionManager.getInstance().isPatient());

            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showErrorDialog("Erreur", "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Check if the user is a patient
            if (!SessionManager.getInstance().isPatient()) {
                showErrorDialog("Erreur", "Seuls les patients peuvent accéder à cette fonctionnalité.");
                return;
            }

            // Get the patient from the session
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            System.out.println("Patient from SessionManager: " + (patient != null ?
                    "ID=" + patient.getId() + ", Nom=" + patient.getNom() : "NULL"));

            if (patient == null) {
                System.out.println("Error: SessionManager.getCurrentPatient() returned null!");
                showErrorDialog("Erreur", "Impossible de récupérer les informations du patient.");
                return;
            }

            // Since we need to set the patient in the controller, we need custom loading
            // rather than using SceneManager directly
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_historique.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the patient
            AjouterHistController controller = loader.getController();
            controller.setPatient(patient);
            System.out.println("Patient passed to controller: ID=" + patient.getId() +
                    ", Nom=" + patient.getNom() + ", Prénom=" + patient.getPrenom());

            // Show the new scene with maximum size
            Stage stage = (Stage) historique.getScene().getWindow();

            // Get screen dimensions for maximum size
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Preserve stylesheets if any
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error during navigation to historique: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur", "Impossible de charger la page d'ajout d'historique: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_patient.fxml", event);
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeDashboard.fxml", event);
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        SceneManager.loadScene("/fxml/rendez-vous-view.fxml", event);
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
    public void navigateToEvent(ActionEvent event) {
        SceneManager.loadScene("/fxml/eventFront.fxml", event);
    }

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
    }
}