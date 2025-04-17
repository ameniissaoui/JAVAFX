package org.example.controllers;

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
            // Get base user information
            var baseUser = collectBaseUserInfo();

            // Create Patient object
            Admin admin = new Admin(
                    baseUser.getId(),
                    baseUser.getNom(),
                    baseUser.getPrenom(),
                    baseUser.getEmail(),
                    baseUser.getMotDePasse(),
                    baseUser.getDateNaissance(),
                    baseUser.getTelephone()
            );

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
            // Show error message
            messageLabel.setText("Registration failed: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}