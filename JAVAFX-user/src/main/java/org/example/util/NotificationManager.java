package org.example.util;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;

public class NotificationManager {

    public enum NotificationType {
        SUCCESS, ERROR, INFO, WARNING
    }

    /**
     * Shows a notification popup with the given title and message
     */
    public static void showNotification(String title, String message, NotificationType type) {
        Platform.runLater(() -> {
            try {
                Stage currentStage = getActiveStage();
                if (currentStage == null) return;

                Popup popup = new Popup();
                popup.setAutoHide(true);

                // Create notification content
                Label titleLabel = new Label(title);
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

                Label messageLabel = new Label(message);
                messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

                HBox contentBox = new HBox(10);
                contentBox.setAlignment(Pos.CENTER_LEFT);
                contentBox.getChildren().addAll(titleLabel, messageLabel);

                StackPane container = new StackPane(contentBox);
                container.setPrefWidth(350);
                container.setPadding(new javafx.geometry.Insets(15));

                // Set color based on notification type
                switch (type) {
                    case SUCCESS:
                        container.setStyle("-fx-background-color: #10b981; -fx-background-radius: 5;");
                        break;
                    case ERROR:
                        container.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 5;");
                        break;
                    case WARNING:
                        container.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 5;");
                        break;
                    case INFO:
                    default:
                        container.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 5;");
                        break;
                }

                popup.getContent().add(container);

                // Position the popup
                popup.show(currentStage);
                popup.setX(currentStage.getX() + currentStage.getWidth() - container.getPrefWidth() - 20);
                popup.setY(currentStage.getY() + 50);

                // Auto-hide after 3 seconds
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(e -> {
                    FadeTransition fade = new FadeTransition(Duration.millis(500), container);
                    fade.setFromValue(1);
                    fade.setToValue(0);
                    fade.setOnFinished(fe -> popup.hide());
                    fade.play();
                });
                delay.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Stage getActiveStage() {
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
            if (window instanceof Stage && window.isShowing()) {
                return (Stage) window;
            }
        }
        return null;
    }
}