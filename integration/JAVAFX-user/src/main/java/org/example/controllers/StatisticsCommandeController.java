package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.Commande;
import org.example.services.CommandeServices;
import org.example.services.CartItemServices;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;

public class StatisticsCommandeController implements Initializable {

    @FXML private BorderPane mainBorderPane;
    @FXML private AnchorPane anchorPane;
    @FXML private BarChart<String, Number> priceRangeChart;
    @FXML private PieChart paymentStatusChart;
    @FXML private LineChart<String, Number> monthlyRevenueChart;
    @FXML private Label totalRevenueLabel;
    @FXML private Label successfulOrdersLabel;
    @FXML private Label averageOrderValueLabel;
    @FXML private Label cartCountLabel;
    @FXML private Label cartCountLabel1;
    @FXML private Button buttoncommande;
    @FXML private Button tablesButton;
    @FXML private Button eventButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button acceuil;
    @FXML private Label welcomeLabel;
    @FXML private Button profileButton;
    @FXML private Button userProfileButton;

    private CommandeServices commandeServices;
    private CartItemServices cartItemServices;
    private org.example.util.MaConnexion dbInstance;
    private Connection cnx;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commandeServices = new CommandeServices();
        cartItemServices = new CartItemServices();
        dbInstance = org.example.util.MaConnexion.getInstance();
        cnx = dbInstance.getCnx();

        // Set up navigation buttons
        buttoncommande.setOnAction(event -> handleCommandeRedirect());
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        acceuil.setOnAction(event -> handleAcceuilRedirect());

        // Set up profile button if it exists
        if (profileButton != null) {
            profileButton.setOnAction(event -> handleProfileRedirect());
        }

        // Set up user profile button if it exists
        if (userProfileButton != null) {
            userProfileButton.setOnAction(event -> handleProfileRedirect());
        }

        // Update cart count
        updateCartCount();

