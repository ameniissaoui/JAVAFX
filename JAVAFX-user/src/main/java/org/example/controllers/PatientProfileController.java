package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.PatientService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PatientProfileController extends BaseProfileController {
    @FXML
    private Label cartCountLabel;

    @FXML
    private Label cartCountLabel1;

    private PatientService patientService;

    @FXML
    void initialize() {
        // Initialize any necessary components
        updateCartCount();

        // Maximize the stage when the view is loaded
        javafx.application.Platform.runLater(() -> {
            if (cartCountLabel != null && cartCountLabel.getScene() != null) {
                Stage stage = (Stage) cartCountLabel.getScene().getWindow();
                maximizeStage(stage);
            }
        });
    }

    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }

    private void updateCartCount() {
        // This would typically fetch the cart count from a service
        // For now, just set it to 0
        if (cartCountLabel != null) {
            cartCountLabel.setText("0");
        }
        if (cartCountLabel1 != null) {
            cartCountLabel1.setText("0");
        }
    }

    // Navigation methods from SuccessController
    @FXML
    void navigateToHome() {
        navigateTo("/fxml/front/home.fxml");
    }

    @FXML
    void navigateToHistoriques() {
        navigateTo("/fxml/historiques.fxml");
    }

    @FXML
    void redirectToDemande() {
        navigateTo("/fxml/DemandeDashboard.fxml");
    }

    @FXML
    void redirectToRendezVous() {
        navigateTo("/fxml/rendez-vous-view.fxml");
    }

    @FXML
    void navigateToTraitement() {
        navigateTo("/fxml/traitement.fxml");
    }

    @FXML
    void viewDoctors() {
        navigateTo("/fxml/DoctorList.fxml");
    }

    @FXML
    void navigateToContact() {
        navigateTo("/fxml/front/contact.fxml");
    }

    @FXML
    void navigateToProfile() {
        navigateTo("/fxml/front/profile.fxml");
    }

    @FXML
    void navigateToFavorites() {
        navigateTo("/fxml/front/favoris.fxml");
    }

    @FXML
    void commande() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToCommandes() {
        navigateTo("/fxml/front/ShowCommande.fxml");
    }

    @FXML
    void navigateToShop() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            System.out.println("Attempting to navigate to " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException(fxmlPath + " resource not found");
            }
            Parent root = loader.load();

            // Get the current stage
            Stage stage = null;
            if (cartCountLabel != null && cartCountLabel.getScene() != null) {
                stage = (Stage) cartCountLabel.getScene().getWindow();
            } else if (cartCountLabel1 != null && cartCountLabel1.getScene() != null) {
                stage = (Stage) cartCountLabel1.getScene().getWindow();
            }

            if (stage == null) {
                throw new RuntimeException("Cannot get current stage");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
            System.out.println("Successfully navigated to " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page", e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during navigation: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isPatient()) {
            showMessage("Erreur: Accès non autorisé", "danger");
            handleLogout();
            return;
        }

        Patient patient = SessionManager.getInstance().getCurrentPatient();
        if (patient != null) {
            currentUser = patient;
            loadUserData();
        }

        patientService = new PatientService();
        setUserService();
        super.initialize(url, resourceBundle);

        // Initialize cart count
        updateCartCount();
    }

    @Override
    protected void setUserService() {
        this.userService = patientService;
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Patient) {
            super.setUser(user, "patient");
            setUserService();
        } else {
            throw new IllegalArgumentException("User must be a Patient");
        }
    }

    public void setPatient(Patient patient) {
        this.currentUser = patient;
        this.userType = "patient";
        loadUserData();
        setUserService();
        if (patient != null) {
            SessionManager.getInstance().setCurrentUser(patient, "patient");
        }
    }

    @Override
    protected void saveUser() {
        if (currentUser instanceof Patient) {
            try {
                patientService.update((Patient) currentUser);
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                updateDisplayLabels();
                showMessage("Profil patient mis à jour avec succès", "success");
            } catch (Exception e) {
                showMessage("Erreur lors de la mise à jour du profil: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void redirectProduit(ActionEvent event) {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert("Vous devez être connecté pour accéder à cette page.", "error");
                return;
            }

            // Use SceneManager to load the new scene in full screen
            SceneManager.loadScene("/fxml/front/showProduit.fxml", event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening produits view: " + e.getMessage(), "error");
        }
    }
    private void showAlert(String message, String type) {
        Alert alert = new Alert(type.equals("error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(type.equals("error") ? "Erreur" : "Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}