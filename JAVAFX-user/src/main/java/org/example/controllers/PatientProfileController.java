package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
        // Check if user is logged in and is a patient
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isPatient()) {
            showMessage("Erreur: Accès non autorisé", "danger");
            handleLogout();
            return;
        }

        // Get current patient from session
        Patient patient = SessionManager.getInstance().getCurrentPatient();
        if (patient != null) {
            currentUser = patient;
            loadUserData();
        }

        // Initialize service and set it
        patientService = new PatientService();
        setUserService();

        super.initialize(url, resourceBundle);
    }

    @Override
    protected void setUserService() {
        this.userService = patientService;
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Patient) {
            super.setUser(user, "patient");
            setUserService(); // Ensure service is set
        } else {
            throw new IllegalArgumentException("User must be a Patient");
        }
    }

    public void setPatient(Patient patient) {
        this.currentUser = patient;
        this.userType = "patient";
        loadUserData();
        setUserService(); // Ensure service is set

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
    public void redirectToDemande(ActionEvent event) {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
                Parent root = loader.load();
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
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rendez-vous-view.fxml"));
                Parent root = loader.load();
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
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
                Parent root = loader.load();
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
                    "Impossible d'ouvrir la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
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

        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (file != null) {
            long fileSize = file.length();
            long maxSize = 2 * 1024 * 1024;

            if (fileSize > maxSize) {
                showMessage("La photo est trop volumineuse (max 2MB)", "danger");
                return;
            }

            try {
                Path uploadDir = Paths.get("uploads", "profiles");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String originalFilename = file.getName();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

                Path targetPath = uploadDir.resolve(uniqueFilename);
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                if (currentUser instanceof Patient) {
                    ((Patient) currentUser).setProfilePicture(targetPath.toString()); // Requires profilePicture field
                    patientService.update((Patient) currentUser); // Save to database
                }

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