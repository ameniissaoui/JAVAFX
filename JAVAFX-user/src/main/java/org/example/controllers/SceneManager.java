package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for managing scene transitions in JavaFX applications
 * with a focus on maintaining full-screen consistency
 */
public class SceneManager {

    /**
     * Loads a new FXML scene and displays it in full screen
     *
     * @param fxmlPath Path to the FXML file
     * @param event The event that triggered the scene change
     * @param preserveStylesheets Whether to preserve stylesheets from the previous scene
     */
    public static void loadScene(String fxmlPath, ActionEvent event, boolean preserveStylesheets) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create the new scene with screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Apply stylesheets from the previous scene if needed
            if (preserveStylesheets && stage.getScene() != null &&
                    !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            // Set the new scene
            stage.setScene(scene);

            // Make sure it's maximized for full screen display
            stage.setMaximized(true);

            // Show the stage
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load the requested page: " + e.getMessage());
        }
    }

    /**
     * Loads a new FXML scene and displays it in full screen while preserving stylesheets
     *
     * @param fxmlPath Path to the FXML file
     * @param event The event that triggered the scene change
     */
    public static void loadScene(String fxmlPath, ActionEvent event) {
        loadScene(fxmlPath, event, true);
    }

    /**
     * Creates a new stage and displays content in full screen
     * Useful for showing new windows like dialogs
     *
     * @param fxmlPath Path to the FXML file
     * @param title The title of the new window
     * @return The newly created stage
     */
    public static Stage createNewStage(String fxmlPath, String title) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Create a new stage
            Stage stage = new Stage();
            stage.setTitle(title);

            // Create scene with screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            stage.setScene(scene);
            stage.setMaximized(true);

            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Window Creation Error", "Could not create new window: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the controller instance from an FXML file
     *
     * @param fxmlPath Path to the FXML file
     * @param <T> The type of controller
     * @return The controller instance
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> T getController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }

    /**
     * Shows an error alert
     *
     * @param title The title of the alert
     * @param message The message to display
     */
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}