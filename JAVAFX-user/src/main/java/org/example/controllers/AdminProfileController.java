package org.example.controllers;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Admin;
import org.example.models.User;
import org.example.services.AdminService;
import org.example.util.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminProfileController extends BaseProfileController {
    private AdminService adminService;
    @FXML
    private VBox statisticsSubmenu;

    @FXML
    private Button statisticsMenuButton;
    private boolean statisticsMenuExpanded = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isAdmin()) {
            showErrorDialog("Erreur", "Accès non autorisé");
            handleLogout();
            return;
        }

        Admin admin = SessionManager.getInstance().getCurrentAdmin();
        if (admin != null) {
            currentUser = admin;
            loadUserData();
        }

        adminService = new AdminService();
        setUserService();
        super.initialize(url, resourceBundle);
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
    @FXML private void navigateToReportDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminReportDashboard.fxml", event);
    }
    @FXML
    private void handlestatRedirect(ActionEvent event)  {
        SceneManager.loadScene("/fxml/statistique.fxml", event);

    }
    @FXML
    private void handleStatisticsRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/statistics-view.fxml", event);
    }
    @FXML
    private void handleStatisticsCommandeRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/back/statistics.fxml", event);
    }
    @FXML private void handleProfileRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/admin_profile.fxml", event);
    }
    @FXML private void navigateToRegister(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminRegistration.fxml", event);
    }

    @Override
    protected void setUserService() {
        this.userService = adminService;
    }

    void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleAcceuilRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminDashboard.fxml", event);

    }

    @Override
    public void setUser(User user) {
        if (user instanceof Admin) {
            super.setUser(user);
            setUserService();
        } else {
            throw new IllegalArgumentException("User must be an Admin");
        }
    }

    public void setAdmin(Admin admin) {
        this.currentUser = admin;
        loadUserData();
        setUserService();
    }


    @Override
    protected void saveUser() {
        if (currentUser instanceof Admin) {
            Admin updatedAdmin = (Admin) currentUser;
            adminService.update(updatedAdmin);
            SessionManager.getInstance().setCurrentUser(updatedAdmin, "admin");
        }
    }

    @Override
    public void handleLogout() {
        super.handleLogout();
    }
}