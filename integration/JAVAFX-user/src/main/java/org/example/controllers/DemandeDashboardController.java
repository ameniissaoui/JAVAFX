package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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