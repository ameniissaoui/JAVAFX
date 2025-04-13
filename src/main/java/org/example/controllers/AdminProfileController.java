package org.example.controllers;

import javafx.event.ActionEvent;
import org.example.models.Admin;
import org.example.models.User;
import org.example.services.AdminService;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminProfileController extends BaseProfileController {
    private AdminService adminService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminService = new AdminService();
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Admin) {
            super.setUser(user);
        } else {
            throw new IllegalArgumentException("User must be a Patient");
        }
    }

    public void setAdmin(Admin admin) {
        this.currentUser = admin;
        loadUserData();
    }

    @Override
    protected void saveUser() {
        if (currentUser instanceof Admin) {
            adminService.update((Admin) currentUser);
        }
    }

    public void handleOverview(ActionEvent actionEvent) {
    }

    public void handleChangePassword(ActionEvent actionEvent) {
    }

    public void handleModifier(ActionEvent actionEvent) {
    }
}