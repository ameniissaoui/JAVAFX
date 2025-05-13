package org.example.controllers;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.services.StatisticsService;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class StatisticsController implements Initializable {

    @FXML private PieChart userRoleChart;
    @FXML private BarChart<String, Number> ageDistributionChart;
    @FXML private PieChart doctorVerificationChart;
    @FXML private BarChart<String, Number> registrationByMonthChart;
    @FXML private PieChart specialtyDistributionChart;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label verifiedDoctorsLabel;
    @FXML private Label totalPatientsLabel;

    private StatisticsService statisticsService;

    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button buttoncommande;
    @FXML private Button tablesButton;
    @FXML private Button eventButton;
    private boolean statisticsMenuExpanded = false;
    @FXML
    private VBox statisticsSubmenu;

    @FXML
    private Button statisticsMenuButton;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statisticsService = new StatisticsService();
        loadAllStatistics();
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        buttoncommande.setOnAction(event -> handleCommandeRedirect());

        // Link the profile button to its handler
        setupStatisticsMenuIcon();

    }
    private void setupStatisticsMenuIcon() {
        // Add a dropdown arrow icon to the statistics menu button
        FontIcon arrowIcon = new FontIcon("fas-chevron-right");
        arrowIcon.setIconSize(12);
        arrowIcon.setIconColor(javafx.scene.paint.Color.valueOf("#64748b"));

        // Add it to the button (assuming you have an HBox in the button to hold both icons)
        statisticsMenuButton.setGraphic(new javafx.scene.layout.HBox(5,
                new FontIcon("fas-chart-bar"), arrowIcon));
    }
    @FXML
    public void toggleStatisticsMenu() {
        // Toggle the visibility of the statistics submenu
        statisticsMenuExpanded = !statisticsMenuExpanded;

        // Update the arrow icon direction
        FontIcon arrowIcon = statisticsMenuExpanded ?
                new FontIcon("fas-chevron-down") : new FontIcon("fas-chevron-right");
        arrowIcon.setIconSize(12);
        arrowIcon.setIconColor(javafx.scene.paint.Color.valueOf("#64748b"));

        // Create chart icon
        FontIcon chartIcon = new FontIcon("fas-chart-bar");
        chartIcon.setIconSize(16);
        chartIcon.setIconColor(javafx.scene.paint.Color.valueOf("#64748b"));

        // Update button graphics
        statisticsMenuButton.setGraphic(new javafx.scene.layout.HBox(5, chartIcon, arrowIcon));

        // Animate the submenu visibility
        if (statisticsMenuExpanded) {
            statisticsSubmenu.setVisible(true);
            statisticsSubmenu.setManaged(true);

            // Optional: add a slide-down animation
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), statisticsSubmenu);
            tt.setFromY(-20);
            tt.setToY(0);
            tt.play();
        } else {
            // Optional: add a slide-up animation
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), statisticsSubmenu);
            tt.setFromY(0);
            tt.setToY(-20);
            tt.setOnFinished(e -> {
                statisticsSubmenu.setVisible(false);
                statisticsSubmenu.setManaged(false);
            });
            tt.play();
        }
    }
    @FXML
    private void handleStatisticsCommandeRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/back/statistics.fxml", event);
    }
    @FXML private void acceuiRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminDashboard.fxml", event);
    }
    private void loadAllStatistics() {
        loadUserRoleDistribution();
        loadAgeDistribution();
        loadDoctorVerificationStats();
        loadRegistrationByMonth();
        loadSpecialtyDistribution();
        loadUserSummary();
    }

    private void loadUserRoleDistribution() {
        userRoleChart.getData().clear();
        Map<String, Integer> roleCounts = statisticsService.getUserCountByRole();

        for (Map.Entry<String, Integer> entry : roleCounts.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            userRoleChart.getData().add(slice);
        }
    }

    private void loadAgeDistribution() {
        ageDistributionChart.getData().clear();
        Map<String, Integer> ageGroups = statisticsService.getAgeDistribution();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("User Count");

        for (Map.Entry<String, Integer> entry : ageGroups.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        ageDistributionChart.getData().add(series);
    }

    private void loadDoctorVerificationStats() {
        doctorVerificationChart.getData().clear();
        Map<String, Integer> verificationStats = statisticsService.getDoctorVerificationStats();

        for (Map.Entry<String, Integer> entry : verificationStats.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            doctorVerificationChart.getData().add(slice);
        }
    }

    private void loadRegistrationByMonth() {
        registrationByMonthChart.getData().clear();
        Map<String, Integer> monthlyStats = statisticsService.getRegistrationByMonth();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("New Users");

        for (Map.Entry<String, Integer> entry : monthlyStats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        registrationByMonthChart.getData().add(series);
    }

    private void loadSpecialtyDistribution() {
        specialtyDistributionChart.getData().clear();
        Map<String, Integer> specialties = statisticsService.getSpecialtyDistribution();

        for (Map.Entry<String, Integer> entry : specialties.entrySet()) {
            if (entry.getValue() > 0) { // Only add non-zero values
                PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
                specialtyDistributionChart.getData().add(slice);
            }
        }
    }

    private void loadUserSummary() {
        Map<String, Integer> roleCounts = statisticsService.getUserCountByRole();
        Map<String, Integer> bannedStats = statisticsService.getBannedUserStats();
        Map<String, Integer> verificationStats = statisticsService.getDoctorVerificationStats();

        int totalUsers = roleCounts.values().stream().mapToInt(Integer::intValue).sum();
        int totalDoctors = roleCounts.getOrDefault("medecin", 0);
        int totalPatients = roleCounts.getOrDefault("patient", 0);
        int verifiedDoctors = verificationStats.getOrDefault("verified", 0);
        int activeUsers = bannedStats.getOrDefault("active", 0);
        int bannedUsers = bannedStats.getOrDefault("banned", 0);

        totalUsersLabel.setText(String.valueOf(totalUsers));
        activeUsersLabel.setText(String.valueOf(activeUsers));
        bannedUsersLabel.setText(String.valueOf(bannedUsers));
        totalDoctorsLabel.setText(String.valueOf(totalDoctors));
        verifiedDoctorsLabel.setText(String.valueOf(verifiedDoctors));
        totalPatientsLabel.setText(String.valueOf(totalPatients));
    }

    // You can add methods to handle refresh button clicks, etc.
    @FXML
    private void refreshStatistics() {
        loadAllStatistics();
    }
    private void handleCommandeRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/back/showCommande.fxml"));
            Stage stage = (Stage) buttoncommande.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
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
    private void handleeventRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/listevent.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleTablesRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/back/showProduit.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML private void handleProfileRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_profile.fxml"));
            Parent profileRoot = loader.load();

            Stage stage = (Stage) profileButton.getScene().getWindow();
            Scene profileScene = new Scene(profileRoot);
            stage.setScene(profileScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profil: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void navigateToReportDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminReportDashboard.fxml", event);
    }
    @FXML
    private void handleDashboardRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminDashboard.fxml", event);
    }
    private void handleSuiviRedirect() {
        try {
            // Use SceneManager to load the scene and ensure it displays maximized
            SceneManager.loadScene("/fxml/liste_suivi_back.fxml", new ActionEvent(suivi, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des suivis: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleHistoriqueRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_historique_back.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML private void navigateToRegister(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminRegistration.fxml", event);
    }

}
