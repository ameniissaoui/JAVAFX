package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Enhanced utility class for product-related navigation with guaranteed full-screen display
 * and proper content scaling
 */
public class ProduitNavigationUtil {

    /**
     * Redirects to the showProduit.fxml page with guaranteed full screen dimensions
     * and ensures content properly fills the screen
     *
     * @param event The event that triggered the navigation
     */
    public static void redirectToShowProduit(ActionEvent event) {
        try {
            System.out.println("Starting redirection to showProduit.fxml with content scaling...");

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Get screen dimensions for maximum size
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            System.out.println("Screen dimensions: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(ProduitNavigationUtil.class.getResource("/fxml/front/showProduit.fxml"));
            Parent root = loader.load();

            // If the root is an AnchorPane, ensure it fills the entire screen
            if (root instanceof AnchorPane) {
                AnchorPane anchorPane = (AnchorPane) root;

                // Set preferred size to screen size
                anchorPane.setPrefWidth(screenBounds.getWidth());
                anchorPane.setPrefHeight(screenBounds.getHeight());

                // Set min size to screen size
                anchorPane.setMinWidth(screenBounds.getWidth());
                anchorPane.setMinHeight(screenBounds.getHeight());

                // Ensure all anchor constraints are set to 0
                for (Node child : anchorPane.getChildren()) {
                    AnchorPane.setTopAnchor(child, 0.0);
                    AnchorPane.setBottomAnchor(child, 0.0);
                    AnchorPane.setLeftAnchor(child, 0.0);
                    AnchorPane.setRightAnchor(child, 0.0);
                }

                System.out.println("AnchorPane constraints applied");
            }

            // Create the new scene with screen dimensions
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Set the new scene
            stage.setScene(scene);

            // Force the stage to be maximized
            stage.setMaximized(true);

            // Set explicit dimensions
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            // Show the stage
            stage.show();

            // Use Platform.runLater to ensure the content is properly sized after the scene is displayed
            Platform.runLater(() -> {
                // Get the controller to call its layout method
                ShowProduitFrontController controller = loader.getController();

                // Force another layout pass
                if (root instanceof AnchorPane) {
                    AnchorPane anchorPane = (AnchorPane) root;
                    anchorPane.requestLayout();
                    System.out.println("Final AnchorPane dimensions: " +
                            anchorPane.getWidth() + "x" + anchorPane.getHeight());
                }

                System.out.println("Content scaling completed");
            });

            System.out.println("Successfully redirected to showProduit.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load the product page: " + e.getMessage());
        }
    }

    /**
     * Shows an error alert
     *
     * @param title The title of the alert
     * @param message The message to display
     */
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
