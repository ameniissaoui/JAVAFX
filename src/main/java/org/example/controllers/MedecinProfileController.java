package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.models.Medecin;
import org.example.services.MedecinService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.UUID;

public class MedecinProfileController extends BaseProfileController {
    @FXML private TextField specialiteField;
    @FXML private Label diplomaPathLabel;
    @FXML private ImageView diplomaImageView;
    @FXML private Label specialiteErrorLabel;

    // New fields for the updated design
    @FXML private ImageView profileImageView;
    @FXML private Label fullNameLabel;
    @FXML private Label nomDisplayLabel;
    @FXML private Label prenomDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Label telephoneDisplayLabel;
    @FXML private Label dateNaissanceDisplayLabel;
    @FXML private Button downloadDiplomaButton;

    private final MedecinService medecinService = new MedecinService();
    private File selectedDiplomaFile;
    private Medecin currentMedecin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        System.out.println("Initializing MedecinProfileController...");

        // Try to load default profile image
        try {
            Image defaultProfileImage = new Image(getClass().getResourceAsStream("/images/default-profile.png"));
            if (profileImageView != null) {
                profileImageView.setImage(defaultProfileImage);
            }
        } catch (Exception e) {
            System.err.println("Could not load default profile image: " + e.getMessage());
        }
    }

    public void setMedecin(Medecin medecin) {
        this.currentMedecin = medecin;
        // Call setUser from parent class to handle common fields
        super.setUser(medecin);
        // Load medecin-specific data
        loadMedecinData();
    }

    private void loadMedecinData() {
        if (currentMedecin != null) {
            // Set editable fields
            specialiteField.setText(currentMedecin.getSpecialite());
            //usernameField.setText(currentUser.getNom());

            // Set display labels for overview tab
            String nom = currentUser.getNom();
            String prenom = currentUser.getPrenom();

            nomDisplayLabel.setText(nom);
            prenomDisplayLabel.setText(prenom);
            emailDisplayLabel.setText(currentUser.getEmail());
            telephoneDisplayLabel.setText(currentUser.getTelephone());

            // Format date for display
            if (currentUser.getDateNaissance() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateNaissanceDisplayLabel.setText(sdf.format(currentUser.getDateNaissance()));
            }

            // Set full name in profile card
            fullNameLabel.setText(prenom + " " + nom);

            // Load diploma info
            String diplomaPath = currentMedecin.getDiploma();
            if (diplomaPath != null && !diplomaPath.isEmpty()) {
                diplomaPathLabel.setText(Paths.get(diplomaPath).getFileName().toString());

                // Try to load the diploma preview if it's an image
                try {
                    if (diplomaPath.toLowerCase().endsWith(".png") ||
                            diplomaPath.toLowerCase().endsWith(".jpg") ||
                            diplomaPath.toLowerCase().endsWith(".jpeg")) {

                        Image diplomaImage = new Image(new File(diplomaPath).toURI().toString());
                        diplomaImageView.setImage(diplomaImage);
                    } else {
                        // If it's a PDF, try to show a PDF icon
                        try {
                            Image pdfIcon = new Image(getClass().getResourceAsStream("/images/pdf-icon.png"));
                            diplomaImageView.setImage(pdfIcon);
                        } catch (Exception e) {
                            System.err.println("PDF icon not found: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading diploma image: " + e.getMessage());
                }
            }

            // Try to load user profile picture if available
            String profilePicturePath = currentMedecin.getDiploma();
            if (profilePicturePath != null && !profilePicturePath.isEmpty() && profileImageView != null) {
                try {
                    File profilePictureFile = new File(profilePicturePath);
                    if (profilePictureFile.exists()) {
                        Image profileImage = new Image(profilePictureFile.toURI().toString());
                        profileImageView.setImage(profileImage);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile picture: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void handleBrowseDiploma() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner votre diplôme");

        // Set extension filters
        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter("Documents PDF (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().addAll(pdfFilter, imageFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(specialiteField.getScene().getWindow());

        if (file != null) {
            // Check file size (max 5MB)
            long fileSize = file.length();
            long maxSize = 5 * 1024 * 1024; // 5MB in bytes

            if (fileSize > maxSize) {
                showMessage("Le fichier est trop volumineux (max 5MB)", "danger");
                return;
            }

            // Update the UI
            selectedDiplomaFile = file;
            diplomaPathLabel.setText(file.getName());

            // Preview the image if possible
            if (file.getName().toLowerCase().endsWith(".png") ||
                    file.getName().toLowerCase().endsWith(".jpg") ||
                    file.getName().toLowerCase().endsWith(".jpeg")) {
                try {
                    Image newImage = new Image(file.toURI().toString());
                    diplomaImageView.setImage(newImage);
                } catch (Exception e) {
                    System.err.println("Error loading preview: " + e.getMessage());
                }
            } else {
                // If it's a PDF, show a PDF icon
                try {
                    Image pdfIcon = new Image(getClass().getResourceAsStream("/images/pdf-icon.png"));
                    diplomaImageView.setImage(pdfIcon);
                } catch (Exception e) {
                    System.err.println("Error loading PDF icon: " + e.getMessage());
                }
            }
        }
    }

    @Override
    protected boolean validateFields() {
        boolean baseFieldsValid = super.validateFields();
        boolean medecinFieldsValid = validateMedecinFields();

        return baseFieldsValid && medecinFieldsValid;
    }

    private boolean validateMedecinFields() {
        boolean isValid = true;

        // Validate specialite field
        if (specialiteField.getText() == null || specialiteField.getText().trim().isEmpty()) {
            specialiteField.setStyle("-fx-border-color: red;");
            if (specialiteErrorLabel != null) {
                specialiteErrorLabel.setVisible(true);
            }
            isValid = false;
        } else {
            specialiteField.setStyle("");
            if (specialiteErrorLabel != null) {
                specialiteErrorLabel.setVisible(false);
            }
        }

        return isValid;
    }

    @Override
    protected void updateUserData() {
        super.updateUserData();

        if (currentMedecin != null) {
            currentMedecin.setSpecialite(specialiteField.getText().trim());

            // Update diploma path if a new file was selected
            if (selectedDiplomaFile != null) {
                try {
                    String diplomaPath = saveDiplomaFile();
                    currentMedecin.setDiploma(diplomaPath);
                } catch (IOException e) {
                    showMessage("Erreur lors de l'enregistrement du diplôme: " + e.getMessage(), "danger");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void saveUser() {
        if (currentMedecin != null) {
            try {
                medecinService.update(currentMedecin);

                // Update the Overview tab display labels after saving
                updateDisplayLabels();

                showMessage("Profil médecin mis à jour avec succès", "success");
                            } catch (Exception e) {
                showMessage("Erreur lors de la mise à jour du profil: " + e.getMessage(), "danger");
                        e.printStackTrace();
                            }
                                    }
    }

private void updateDisplayLabels() {
    // Update all display labels with current values
    nomDisplayLabel.setText(currentUser.getNom());
    prenomDisplayLabel.setText(currentUser.getPrenom());
    emailDisplayLabel.setText(currentUser.getEmail());
    telephoneDisplayLabel.setText(currentUser.getTelephone());

    // Update full name in profile card
    fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

    // Format date for display
    if (currentUser.getDateNaissance() != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        dateNaissanceDisplayLabel.setText(sdf.format(currentUser.getDateNaissance()));
    }
}

private String saveDiplomaFile() throws IOException {
    if (selectedDiplomaFile == null) {
        return currentMedecin.getDiploma();
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

@FXML
private void showDiplomaDetails() {
    if (currentMedecin != null && currentMedecin.getDiploma() != null) {
        try {
            // Try to open the file with the default system application
            File diplomaFile = new File(currentMedecin.getDiploma());
            if (diplomaFile.exists()) {
                java.awt.Desktop.getDesktop().open(diplomaFile);
            } else {
                showMessage("Le fichier du diplôme est introuvable", "danger");
            }
        } catch (IOException e) {
            showMessage("Impossible d'ouvrir le fichier: " + e.getMessage(), "danger");
            e.printStackTrace();
        }
    } else {
        showMessage("Aucun diplôme disponible", "info");
    }
}

// Method to handle profile picture upload
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
            currentMedecin.setDiploma(targetPath.toString());

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