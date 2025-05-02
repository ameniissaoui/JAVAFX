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
import org.example.services.DiplomaVerificationService;

public class MedecinRegistrationController extends BaseRegistrationController {

    @FXML private TextField specialiteField;
    @FXML private TextField diplomaPathField;
    @FXML private Label specialiteErrorLabel;
    @FXML private Label diplomaErrorLabel;
    @FXML private Label diplomaInfoLabel;
    @FXML private Label messageLabel;

    private final MedecinService medecinService = new MedecinService();
    private File selectedDiplomaFile;
    private final DiplomaVerificationService diplomaVerificationService = new DiplomaVerificationService();

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
    }

    @FXML
    protected void handleBrowseDiploma() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner votre diplôme");
        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter("Documents PDF (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().addAll(pdfFilter, imageFilter);

        File file = fileChooser.showOpenDialog(diplomaPathField.getScene().getWindow());
        if (file != null) {
            long fileSize = file.length();
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (fileSize > maxSize) {
                diplomaErrorLabel.setText("Le fichier est trop volumineux (max 5MB)");
                diplomaErrorLabel.setVisible(true);
                return;
            }
            selectedDiplomaFile = file;
            diplomaPathField.setText(file.getName());
            diplomaErrorLabel.setVisible(false);
        }
    }

    @Override
    @FXML
    protected void handleRegistration() {
        try {
            if (!validateAllFields()) {
                messageLabel.setText("Veuillez corriger les erreurs dans le formulaire.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (medecinService.findByEmail(emailField.getText().trim()) != null) {
                messageLabel.setText("Cet email est déjà utilisé.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String diplomaPath = saveDiplomaFile();
            boolean isDiplomaValid = diplomaVerificationService.verifyDiploma(diplomaPath);
            Medecin medecin = createMedecinFromFields(diplomaPath);
            medecin.setIs_verified(isDiplomaValid);
            medecinService.add(medecin);
            if (isDiplomaValid) {
                messageLabel.setText("Compte médecin créé et vérifié avec succès !");
            } else {
                messageLabel.setText("Compte créé, mais le diplôme n'a pas pu être vérifié automatiquement. Un administrateur vérifiera votre diplôme.");
            }
            messageLabel.setStyle("-fx-text-fill: green;");

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

        Path uploadDir = Paths.get("uploads", "diplomas");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = selectedDiplomaFile.getName();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        Path targetPath = uploadDir.resolve(uniqueFilename);
        Files.copy(selectedDiplomaFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    private boolean validateAllFields() {
        boolean baseFieldsValid = validateBaseFields();
        boolean medecinFieldsValid = validateMedecinFields();
        return baseFieldsValid && medecinFieldsValid;
    }

    private boolean validateMedecinFields() {
        boolean isValid = true;

        specialiteErrorLabel.setVisible(false);
        diplomaErrorLabel.setVisible(false);

        if (specialiteField.getText() == null || specialiteField.getText().trim().isEmpty()) {
            specialiteField.setStyle("-fx-border-color: red;");
            specialiteErrorLabel.setVisible(true);
            isValid = false;
        } else {
            specialiteField.setStyle("");
        }

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
            var baseUser = collectBaseUserInfo();
            Medecin medecin = new Medecin(
                    baseUser.getNom(),
                    baseUser.getPrenom(),
                    baseUser.getEmail(),
                    baseUser.getMotDePasse(),
                    baseUser.getDateNaissance(),
                    baseUser.getTelephone(),
                    null, // image
                    "medecin", // role
                    specialiteField.getText().trim(),
                    diplomaPath
            );
            return medecin;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la création du médecin : " + e.getMessage());
        }
    }

    private void redirectToLogin() {
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