package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.ChronoSernaApp;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.AdminService;
import org.example.services.FaceRecognitionService;
import org.example.services.MedecinService;
import org.example.services.PatientService;
import org.example.util.SessionManager;
import org.example.util.WebcamCapture;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class FaceLoginController implements Initializable {

    @FXML private ImageView cameraView;
    @FXML private Button captureButton;
    @FXML private Button startCameraButton;
    @FXML private Label statusLabel;
    @FXML private VBox alertBox;
    @FXML private Label messageLabel;
    @FXML private Label alertIcon;

    private WebcamCapture webcamCapture;
    private FaceRecognitionService faceService;
    private boolean cameraActive = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webcamCapture = WebcamCapture.getInstance();
        faceService = FaceRecognitionService.getInstance();

        alertBox.setManaged(false);
        alertBox.setVisible(false);

        // Bind camera image to the ImageView
        cameraView.imageProperty().bind(webcamCapture.imageProperty());

        // Disable capture button until camera is started
        captureButton.setDisable(true);
    }

    @FXML
    private void startCamera() {
        if (!cameraActive) {
            boolean success = webcamCapture.startCamera(0); // Use camera device 0 (default)

            if (success) {
                cameraActive = true;
                startCameraButton.setText("Stop Camera");
                captureButton.setDisable(false);
                statusLabel.setText("Camera started. Click 'Capture' to authenticate.");
            } else {
                showAlert("danger", "Failed to start camera. Make sure your webcam is connected.");
            }
        } else {
            webcamCapture.stopCamera();
            cameraActive = false;
            startCameraButton.setText("Start Camera");
            captureButton.setDisable(true);
            statusLabel.setText("Camera stopped.");
        }
    }

    @FXML
    private void captureAndAuthenticate() {
        if (!cameraActive) {
            showAlert("warning", "Please start the camera first.");
            return;
        }

        statusLabel.setText("Capturing image and authenticating...");
        captureButton.setDisable(true);

        // Capture the current frame
        Image capturedImage = webcamCapture.captureImage();

        if (capturedImage == null) {
            showAlert("danger", "Failed to capture image.");
            captureButton.setDisable(false);
            return;
        }

        // Authenticate using face recognition
        CompletableFuture<JSONObject> future = faceService.recognizeFace(capturedImage);

        future.thenAccept(result -> {
            Platform.runLater(() -> {
                boolean success = result.optBoolean("success", false);

                if (success) {
                    String userId = result.optString("user_id", "");

                    if (!userId.isEmpty()) {
                        try {
                            int id = Integer.parseInt(userId);
                            authenticateUser(id);
                        } catch (NumberFormatException e) {
                            showAlert("danger", "Invalid user ID format.");
                            captureButton.setDisable(false);
                        }
                    } else {
                        showAlert("danger", "User ID not found in recognition result.");
                        captureButton.setDisable(false);
                    }
                } else {
                    String message = result.optString("message", "Authentication failed.");
                    showAlert("warning", message);
                    captureButton.setDisable(false);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showAlert("danger", "Error during face recognition: " + ex.getMessage());
                captureButton.setDisable(false);
            });
            return null;
        });
    }

    private void authenticateUser(int userId) {
        // Try to find the user in each service
        AdminService adminService = new AdminService();
        Admin admin = adminService.getOne(userId); // Changed from findById to getOne

        if (admin != null) {
            if (admin.isBanned()) {
                showAlert("danger", "Your account has been suspended. Please contact the administrator.");
                captureButton.setDisable(false);
                return;
            }
            SessionManager.getInstance().setCurrentUser(admin, "admin");
            navigateToProfile("admin", admin);
            return;
        }

        MedecinService medecinService = new MedecinService();
        Medecin medecin = medecinService.getOne(userId); // Changed from findById to getOne

        if (medecin != null) {
            if (medecin.isBanned()) {
                showAlert("danger", "Your account has been suspended. Please contact the administrator.");
                captureButton.setDisable(false);
                return;
            }
            if (!medecin.isIs_verified()) {
                showAlert("warning", "Your account is pending verification. Please wait for your diploma to be verified by an administrator.");
                captureButton.setDisable(false);
                return;
            }
            SessionManager.getInstance().setCurrentUser(medecin, "medecin");
            navigateToProfile("medecin", medecin);
            return;
        }

        PatientService patientService = new PatientService();
        Patient patient = patientService.getOne(userId); // Changed from findById to getOne

        if (patient != null) {
            if (patient.isBanned()) {
                showAlert("danger", "Your account has been suspended. Please contact the administrator.");
                captureButton.setDisable(false);
                return;
            }
            SessionManager.getInstance().setCurrentUser(patient, "patient");
            navigateToProfile("patient", patient);
            return;
        }

        showAlert("danger", "User not found in the system.");
        captureButton.setDisable(false);
    }

    private void navigateToProfile(String userType, User user) {
        try {
            // Stop the camera before navigating
            if (cameraActive) {
                webcamCapture.stopCamera();
                cameraActive = false;
            }

            // Navigate to the appropriate profile page
            String fxmlPath;
            switch (userType) {
                case "admin":
                    fxmlPath = "/fxml/AdminDashboard.fxml";
                    break;
                case "medecin":
                    fxmlPath = "/fxml/medecin_profile.fxml";
                    break;
                case "patient":
                    fxmlPath = "/fxml/patient_profile.fxml";
                    break;
                default:
                    showAlert("danger", "Type d'utilisateur non reconnu");
                    return;
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) cameraView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("danger", "Error during navigation: " + e.getMessage());
            captureButton.setDisable(false);
        }
    }

    @FXML
    private void closeAlert() {
        alertBox.setManaged(false);
        alertBox.setVisible(false);
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
    private void backToLogin(ActionEvent event) {
        try {
            // Stop the camera before navigating back
            if (cameraActive) {
                webcamCapture.stopCamera();
                cameraActive = false;
            }

            // Navigate back to the login screen
            ChronoSernaApp app = new ChronoSernaApp();
            app.loadNewScene("/fxml/Login.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("danger", "Error navigating back: " + e.getMessage());
        }
    }
}