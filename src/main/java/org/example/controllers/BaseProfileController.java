package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.User;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public abstract class BaseProfileController implements Initializable {
    @FXML protected Label usernameField;  // Changed from TextField to Label// Must match the type in FXML (Label)
    @FXML protected TextField nomField;
    @FXML protected TextField prenomField;
    @FXML protected TextField emailField;
    @FXML protected PasswordField passwordField;
    @FXML protected PasswordField confirmPasswordField;
    @FXML protected DatePicker dateNaissancePicker;
    @FXML protected TextField telephoneField;
    @FXML protected Label messageLabel;
    @FXML public Button submitButton;
    @FXML private Label fullNameLabel;
    @FXML private Button historique;
    @FXML private Label nomDisplayLabel;
    @FXML private Label prenomDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Label telephoneDisplayLabel;
    @FXML private Label dateNaissanceDisplayLabel;
    protected User currentUser;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing BaseProfileController...");
        System.out.println("nomField: " + nomField);
        System.out.println("prenomField: " + prenomField);
        System.out.println("emailField: " + emailField);
        System.out.println("passwordField: " + passwordField);
        System.out.println("confirmPasswordField: " + confirmPasswordField);
        System.out.println("dateNaissancePicker: " + dateNaissancePicker);
        System.out.println("telephoneField: " + telephoneField);
        System.out.println("messageLabel: " + messageLabel);

        historique.setOnAction(event -> handleHistoRedirect());

    }

    public void handleHistoRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_historique.fxml"));
            Stage stage = (Stage) historique.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    protected void loadUserData() {
        if (currentUser != null) {
            String nom = currentUser.getNom();
            String prenom = currentUser.getPrenom();

            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());

            nomDisplayLabel.setText(nom);
            prenomDisplayLabel.setText(prenom);
            emailDisplayLabel.setText(currentUser.getEmail());
            telephoneDisplayLabel.setText(currentUser.getTelephone());
            // Don't set password for security reasons
            if (currentUser.getDateNaissance() != null) {
                LocalDate localDate;
                if (currentUser.getDateNaissance() instanceof java.sql.Date) {
                    // Direct conversion for SQL Date
                    localDate = ((java.sql.Date) currentUser.getDateNaissance()).toLocalDate();
                } else {
                    // For util.Date, use Instant path
                    localDate = currentUser.getDateNaissance().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                }
                dateNaissancePicker.setValue(localDate);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateNaissanceDisplayLabel.setText(localDate.format(formatter));

            }
            telephoneField.setText(currentUser.getTelephone());
            fullNameLabel.setText(prenom + " " + nom);

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

    @FXML
    private StackPane contentArea;

    @FXML
    private void handleEdit() {
        // Replace the current content in contentArea with editable fields
        VBox editContent = new VBox();
        // Add editable fields here
        contentArea.getChildren().clear();
        contentArea.getChildren().add(editContent);
    }
    @FXML
    protected void handleSave() {
        if (validateFields()) {
            updateUserData();
            saveUser();
            showMessage("Profil mis à jour avec succès", "success");
            updateDisplayLabels();

        }
    }

    protected boolean validateFields() {
        boolean isValid = true;

        // Validate fields similarly to registration validation
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            nomField.setStyle("");
        }

        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            prenomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            prenomField.setStyle("");
        }

        if (emailField.getText() == null || !emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            emailField.setStyle("");
        }

        // Only validate password if it's being changed
        if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
            if (passwordField.getText().length() < 8) {
                passwordField.setStyle("-fx-border-color: red;");
                isValid = false;
            } else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                passwordField.setStyle("-fx-border-color: red;");
                confirmPasswordField.setStyle("-fx-border-color: red;");
                isValid = false;
            } else {
                passwordField.setStyle("");
                confirmPasswordField.setStyle("");
            }
        }

        if (dateNaissancePicker.getValue() == null) {
            dateNaissancePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            dateNaissancePicker.setStyle("");
        }

        if (telephoneField.getText() == null || !telephoneField.getText().matches("^\\d{8}$")) {
            telephoneField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            telephoneField.setStyle("");
        }

        if (!isValid) {
            showMessage("Veuillez corriger les champs en rouge", "warning");
        }

        return isValid;
    }

    protected void updateUserData() {
        if (currentUser != null) {
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());

            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                currentUser.setMotDePasse(passwordField.getText());
            }

            LocalDate localDate = dateNaissancePicker.getValue();
            if (localDate != null) {
                // Let's examine the User class method parameter type
                try {
                    // If it expects java.util.Date
                    java.util.Date utilDate = java.util.Date.from(
                            localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    currentUser.setDateNaissance(utilDate);
                } catch (Exception e) {
                    // If the above fails, try with java.sql.Date
                    try {
                        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
                        // This will work if setDateNaissance accepts java.sql.Date
                        currentUser.setDateNaissance(sqlDate);
                    } catch (Exception ex) {
                        showMessage("Erreur lors de la conversion de la date", "danger");
                    }
                }
            }
            currentUser.setTelephone(telephoneField.getText().trim());
        }
    }

    protected abstract void saveUser();

    @FXML
    void redirectProduit(ActionEvent event) {
        try {
            // Make sure to provide the correct path to your FXML file
            URL fxmlLocation = getClass().getResource("/fxml/front/showProduit.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found!");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Create a new scene and stage
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Commandes");

            // Set the new stage to maximized
            newStage.setMaximized(true);

            // Show the new stage
            newStage.show();

            // Close the current stage
            Stage currentStage = (Stage) submitButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening commandes view: " + e.getMessage(), "error");
        }

    }
    private void showAlert(String message, String type) {
        // You can implement this method to show alerts
        // This could use JavaFX Alert or a custom alert dialog
        System.out.println(type.toUpperCase() + ": " + message);

        // Example implementation with JavaFX Alert:
        // Alert alert = new Alert(type.equals("error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        // alert.setTitle(type.equals("error") ? "Erreur" : "Succès");
        // alert.setHeaderText(null);
        // alert.setContentText(message);
        // alert.showAndWait();
    }
    @FXML
    private void handleLogout() {
        // Clear saved preferences
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

        // Use string literals since we don't have direct access to the constants in LoginController
        prefs.remove("savedEmail");
        prefs.remove("savedPassword");
        prefs.putBoolean("rememberMe", false);

        // Navigate back to login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

            // Close current window - using nomField which exists in all profile controllers
            Stage currentStage = (Stage) nomField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during logout: " + e.getMessage());
        }
    }
    protected void showMessage(String message, String type) {
        messageLabel.setText(message);

        switch (type) {
            case "danger":
                messageLabel.setStyle("-fx-text-fill: #d63031;");
                break;
            case "warning":
                messageLabel.setStyle("-fx-text-fill: #856404;");
                break;
            case "success":
                messageLabel.setStyle("-fx-text-fill: #155724;");
                break;
        }

        messageLabel.setVisible(true);
    }
}