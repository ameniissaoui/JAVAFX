package org.example.util;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {

    /**
     * Creates a fade-in animation for a node
     */
    public static FadeTransition fadeIn(Node node, int durationMillis) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMillis), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
        return fadeTransition;
    }

    /**
     * Creates a slide-in-from-bottom animation for a node
     */
    public static TranslateTransition slideInFromBottom(Node node, int durationMillis) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(durationMillis), node);
        translateTransition.setFromY(50);
        translateTransition.setToY(0);
        translateTransition.play();
        return translateTransition;
    }

    /**
     * Creates a rotation animation for a node
     */
    public static RotateTransition rotateTransition(Node node, double angle, int durationMillis) {
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(durationMillis), node);
        rotateTransition.setByAngle(angle);
        return rotateTransition;
    }
}