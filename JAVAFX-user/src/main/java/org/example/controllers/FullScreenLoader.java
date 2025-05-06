package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A utility class for loading FXML files in full screen with proper content scaling
 * This is a more general solution that can be used for any FXML file
 */
public class FullScreenLoader {

    /**
     * Loads an FXML file and displays it in full screen with proper content scaling
     *
     * @param stage The stage to display the FXML in
     * @param fxmlPath The path to the FXML file
     * @return The controller instance
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> T loadFXMLInFullScreen(Stage stage, String fxmlPath) throws IOException {
        System.out.println("Loading " + fxmlPath + " in full screen...");

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        System.out.println("Screen dimensions: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());

        // Load the FXML
        FXMLLoader loader = new FXMLLoader(FullScreenLoader.class.getResource(fxmlPath));
        Parent root = loader.load();

        // Apply full screen constraints based on the root type
        applyFullScreenConstraints(root, screenBounds);

        // Create the scene with screen dimensions
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        // Set the scene on the stage
        stage.setScene(scene);

        // Maximize the stage
        stage.setMaximized(true);

        // Set explicit dimensions
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Show the stage
        stage.show();

        // Final layout pass after the scene is displayed
        Platform.runLater(() -> {
            // Force another layout pass
            root.requestLayout();
            System.out.println("Final root dimensions: " + root.getBoundsInLocal().getWidth() +
                    "x" + root.getBoundsInLocal().getHeight());
        });

        System.out.println("Successfully loaded " + fxmlPath + " in full screen");

        // Return the controller
        return loader.getController();
    }

    /**
     * Applies full screen constraints to the root node based on its type
     *
     * @param root The root node
     * @param screenBounds The screen bounds
     */
    private static void applyFullScreenConstraints(Parent root, Rectangle2D screenBounds) {
        if (root instanceof Region) {
            Region region = (Region) root;

            // Set preferred size to screen size
            region.setPrefWidth(screenBounds.getWidth());
            region.setPrefHeight(screenBounds.getHeight());

            // Set min size to screen size
            region.setMinWidth(screenBounds.getWidth());
            region.setMinHeight(screenBounds.getHeight());
        }

        if (root instanceof AnchorPane) {
            AnchorPane anchorPane = (AnchorPane) root;

            // Ensure all anchor constraints are set to 0 for direct children
            for (javafx.scene.Node child : anchorPane.getChildren()) {
                if (child instanceof Pane || child instanceof BorderPane) {
                    AnchorPane.setTopAnchor(child, 0.0);
                    AnchorPane.setBottomAnchor(child, 0.0);
                    AnchorPane.setLeftAnchor(child, 0.0);
                    AnchorPane.setRightAnchor(child, 0.0);
                }
            }

            System.out.println("AnchorPane constraints applied");
        }

        if (root instanceof BorderPane) {
            BorderPane borderPane = (BorderPane) root;

            // Ensure the center region fills the available space
            if (borderPane.getCenter() instanceof Region) {
                Region centerRegion = (Region) borderPane.getCenter();
                centerRegion.setPrefWidth(screenBounds.getWidth());
                centerRegion.setPrefHeight(screenBounds.getHeight());
            }

            System.out.println("BorderPane constraints applied");
        }
    }
}
