package org.example.util;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class Carousel extends StackPane {

    private HBox indicators = new HBox();
    private int currentSlideIndex = 0;
    private Button nextButton;
    private Button prevButton;

    private static final double USE_COMPUTED_SIZE = Region.USE_COMPUTED_SIZE;

    private static class Slide {
        Pane pane;

        Slide(Pane pane) {
            this.pane = pane;
        }
    }

    private java.util.List<Slide> slides = new java.util.ArrayList<>();

    public Carousel() {
        getStyleClass().add("carousel");
        setMinHeight(500);  // 70vh equivalent for most screens

        // Set up indicators
        indicators.getStyleClass().add("carousel-indicators");
        indicators.setAlignment(Pos.CENTER);
        indicators.setPadding(new Insets(0, 0, 20, 0));
        indicators.setSpacing(8);

        // Add navigation buttons with web-style design
        nextButton = createNavButton("›", true);
        prevButton = createNavButton("‹", false);

        // Add all elements to the carousel
        getChildren().addAll(indicators, nextButton, prevButton);
        setAlignment(indicators, Pos.BOTTOM_CENTER);
        indicators.setTranslateY(-30);

        // Auto-slide every 5 seconds
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> showNextSlide())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private Button createNavButton(String text, boolean isNext) {
        Button button = new Button(text);
        button.getStyleClass().addAll("carousel-nav-button",
                isNext ? "carousel-control-next-icon" : "carousel-control-prev-icon");

        button.setOnAction(e -> {
            if (isNext) {
                showNextSlide();
            } else {
                showPrevSlide();
            }
        });

        // Position the button on the right or left side
        StackPane.setAlignment(button, isNext ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // Add some margin
        if (isNext) {
            button.setTranslateX(-15);
        } else {
            button.setTranslateX(15);
        }

        return button;
    }

    public void addSlide(String title, String description, Image backgroundImage, String buttonText) {
        // Create the slide content (carousel-item in web terms)
        StackPane slide = new StackPane();
        slide.getStyleClass().add("carousel-slide");
        slide.setPrefWidth(USE_COMPUTED_SIZE);
        slide.setMaxWidth(Double.MAX_VALUE);
        slide.setMinHeight(500);  // 70vh equivalent for most screens

        // Background image - make it full width and height
        ImageView background = new ImageView(backgroundImage);
        background.getStyleClass().add("carousel-image");
        background.setFitWidth(1200);
        background.setFitHeight(500);
        background.setPreserveRatio(false);

        // Semi-transparent overlay for the entire slide
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        overlay.setPrefWidth(USE_COMPUTED_SIZE);
        overlay.setMaxWidth(Double.MAX_VALUE);
        overlay.setPrefHeight(USE_COMPUTED_SIZE);
        overlay.setMaxHeight(Double.MAX_VALUE);

        // Content container - positioned at the bottom center
        VBox contentContainer = new VBox(15);
        contentContainer.getStyleClass().add("carousel-content-container");
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setMaxWidth(800);

        // Position the content at the bottom center of the slide
        StackPane.setAlignment(contentContainer, Pos.BOTTOM_CENTER);
        StackPane.setMargin(contentContainer, new Insets(0, 0, 50, 0));

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");
        titleLabel.setMaxWidth(700);
        titleLabel.setAlignment(Pos.CENTER);

        // Description
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("carousel-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(700);
        descLabel.setAlignment(Pos.CENTER);

        // Button
        Button button = new Button(buttonText);
        button.getStyleClass().add("carousel-button");

        // Add components to the content container
        contentContainer.getChildren().addAll(titleLabel, descLabel, button);

        // Add all elements to the slide
        slide.getChildren().addAll(background, overlay, contentContainer);

        // Create indicator dot
        Circle indicator = new Circle(6);
        indicator.getStyleClass().add("carousel-indicator");
        if (slides.isEmpty()) {
            indicator.getStyleClass().add("active");
        }

        // Make indicator clickable to navigate to specific slide
        final int slideIndex = slides.size();
        indicator.setOnMouseClicked(e -> showSlide(slideIndex));

        indicators.getChildren().add(indicator);

        // Set the initial opacity to 0 except for the first slide
        if (!slides.isEmpty()) {
            slide.setOpacity(0);
        }

        slides.add(new Slide(slide));
        if (slides.size() == 1) {
            getChildren().add(0, slide); // Add the first slide
        }
    }

    private void showNextSlide() {
        int nextSlideIndex = (currentSlideIndex + 1) % slides.size();
        showSlide(nextSlideIndex);
    }

    private void showPrevSlide() {
        int prevSlideIndex = (currentSlideIndex - 1 + slides.size()) % slides.size();
        showSlide(prevSlideIndex);
    }

    private void showSlide(int index) {
        if (slides.size() <= 1 || index == currentSlideIndex) return;

        Slide currentSlide = slides.get(currentSlideIndex);
        Slide nextSlide = slides.get(index);

        // Fade transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), currentSlide.pane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), nextSlide.pane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> {
            getChildren().remove(currentSlide.pane);
            getChildren().add(0, nextSlide.pane);
            fadeIn.play();
        });

        // Update indicators to match web-style active state
        Circle currentIndicator = (Circle) indicators.getChildren().get(currentSlideIndex);
        Circle nextIndicator = (Circle) indicators.getChildren().get(index);

        currentIndicator.getStyleClass().remove("active");
        nextIndicator.getStyleClass().add("active");

        fadeOut.play();
        currentSlideIndex = index;
    }
}