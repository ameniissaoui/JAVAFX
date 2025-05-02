package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.models.UserDTO;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

public abstract class BaseRegistrationController {
    @FXML protected TextField nomField;
    @FXML protected TextField prenomField;
    @FXML protected TextField emailField;
    @FXML protected PasswordField passwordField;
    @FXML protected DatePicker dateNaissancePicker;
    @FXML protected TextField telephoneField;

    // Error labels
    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label dateNaissanceErrorLabel;
    @FXML private Label telephoneErrorLabel;

    @FXML
    protected void initialize() {
        System.out.println("CSS loaded: " + getClass().getResource("/css/registration.css"));
    }

    /**
     * Validates the base user fields and returns a UserDTO with the validated data.
     * @return a UserDTO object if all fields are valid.
     * @throws IllegalStateException if any required field is missing or invalid.
     */
    protected UserDTO collectBaseUserInfo() throws IllegalStateException {
        if (!validateBaseFields()) {
            throw new IllegalStateException("Validation failed: please check the highlighted fields.");
        }

        try {
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            LocalDate localDate = dateNaissancePicker.getValue();
            Date dateNaissance = Date.valueOf(localDate);

            String telephone = telephoneField.getText().trim();

            return new UserDTO(nom, prenom, email, password, dateNaissance, telephone);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error during data collection: " + e.getMessage());
        }
    }

    /**
     * Validates common input fields (nom, prenom, email, password, date, phone).
     * Highlights invalid fields in red.
     * @return true if all fields are valid.
     */
    protected boolean validateBaseFields() {
        boolean isValid = true;

        // Reset error messages and border colors
        nomErrorLabel.setVisible(false);
        prenomErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        dateNaissanceErrorLabel.setVisible(false);
        telephoneErrorLabel.setVisible(false);

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            nomErrorLabel.setVisible(true);
            isValid = false;
        } else {
            nomField.setStyle("");
        }

        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            prenomField.setStyle("-fx-border-color: red;");
            prenomErrorLabel.setVisible(true);
            isValid = false;
        } else {
            prenomField.setStyle("");
        }

        if (emailField.getText() == null || !emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailField.setStyle("-fx-border-color: red;");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else {
            emailField.setStyle("");
        }

        if (passwordField.getText() == null || passwordField.getText().length() < 8) {
            passwordField.setStyle("-fx-border-color: red;");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } else {
            passwordField.setStyle("");
        }

        if (dateNaissancePicker.getValue() == null) {
            dateNaissancePicker.setStyle("-fx-border-color: red;");
            dateNaissanceErrorLabel.setVisible(true);
            isValid = false;
        } else {
            dateNaissancePicker.setStyle("");
        }

        if (telephoneField.getText() == null || !telephoneField.getText().matches("^\\d{8}$")) {
            telephoneField.setStyle("-fx-border-color: red;");
            telephoneErrorLabel.setVisible(true);
            isValid = false;
        } else {
            telephoneField.setStyle("");
        }

        return isValid;
    }

    /**
     * Navigates back to the RoleSelection view.
     */
    @FXML
    protected void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RoleSelection.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) nomField.getScene().getWindow();
            if (stage != null) {
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("Failed to load RoleSelection.fxml: " + e.getMessage());
        }
    }

    /**
     * Subclasses must implement their own registration logic.
     */
    @FXML
    protected abstract void handleRegistration();
}