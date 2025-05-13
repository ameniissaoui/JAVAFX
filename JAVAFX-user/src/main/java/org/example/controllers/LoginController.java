package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.ChronoSernaApp;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.*;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
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
    @FXML private Button googleSignInButton;

    private Preferences prefs;
    private final String PREF_EMAIL = "savedEmail";
    private final String PREF_PASSWORD = "savedPassword";
    private final String PREF_REMEMBER = "rememberMe";
    private final String SECRET_KEY = "ChronoSerena2025";

    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private final UserService<? extends User> userService = new PatientService(); // Fix applied here
    @FXML private Button faceLoginButton; // New button for face login

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setMaximized(true);
        });
        alertBox.setManaged(false);
        alertBox.setVisible(false);
        prefs = Preferences.userNodeForPackage(LoginController.class);
        boolean remembered = prefs.getBoolean(PREF_REMEMBER, false);
        if (remembered) {
            String savedEmail = prefs.get(PREF_EMAIL, "");
            String savedPassword = decrypt(prefs.get(PREF_PASSWORD, ""));
            emailField.setText(savedEmail);
            passwordField.setText(savedPassword);
            rememberMeCheckbox.setSelected(true);
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

        AdminService adminService = new AdminService();
        Admin admin = adminService.findByEmail(email);
        if (admin != null && adminService.verifyPassword(admin.getId(), password)) {
            if (admin.isBanned()) {
                showAlert("danger", "Votre compte a été suspendu. Veuillez contacter l'administrateur.");
                return;
            }
            SessionManager.getInstance().setCurrentUser(admin, "Admin");
            navigateToProfile("Admin", admin);
            return;
        }

        MedecinService medecinService = new MedecinService();
        Medecin medecin = medecinService.findByEmail(email);
        if (medecin != null && medecinService.verifyPassword(medecin.getId(), password)) {
            if (medecin.isBanned()) {
                showAlert("danger", "Votre compte a été suspendu. Veuillez contacter l'administrateur.");
                return;
            }
            if (!medecin.isIs_verified()) {
                showAlert("warning", "Votre compte est en attente de vérification. Veuillez attendre que votre diplôme soit vérifié par un administrateur.");
                return;
            }
            SessionManager.getInstance().setCurrentUser(medecin, "Médecin");
            navigateToProfile("Médecin", medecin);
            return;
        }

        PatientService patientService = new PatientService();
        Patient patient = patientService.findByEmail(email);
        if (patient != null && patientService.verifyPassword(patient.getId(), password)) {
            if (patient.isBanned()) {
                showAlert("danger", "Votre compte a été suspendu. Veuillez contacter l'administrateur.");
                return;
            }
            SessionManager.getInstance().setCurrentUser(patient, "Patient");
            navigateToProfile("Patient", patient);
            return;
        }

        showAlert("danger", "Email ou mot de passe invalide");
    }

    @FXML
    private void handleGoogleSignIn() {
        try {
            // Step 1: Initiate Google login and get authorization code
            String authCode = googleAuthService.initiateLogin();
            if (authCode == null) {
                showAlert("danger", "Échec de l'authentification avec Google.");
                return;
            }

            // Step 2: Exchange code for access token
            String accessToken = googleAuthService.exchangeCodeForTokens(authCode);
            if (accessToken == null) {
                showAlert("danger", "Échec de la récupération des jetons Google.");
                return;
            }

            // Step 3: Get user email
            String email = googleAuthService.getUserEmail(accessToken);
            if (email == null) {
                showAlert("danger", "Échec de la récupération de l'email Google.");
                return;
            }

            // Step 4: Store or retrieve user in database
            User user = userService.getOrCreateUser(email, "Patient");
            if (user == null) {
                showAlert("danger", "Échec de la création ou récupération de l'utilisateur.");
                return;
            }

            // Step 5: Check if user is banned
            if (user.isBanned()) {
                showAlert("danger", "Votre compte a été suspendu. Veuillez contacter l'administrateur.");
                return;
            }

            // Step 6: Get the role from the user object
            String userRole = user.getRole();
            System.out.println("User role from database: " + userRole);

            // Step 7: Determine the correct user type based on the role, not the object type
            String userType;
            User correctUserObject = user; // Default to the original user

            if ("Admin".equalsIgnoreCase(userRole)) {
                // If role is Admin, get the Admin object
                AdminService adminService = new AdminService();
                Admin admin = adminService.findByEmail(email);
                if (admin != null) {
                    correctUserObject = admin; // Admin extends User
                    userType = "Admin";
                } else {
                    // Fallback to the original user object if admin not found
                    userType = "Patient";
                    System.out.println("Warning: User has Admin role but Admin object not found. Defaulting to Patient.");
                }
            } else if ("Médecin".equalsIgnoreCase(userRole)) {
                // If role is Médecin, get the Medecin object
                MedecinService medecinService = new MedecinService();
                Medecin medecin = medecinService.findByEmail(email);
                if (medecin != null) {
                    // Check if the doctor is verified
                    if (!medecin.isIs_verified()) {
                        showAlert("warning", "Votre compte est en attente de vérification. Veuillez attendre que votre diplôme soit vérifié par un administrateur.");
                        return;
                    }
                    correctUserObject = medecin; // Medecin extends User
                    userType = "Médecin";
                } else {
                    // Fallback to the original user object if medecin not found
                    userType = "Patient";
                    System.out.println("Warning: User has Médecin role but Médecin object not found. Defaulting to Patient.");
                }
            } else {
                // Default to Patient
                PatientService patientService = new PatientService();
                Patient patient = patientService.findByEmail(email);
                if (patient != null) {
                    correctUserObject = patient; // Patient extends User
                }
                userType = "Patient";
            }

            System.out.println("Final user type: " + userType);
            System.out.println("Final user object: " + correctUserObject);

            // Step 8: Set the session with the correct user object and type
            SessionManager.getInstance().setCurrentUser(correctUserObject, userType);

            // Step 9: Navigate to the correct profile
            navigateToProfile(userType, correctUserObject);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("danger", "Erreur lors de la connexion avec Google: " + e.getMessage());
        }
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

    public void navigateToProfile(String userType, Object user) {
        try {
            // Save preferences if "Remember Me" is checked
            if (rememberMeCheckbox.isSelected()) {
                prefs.put(PREF_EMAIL, emailField.getText());
                prefs.put(PREF_PASSWORD, encrypt(passwordField.getText()));
                prefs.putBoolean(PREF_REMEMBER, true);
            } else {
                prefs.remove(PREF_EMAIL);
                prefs.remove(PREF_PASSWORD);
                prefs.putBoolean(PREF_REMEMBER, false);
            }

            // Determine the FXML path based on user type
            String fxmlPath;
            switch (userType) {
                case "Admin":
                    fxmlPath = "/fxml/AdminDashboard.fxml";
                    break;
                case "Médecin":
                    fxmlPath = "/fxml/main_view_medecin.fxml";
                    break;
                case "Patient":
                    fxmlPath = "/fxml/main_view_patient.fxml";
                    break;
                default:
                    showAlert("danger", "Type d'utilisateur non reconnu");
                    return;
            }
            // Load the appropriate FXML
            ReminderNotificationChecker.getInstance();

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Create a new scene with screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Preserve stylesheets if needed
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            // Set the new scene
            stage.setScene(scene);

            // Make sure it's maximized for full screen display
            stage.setMaximized(true);

            // Show the stage
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("danger", "Erreur lors de la navigation vers le profil");
        }
    }
    @FXML
    public void openRoleSelection(ActionEvent event) {
        ChronoSernaApp app = new ChronoSernaApp();
        SceneManager.loadScene("/fxml/RoleSelection.fxml", event);

    }

    @FXML
    private void resetPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ResetPassword.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("danger", "Erreur lors de l'ouverture de la page de réinitialisation");
        }
    }

    @FXML
    private void handleFaceLogin() {
        try {
            // Load the face login screen
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/FaceLogin.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("danger", "Erreur lors de l'ouverture de la page de reconnaissance faciale: " + e.getMessage());
        }
    }
}