package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.models.User;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Demande Dashboard screen.
 * Provides navigation to both Demande and Recommendations views.
 */
public class DemandeDashboardController implements Initializable {

    @FXML
    private VBox demandeCard;

    @FXML
    private VBox recommendationCard;

    @FXML
    private Button profile;
    
    @FXML
    private Button chatButton;
    
    private Stage chatStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Check if a user is logged in
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                    "Vous devez être connecté pour accéder à cette page.");

            // Redirect to login after a short delay
            redirectToLogin();
        }
        profile.setOnAction(event -> handleProfileRedirect());
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

    private void handleProfileRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/patient_profile.fxml"));
            Stage stage = (Stage) profile.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the demande view from a button action
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToDemandeAction(ActionEvent event) {
        navigateToDemandeView((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    /**
     * Navigate to the demande view from a mouse click on a card
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToDemandeClick(MouseEvent event) {
        navigateToDemandeView((Stage) demandeCard.getScene().getWindow());
    }

    /**
     * Common method for navigating to the demande view
     * @param stage The stage to show the new scene in
     */
    private void navigateToDemandeView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeMyView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
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
        navigateToRecommendationsView((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    /**
     * Navigate to the recommendations view from a mouse click on a card
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToRecommendationsClick(MouseEvent event) {
        navigateToRecommendationsView((Stage) recommendationCard.getScene().getWindow());
    }

    /**
     * Common method for navigating to the recommendations view
     * @param stage The stage to show the new scene in
     */
    private void navigateToRecommendationsView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeRecommendations.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
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
        try {
            // Create a very basic interface programmatically to avoid any FXML loading issues
            BorderPane root = new BorderPane();
            
            // Create a header with a back button
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(10, 15, 10, 15));
            
            Label title = new Label("Tableau de Bord Santé");
            title.setStyle("-fx-font-size: 18px;");
            
            HBox rightAlign = new HBox();
            rightAlign.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(rightAlign, Priority.ALWAYS);
            
            Button backButton = new Button("Retour");
            backButton.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
                    Parent dashboardRoot = loader.load();
                    Scene scene = new Scene(dashboardRoot);
                    Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation", 
                           "Impossible de retourner au tableau de bord: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            
            rightAlign.getChildren().add(backButton);
            header.getChildren().addAll(title, rightAlign);
            
            // Create center content
            VBox center = new VBox(20);
            center.setAlignment(Pos.CENTER);
            center.setPadding(new Insets(20));
            
            Label maintenanceLabel = new Label("Tableau de bord en cours de maintenance");
            maintenanceLabel.setStyle("-fx-font-size: 16px;");
            
            Label tryAgainLabel = new Label("Veuillez réessayer plus tard");
            
            center.getChildren().addAll(maintenanceLabel, tryAgainLabel);
            
            // Set the components to the BorderPane
            root.setTop(header);
            root.setCenter(center);
            
            // Show the scene
            Scene scene = new Scene(root, 800, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir le tableau de bord santé: " + e.getMessage());
            e.printStackTrace();
        }
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

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}