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
import org.example.models.User;
import org.example.services.FaceRecognitionService;
import org.example.util.SessionManager;
import org.example.util.WebcamCapture;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class FaceRegistrationController implements Initializable {

    @FXML private ImageView cameraView;
    @FXML private Button captureButton;
    @FXML private Button startCameraButton;
    @FXML private Label statusLabel;
    @FXML private VBox alertBox;
    @FXML private Label messageLabel;
    @FXML private Label alertIcon;
    @FXML private Button backButton;

    private WebcamCapture webcamCapture;
    private FaceRecognitionService faceService;
    private boolean cameraActive = false;
    private User currentUser;
    private boolean opencvAvailable = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            webcamCapture = WebcamCapture.getInstance();
            faceService = FaceRecognitionService.getInstance();

            // Get current user from session
            currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser == null) {
                showAlert("danger", "No user logged in. Please log in first.");
                captureButton.setDisable(true);
                startCameraButton.setDisable(true);
                return;
            }

            alertBox.setManaged(false);
            alertBox.setVisible(false);

            // Try to bind camera image to the ImageView
            try {
                cameraView.imageProperty().bind(webcamCapture.imageProperty());
            } catch (Exception e) {
                opencvAvailable = false;
                showAlert("warning", "OpenCV not available. Face registration is disabled.");
                captureButton.setDisable(true);
                startCameraButton.setDisable(true);
            }

            // Disable capture button until camera is started
            captureButton.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
            opencvAvailable = false;
            showAlert("danger", "Error initializing face registration: " + e.getMessage());
            captureButton.setDisable(true);
            startCameraButton.setDisable(true);
        }
    }

    @FXML
    private void startCamera() {
        if (!opencvAvailable) {
            showAlert("warning", "OpenCV not available. Face registration is disabled.");
            return;
        }

        if (!cameraActive) {
            boolean success = webcamCapture.startCamera(0); // Use camera device 0 (default)

            if (success) {
                cameraActive = true;
                startCameraButton.setText("Stop Camera");
                captureButton.setDisable(false);
                statusLabel.setText("Camera started. Click 'Capture' to register your face.");
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
    private void captureAndRegister() {
        if (!opencvAvailable) {
            showAlert("warning", "OpenCV not available. Face registration is disabled.");
            return;
        }

        if (!cameraActive) {
            showAlert("warning", "Please start the camera first.");
            return;
        }

        statusLabel.setText("Capturing image and registering face...");
        captureButton.setDisable(true);

        // Capture the current frame
        Image capturedImage = webcamCapture.captureImage();

        if (capturedImage == null) {
            showAlert("danger", "Failed to capture image.");
            captureButton.setDisable(false);
            return;
        }

        // Register face
        CompletableFuture<JSONObject> future = faceService.registerFace(currentUser.getId(), capturedImage);

        future.thenAccept(result -> {
            Platform.runLater(() -> {
                boolean success = result.optBoolean("success", false);

                if (success) {
                    showAlert("success", "Face registered successfully! You can now use face login.");
                    // Stop camera after successful registration
                    if (cameraActive) {
                        webcamCapture.stopCamera();
                        cameraActive = false;
                        startCameraButton.setText("Start Camera");
                    }
                } else {
                    String message = result.optString("message", "Registration failed.");
                    showAlert("warning", message);
                    captureButton.setDisable(false);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showAlert("danger", "Error during face registration: " + ex.getMessage());
                captureButton.setDisable(false);
            });
            return null;
        });
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            // Stop the camera before going back
            if (cameraActive) {
                webcamCapture.stopCamera();
                cameraActive = false;
            }

            // Go back to the appropriate profile page based on user role
            String userType = SessionManager.getInstance().getUserType();
            String fxmlPath;

            switch (userType.toLowerCase()) {
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
                    showAlert("danger", "Unknown user type");
                    return;
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) cameraView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("danger", "Error navigating back: " + e.getMessage());
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
}