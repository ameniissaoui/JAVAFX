package org.example.controllers;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.ChronoSernaApp;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.AdminService;
import org.example.services.MedecinService;
import org.example.services.PatientService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public class LoginController implements Initializable {

    @FXML private TextField nomField;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private VBox alertBox;
    @FXML private Label alertIcon;
    @FXML private CheckBox rememberMeCheckbox;
    private Preferences prefs;
    private final String PREF_EMAIL = "savedEmail";
    private final String PREF_PASSWORD = "savedPassword";
    private final String PREF_REMEMBER = "rememberMe";
    private final String SECRET_KEY = "ChronoSerena2025"; // Should use a stronger key generation in production

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Hide alert box initially
        Platform.runLater(() -> {
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setMaximized(true);
        });
        alertBox.setManaged(false);
        alertBox.setVisible(false);
        prefs = Preferences.userNodeForPackage(LoginController.class);

        // Check if "Remember Me" was selected before
        boolean remembered = prefs.getBoolean(PREF_REMEMBER, false);
        if (remembered) {
            // Restore saved credentials
            String savedEmail = prefs.get(PREF_EMAIL, "");
            String savedPassword = decrypt(prefs.get(PREF_PASSWORD, ""));

            // Set fields
            emailField.setText(savedEmail);
            passwordField.setText(savedPassword);
            rememberMeCheckbox.setSelected(true);

            // Auto login option - uncomment this if you want immediate login
            // handleLogin();
        }
    }
    private String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("danger", "Erreur de chiffrement");
            return "";
        }
    }

    private String decrypt(String encryptedData) {
        try {
            if (encryptedData.isEmpty()) return "";

            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            showAlert("warning", "Veuillez saisir votre email et mot de passe");
            return;
        }

        // Try to find user in admin table
        AdminService adminService = new AdminService();
        Admin admin = adminService.findByEmail(email);
        if (admin != null && admin.getMotDePasse().equals(password)) {
            navigateToProfile("admin", admin);
            return;
        }
        // Try to find user in medecin table
        MedecinService medecinService = new MedecinService();
        Medecin medecin = medecinService.findByEmail(email);
        if (medecin != null && medecin.getMotDePasse().equals(password)) {
            navigateToProfile("medecin", medecin);
            return;
        }
        // Try to find user in patient table
        PatientService patientService = new PatientService();
        Patient patient = patientService.findByEmail(email);
        if (patient != null && patient.getMotDePasse().equals(password)) {
            navigateToProfile("patient", patient);
            return;
        }

        // No valid user found
        showAlert("danger", "Email ou mot de passe invalide");
        ChronoSernaApp app = new ChronoSernaApp();
        app.loadNewScene("/fxml/Dashboard.fxml", event);
    }

    private void showAlert(String type, String message) {
        alertBox.setManaged(true);
        alertBox.setVisible(true);
        messageLabel.setText(message);

        switch (type) {
            case "danger":
                alertBox.setStyle("-fx-background-color: #fee8e7; -fx-border-color: #fdd6d3;");
                messageLabel.setStyle("-fx-text-fill: #d63031;");
                alertIcon.setText("❌");
                break;
            case "warning":
                alertBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeeba;");
                messageLabel.setStyle("-fx-text-fill: #856404;");
                alertIcon.setText("⚠️");
                break;
            case "success":
                alertBox.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb;");
                messageLabel.setStyle("-fx-text-fill: #155724;");
                alertIcon.setText("✅");
                break;
        }
    }

    @FXML
    private void closeAlert() {
        alertBox.setManaged(false);
        alertBox.setVisible(false);
    }

    private Admin findAdminByEmail(String email) {
        AdminService adminService = new AdminService();
        for (Admin admin : adminService.getAll()) {
            if (admin.getEmail().equals(email)) {
                return admin;
            }
        }
        return null;
    }

    private Medecin findMedecinByEmail(String email) {
        MedecinService medecinService = new MedecinService();
        for (Medecin medecin : medecinService.getAll()) {
            if (medecin.getEmail().equals(email)) {
                return medecin;
            }
        }
        return null;
    }

    private Patient findPatientByEmail(String email) {
        PatientService patientService = new PatientService();
        for (Patient patient : patientService.getAll()) {
            if (patient.getEmail().equals(email)) {
                return patient;
            }
        }
        return null;
    }

    private void navigateToProfile(String userType, Object user) {
        try {
            if (rememberMeCheckbox.isSelected()) {
                // Save credentials securely
                prefs.put(PREF_EMAIL, emailField.getText());
                prefs.put(PREF_PASSWORD, encrypt(passwordField.getText()));
                prefs.putBoolean(PREF_REMEMBER, true);
            } else {
                // Clear saved credentials
                prefs.remove(PREF_EMAIL);
                prefs.remove(PREF_PASSWORD);
                prefs.putBoolean(PREF_REMEMBER, false);
            }
            String profilePath;
            FXMLLoader loader;

            // Handle admin dashboard separately
            if (userType.equals("admin") && user instanceof Admin) {
                profilePath = "/fxml/AdminDashboard.fxml";
                loader = new FXMLLoader(getClass().getResource(profilePath));
                Parent root = loader.load();

                // Set admin in dashboard controller
                AdminDashboardController dashboardController = loader.getController();
                dashboardController.setAdmin((Admin) user);

                Scene scene = new Scene(root);
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
            // Handle other profile types using the original approach
            else {
                profilePath = "/fxml/" + userType + "_profile.fxml";
                loader = new FXMLLoader(getClass().getResource(profilePath));
                Parent root = loader.load();

                // Get the controller and pass the user object
                BaseProfileController controller = loader.getController();

                // Set the appropriate user object based on type
                if (userType.equals("medecin") && user instanceof Medecin) {
                    ((MedecinProfileController) controller).setMedecin((Medecin) user);
                } else if (userType.equals("patient") && user instanceof Patient) {
                    ((PatientProfileController) controller).setPatient((Patient) user);
                }

                Scene scene = new Scene(root);
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("danger", "Erreur lors de la navigation vers le profil");
        }
    }
    @FXML
    private void openRoleSelection(ActionEvent event) {
        ChronoSernaApp app = new ChronoSernaApp();
        app.loadNewScene("/fxml/RoleSelection.fxml", event);
    }

    @FXML
    private void resetPassword() {
        // Implement password reset functionality
        showAlert("warning", "Fonctionnalité de réinitialisation de mot de passe à implémenter");
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
}