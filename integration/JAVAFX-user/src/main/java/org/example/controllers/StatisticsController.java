package org.example.controllers;

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
import javafx.stage.Stage;
import org.example.services.StatisticsService;

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
        profileButton.setOnAction(event -> handleProfileRedirect());

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
    private void handleSuiviRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_suivi_back.fxml"));
            Stage stage = (Stage) suivi.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
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
}
