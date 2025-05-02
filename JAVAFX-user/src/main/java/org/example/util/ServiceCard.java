package org.example.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

public class ServiceCard extends VBox {

    public ServiceCard(String title, String description, String iconName) {
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        getStyleClass().add("service-item");
        setPrefWidth(300);
        setPadding(new Insets(30, 20, 30, 20));

        // Create icon container with larger size
        Circle iconCircle = new Circle(40);
        iconCircle.getStyleClass().add("icon-circle");

        // Create icon with proper size and color
        FontIcon icon = new FontIcon(iconName); // No prefix manipulation
        // Use a try-catch block to handle invalid icon names
        try {
            // Check if it's a FontAwesome icon
            if (iconName.startsWith("fa")) {
                icon = new FontIcon(iconName);
            } else {
                // Try Bootstrap icon
                icon = new FontIcon("bi-" + iconName);
            }
        } catch (Exception e) {
            // Fallback to a safe icon that definitely exists
            System.err.println("Icon not found: " + iconName + ". Using fallback icon.");
            icon = new FontIcon("bi-box");
        }

        icon.setIconSize(30);
        icon.setIconColor(Color.web("#40cbc9")); // Match the teal color from screenshots

        // Stack the icon on the circle
        StackPane iconContainer = new StackPane();
        iconContainer.getChildren().addAll(iconCircle, icon);
        iconContainer.setPadding(new Insets(0, 0, 15, 0));

        // Create title with proper styling
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Create description with proper styling
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("description");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);

        // Add all to card
        getChildren().addAll(iconContainer, titleLabel, descLabel);

        // Add hover effect
        this.setOnMouseEntered(e -> {
            this.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(64, 203, 201, 0.4), 15, 0, 0, 10);");
        });

        this.setOnMouseExited(e -> {
            this.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 10, 0, 0, 10);");
        });
    }
}