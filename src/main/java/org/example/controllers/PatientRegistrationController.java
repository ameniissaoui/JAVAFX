package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.models.Patient;
import org.example.models.UserDTO;
import org.example.services.PatientService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PatientRegistrationController extends BaseRegistrationController {
    @FXML private Label messageLabel;
    private PatientService patientService = new PatientService();

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
            if (patientService.findByEmail(emailField.getText().trim()) != null) {
                messageLabel.setText("Cet email est déjà utilisé.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Create Patient object
            Patient patient = createPatientFromFields();

            // Save to database
            patientService.add(patient);

            // Show success message
            messageLabel.setText("Patient registered successfully!");
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
        } catch (Exception e) {
            messageLabel.setText("Unexpected error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private Patient createPatientFromFields() throws IllegalStateException {
        try {
            UserDTO userDTO = collectBaseUserInfo();
            Patient patient = new Patient(
                    userDTO.getNom(),
                    userDTO.getPrenom(),
                    userDTO.getEmail(),
                    userDTO.getMotDePasse(),
                    userDTO.getDateNaissance(),
                    userDTO.getTelephone()
            );
            patient.setImage(null);
            patient.setRole("patient");
            return patient;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la création du patient : " + e.getMessage());
        }
    }
}