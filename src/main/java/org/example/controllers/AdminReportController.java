package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.models.Report;
import org.example.services.ReportService;
import org.example.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.layout.TilePane;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class AdminReportController {

    @FXML private TilePane reportCardsContainer;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private Button filterButton;

    private ReportService reportService;

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isAdmin()) {
            Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
            showAlert(stage, "danger", "Accès non autorisé. Veuillez vous connecter en tant qu'administrateur.");
            navigateToLogin();
            return;
        }

        reportService = new ReportService();

        // Initialize UI components
        statusFilter.setItems(FXCollections.observableArrayList("PENDING", "UNDER_REVIEW", "RESOLVED"));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> handleFilterChange());
        startDate.valueProperty().addListener((obs, oldVal, newVal) -> handleFilterChange());
        endDate.valueProperty().addListener((obs, oldVal, newVal) -> handleFilterChange());
        filterButton.setText("Réinitialiser");
        filterButton.getStyleClass().remove("btn-primary");
        filterButton.getStyleClass().add("btn-secondary");
        fadeInContent();

        javafx.application.Platform.runLater(this::loadReports);
    }

    private void fadeInContent() {
        reportCardsContainer.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), reportCardsContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(300));
        fadeIn.play();
    }

    private void loadReports() {
        reportCardsContainer.getChildren().clear();
        try {
            var reports = reportService.getReports(null, null, null, null);
            System.out.println("Loading " + reports.size() + " reports into cards");

            // Group reports by doctor ID
            Map<Integer, List<Report>> reportsByDoctor = new HashMap<>();

            for (Report report : reports) {
                int doctorId = report.getDoctorId();
                if (!reportsByDoctor.containsKey(doctorId)) {
                    reportsByDoctor.put(doctorId, new ArrayList<>());
                }
                reportsByDoctor.get(doctorId).add(report);
            }

            int groupIndex = 0;

            // Create a section for each doctor
            for (Map.Entry<Integer, List<Report>> entry : reportsByDoctor.entrySet()) {
                int doctorId = entry.getKey();
                List<Report> doctorReports = entry.getValue();

                if (doctorReports.isEmpty()) continue;

                // Get doctor info from the first report
                Report firstReport = doctorReports.get(0);
                String doctorName = (firstReport.getDoctorNom() != null ? firstReport.getDoctorNom() : "N/A") + " " +
                        (firstReport.getDoctorPrenom() != null ? firstReport.getDoctorPrenom() : "");

                // Create doctor section header
                VBox doctorSection = new VBox(15);
                doctorSection.getStyleClass().add("doctor-section");
                doctorSection.setPadding(new Insets(15, 0, 5, 0));

                HBox doctorHeaderBox = new HBox(10);
                doctorHeaderBox.setAlignment(Pos.CENTER_LEFT);

                // Doctor image
                ImageView doctorImageView = createDoctorImageView(firstReport.getDoctorImage());
                doctorImageView.setFitWidth(50);
                doctorImageView.setFitHeight(50);

                // Doctor name with icon
                Label doctorIcon = createFontIcon("fas-user-md", "icon-doctor", "🩺");
                Label doctorLabel = new Label(doctorName);
                doctorLabel.getStyleClass().add("doctor-header");
                doctorLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                // Report count badge
                Label countBadge = new Label(String.valueOf(doctorReports.size()));
                countBadge.getStyleClass().add("report-count-badge");

                doctorHeaderBox.getChildren().addAll(doctorImageView, doctorIcon, doctorLabel, countBadge);

                // Create a container for this doctor's reports
                TilePane doctorReportsContainer = new TilePane();
                doctorReportsContainer.setHgap(15);
                doctorReportsContainer.setVgap(15);
                doctorReportsContainer.setPrefColumns(3); // Adjust based on your UI
                doctorReportsContainer.getStyleClass().add("doctor-reports-container");

                // Add all reports for this doctor
                int cardIndex = 0;
                for (Report report : doctorReports) {
                    VBox card = createReportCard(report);
                    card.setOpacity(0);
                    card.setScaleX(0.95);
                    card.setScaleY(0.95);

                    doctorReportsContainer.getChildren().add(card);

                    int finalCardIndex = cardIndex;
                    javafx.application.Platform.runLater(() -> {
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.setDelay(Duration.millis(50 * finalCardIndex));

                        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), card);
                        scaleIn.setFromX(0.95);
                        scaleIn.setFromY(0.95);
                        scaleIn.setToX(1);
                        scaleIn.setToY(1);
                        scaleIn.setDelay(Duration.millis(50 * finalCardIndex));

                        fadeIn.play();
                        scaleIn.play();
                    });

                    cardIndex++;
                }

                // Add a separator
                Separator separator = new Separator();
                separator.setStyle("-fx-background-color: #E5E7EB;");

                // Add everything to the doctor section
                doctorSection.getChildren().addAll(doctorHeaderBox, doctorReportsContainer, separator);
                doctorSection.setOpacity(0);

                // Add the doctor section to the main container
                reportCardsContainer.getChildren().add(doctorSection);

                int finalGroupIndex = groupIndex;
                javafx.application.Platform.runLater(() -> {
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(400), doctorSection);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.setDelay(Duration.millis(100 * finalGroupIndex));
                    fadeIn.play();
                });

                groupIndex++;
            }

            if (reports.isEmpty()) {
                Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
                Platform.runLater(() -> showAlert(stage, "warning", "Aucun signalement trouvé."));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
            Platform.runLater(() -> showAlert(stage, "danger", "Erreur lors du chargement des signalements: " + e.getMessage()));
        }
    }
    private VBox createReportCard(Report report) {
        VBox card = new VBox(10);
        card.getStyleClass().add("report-card");

        HBox statusIndicatorBox = new HBox();
        statusIndicatorBox.setAlignment(Pos.CENTER_RIGHT);
        Label statusIndicator = new Label(report.getStatus());
        statusIndicator.getStyleClass().addAll("status-indicator", getStatusStyleClass(report.getStatus()));
        statusIndicatorBox.getChildren().add(statusIndicator);

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label titleIcon = createFontIcon("bi-file-text", "icon-report", "📄");
        Tooltip.install(titleIcon, new Tooltip("Signalement"));
        Label titleLabel = new Label("Signalement #" + report.getId());
        titleLabel.getStyleClass().add("report-title");
        titleBox.getChildren().addAll(titleIcon, titleLabel);

        ImageView doctorImageView = createDoctorImageView(report.getDoctorImage());
        HBox imageContainer = new HBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().add(doctorImageView);
        imageContainer.setPadding(new Insets(5, 0, 5, 0));

        HBox doctorBox = new HBox(8);
        doctorBox.setAlignment(Pos.CENTER_LEFT);
        Label doctorIcon = createFontIcon("fas-user-md", "icon-doctor", "🩺");
        Tooltip.install(doctorIcon, new Tooltip("Médecin"));
        Label doctorLabel = new Label((report.getDoctorNom() != null ? report.getDoctorNom() : "N/A") + " " +
                (report.getDoctorPrenom() != null ? report.getDoctorPrenom() : ""));
        doctorLabel.getStyleClass().add("report-info");
        doctorBox.getChildren().addAll(doctorIcon, doctorLabel);

        HBox patientBox = new HBox(8);
        patientBox.setAlignment(Pos.CENTER_LEFT);
        Label patientIcon = createFontIcon("bi-person", "icon-patient", "👤");
        Tooltip.install(patientIcon, new Tooltip("Patient"));
        Label patientLabel = new Label((report.getPatientNom() != null ? report.getPatientNom() : "N/A") + " " +
                (report.getPatientPrenom() != null ? report.getPatientPrenom() : ""));
        patientLabel.getStyleClass().add("report-info");
        patientBox.getChildren().addAll(patientIcon, patientLabel);

        HBox reasonBox = new HBox(8);
        reasonBox.setAlignment(Pos.CENTER_LEFT);
        Label reasonIcon = createFontIcon("bi-exclamation-circle", "icon-reason", "⚠️");
        Tooltip.install(reasonIcon, new Tooltip("Raison"));
        Label reasonLabel = new Label(report.getReason());
        reasonLabel.setWrapText(true);
        reasonLabel.getStyleClass().add("report-info");
        reasonBox.getChildren().addAll(reasonIcon, reasonLabel);

        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = createFontIcon("bi-calendar", "icon-date", "📅");
        Tooltip.install(dateIcon, new Tooltip("Date"));
        Label dateLabel = new Label(report.getCreatedAt().toString());
        dateLabel.getStyleClass().add("report-info");
        dateBox.getChildren().addAll(dateIcon, dateLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button viewButton = new Button("Voir les détails");
        viewButton.getStyleClass().add("btn-view");
        Label viewIcon = createFontIcon("bi-eye", "icon-view", "👁️");
        Tooltip.install(viewIcon, new Tooltip("Voir les détails"));
        viewButton.setGraphic(viewIcon);
        viewButton.setMaxWidth(Double.MAX_VALUE);
        viewButton.setOnAction(event -> showReportDetails(report));

        card.getChildren().addAll(statusIndicatorBox, titleBox, doctorBox, patientBox, reasonBox, dateBox, spacer, viewButton);

        card.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), card);
            scaleUp.setToX(1.02);
            scaleUp.setToY(1.02);
            scaleUp.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), card);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();
        });

        return card;
    }

    private ImageView createDoctorImageView(String imagePath) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.getStyleClass().add("doctor-image");

        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), true);
                    imageView.setImage(image);
                    image.errorProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            loadPlaceholderImage(imageView);
                        }
                    });
                } else {
                    loadPlaceholderImage(imageView);
                }
            } else {
                loadPlaceholderImage(imageView);
            }
        } catch (Exception e) {
            System.err.println("Error loading doctor image: " + e.getMessage());
            loadPlaceholderImage(imageView);
        }

        imageView.setClip(new javafx.scene.shape.Circle(40, 40, 40));
        return imageView;
    }

    private void loadPlaceholderImage(ImageView imageView) {
        try {
            Image placeholder = new Image(getClass().getResourceAsStream("/images/avatar_paceholder.jpg"));
            imageView.setImage(placeholder);
        } catch (Exception e) {
            System.err.println("Failed to load placeholder image: " + e.getMessage());
        }
    }

    private String getStatusStyleClass(String status) {
        if (status == null) return "status-pending";
        switch (status) {
            case "PENDING":
                return "status-pending";
            case "UNDER_REVIEW":
                return "status-under-review";
            case "RESOLVED":
                return "status-resolved";
            default:
                return "status-pending";
        }
    }

    private Label createFontIcon(String iconCode, String styleClass, String fallbackUnicode) {
        try {
            FontIcon icon = new FontIcon(iconCode);
            icon.getStyleClass().add(styleClass);

            if (styleClass.contains("icon-report")) {
                icon.setIconColor(javafx.scene.paint.Color.valueOf("#3B82F6"));
            } else if (styleClass.contains("icon-doctor")) {
                icon.setIconColor(javafx.scene.paint.Color.valueOf("#10B981"));
            } else if (styleClass.contains("icon-patient")) {
                icon.setIconColor(javafx.scene.paint.Color.valueOf("#F59E0B"));
            } else if (styleClass.contains("icon-reason")) {
                icon.setIconColor(javafx.scene.paint.Color.valueOf("#EF4444"));
            } else if (styleClass.contains("icon-date")) {
                icon.setIconColor(javafx.scene.paint.Color.valueOf("#8B5CF6"));
            } else if (styleClass.contains("icon-view")) {
                icon.setIconColor(javafx.scene.paint.Color.valueOf("#FFFFFF"));
            } else if (styleClass.contains("alert-icon")) {
                if (styleClass.contains("success")) {
                    icon.setIconColor(javafx.scene.paint.Color.valueOf("#10B981"));
                } else if (styleClass.contains("warning")) {
                    icon.setIconColor(javafx.scene.paint.Color.valueOf("#F59E0B"));
                } else if (styleClass.contains("danger")) {
                    icon.setIconColor(javafx.scene.paint.Color.valueOf("#EF4444"));
                }
            }

            Label label = new Label("", icon);
            return label;
        } catch (Exception e) {
            System.err.println("Error loading icon: " + iconCode + " - " + e.getMessage());
            Label fallback = new Label(fallbackUnicode);
            fallback.getStyleClass().add(styleClass);
            return fallback;
        }
    }

    private void handleFilterChange() {
        javafx.application.Platform.runLater(() -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), filterButton);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            scaleDown.setOnFinished(e -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), filterButton);
                scaleUp.setToX(1);
                scaleUp.setToY(1);
                scaleUp.play();
                applyFilter();
            });
            scaleDown.play();
        });
    }

    private void applyFilter() {
        String status = statusFilter.getValue();
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), reportCardsContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0.5);
        fadeOut.setOnFinished(e -> {
            reportCardsContainer.getChildren().clear();
            var reports = reportService.getReports(status, null, start, end);
            System.out.println("Loading " + reports.size() + " filtered reports into cards");

            // Group reports by doctor ID
            Map<Integer, List<Report>> reportsByDoctor = new HashMap<>();

            for (Report report : reports) {
                int doctorId = report.getDoctorId();
                if (!reportsByDoctor.containsKey(doctorId)) {
                    reportsByDoctor.put(doctorId, new ArrayList<>());
                }
                reportsByDoctor.get(doctorId).add(report);
            }

            int groupIndex = 0;

            // Create a section for each doctor
            for (Map.Entry<Integer, List<Report>> entry : reportsByDoctor.entrySet()) {
                int doctorId = entry.getKey();
                List<Report> doctorReports = entry.getValue();

                if (doctorReports.isEmpty()) continue;

                // Get doctor info from the first report
                Report firstReport = doctorReports.get(0);
                String doctorName = (firstReport.getDoctorNom() != null ? firstReport.getDoctorNom() : "N/A") + " " +
                        (firstReport.getDoctorPrenom() != null ? firstReport.getDoctorPrenom() : "");

                // Create doctor section header
                VBox doctorSection = new VBox(15);
                doctorSection.getStyleClass().add("doctor-section");
                doctorSection.setPadding(new Insets(15, 0, 5, 0));

                HBox doctorHeaderBox = new HBox(10);
                doctorHeaderBox.setAlignment(Pos.CENTER_LEFT);

                // Doctor image
                ImageView doctorImageView = createDoctorImageView(firstReport.getDoctorImage());
                doctorImageView.setFitWidth(50);
                doctorImageView.setFitHeight(50);

                // Doctor name with icon
                Label doctorIcon = createFontIcon("fas-user-md", "icon-doctor", "🩺");
                Label doctorLabel = new Label(doctorName);
                doctorLabel.getStyleClass().add("doctor-header");
                doctorLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                // Report count badge
                Label countBadge = new Label(String.valueOf(doctorReports.size()));
                countBadge.getStyleClass().add("report-count-badge");

                doctorHeaderBox.getChildren().addAll(doctorImageView, doctorIcon, doctorLabel, countBadge);

                // Create a container for this doctor's reports
                TilePane doctorReportsContainer = new TilePane();
                doctorReportsContainer.setHgap(15);
                doctorReportsContainer.setVgap(15);
                doctorReportsContainer.setPrefColumns(3); // Adjust based on your UI
                doctorReportsContainer.getStyleClass().add("doctor-reports-container");

                // Add all reports for this doctor
                int cardIndex = 0;
                for (Report report : doctorReports) {
                    VBox card = createReportCard(report);
                    doctorReportsContainer.getChildren().add(card);

                    card.setOpacity(0);
                    card.setTranslateY(20);

                    int finalCardIndex = cardIndex;
                    javafx.application.Platform.runLater(() -> {
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), card);
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.setDelay(Duration.millis(30 * finalCardIndex));

                        TranslateTransition moveUp = new TranslateTransition(Duration.millis(200), card);
                        moveUp.setFromY(20);
                        moveUp.setToY(0);
                        moveUp.setDelay(Duration.millis(30 * finalCardIndex));

                        fadeIn.play();
                        moveUp.play();
                    });

                    cardIndex++;
                }

                // Add a separator
                Separator separator = new Separator();
                separator.setStyle("-fx-background-color: #E5E7EB;");

                // Add everything to the doctor section
                doctorSection.getChildren().addAll(doctorHeaderBox, doctorReportsContainer, separator);
                doctorSection.setOpacity(0);

                // Add the doctor section to the main container
                reportCardsContainer.getChildren().add(doctorSection);

                int finalGroupIndex = groupIndex;
                javafx.application.Platform.runLater(() -> {
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), doctorSection);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.setDelay(Duration.millis(50 * finalGroupIndex));
                    fadeIn.play();
                });

                groupIndex++;
            }

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), reportCardsContainer);
            fadeIn.setFromValue(0.5);
            fadeIn.setToValue(1);
            fadeIn.play();

            if (reports.isEmpty()) {
                Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
                showAlert(stage, "warning", "Aucun signalement trouvé après filtrage.");
            }
        });
        fadeOut.play();
    }
    @FXML
    private void handleFilter(ActionEvent event) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), filterButton);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);
        scaleDown.setOnFinished(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), filterButton);
            scaleUp.setToX(1);
            scaleUp.setToY(1);
            scaleUp.play();
        });
        scaleDown.play();

        applyFilter();
    }

    @FXML
    private void handleReset(ActionEvent event) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), filterButton);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);
        scaleDown.setOnFinished(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), filterButton);
            scaleUp.setToX(1);
            scaleUp.setToY(1);
            scaleUp.play();

            statusFilter.setValue(null);
            startDate.setValue(null);
            endDate.setValue(null);

            loadReports();
        });
        scaleDown.play();
    }

    @FXML
    private void backToProfile(ActionEvent event) {
        try {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), reportCardsContainer.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_profile.fxml"));
                    Parent root = loader.load();
                    root.setOpacity(0);

                    Scene scene = new Scene(root);
                    Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
                    stage.setScene(scene);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();

                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
                    showAlert(stage, "danger", "Erreur lors du retour au profil.");
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
            showAlert(stage, "danger", "Erreur lors du retour au profil.");
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) reportCardsContainer.getScene().getWindow();

            FadeTransition fade = new FadeTransition(Duration.millis(400), root);
            fade.setFromValue(0);
            fade.setToValue(1);

            stage.setScene(scene);
            fade.play();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) reportCardsContainer.getScene().getWindow();
            showAlert(stage, "danger", "Erreur lors de la redirection vers la page de connexion.");
        }
    }

    private void showReportDetails(Report report) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Détails du Signalement");
        dialogStage.initStyle(StageStyle.DECORATED); // Use DECORATED instead of TRANSPARENT
        dialogStage.initOwner((Stage) reportCardsContainer.getScene().getWindow());
        dialogStage.initModality(Modality.WINDOW_MODAL);

        VBox content = new VBox(15);
        content.getStyleClass().add("dialog-content");
        content.setPrefWidth(500);

        Label headerLabel = new Label("Signalement #" + report.getId());
        headerLabel.getStyleClass().add("dialog-header-label");

        HBox headerStatusBox = new HBox(10);
        headerStatusBox.setAlignment(Pos.CENTER_RIGHT);
        Label statusIndicator = new Label(report.getStatus());
        statusIndicator.getStyleClass().addAll("status-indicator", getStatusStyleClass(report.getStatus()));
        headerStatusBox.getChildren().add(statusIndicator);

        HBox headerContent = new HBox();
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.getChildren().add(headerLabel);

        HBox.setHgrow(headerContent, javafx.scene.layout.Priority.ALWAYS);

        HBox headerBox = new HBox(headerContent, headerStatusBox);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 10, 0, 0));

        VBox headerContainer = new VBox(headerBox);
        headerContainer.getStyleClass().add("dialog-header");

        VBox infoSection = new VBox(12);
        infoSection.setPadding(new Insets(10, 0, 20, 0));

        HBox doctorBox = new HBox(8);
        doctorBox.setAlignment(Pos.CENTER_LEFT);
        ImageView doctorImageView = createDoctorImageView(report.getDoctorImage());
        Label doctorIcon = createFontIcon("fas-user-md", "icon-doctor", "🩺");
        Tooltip.install(doctorIcon, new Tooltip("Médecin"));
        Label doctorLabel = new Label("Médecin: " + (report.getDoctorNom() != null ? report.getDoctorNom() : "N/A") + " " +
                (report.getDoctorPrenom() != null ? report.getDoctorPrenom() : ""));
        doctorLabel.getStyleClass().add("report-info");
        doctorBox.getChildren().addAll(doctorImageView, doctorIcon, doctorLabel);

        HBox patientBox = new HBox(8);
        patientBox.setAlignment(Pos.CENTER_LEFT);
        Label patientIcon = createFontIcon("bi-person", "icon-patient", "👤");
        Tooltip.install(patientIcon, new Tooltip("Patient"));
        Label patientLabel = new Label("Patient: " + (report.getPatientNom() != null ? report.getPatientNom() : "N/A") + " " +
                (report.getPatientPrenom() != null ? report.getPatientPrenom() : ""));
        patientLabel.getStyleClass().add("report-info");
        patientBox.getChildren().addAll(patientIcon, patientLabel);

        HBox reasonBox = new HBox(8);
        reasonBox.setAlignment(Pos.TOP_LEFT);
        Label reasonIcon = createFontIcon("bi-exclamation-circle", "icon-reason", "⚠️");
        Tooltip.install(reasonIcon, new Tooltip("Raison"));
        Label reasonLabel = new Label("Raison: " + report.getReason());
        reasonLabel.setWrapText(true);
        reasonLabel.getStyleClass().add("report-info");
        reasonLabel.setMaxWidth(400);
        reasonBox.getChildren().addAll(reasonIcon, reasonLabel);

        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = createFontIcon("bi-calendar", "icon-date", "📅");
        Tooltip.install(dateIcon, new Tooltip("Date"));
        Label dateLabel = new Label("Date: " + report.getCreatedAt());
        dateLabel.getStyleClass().add("report-info");
        dateBox.getChildren().addAll(dateIcon, dateLabel);

        VBox commentsBox = new VBox(5);
        Label commentsTitle = new Label("Commentaires Admin:");
        commentsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label commentsContent = new Label(report.getAdminComments() != null ? report.getAdminComments() : "Aucun");
        commentsContent.setWrapText(true);
        commentsContent.getStyleClass().add("report-info");
        commentsBox.getChildren().addAll(commentsTitle, commentsContent);

        infoSection.getChildren().addAll(doctorBox, patientBox, reasonBox, dateBox, commentsBox);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E5E7EB;");

        Label actionTitle = new Label("Actions");
        actionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 0 5 0;");

        HBox statusSelectionBox = new HBox(10);
        statusSelectionBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Nouveau Statut:");
        statusLabel.setPrefWidth(120);
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("PENDING", "UNDER_REVIEW", "RESOLVED"));
        statusBox.setValue(report.getStatus());
        statusBox.setPrefWidth(200);
        statusSelectionBox.getChildren().addAll(statusLabel, statusBox);

        HBox actionSelectionBox = new HBox(10);
        actionSelectionBox.setAlignment(Pos.CENTER_LEFT);
        Label actionLabel = new Label("Action:");
        actionLabel.setPrefWidth(120);
        ComboBox<String> actionBox = new ComboBox<>(FXCollections.observableArrayList(
                 "CLARIFIED", "WARNING", "UNBANNED", "BANNED"));
        actionBox.setPromptText("Choisir une action");
        actionBox.setPrefWidth(200);
        actionSelectionBox.getChildren().addAll(actionLabel, actionBox);

        VBox commentsInputBox = new VBox(5);
        Label commentsInputLabel = new Label("Commentaires:");
        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Ajouter des commentaires...");
        commentsArea.getStyleClass().add("text-area");
        commentsArea.setPrefRowCount(4);
        commentsInputBox.getChildren().addAll(commentsInputLabel, commentsArea);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button submitButton = new Button("Soumettre");
        submitButton.getStyleClass().add("btn-primary");
        submitButton.setPrefWidth(120);

        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().add("btn-secondary");
        cancelButton.setPrefWidth(120);

        buttonBox.getChildren().addAll(cancelButton, submitButton);

        submitButton.setOnAction(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), submitButton);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            scaleDown.setOnFinished(event -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), submitButton);
                scaleUp.setToX(1);
                scaleUp.setToY(1);
                scaleUp.play();

                if (!statusBox.getValue().equals(report.getStatus())) {
                    reportService.updateReportStatus(
                            report.getId(),
                            statusBox.getValue(),
                            commentsArea.getText(),
                            SessionManager.getInstance().getCurrentUser().getId()
                    );
                }
                if (actionBox.getValue() != null) {
                    reportService.logAction(
                            report.getId(),
                            SessionManager.getInstance().getCurrentUser().getId(),
                            actionBox.getValue(),
                            commentsArea.getText()
                    );
                }

                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), content);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(closeEvent -> {
                    dialogStage.close();
                    loadReports(); // Calls loadReports after dialog closes
                });
                fadeOut.play();
            });
            scaleDown.play();
        });
        cancelButton.setOnAction(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), cancelButton);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            scaleDown.setOnFinished(event -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), cancelButton);
                scaleUp.setToX(1);
                scaleUp.setToY(1);
                scaleUp.play();

                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), content);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(closeEvent -> dialogStage.close());
                fadeOut.play();
            });
            scaleDown.play();
        });

        content.getChildren().addAll(
                headerContainer,
                infoSection,
                separator,
                actionTitle,
                statusSelectionBox,
                actionSelectionBox,
                commentsInputBox,
                buttonBox
        );

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource("/css/admin_dashboard.css").toExternalForm());
        dialogStage.setScene(scene);

        content.setOpacity(0);
        dialogStage.setOnShown(e -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - dialogStage.getWidth()) / 2;
            double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - dialogStage.getHeight()) / 2;
            dialogStage.setX(centerX);
            dialogStage.setY(centerY);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });

        dialogStage.setOnCloseRequest(e -> {
            content.getChildren().clear();
        });

        dialogStage.showAndWait();
    }

    private void showAlert(Stage parentStage, String type, String message) {
        Platform.runLater(() -> {
            Stage alertStage = new Stage();
            alertStage.initModality(Modality.APPLICATION_MODAL);
            alertStage.initOwner(parentStage);
            alertStage.initStyle(StageStyle.TRANSPARENT);
            alertStage.setTitle("ChronoSerena - Notification");

            VBox alertBox = new VBox(15);
            alertBox.setAlignment(Pos.CENTER);
            alertBox.setPadding(new Insets(25));
            alertBox.getStyleClass().add("alert-dialog");
            alertBox.getStyleClass().add(type);
            alertBox.setMinWidth(400);
            alertBox.setMaxWidth(500);

            HBox headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(0, 0, 10, 0));

            Label alertIcon = createFontIcon(
                    type.equals("success") ? "bi-check-circle" :
                            type.equals("warning") ? "bi-exclamation-triangle" : "bi-x-circle",
                    "alert-icon " + type,
                    type.equals("success") ? "✅" : type.equals("warning") ? "⚠️" : "❌"
            );
            Tooltip.install(alertIcon, new Tooltip(type.equals("success") ? "Succès" : type.equals("warning") ? "Avertissement" : "Erreur"));

            Label alertMessage = new Label(message);
            alertMessage.getStyleClass().add("alert-message");
            alertMessage.setWrapText(true);
            headerBox.getChildren().addAll(alertIcon, alertMessage);

            Button closeButton = new Button("Fermer");
            closeButton.getStyleClass().add("btn-close-alert");
            closeButton.setPrefWidth(120);
            closeButton.setOnAction(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), alertBox);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(event -> alertStage.close());
                fadeOut.play();
            });

            alertBox.getChildren().addAll(headerBox, closeButton);

            Scene scene = new Scene(alertBox);
            scene.getStylesheets().add(getClass().getResource("/css/admin_dashboard.css").toExternalForm());
            scene.setFill(null);
            alertStage.setScene(scene);

            alertStage.setOnShown(e -> {
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - alertStage.getWidth()) / 2;
                double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - alertStage.getHeight()) / 2;
                alertStage.setX(centerX);
                alertStage.setY(centerY);

                alertBox.setScaleX(0.9);
                alertBox.setScaleY(0.9);
                alertBox.setOpacity(0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), alertBox);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), alertBox);
                scaleIn.setFromX(0.9);
                scaleIn.setFromY(0.9);
                scaleIn.setToX(1);
                scaleIn.setToY(1);

                fadeIn.play();
                scaleIn.play();
            });

            if (!type.equals("danger")) {
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        javafx.application.Platform.runLater(() -> {
                            if (alertStage.isShowing()) {
                                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), alertBox);
                                fadeOut.setFromValue(1);
                                fadeOut.setToValue(0);
                                fadeOut.setOnFinished(event -> alertStage.close());
                                fadeOut.play();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            alertStage.setOnCloseRequest(e -> alertBox.getChildren().clear());
            alertStage.showAndWait();
        });
    }
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminDashboard.fxml", event);
    }
}