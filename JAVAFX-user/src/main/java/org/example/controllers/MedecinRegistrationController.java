package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.Medecin;
import org.example.services.MedecinService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class MedecinRegistrationController extends BaseRegistrationController {

    @FXML private TextField specialiteField;
    @FXML private TextField diplomaPathField;
    @FXML private Label specialiteErrorLabel;
    @FXML private Label diplomaErrorLabel;
    @FXML private Label diplomaInfoLabel;
    @FXML private Label messageLabel;

    private final MedecinService medecinService = new MedecinService();
    private File selectedDiplomaFile;

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        // Additional initialization for medecin-specific fields if needed
    }

    @FXML
    protected void handleBrowseDiploma() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner votre diplôme");

        // Set extension filters
        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter("Documents PDF (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().addAll(pdfFilter, imageFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(diplomaPathField.getScene().getWindow());

        if (file != null) {
            // Check file size (max 5MB)
            long fileSize = file.length();
            long maxSize = 5 * 1024 * 1024; // 5MB in bytes

            if (fileSize > maxSize) {
                diplomaErrorLabel.setText("Le fichier est trop volumineux (max 5MB)");
                diplomaErrorLabel.setVisible(true);
                return;
            }

            // Update the UI
            selectedDiplomaFile = file;
            diplomaPathField.setText(file.getName());
            diplomaErrorLabel.setVisible(false);
        }
    }

    @Override
    @FXML
    protected void handleRegistration() {
        try {
            // First validate base fields
            if (!validateAllFields()) {
                messageLabel.setText("Veuillez corriger les erreurs dans le formulaire.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Check if email already exists
            if (medecinService.findByEmail(emailField.getText().trim()) != null) {
                messageLabel.setText("Cet email est déjà utilisé.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Save the diploma file to a permanent location and get the path
            String diplomaPath = saveDiplomaFile();

            // Create a new Medecin with the base user info and medecin-specific info
            Medecin medecin = createMedecinFromFields(diplomaPath);

            // Add the new medecin to the database
            medecinService.add(medecin);

            // Show success message
            messageLabel.setText("Compte médecin créé avec succès !");
            messageLabel.setStyle("-fx-text-fill: green;");

            // Redirect to login or dashboard after short delay
            redirectToLogin();

        } catch (IllegalStateException e) {
            messageLabel.setText("Erreur : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            messageLabel.setText("Une erreur inattendue s'est produite : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    private String saveDiplomaFile() throws IOException {
        if (selectedDiplomaFile == null) {
            return "";
        }

        // Create directory if it doesn't exist
        Path uploadDir = Paths.get("uploads", "diplomas");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate a unique filename to avoid conflicts
        String originalFilename = selectedDiplomaFile.getName();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save the file to the uploads directory
        Path targetPath = uploadDir.resolve(uniqueFilename);
        Files.copy(selectedDiplomaFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    /**
     * Validates all fields including medecin-specific fields
     * @return true if all fields are valid
     */
    private boolean validateAllFields() {
        boolean baseFieldsValid = validateBaseFields();
        boolean medecinFieldsValid = validateMedecinFields();

        return baseFieldsValid && medecinFieldsValid;
    }

    private boolean validateMedecinFields() {
        boolean isValid = true;

        // Reset error styles
        specialiteErrorLabel.setVisible(false);
        diplomaErrorLabel.setVisible(false);

        // Validate specialite field
        if (specialiteField.getText() == null || specialiteField.getText().trim().isEmpty()) {
            specialiteField.setStyle("-fx-border-color: red;");
            specialiteErrorLabel.setVisible(true);
            isValid = false;
        } else {
            specialiteField.setStyle("");
        }

        // Validate diploma field
        if (selectedDiplomaFile == null) {
            diplomaPathField.setStyle("-fx-border-color: red;");
            diplomaErrorLabel.setText("Veuillez télécharger votre diplôme");
            diplomaErrorLabel.setVisible(true);
            isValid = false;
        } else {
            diplomaPathField.setStyle("");
        }

        return isValid;
    }

    private Medecin createMedecinFromFields(String diplomaPath) throws IllegalStateException {
        try {
            // Get base user info
            var baseUser = collectBaseUserInfo();

            // Create and return a new Medecin
            return new Medecin(
                    baseUser.getId(),
                    baseUser.getNom(),
                    baseUser.getPrenom(),
                    baseUser.getEmail(),
                    baseUser.getMotDePasse(),
                    baseUser.getDateNaissance(),
                    baseUser.getTelephone(),
                    specialiteField.getText().trim(),
                    diplomaPath
            );
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la création du médecin : " + e.getMessage());
        }
    }

    /**
     * Redirects to the login page after successful registration
     */
    private void redirectToLogin() {
        // You can add a small delay here if desired
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) nomField.getScene().getWindow();
            if (stage != null) {
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("Failed to load Login.fxml: " + e.getMessage());
        }
    }
}