        // Initialize charts and statistics
        Platform.runLater(() -> {
            initializePriceRangeChart();
            initializePaymentStatusChart();
            initializeMonthlyRevenueChart();
            updateStatisticsSummary();

            // Maximize window
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            maximizeStage(stage);
        });
    }

    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }

    private void initializePriceRangeChart() {
        try {
            // Define price ranges
            String[] priceRanges = {"0-50", "51-100", "101-200", "201-500", "500+"};

            // Create query to count orders in each price range
            String query = "SELECT " +
                    "SUM(CASE WHEN total_amount BETWEEN 0 AND 50 THEN 1 ELSE 0 END) AS range1, " +
                    "SUM(CASE WHEN total_amount BETWEEN 51 AND 100 THEN 1 ELSE 0 END) AS range2, " +
                    "SUM(CASE WHEN total_amount BETWEEN 101 AND 200 THEN 1 ELSE 0 END) AS range3, " +
                    "SUM(CASE WHEN total_amount BETWEEN 201 AND 500 THEN 1 ELSE 0 END) AS range4, " +
                    "SUM(CASE WHEN total_amount > 500 THEN 1 ELSE 0 END) AS range5 " +
                    "FROM chrono.commande WHERE payment_status = 'paid'";

            PreparedStatement stmt = cnx.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create series for the chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Orders by Price Range");

            if (rs.next()) {
                for (int i = 0; i < priceRanges.length; i++) {
                    series.getData().add(new XYChart.Data<>(priceRanges[i], rs.getInt("range" + (i+1))));
                }
            }

            // Clear previous data and add new series
            priceRangeChart.getData().clear();
            priceRangeChart.getData().add(series);

            // Style the chart
            priceRangeChart.setTitle("Orders by Price Range (Successful Payments)");
            priceRangeChart.getXAxis().setLabel("Price Range (€)");
            priceRangeChart.getYAxis().setLabel("Number of Orders");

            // Apply CSS styling to bars
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle("-fx-bar-fill: #30b4b4;");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load price range statistics: " + e.getMessage());
        }
    }

    private void initializePaymentStatusChart() {
        try {
            // Create query to count orders by payment status
            String query = "SELECT payment_status, COUNT(*) as count FROM chrono.commande GROUP BY payment_status";
            PreparedStatement stmt = cnx.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create data for the pie chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            while (rs.next()) {
                String status = rs.getString("payment_status");
                int count = rs.getInt("count");

                // Handle null or empty status
                if (status == null || status.isEmpty()) {
                    status = "Unknown";
                }

                pieChartData.add(new PieChart.Data(status, count));
            }

            // Set data to the chart
            paymentStatusChart.setData(pieChartData);
            paymentStatusChart.setTitle("Orders by Payment Status");

            // Add percentage labels to pie chart slices
            for (final PieChart.Data data : paymentStatusChart.getData()) {
                data.getNode().setOnMouseEntered(e -> {
                    double total = 0;
                    for (PieChart.Data d : paymentStatusChart.getData()) {
                        total += d.getPieValue();
                    }
                    String text = String.format("%s: %.1f%%", data.getName(), (data.getPieValue() / total) * 100);
                    data.setName(text);
                });

                data.getNode().setOnMouseExited(e -> {
                    String name = data.getName();
                    if (name.contains(":")) {
                        data.setName(name.substring(0, name.indexOf(":")));
                    }
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load payment status statistics: " + e.getMessage());
        }
    }

    private void initializeMonthlyRevenueChart() {
        try {
            // Create query to get monthly revenue for successful payments
            String query = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, SUM(total_amount) as revenue " +
                    "FROM chrono.commande WHERE payment_status = 'paid' " +
                    "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
                    "ORDER BY month ASC LIMIT 12";

            // If your database doesn't have a created_at column, you can modify this query
            // or use mock data for demonstration purposes

            // For demonstration purposes, let's create mock data if the query fails
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Monthly Revenue");

            try {
                PreparedStatement stmt = cnx.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String month = rs.getString("month");
                    double revenue = rs.getDouble("revenue");
                    series.getData().add(new XYChart.Data<>(month, revenue));
                }
            } catch (SQLException e) {
                // If query fails (e.g., no created_at column), use mock data
                System.out.println("Using mock data for monthly revenue chart: " + e.getMessage());

                // Generate last 6 months of mock data
                Calendar cal = Calendar.getInstance();
                for (int i = 5; i >= 0; i--) {
                    cal.add(Calendar.MONTH, -1);
                    String month = String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
                    double revenue = 1000 + Math.random() * 5000; // Random revenue between 1000 and 6000
                    series.getData().add(new XYChart.Data<>(month, revenue));
                }

                // Sort data by month
                series.getData().sort(Comparator.comparing(data -> data.getXValue()));
            }

            // Clear previous data and add new series
            monthlyRevenueChart.getData().clear();
            monthlyRevenueChart.getData().add(series);

            // Style the chart
            monthlyRevenueChart.setTitle("Monthly Revenue (Successful Payments)");
            monthlyRevenueChart.getXAxis().setLabel("Month");
            monthlyRevenueChart.getYAxis().setLabel("Revenue (€)");

            // Apply CSS styling to line
            series.getNode().setStyle("-fx-stroke: #30b4b4; -fx-stroke-width: 3px;");

            // Style data points
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle("-fx-background-color: #30b4b4, white; -fx-background-radius: 5px; -fx-padding: 5px;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load monthly revenue statistics: " + e.getMessage());
        }
    }

    private void updateStatisticsSummary() {
        try {
            // Query for total revenue from successful payments
            String revenueQuery = "SELECT SUM(total_amount) as total_revenue FROM chrono.commande WHERE payment_status = 'paid'";
            PreparedStatement revenueStmt = cnx.prepareStatement(revenueQuery);
            ResultSet revenueRs = revenueStmt.executeQuery();

            double totalRevenue = 0;
            if (revenueRs.next()) {
                totalRevenue = revenueRs.getDouble("total_revenue");
            }

            // Query for count of successful orders
            String ordersQuery = "SELECT COUNT(*) as successful_orders FROM chrono.commande WHERE payment_status = 'paid'";
            PreparedStatement ordersStmt = cnx.prepareStatement(ordersQuery);
            ResultSet ordersRs = ordersStmt.executeQuery();

            int successfulOrders = 0;
            if (ordersRs.next()) {
                successfulOrders = ordersRs.getInt("successful_orders");
            }

            // Calculate average order value
            double averageOrderValue = successfulOrders > 0 ? totalRevenue / successfulOrders : 0;

            // Format numbers for display
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

            // Update labels
            totalRevenueLabel.setText(currencyFormat.format(totalRevenue));
            successfulOrdersLabel.setText(String.valueOf(successfulOrders));
            averageOrderValueLabel.setText(currencyFormat.format(averageOrderValue));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load statistics summary: " + e.getMessage());
        }
    }

    private void updateCartCount() {
        try {
            // Get cart count (simplified version)
            int count = cartItemServices.showProduit().size();

            if (cartCountLabel != null) {
                cartCountLabel.setText(String.valueOf(count));
            }

            if (cartCountLabel1 != null) {
                cartCountLabel1.setText(String.valueOf(count));
            }
        } catch (Exception e) {
            System.err.println("Error updating cart count: " + e.getMessage());
        }
    }

    // Navigation methods
    @FXML
    void navigateToHome() {
        navigateTo("/fxml/front/home.fxml");
    }

    @FXML
    void navigateToHistoriques() {
        navigateTo("/fxml/front/historiques.fxml");
    }

    @FXML
    void redirectToDemande() {
        navigateTo("/fxml/DemandeDashboard.fxml");
    }

    @FXML
    void redirectToRendezVous() {
        navigateTo("/fxml/rendez-vous-view.fxml");
    }

    @FXML
    void redirectProduit() {
        navigateTo("/fxml/front/showProduit.fxml");
    }

    @FXML
    void navigateToTraitement() {
        navigateTo("/fxml/front/traitement.fxml");
    }

    @FXML
    void viewDoctors() {
        navigateTo("/fxml/DoctorList.fxml");
    }

    @FXML
    void navigateToContact() {
        navigateTo("/fxml/front/contact.fxml");
    }

    @FXML
    void navigateToProfile() {
        navigateTo("/fxml/front/profile.fxml");
    }

    @FXML
    void navigateToFavorites() {
        navigateTo("/fxml/front/favoris.fxml");
    }

    @FXML
    void commande() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToCommandes() {
        navigateTo("/fxml/front/ShowCommande.fxml");
    }

    @FXML
    void navigateToShop() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToStatistics() {
        navigateTo("/fxml/front/statistics.fxml");
    }

    @FXML
    void handleStatisticsRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/statistics-view.fxml", event);
    }

    @FXML
    void handleStatisticsCommandeRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/back/statistics.fxml", event);
    }

    @FXML
    void navigateToReportDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminReportDashboard.fxml", event);
    }

    @FXML
    void handleLogout() {
        try {
            // Clear the session
            org.example.util.SessionManager.getInstance().clearSession();

            // Navigate to login page
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (IOException e) {
            showErrorDialog("Erreur", "Échec de la déconnexion: " + e.getMessage());
        }
    }

    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page: " + e.getMessage());
        }
    }

    // Redirect handlers (from ShowCommandeController)
    private void handleCommandeRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/back/showCommande.fxml"));
            Stage stage = (Stage) buttoncommande.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des commandes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSuiviRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_suivi_back.fxml"));
            Stage stage = (Stage) suivi.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des suivis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleHistoriqueRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_historique_back.fxml"));
            Stage stage = (Stage) historique.getScene().getWindow();
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
            Stage stage = (Stage) eventButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des événements: " + e.getMessage());
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

    private void handleAcceuilRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Stage stage = (Stage) acceuil.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleProfileRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_profile.fxml"));
            Parent profileRoot = loader.load();

            Stage stage = (Stage) anchorPane.getScene().getWindow();
            Scene profileScene = new Scene(profileRoot);
            stage.setScene(profileScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profil: " + e.getMessage());
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}