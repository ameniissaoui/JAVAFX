package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.Commande;
import org.example.services.CommandeServices;
import org.example.services.CartItemServices;

import java.io.IOException;
import java.net.URL;

public class EditCommandeController {

    // Private field to store the commande ID
    private int commandeId;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField adresseField;
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label adresseError;
    @FXML private Button submitButton;
    @FXML private Label cartCountLabel;
    @FXML private Label cartCountLabel1;

    private Commande currentCommande;
    private CommandeServices cs = new CommandeServices();
    private CartItemServices cartItemServices = new CartItemServices();

    @FXML
    void initialize() {
        // Clear errors first
        clearErrors();

        // Update cart count
        updateCartCount();

        // Add listener to maximize the window once the scene is fully loaded
        Platform.runLater(() -> {
            if (nomField.getScene() != null && nomField.getScene().getWindow() != null) {
                Stage stage = (Stage) nomField.getScene().getWindow();
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

    public void initData(Commande commande) {
        this.currentCommande = commande;
        this.commandeId = commande.getId(); // Store ID internally

        // Fill the form with commande data
        nomField.setText(commande.getNom());
        prenomField.setText(commande.getPrenom());
        emailField.setText(commande.getEmail());
        phoneField.setText(String.valueOf(commande.getPhone_number()));
        adresseField.setText(commande.getAdresse());

        // Ensure window is maximized when data is loaded
        Platform.runLater(() -> {
            if (nomField.getScene() != null && nomField.getScene().getWindow() != null) {
                Stage stage = (Stage) nomField.getScene().getWindow();
                maximizeStage(stage);
            }
        });
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        clearErrors();

        if (validateInput()) {
            try {
                int phoneValue = Integer.parseInt(phoneField.getText());

                // Update the current commande with new values
                currentCommande.setNom(nomField.getText());
                currentCommande.setPrenom(prenomField.getText());
                currentCommande.setEmail(emailField.getText());
                currentCommande.setAdresse(adresseField.getText());
                currentCommande.setPhone_number(phoneValue);

                cs.editProduit(currentCommande);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Commande mise à jour avec succès");
                navigateBack();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Échec de la mise à jour de la commande: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    void show(ActionEvent event) {
        try {
            // Try to find the correct FXML path
            URL fxmlLocation = null;
            String[] possiblePaths = {
                    "/fxml/front/showCommande.fxml"
            };

            for (String path : possiblePaths) {
                URL url = getClass().getResource(path);
                if (url != null) {
                    fxmlLocation = url;
                    break;
                }
            }

            if (fxmlLocation == null) {
                throw new IllegalStateException("Could not find showCommande.fxml in any location!");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) nomField.getScene().getWindow();

            // Create new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Apply aggressive maximization
            stage.setMaximized(true);

            // Set explicit size to screen size
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            // Show stage
            stage.show();

            // Multiple maximization attempts
            Platform.runLater(() -> {
                stage.setMaximized(true);

                // Second attempt with delay
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                delay.setOnFinished(e -> stage.setMaximized(true));
                delay.play();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to display orders: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate nom
        if (nomField.getText().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            isValid = false;
        }

        // Validate prénom
        if (prenomField.getText().isEmpty()) {
            prenomError.setText("Le prénom est obligatoire");
            isValid = false;
        }

        // Validate email
        if (emailField.getText().isEmpty()) {
            emailError.setText("L'email est obligatoire");
            isValid = false;
        } else if (!isValidEmail(emailField.getText())) {
            emailError.setText("Format d'email invalide");
            isValid = false;
        }

        // Validate phone
        if (phoneField.getText().isEmpty()) {
            phoneError.setText("Le téléphone est obligatoire");
            isValid = false;
        } else {
            try {
                Integer.parseInt(phoneField.getText());
            } catch (NumberFormatException e) {
                phoneError.setText("Le téléphone doit être un nombre");
                isValid = false;
            }
        }

        // Validate adresse
        if (adresseField.getText().isEmpty()) {
            adresseError.setText("L'adresse est obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void clearErrors() {
        nomError.setText("");
        prenomError.setText("");
        emailError.setText("");
        phoneError.setText("");
        adresseError.setText("");
    }

    private void navigateBack() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showCommande.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("FXML file not found!");
                return;
            }
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) nomField.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Make sure it's maximized
            stage.setMaximized(true);

            // Show the stage
            stage.show();

            // Additional call to ensure maximization
            Platform.runLater(() -> {
                stage.setMaximized(true);
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de retourner à la liste des commandes.");

            // Attempt to find the FXML file in alternative locations
            tryAlternativeNavigation();
        }
    }

    private void navigateToShowCommande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/showCommande.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) nomField.getScene().getWindow();

            // Create and set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Ensure it's maximized before showing
            stage.setMaximized(true);

            // Show the stage
            stage.show();

            // Extra call to ensure maximization
            Platform.runLater(() -> {
                stage.setMaximized(true);
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de naviguer vers la liste des commandes.");
            tryAlternativeNavigation();
        }
    }

    private void tryAlternativeNavigation() {
        // Try alternative FXML paths if the primary one failed
        String[] possiblePaths = {
                "showCommande.fxml",
                "../showCommande.fxml",
                "/showCommande.fxml",
                "/views/showCommande.fxml",
                "/view/showCommande.fxml",
                "/fxml/showCommande.fxml",
                "/front/showCommande.fxml",
                "/back/showCommande.fxml"
        };

        for (String path : possiblePaths) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Parent root = loader.load();
                Stage stage = (Stage) nomField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();

                // Additional maximization call
                Platform.runLater(() -> {
                    stage.setMaximized(true);
                });

                return; // Successfully navigated
            } catch (Exception e) {
                // Continue trying other paths
                System.out.println("Failed to load: " + path);
            }
        }

        // If all navigation attempts failed, show a more detailed error
        showAlert(Alert.AlertType.ERROR, "Erreur critique de navigation",
                "Impossible de naviguer vers la liste des commandes. Veuillez redémarrer l'application.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods from DetailsProduitController
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
    void redirectProduit() {
        navigateTo("/fxml/front/showProduit.fxml");
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
        navigateTo("/fxml/front/showProduit.fxml");
    }

    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page: " + e.getMessage());
        }
    }

    // Update cart count
    private void updateCartCount() {
        try {
            org.example.util.SessionManager session = org.example.util.SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            int count = 0;

            if (currentUser != null) {
                // Get cart count logic would go here
                // This is a simplified version
                count = cartItemServices.showProduit().size();
            }

            if (cartCountLabel != null) {
                cartCountLabel.setText(String.valueOf(count));
            }

            if (cartCountLabel1 != null) {
                cartCountLabel1.setText(String.valueOf(count));
            }
        } catch (Exception e) {
            System.err.println("Error updating cart count: " + e.getMessage());
        }
    }
}