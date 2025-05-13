package org.example.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.example.models.Reminder;

import java.time.format.DateTimeFormatter;

public class NotificationDisplayService {
    private static NotificationDisplayService instance;

    private NotificationDisplayService() {
        // Private constructor for singleton
    }

    public static synchronized NotificationDisplayService getInstance() {
        if (instance == null) {
            instance = new NotificationDisplayService();
        }
        return instance;
    }

    public void showReminderNotification(Reminder reminder) {
        // Create a new stage
        Stage notificationStage = new Stage();
        notificationStage.setTitle("Rappel de médicament");
        notificationStage.initModality(Modality.NONE);
        notificationStage.initStyle(StageStyle.UTILITY);
        notificationStage.setAlwaysOnTop(true);

        // Create notification content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("Rappel: " + reminder.getMedicationName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Date/Time
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        Label timeLabel = new Label("À prendre le " +
                reminder.getDate().format(dateFormat) + " à " +
                reminder.getTime().format(timeFormat));

        // Description if available
        Label descLabel = null;
        if (reminder.getDescription() != null && !reminder.getDescription().isEmpty()) {
            descLabel = new Label(reminder.getDescription());
            descLabel.setWrapText(true);
        }

        // Button to dismiss
        Button closeButton = new Button("OK");
        closeButton.setOnAction(e -> notificationStage.close());
        closeButton.setStyle("-fx-background-color: #3fbbc0; -fx-text-fill: white;");

        // Add components to layout
        content.getChildren().addAll(titleLabel, timeLabel);
        if (descLabel != null) {
            content.getChildren().add(descLabel);
        }
        content.getChildren().add(closeButton);

        // Set up scene
        Scene scene = new Scene(content, 300, 200);
        notificationStage.setScene(scene);

        // Show the notification
        notificationStage.show();
    }
}