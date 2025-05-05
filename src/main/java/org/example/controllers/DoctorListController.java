package org.example.controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.example.models.Medecin;
import org.example.services.MedecinService;
import org.example.util.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import javafx.scene.control.Label;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorListController {

    @FXML private TilePane doctorList;

    private MedecinService medecinService;

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isPatient()) {
            Stage stage = (Stage) doctorList.getScene().getWindow();
            showAlert(stage, "danger", "Accès non autorisé. Veuillez vous connecter en tant que patient.");
            navigateToLogin();
            return;
        }

        this.medecinService = new MedecinService();
        loadDoctors();
    }

    private void loadDoctors() {
        List<Medecin> doctors = medecinService.getAll();
        List<Medecin> verifiedDoctors = doctors.stream()
                .filter(Medecin::isIs_verified)
                .collect(Collectors.toList());
        doctorList.getChildren().clear();
        doctorList.setHgap(20);
        doctorList.setVgap(20);
        doctorList.setPrefColumns(3);
        doctorList.setAlignment(Pos.CENTER);

        for (Medecin doctor : verifiedDoctors) {
            VBox doctorCard = new VBox();
            doctorCard.setSpacing(10);
            doctorCard.getStyleClass().add("doctor-card");

            // Profile Image
            ImageView profileImageView = new ImageView();
            profileImageView.setFitWidth(80);
            profileImageView.setFitHeight(80);
            profileImageView.setPreserveRatio(true);
            profileImageView.getStyleClass().add("doctor-image");

            // Apply circular clip
            Circle clip = new Circle(40, 40, 40); // CenterX, CenterY, Radius
            profileImageView.setClip(clip);
            try {
                if (doctor.getImage() != null && !doctor.getImage().isEmpty()) {
                    // Convert file path to URL
                    File imageFile = new File(doctor.getImage());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        profileImageView.setImage(image);
                    } else {
                        // Fall back to placeholder if file doesn't exist
                        Image placeholder = loadImage("/images/avatar_paceholder.jpg");
                        profileImageView.setImage(placeholder);
                    }
                } else {
                    // Handle null or empty image path
                    Image placeholder = loadImage("/images/avatar_paceholder.jpg");
                    profileImageView.setImage(placeholder);
                }
            } catch (Exception e) {
                System.err.println("Error loading image for doctor " + doctor.getNom() + ": " + e.getMessage());
                // Try to load placeholder even after error
                try {
                    Image placeholder = loadImage("/images/avatar_paceholder.jpg");
                    profileImageView.setImage(placeholder);
                } catch (Exception ex) {
                    profileImageView.setImage(null); // Ultimate fallback to no image
                }
            }

            // Name with Icon (Using Bootstrap Icons)
            HBox nameBox = new HBox(8);
            Label nameIcon = createFontIcon("bi-person", "icon-name", "👨‍⚕️");
            Label nameLabel = new Label(doctor.getNom() + " " + doctor.getPrenom());
            nameLabel.getStyleClass().add("doctor-name");
            nameBox.getChildren().addAll(nameIcon, nameLabel);

            // Email with Icon
            HBox emailBox = new HBox(8);
            Label emailIcon = createFontIcon("bi-envelope", "icon-email", "✉️");
            Label emailLabel = new Label(doctor.getEmail());
            emailLabel.getStyleClass().add("doctor-info");
            emailBox.getChildren().addAll(emailIcon, emailLabel);

            // Specialty with Icon
            HBox specialtyBox = new HBox(8);
            Label specialtyIcon = createFontIcon("bi-heart", "icon-specialty", "🩺");
            Label specialtyLabel = new Label(doctor.getSpecialite());
            specialtyLabel.getStyleClass().add("doctor-info");
            specialtyBox.getChildren().addAll(specialtyIcon, specialtyLabel);

            // Report Button
            Button reportButton = new Button("Signaler");
            reportButton.getStyleClass().add("btn-report");
            Label reportIcon = createFontIcon("bi-exclamation-circle", "icon-report", "⚠️");
            reportButton.setGraphic(reportIcon);
            reportButton.setOnAction(event -> showReportDialog(doctor));

            doctorCard.getChildren().addAll(profileImageView, nameBox, emailBox, specialtyBox, reportButton);
            doctorList.getChildren().add(doctorCard);
        }

        if (doctors.isEmpty()) {
            Stage stage = (Stage) doctorList.getScene().getWindow();
            showAlert(stage, "warning", "Aucun médecin trouvé.");
        }
    }

    private Image loadImage(String path) {
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            if (image.isError()) {
                throw new Exception("Image failed to load: " + path);
            }
            System.out.println("Successfully loaded image: " + path);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load image at " + path + ": " + e.getMessage());
            System.err.println("Ensure the file exists at src/main/resources" + path);
            return null; // Fallback to no image
        }
    }

    private Label createFontIcon(String iconCode, String styleClass, String fallbackUnicode) {
        try {
            FontIcon icon = new FontIcon();

            // Use specific icons from BootstrapIcons enum
            switch (styleClass) {
                case "icon-name":
                    icon.setIconCode(BootstrapIcons.PERSON_BADGE);
                    icon.setIconColor(javafx.scene.paint.Color.valueOf("#005C99")); // Deep blue
                    break;
                case "icon-email":
                    icon.setIconCode(BootstrapIcons.ENVELOPE_FILL);
                    icon.setIconColor(javafx.scene.paint.Color.valueOf("#FF9800")); // Orange
                    break;
                case "icon-specialty":
                    icon.setIconCode(FontAwesomeSolid.STETHOSCOPE);
                    icon.setIconColor(javafx.scene.paint.Color.valueOf("#4CAF50")); // Green
                    break;
                case "icon-report":
                    icon.setIconCode(BootstrapIcons.EXCLAMATION_TRIANGLE_FILL);
                    icon.setIconColor(javafx.scene.paint.Color.valueOf("#FFFFFF")); // White
                    break;
                case "alert-icon":
                    if (iconCode.contains("check")) {
                        icon.setIconCode(BootstrapIcons.CHECK_CIRCLE_FILL);
                        icon.setIconColor(javafx.scene.paint.Color.valueOf("#66CCCC")); // Teal
                    } else if (iconCode.contains("exclamation")) {
                        icon.setIconCode(BootstrapIcons.EXCLAMATION_CIRCLE_FILL);
                        icon.setIconColor(javafx.scene.paint.Color.valueOf("#FFAB91")); // Orange
                    } else {
                        icon.setIconCode(BootstrapIcons.X_CIRCLE_FILL);
                        icon.setIconColor(javafx.scene.paint.Color.valueOf("#EF4444")); // Red
                    }
                    break;
                default:
                    // Fallback to a default icon if iconCode is not recognized
                    icon.setIconCode(BootstrapIcons.QUESTION_CIRCLE);
                    break;
            }

            icon.getStyleClass().add(styleClass);
            return new Label("", icon);
        } catch (Exception e) {
            System.err.println("Error loading icon: " + iconCode + " - " + e.getMessage());
            Label fallback = new Label(fallbackUnicode);
            fallback.getStyleClass().add(styleClass);
            return fallback;
        }
    }
    private void showReportDialog(Medecin doctor) {
        int patientId = SessionManager.getInstance().getCurrentUser().getId();
        if (medecinService.hasReported(doctor.getId(), patientId)) {
            Stage stage = (Stage) doctorList.getScene().getWindow();
            showAlert(stage, "warning", "Vous avez déjà signalé ce médecin.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("ChronoSerena - Signaler un Médecin");
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner((Stage) doctorList.getScene().getWindow());

        // Dialog Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("dialog-header");
        Label headerIcon = createFontIcon("bi-clipboard", "icon-report", "📋");
        headerIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF;");
        Label headerLabel = new Label("Signaler " + doctor.getNom() + " " + doctor.getPrenom());
        headerLabel.getStyleClass().add("dialog-header-label");
        headerBox.getChildren().addAll(headerIcon, headerLabel);

        // Dialog Content
        VBox contentBox = new VBox(10);
        contentBox.getStyleClass().add("dialog-content");

        Label instructionLabel = new Label("Veuillez expliquer pourquoi vous signalez ce médecin :");
        instructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2D3748;");

        TextArea reasonField = new TextArea();
        reasonField.setPromptText("Raison du signalement (max 500 caractères)...");
        reasonField.setPrefRowCount(4);
        reasonField.setPrefColumnCount(30);
        reasonField.getStyleClass().add("text-area");
        reasonField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.length() > 500) {
                reasonField.setText(newValue.substring(0, 500));
            }
        });

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        // Buttons
        ButtonType reportButtonType = new ButtonType("Signaler", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(reportButtonType, cancelButtonType);

        Button reportButton = (Button) dialog.getDialogPane().lookupButton(reportButtonType);
        reportButton.getStyleClass().add("btn-report");
        reportButton.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("btn-close-alert");
        cancelButton.setStyle("-fx-font-size: 13px; -fx-font-weight: normal;");

        contentBox.getChildren().addAll(headerBox, instructionLabel, reasonField, errorLabel);
        dialog.getDialogPane().setContent(contentBox);

        // Disable report button if reason is empty
        reasonField.textProperty().addListener((obs, old, newValue) -> {
            reportButton.setDisable(newValue.trim().isEmpty());
        });
        reportButton.setDisable(true);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reportButtonType) {
                return reasonField.getText();
            }
            return null;
        });

        // Apply CSS
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/doctor_list.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");

        dialog.showAndWait().ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                errorLabel.setText("Veuillez fournir une raison pour le signalement.");
                errorLabel.setVisible(true);
            } else {
                submitReport(doctor, reason);
            }
        });
    }

    private void submitReport(Medecin doctor, String reason) {
        int patientId = SessionManager.getInstance().getCurrentUser().getId();
        boolean success = medecinService.submitReport(doctor.getId(), patientId, reason);
        Stage stage = (Stage) doctorList.getScene().getWindow();
        if (success) {
            showAlert(stage, "success", "Signalement soumis avec succès. Un administrateur examinera votre demande.");
        } else {
            showAlert(stage, "danger", "Erreur lors de la soumission du signalement. Veuillez réessayer.");
        }
    }

    @FXML
    private void backToProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient_profile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) doctorList.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) doctorList.getScene().getWindow();
            showAlert(stage, "danger", "Erreur lors du retour au profil.");
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) doctorList.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) doctorList.getScene().getWindow();
            showAlert(stage, "danger", "Erreur lors de la redirection vers la page de connexion.");
        }
    }

    private void showAlert(Stage parentStage, String type, String message) {
        // Create a new Stage for the alert
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
        alertStage.initOwner(parentStage); // Set the parent stage
        alertStage.initStyle(StageStyle.TRANSPARENT); // Transparent for custom styling
        alertStage.setTitle("ChronoSerena - Notification");

        // Dialog content
        VBox alertBox = new VBox(10);
        alertBox.setAlignment(Pos.CENTER);
        alertBox.setPadding(new Insets(20));
        alertBox.getStyleClass().add("alert-dialog");
        alertBox.getStyleClass().add(type);
        alertBox.setMinWidth(400);
        alertBox.setMaxWidth(500);

        // Header with icon and message
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        Label alertIcon = createFontIcon(
                type.equals("success") ? "bi-check-circle" : type.equals("warning") ? "bi-exclamation-circle" : "bi-x-circle",
                "alert-icon",
                type.equals("success") ? "✅" : type.equals("warning") ? "⚠️" : "❌"
        );
        Label alertMessage = new Label(message);
        alertMessage.getStyleClass().add("alert-message");
        alertMessage.setWrapText(true);
        headerBox.getChildren().addAll(alertIcon, alertMessage);

        // Close button
        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("btn-close-alert");
        closeButton.setOnAction(e -> {
            System.out.println("Close button clicked for alert: " + message);
            alertStage.close(); // Close the stage
            System.out.println("Alert stage closed");
        });

        alertBox.getChildren().addAll(headerBox, closeButton);

        // Create the scene and apply CSS
        Scene scene = new Scene(alertBox);
        scene.getStylesheets().add(getClass().getResource("/css/doctor_list.css").toExternalForm());
        scene.setFill(null); // Transparent background
        alertStage.setScene(scene);

        // Center the stage on the screen (not relative to parent stage)
        alertStage.setOnShown(e -> {
            // Get the primary screen's bounds
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();

            // Calculate center position based on screen dimensions and alert stage size
            double centerX = screenBounds.getMinX() + (screenWidth - alertStage.getWidth()) / 2;
            double centerY = screenBounds.getMinY() + (screenHeight - alertStage.getHeight()) / 2;

            alertStage.setX(centerX);
            alertStage.setY(centerY);
            System.out.println("Alert centered at: (" + centerX + ", " + centerY + ")");
        });

        // Simple fade-in animation
        alertBox.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), alertBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Auto-dismiss for success and warning alerts (simplified)
        if (!type.equals("danger")) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // 5 seconds
                    javafx.application.Platform.runLater(() -> {
                        if (alertStage.isShowing()) {
                            System.out.println("Auto-dismissing alert: " + message);
                            alertStage.close();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Ensure the stage is cleaned up on close
        alertStage.setOnCloseRequest(e -> {
            System.out.println("Alert stage close request received");
            alertBox.getChildren().clear(); // Clear content to free resources
        });

        alertStage.showAndWait();
    }
}