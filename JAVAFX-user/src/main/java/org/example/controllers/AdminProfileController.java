package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.Admin;
import org.example.models.User;
import org.example.services.AdminService;
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

public class AdminProfileController extends BaseProfileController {
    private AdminService adminService;
    @FXML private ImageView profileImageView;
    @FXML private Button acceuil;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Check if user is logged in and is an admin
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isAdmin()) {
            showErrorDialog("Erreur", "Accès non autorisé");
            handleLogout();
            return;
        }

        // Get current admin from session
        Admin admin = SessionManager.getInstance().getCurrentAdmin();
        if (admin != null) {
            currentUser = admin;
            loadUserData();
        }

        acceuil.setOnAction(event -> handleAcceuilRedirect());
        super.initialize(url, resourceBundle);
        adminService = new AdminService();
        setUserService();
        super.initialize(url, resourceBundle);
    }
    @Override
    protected void setUserService() {
        this.userService = adminService;
    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleAcceuilRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Get the stage from the acceuil button
            Stage stage = (Stage) acceuil.getScene().getWindow();

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Admin) {
            super.setUser(user);
            setUserService(); // Set the service when user is set
        } else {
            throw new IllegalArgumentException("User must be an Admin");
        }
    }
    public void setAdmin(Admin admin) {
        this.currentUser = admin;
        loadUserData();
        setUserService();
    }

    @Override
    protected void saveUser() {
        if (currentUser instanceof Admin) {
            Admin updatedAdmin = (Admin) currentUser;
            adminService.update(updatedAdmin);

            // Update the user in session
            SessionManager.getInstance().setCurrentUser(updatedAdmin);
        }
    }

    public void handleLogout() {
        try {
            // Clear the session
            SessionManager.getInstance().clearSession();

            // Navigate to login page
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) acceuil.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (IOException e) {
            showErrorDialog("Erreur", "Échec de la déconnexion: " + e.getMessage());
        }
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