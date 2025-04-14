package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectionController {

    @FXML
    private void openAdminRegistration(ActionEvent event) {
        loadScene("/fxml/AdminRegistration.fxml", event);
    }

    @FXML
    private void openMedecinRegistration(ActionEvent event) {
        loadScene("/fxml/DoctorRegistration.fxml", event);
    }

    @FXML
    private void openPatientRegistration(ActionEvent event) {
        loadScene("/fxml/PatientRegistration.fxml", event);
    }

    @FXML
    private void openLogin(ActionEvent event) {
        loadScene("/fxml/Login.fxml", event);
    }


    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Get the Stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlPath);
            e.printStackTrace();
        }
    }
}