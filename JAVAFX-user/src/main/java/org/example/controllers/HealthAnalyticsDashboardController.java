package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.models.Patient;
import org.example.services.DemandeDAO;
import org.example.util.HealthAnalyticsUtil;
import org.example.util.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HealthAnalyticsDashboardController implements Initializable {
    @FXML private Button historique;

    @FXML
    private VBox noDataView;
    @FXML
    private VBox dataView;
    @FXML
    private Label healthScoreLabel;
    @FXML
    private Label healthScoreDescription;
    @FXML
    private ProgressBar healthScoreProgress;
    @FXML
    private Label recommendationAdherenceLabel;
    @FXML
    private ProgressBar waterProgressBar;
    @FXML
    private ProgressBar activityProgressBar;
    @FXML
    private ProgressBar mealProgressBar;
    @FXML
    private Label waterProgressLabel;
    @FXML
    private Label activityProgressLabel;
    @FXML
    private Label mealProgressLabel;
    @FXML
    private ToggleButton waterToggle;
    @FXML
    private ToggleButton activityToggle;
    @FXML
    private ToggleButton mealToggle;
    @FXML
    private ToggleButton calorieToggle;
    @FXML
    private ToggleGroup chartToggle;
    @FXML
    private LineChart<String, Number> waterChart;
    @FXML
    private LineChart<String, Number> activityChart;
    @FXML
    private LineChart<String, Number> mealChart;
    @FXML
    private LineChart<String, Number> calorieChart;
    @FXML
    private GridPane demandesGrid;
    @FXML
    private ComboBox<String> timeRangeComboBox;

    private DemandeDAO demandeDAO;
    private DateTimeFormatter dateFormatter;
    private int timeRangeLimit = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeDAO = new DemandeDAO();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        setupTimeRangeComboBox();

        if (!verifySessionAndAccess()) {
            return;
        }

        setupChartToggles();
        loadDashboardData();
    }

    private boolean verifySessionAndAccess() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                    "Vous devez être connecté pour accéder à cette page.");
            redirectToLogin();
            return false;
        }

        if (!SessionManager.getInstance().isPatient()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Accès non autorisé",
                    "Seuls les patients peuvent accéder à cette page.");
            redirectToLogin();
            return false;
        }

        return true;
    }

    private void setupTimeRangeComboBox() {
        timeRangeComboBox.setItems(FXCollections.observableArrayList(
                "7 derniers jours",
                "30 derniers jours",
                "3 derniers mois",
                "Toutes les données"
        ));

        timeRangeComboBox.setValue("7 derniers jours");

        timeRangeComboBox.setOnAction(e -> {
            String selectedRange = timeRangeComboBox.getValue();
            switch (selectedRange) {
                case "7 derniers jours" -> timeRangeLimit = 7;
                case "30 derniers jours" -> timeRangeLimit = 30;
                case "3 derniers mois" -> timeRangeLimit = 90;
                case "Toutes les données" -> timeRangeLimit = 0;
            }
            loadDashboardData();
        });
    }

    private void setupChartToggles() {
        waterToggle.setOnAction(e -> switchChart("water"));
        activityToggle.setOnAction(e -> switchChart("activity"));
        mealToggle.setOnAction(e -> switchChart("meal"));
        calorieToggle.setOnAction(e -> switchChart("calorie"));
    }

    private void switchChart(String chartType) {
        waterChart.setVisible(false);
        activityChart.setVisible(false);
        mealChart.setVisible(false);
        calorieChart.setVisible(false);

        switch (chartType) {
            case "water" -> waterChart.setVisible(true);
            case "activity" -> activityChart.setVisible(true);
            case "meal" -> mealChart.setVisible(true);
            case "calorie" -> calorieChart.setVisible(true);
        }
    }

    private void loadDashboardData() {
        Patient currentPatient = SessionManager.getInstance().getCurrentPatient();
        int patientId = currentPatient.getId();

        List<Demande> patientDemands = demandeDAO.getByPatientId(patientId);

        if (patientDemands.isEmpty()) {
            noDataView.setVisible(true);
            dataView.setVisible(false);
            return;
        }

        noDataView.setVisible(false);
        dataView.setVisible(true);

        loadHealthScore(patientId);
        loadRecommendationAdherence(patientId);
        loadCharts(patientId);
        loadDemandCards(patientDemands);
    }

    private void loadHealthScore(int patientId) {
        int healthScore = HealthAnalyticsUtil.calculateHealthScore(patientId);
        healthScoreLabel.setText(String.valueOf(healthScore));
        healthScoreProgress.setProgress(healthScore / 100.0);

        if (healthScore >= 90) {
            healthScoreLabel.getStyleClass().add("health-score-excellent");
            healthScoreDescription.setText("Excellent");
        } else if (healthScore >= 80) {
            healthScoreLabel.getStyleClass().add("health-score-good");
            healthScoreDescription.setText("Très Bon");
        } else if (healthScore >= 70) {
            healthScoreLabel.getStyleClass().add("health-score-good");
            healthScoreDescription.setText("Bon");
        } else if (healthScore >= 60) {
            healthScoreLabel.getStyleClass().add("health-score-average");
            healthScoreDescription.setText("Moyen");
        } else if (healthScore >= 40) {
            healthScoreLabel.getStyleClass().add("health-score-below-average");
            healthScoreDescription.setText("À Améliorer");
        } else {
            healthScoreLabel.getStyleClass().add("health-score-poor");
            healthScoreDescription.setText("Attention");
        }
    }

    private void loadRecommendationAdherence(int patientId) {
        double adherencePercentage = HealthAnalyticsUtil.getRecommendationAdherence(patientId);

        // TODO: Implement detailed adherence calculation in HealthAnalyticsUtil
        // Map<String, Double> adherenceMetrics = HealthAnalyticsUtil.getDetailedRecommendationAdherence(patientId);
        // double waterAdherence = adherenceMetrics.getOrDefault("water", 0.0);
        // double activityAdherence = adherenceMetrics.getOrDefault("activity", 0.0);
        // double mealAdherence = adherenceMetrics.getOrDefault("meal", 0.0);
        
        // Temporarily disable detailed progress bars until the calculation is implemented
        if (waterProgressBar != null) {
             waterProgressBar.setProgress(0.0); // Set to 0 or hide
        }
        if (activityProgressBar != null) {
             activityProgressBar.setProgress(0.0); // Set to 0 or hide
        }
        if (mealProgressBar != null) {
             mealProgressBar.setProgress(0.0); // Set to 0 or hide
        }
        
        if (waterProgressLabel != null) {
             waterProgressLabel.setText("N/A"); // Indicate data not available
        }
        if (activityProgressLabel != null) {
             activityProgressLabel.setText("N/A"); // Indicate data not available
        }
        if (mealProgressLabel != null) {
             mealProgressLabel.setText("N/A"); // Indicate data not available
        }

        recommendationAdherenceLabel.setText(String.format("Adhérence globale: %.0f%%", adherencePercentage)); // Updated label text
    }

    private void loadCharts(int patientId) {
        waterChart.getData().clear();
        activityChart.getData().clear();
        mealChart.getData().clear();
        calorieChart.getData().clear();

        Map<LocalDateTime, Float> waterData = HealthAnalyticsUtil.getWaterConsumptionTrend(patientId, timeRangeLimit);
        XYChart.Series<String, Number> waterSeries = new XYChart.Series<>();
        waterSeries.setName("Consommation d'Eau");
        waterData.forEach((date, value) -> waterSeries.getData().add(new XYChart.Data<>(date.format(dateFormatter), value)));
        waterChart.getData().add(waterSeries);

        Map<LocalDateTime, Float> activityData = HealthAnalyticsUtil.getActivityDurationTrend(patientId, timeRangeLimit);
        XYChart.Series<String, Number> activitySeries = new XYChart.Series<>();
        activitySeries.setName("Durée d'Activité");
        activityData.forEach((date, value) -> activitySeries.getData().add(new XYChart.Data<>(date.format(dateFormatter), value)));
        activityChart.getData().add(activitySeries);

        Map<LocalDateTime, Integer> mealData = HealthAnalyticsUtil.getMealCountTrend(patientId, timeRangeLimit);
        XYChart.Series<String, Number> mealSeries = new XYChart.Series<>();
        mealSeries.setName("Nombre de Repas");
        mealData.forEach((date, value) -> mealSeries.getData().add(new XYChart.Data<>(date.format(dateFormatter), value)));
        mealChart.getData().add(mealSeries);

        Map<LocalDateTime, Float> calorieData = HealthAnalyticsUtil.getCalorieTrend(patientId, timeRangeLimit);
        XYChart.Series<String, Number> calorieSeries = new XYChart.Series<>();
        calorieSeries.setName("Calories");
        calorieData.forEach((date, value) -> calorieSeries.getData().add(new XYChart.Data<>(date.format(dateFormatter), value)));
        calorieChart.getData().add(calorieSeries);
    }

    private void loadDemandCards(List<Demande> demands) {
        demandesGrid.getChildren().clear();
        demands.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));

        List<Demande> recentDemands = demands.size() > 4 ? demands.subList(0, 4) : demands;

        int row = 0;
        int col = 0;

        for (Demande demand : recentDemands) {
            VBox demandCard = createDemandCard(demand);
            demandesGrid.add(demandCard, col, row);
            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDemandCard(Demande demand) {
        VBox card = new VBox(10);
        card.getStyleClass().add("analytics-card");

        Label dateLabel = new Label(demand.getDate().format(dateFormatter));
        dateLabel.getStyleClass().add("analytics-date-label");

        HBox header = new HBox(5);
        Label titleLabel = new Label("Demande #" + demand.getId());
        titleLabel.getStyleClass().add("analytics-subtitle");
        header.getChildren().add(titleLabel);

        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(5);
        details.add(new Label("Eau:"), 0, 0);
        details.add(new Label(demand.getEau() + " L"), 1, 0);
        details.add(new Label("Repas:"), 0, 1);
        details.add(new Label(String.valueOf(demand.getNbr_repas())), 1, 1);
        details.add(new Label("Collations:"), 0, 2);
        details.add(new Label(demand.isSnacks() ? "Oui" : "Non"), 1, 2);
        details.add(new Label("Activité:"), 0, 3);
        details.add(new Label(demand.getActivity() != null ? demand.getActivity() : "Non spécifié"), 1, 3);
        details.add(new Label("Durée:"), 0, 4);
        details.add(new Label(demand.getDuree_activite() + " min"), 1, 4);

        Button viewButton = new Button("Voir détails");
        viewButton.getStyleClass().add("analytics-button");
        viewButton.setOnAction(e -> viewDemandDetails(demand.getId()));

        card.getChildren().addAll(dateLabel, header, details, viewButton);
        return card;
    }

    @FXML
    public void handleBack(ActionEvent event) {
        navigateTo("/fxml/DemandeMyView.fxml", event);
    }

    @FXML
    public void handleCreateDemande(ActionEvent event) {
        navigateTo("/fxml/DemandeCreateView.fxml", event);
    }

    @FXML
    public void handleViewAllDemandes(ActionEvent event) {
        navigateTo("/fxml/DemandeMyView.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation Error", e.getMessage());
        }
    }

    private void viewDemandDetails(int demandId) {
        try {
            Demande demand = demandeDAO.getById(demandId);
            if (demand == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Demande non trouvée", "La demande avec l'ID " + demandId + " n'existe pas.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeEditView.fxml"));
            Parent root = loader.load();
            DemandeEditViewController controller = loader.getController();
            controller.setDemande(demand);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Détails de la Demande #" + demandId);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture des détails", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void redirectToLogin() {
        navigateTo("/fxml/Login.fxml", new ActionEvent());
    }

    @FXML
    public void redirectToHistorique(ActionEvent event) {
        try {
            // Enhanced logging
            System.out.println("Starting history redirect...");
            System.out.println("SessionManager.isLoggedIn(): " + SessionManager.getInstance().isLoggedIn());
            System.out.println("SessionManager.isPatient(): " + SessionManager.getInstance().isPatient());

            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showErrorDialog("Erreur", "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Check if the user is a patient
            if (!SessionManager.getInstance().isPatient()) {
                showErrorDialog("Erreur", "Seuls les patients peuvent accéder à cette fonctionnalité.");
                return;
            }

            // Get the patient from the session
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            System.out.println("Patient from SessionManager: " + (patient != null ?
                    "ID=" + patient.getId() + ", Nom=" + patient.getNom() : "NULL"));

            if (patient == null) {
                System.out.println("Error: SessionManager.getCurrentPatient() returned null!");
                showErrorDialog("Erreur", "Impossible de récupérer les informations du patient.");
                return;
            }

            // Since we need to set the patient in the controller, we need custom loading
            // rather than using SceneManager directly
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_historique.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the patient
            AjouterHistController controller = loader.getController();
            controller.setPatient(patient);
            System.out.println("Patient passed to controller: ID=" + patient.getId() +
                    ", Nom=" + patient.getNom() + ", Prénom=" + patient.getPrenom());

            // Show the new scene with maximum size
            Stage stage = (Stage) historique.getScene().getWindow();

            // Get screen dimensions for maximum size
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Preserve stylesheets if any
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error during navigation to historique: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur", "Impossible de charger la page d'ajout d'historique: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_patient.fxml", event);
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeDashboard.fxml", event);
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        SceneManager.loadScene("/fxml/rendez-vous-view.fxml", event);
    }

    @FXML
    public void viewDoctors(ActionEvent event) {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Use SceneManager to load the DoctorList.fxml in full screen
            SceneManager.loadScene("/fxml/DoctorList.fxml", event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des médecins: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToEvent(ActionEvent event) {
        SceneManager.loadScene("/fxml/eventFront.fxml", event);
    }

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
    }
    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }

    @FXML
    private void redirectProduit(ActionEvent event) {
        SceneManager.loadScene("/fxml/front/showProduit.fxml", event);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
