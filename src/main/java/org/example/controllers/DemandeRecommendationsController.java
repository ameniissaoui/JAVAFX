package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.services.DemandeDAO;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Recommendations view.
 * Displays recommendations based on user's demande data.
 */
public class DemandeRecommendationsController implements Initializable {

    @FXML
    private VBox recommendationsContainer;
    
    @FXML
    private VBox noRecommendationsView;
    
    @FXML
    private VBox recommendationsListView;
    
    private DemandeDAO demandeDAO;
    private Demande currentDemande;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        demandeDAO = new DemandeDAO();
        
        // Check if a user is logged in
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté", 
                    "Vous devez être connecté pour accéder à cette page.");
            
            // Redirect to login after a short delay
            redirectToLogin();
            return;
        }
        
        loadRecommendations();
    }
    
    /**
     * Load recommendations based on user's demande data
     */
    private void loadRecommendations() {
        try {
            // Get current patient ID from session
            int patientId = SessionManager.getCurrentPatientId();
            
            // Get latest demande for this patient
            Optional<Demande> latestDemande = demandeDAO.getByPatientId(patientId).stream().findFirst();
            
            if (latestDemande.isPresent()) {
                // If demande exists, show recommendations
                currentDemande = latestDemande.get();
                
                // For demonstration, we'll just show sample recommendations
                // In a real app, you would generate recommendations based on the demande data
                // For example, if water intake is low, recommend drinking more water
                
                // Toggle visibility
                noRecommendationsView.setVisible(false);
                recommendationsListView.setVisible(true);
                
                // Generate recommendations based on demande data
                // generateRecommendations(currentDemande);
            } else {
                // If no demande exists, show the no recommendations view
                noRecommendationsView.setVisible(true);
                recommendationsListView.setVisible(false);
            }
        } catch (Exception e) {
            // Show error message
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement", 
                    "Impossible de charger les recommandations: " + e.getMessage());
            e.printStackTrace();
            
            // Default to no recommendations view
            noRecommendationsView.setVisible(true);
            recommendationsListView.setVisible(false);
        }
    }
    
    /**
     * Generate a recommendation card and add it to the view
     * 
     * @param title The recommendation title
     * @param content The recommendation content
     */
    private void addRecommendation(String title, String content) {
        VBox card = new VBox();
        card.getStyleClass().add("recommendation-card");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("recommendation-title");
        titleLabel.setWrapText(true);
        
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("recommendation-content");
        contentLabel.setWrapText(true);
        
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));
        
        card.getChildren().addAll(titleLabel, contentLabel);
        recommendationsListView.getChildren().add(card);
    }
    
    /**
     * Navigate back to the demande dashboard
     * 
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation", 
                    "Impossible d'ouvrir le tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to the demande view
     * 
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToDemande(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeMyView.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation", 
                    "Impossible d'ouvrir la page de demande: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an alert dialog with the specified parameters
     * 
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
            Stage stage = (Stage) recommendationsContainer.getScene().getWindow();
            
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