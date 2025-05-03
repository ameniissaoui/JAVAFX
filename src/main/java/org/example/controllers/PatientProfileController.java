package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.controllers.DemandeMyViewController;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.PatientService;
import org.example.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.UUID;

public class PatientProfileController extends BaseProfileController {
    private PatientService patientService;
    @FXML private ImageView profileImageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        patientService = new PatientService();
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Patient) {
            super.setUser(user);
        } else {
            throw new IllegalArgumentException("User must be a Patient");
        }
    }

    public void setPatient(Patient patient) {
        this.currentUser = patient;
        loadUserData();
    }

    @Override
    protected void saveUser() {
        if (currentUser instanceof Patient) {
            patientService.update((Patient) currentUser);
        }
    }

    public void handleOverview(ActionEvent actionEvent) {
    }

    public void handleChangePassword(ActionEvent actionEvent) {
    }

    public void handleModifier(ActionEvent actionEvent) {
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        try {
            // Make sure the currentUser is set in the SessionManager before navigating
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser);
                
                // Load the DemandeDashboard.fxml instead of DemandeMyView.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
                Parent root = loader.load();
                
                // Create new scene and show it
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
            // Make sure the currentUser is set in the SessionManager before navigating
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser);
                
                // Load the RendezVous view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rendez-vous-view.fxml"));
                Parent root = loader.load();
                
                // Get the controller
                RendezVousViewController controller = loader.getController();
                
                // Create new scene and show it
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
    public void redirectProduit(ActionEvent event) {
        // Existing method
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleUploadProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");

        // Set extension filters for images
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (file != null) {
            // Check file size (max 2MB)
            long fileSize = file.length();
            long maxSize = 2 * 1024 * 1024; // 2MB in bytes

            if (fileSize > maxSize) {
                showMessage("La photo est trop volumineuse (max 2MB)", "danger");
                return;
            }

            try {
                // Create upload directory if it doesn't exist
                Path uploadDir = Paths.get("uploads", "profiles");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                // Generate a unique filename
                String originalFilename = file.getName();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

                // Save the file
                Path targetPath = uploadDir.resolve(uniqueFilename);
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Update the model

                // Update the UI
                Image profileImage = new Image(file.toURI().toString());
                profileImageView.setImage(profileImage);

                showMessage("Photo de profil mise à jour", "success");
            } catch (IOException e) {
                showMessage("Erreur lors de l'enregistrement de la photo: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        }
    }
}