package org.example.util;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Utility class for displaying notifications and alerts to the user.
 */
public class NotificationUtil {
    
    /**
     * Shows a success notification dialog.
     * 
     * @param title The title of the notification
     * @param message The message to display
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an error notification dialog.
     * 
     * @param title The title of the notification
     * @param message The message to display
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a warning notification dialog.
     * 
     * @param title The title of the notification
     * @param message The message to display
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation dialog.
     * 
     * @param title The title of the dialog
     * @param message The message to display
     * @return true if OK button is clicked, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait()
                .filter(response -> response == javafx.scene.control.ButtonType.OK)
                .isPresent();
    }
    
    /**
     * Shows an alert dialog with the specified parameters
     * 
     * @param type The type of alert (ERROR, WARNING, INFORMATION, CONFIRMATION)
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     */
    public static void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert
     * 
     * @param title The title of the alert
     * @param message The message to display
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, "Information", message);
    }
    
    /**
     * Shows a validation error message
     * 
     * @param field The name of the field with the error
     * @param message The error message
     */
    public static void showValidationError(String field, String message) {
        showAlert(AlertType.ERROR, "Erreur de validation", 
                "Erreur dans le champ: " + field, message);
    }
    
    /**
     * Show an in-application notification (doesn't use system notifications)
     * 
     * @param container the VBox container to add the notification to
     * @param message the notification message
     * @param type the notification type (success, error, warning, info)
     * @return the created notification HBox
     */
    public static HBox showInAppNotification(VBox container, String message, String type) {
        HBox notificationBox = new HBox();
        notificationBox.setAlignment(Pos.CENTER_LEFT);
        notificationBox.setSpacing(10);
        notificationBox.setPrefHeight(50);
        notificationBox.setPrefWidth(container.getPrefWidth() - 20);
        
        // Style based on type
        String backgroundColor;
        String textColor;
        String iconText;
        
        switch (type) {
            case "success":
                backgroundColor = "#d4edda";
                textColor = "#155724";
                iconText = "✓";
                break;
            case "error":
                backgroundColor = "#f8d7da";
                textColor = "#721c24";
                iconText = "✗";
                break;
            case "warning":
                backgroundColor = "#fff3cd";
                textColor = "#856404";
                iconText = "⚠";
                break;
            case "info":
            default:
                backgroundColor = "#d1ecf1";
                textColor = "#0c5460";
                iconText = "ℹ";
                break;
        }
        
        notificationBox.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                "-fx-border-color: " + textColor + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 10;"
        );
        
        // Create icon
        Text icon = new Text(iconText);
        icon.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 16px;");
        
        // Create message label
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px;");
        messageLabel.setWrapText(true);
        
        notificationBox.getChildren().addAll(icon, messageLabel);
        container.getChildren().add(notificationBox);
        
        return notificationBox;
    }
} 