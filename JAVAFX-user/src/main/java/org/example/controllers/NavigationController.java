package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de navigation pour gérer les transitions entre les vues
 */
public class NavigationController {

    static Stage mainStage;
    private static Map<String, Object> controllers = new HashMap<>();
    private static Map<String, Object> sharedData = new HashMap<>();

    /**
     * Définit la scène principale de l'application
     * @param stage La scène principale
     */
    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    /**
     * Navigue vers une vue spécifiée dans la fenêtre principale
     * @param fxmlPath Chemin du fichier FXML
     * @return Le contrôleur associé à la vue
     */
    public static Object navigateTo(String fxmlPath) {
        try {
            if (mainStage == null) {
                System.err.println("ERREUR: Stage principal non défini. Appelez setMainStage d'abord.");
                return null;
            }

            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            controllers.put(fxmlPath, controller);

            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.show();

            return controller;
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ouvre une fenêtre modale avec la vue spécifiée
     * @param fxmlPath Chemin du fichier FXML
     * @param title Titre de la fenêtre
     * @return Le contrôleur associé à la vue
     */
    public static Object openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            Stage modalStage = new Stage();
            modalStage.setTitle(title);
            modalStage.initModality(Modality.APPLICATION_MODAL);
            if (mainStage != null) {
                modalStage.initOwner(mainStage);
            }

            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            return controller;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre modale " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Stocke une donnée partagée entre les contrôleurs
     * @param key Clé de la donnée
     * @param value Valeur de la donnée
     */
    public static void setSharedData(String key, Object value) {
        sharedData.put(key, value);
    }

    /**
     * Récupère une donnée partagée
     * @param key Clé de la donnée
     * @return La donnée ou null si elle n'existe pas
     */
    public static Object getSharedData(String key) {
        return sharedData.get(key);
    }

    /**
     * Supprime une donnée partagée
     * @param key Clé de la donnée à supprimer
     */
    public static void removeSharedData(String key) {
        sharedData.remove(key);
    }

    /**
     * Récupère un contrôleur précédemment chargé
     * @param fxmlPath Chemin du fichier FXML associé au contrôleur
     * @return Le contrôleur ou null s'il n'existe pas
     */
    public static Object getController(String fxmlPath) {
        return controllers.get(fxmlPath);
    }

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient_calendar.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de navigation");
            alert.setContentText("Impossible de charger le calendrier: " + e.getMessage());
            alert.showAndWait();
        }
    }

}