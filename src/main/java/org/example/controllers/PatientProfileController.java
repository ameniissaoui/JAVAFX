package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.PatientService;

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