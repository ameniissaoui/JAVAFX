package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.models.Admin;
import org.example.services.AdminService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminRegistrationController extends BaseRegistrationController {

    @FXML private Label messageLabel;
    private AdminService adminService = new AdminService();

    @FXML
    @Override
    protected void handleRegistration() {
        try {
            // Validate fields
            if (!validateBaseFields()) {
                messageLabel.setText("Veuillez corriger les erreurs dans le formulaire.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Check if email exists
            if (adminService.findByEmail(emailField.getText().trim()) != null) {
                messageLabel.setText("Cet email est déjà utilisé.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Create Admin object
            Admin admin = createAdminFromFields();

            // Save to database
            adminService.add(admin);

            // Show success message
            messageLabel.setText("Admin registered successfully!");
            messageLabel.setStyle("-fx-text-fill: green;");
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(event -> {
                try {
                    Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
                    Stage stage = (Stage) messageLabel.getScene().getWindow();
                    stage.setScene(new Scene(loginRoot));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            pause.play();
        } catch (IllegalStateException e) {
            messageLabel.setText("Registration failed: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private Admin createAdminFromFields() throws IllegalStateException {
        try {
            var baseUser = collectBaseUserInfo();
            Admin admin = new Admin(
                    baseUser.getNom(),
                    baseUser.getPrenom(),
                    baseUser.getEmail(),
                    baseUser.getMotDePasse(),
                    baseUser.getDateNaissance(),
                    baseUser.getTelephone()
            );
            admin.setImage(null); // Ensure image is null
            admin.setRole("admin"); // Ensure role is set
            return admin;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la création de l'admin : " + e.getMessage());
        }
    }
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminDashboard.fxml", event);
    }
}