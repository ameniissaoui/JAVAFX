package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private Button clientButton;

    @FXML
    private Button adminButton;

    @FXML
    void openClientInterface(ActionEvent event) {
        try {
            // Load the client interface (showProduit.fxml)
            Parent root = FXMLLoader.load(getClass().getResource("/front/showProduit.fxml"));
            Stage stage = (Stage) clientButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading showProduit.fxml: " + e.getMessage());
        }
    }

    @FXML
    void openAdminInterface(ActionEvent event) {
        try {
            // Load the admin interface (addproduit.fxml)
            Parent root = FXMLLoader.load(getClass().getResource("/back/showProduit.fxml"));
            Stage stage = (Stage) adminButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading addproduit.fxml: " + e.getMessage());
        }
    }
}