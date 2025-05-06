package org.example.controllers;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.example.util.SessionManager;

import java.util.logging.Logger;



public class DynamicHeaderSetup {

    private static final Logger LOGGER = Logger.getLogger(DynamicHeaderSetup.class.getName());

    public static void setupHeader(HBox navButtonsHBox) {
        SessionManager session = SessionManager.getInstance();
        navButtonsHBox.getChildren().clear();

        if (session.isMedecin()) {
            // Medecin-specific navigation buttons
            addButton(navButtonsHBox, "ACCUEIL", "#navigateToHome", false);
            addButton(navButtonsHBox, "EVENEMENT", "#navigateToEvent", false);
            addButton(navButtonsHBox, "PRODUIT", "#redirectProduit", true);
            addButton(navButtonsHBox, "SUIVIE MEDICALE", "#redirectToSuivi", false);
            addButton(navButtonsHBox, "RECOMMENDATION", "#navigateToRecom", false);
            addButton(navButtonsHBox, "PLANNING", "#redirectToPlanning", false);
        } else {
            // Patient-specific navigation buttons (default)
            addButton(navButtonsHBox, "ACCUEIL", "#navigateToHome", false);
            addButton(navButtonsHBox, "EVENEMENT", "#navigateToEvent", false);
            addButton(navButtonsHBox, "PRODUIT", "#redirectProduit", true);
            addButton(navButtonsHBox, "HISTORIQUE", "#navigateToHistoriques", false);
            addButton(navButtonsHBox, "CALENDIER", "#redirectToCalendar", false);
            addButton(navButtonsHBox, "DEMANDE", "#redirectToDemande", false);
            addButton(navButtonsHBox, "RENDEZ-VOUS", "#redirectToRendezVous", false);
            addButton(navButtonsHBox, "MÉDECIN", "#viewDoctors", false);

        }
    }

    private static void addButton(HBox container, String text, String action, boolean isHighlighted) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (isHighlighted ? "#30b4b4" : "#333333") + ";");
        button.setFont(new javafx.scene.text.Font(14.0));
        button.setOnAction(e -> {
            try {
                Object userData = container.getScene().getRoot().getUserData();
                if (!(userData instanceof ShowProduitFrontController)) {
                    LOGGER.severe("Controller not set in userData");
                    return;
                }
                ShowProduitFrontController controller = (ShowProduitFrontController) userData;
                // Special handling for navigateToHome to ensure it's called directly
                if (action.equals("#navigateToHome")) {
                    controller.navigateToHome();
                } else {
                    // Use reflection for other methods
                    controller.getClass()
                            .getMethod(action.replace("#", ""), javafx.event.ActionEvent.class)
                            .invoke(controller, new javafx.event.ActionEvent(button, null));
                }
            } catch (Exception ex) {
                LOGGER.severe("Error invoking action " + action + ": " + ex.getMessage());
            }
        });
        container.getChildren().add(button);
    }
}