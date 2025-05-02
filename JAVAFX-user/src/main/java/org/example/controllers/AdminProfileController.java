package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.models.Admin;
import org.example.models.User;
import org.example.services.AdminService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminProfileController extends BaseProfileController {
    private AdminService adminService;
    @FXML private Button acceuil;

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

        acceuil.setOnAction(event -> handleAcceuilRedirect());
    }

    @Override
    protected void setUserService() {
        this.userService = adminService;
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleAcceuilRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) acceuil.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
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