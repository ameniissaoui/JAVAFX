package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Planning;
import org.example.services.PlanningDAO;
import org.example.util.MaConnexion;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AjouterPlanningController implements Initializable {

    @FXML
    private ComboBox<String> cmbJour;

    @FXML
    private ComboBox<String> cmbHeureDebut;

    @FXML
    private ComboBox<String> cmbHeureFin;

    @FXML
    private Button btnEnregistrer;

    @FXML
    private Button btnAnnuler;

    private PlanningDAO planningDAO = new PlanningDAO();
    private PlanningViewController parentController;
    private static final int MAX_RETRIES = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupButtons();
    }

    public void setParentController(PlanningViewController controller) {
        this.parentController = controller;
    }

    private void setupComboBoxes() {
        // Remplir la liste des jours
        cmbJour.getItems().addAll("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");
        cmbJour.setValue("Lundi"); // Valeur par défaut

        // Remplir les heures
        for (int h = 8; h <= 20; h++) {
            cmbHeureDebut.getItems().add(String.format("%02d:00", h));
            cmbHeureDebut.getItems().add(String.format("%02d:30", h));
            cmbHeureFin.getItems().add(String.format("%02d:00", h));
            cmbHeureFin.getItems().add(String.format("%02d:30", h));
        }
        cmbHeureFin.getItems().add("21:00");

        // Valeurs par défaut
        cmbHeureDebut.setValue("08:00");
        cmbHeureFin.setValue("17:00");
    }

    private void setupButtons() {
        btnEnregistrer.setOnAction(e -> savePlanning());
        btnAnnuler.setOnAction(e -> closeWindow());
    }

    private void savePlanning() {
        if (validateFields()) {
            try {
                Planning planning = new Planning();
                planning.setJour(cmbJour.getValue());
                planning.setHeuredebut(LocalTime.parse(cmbHeureDebut.getValue()));
                planning.setHeurefin(LocalTime.parse(cmbHeureFin.getValue()));

                // Attempt to save with retry logic
                saveWithRetry(planning, 0);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    private void saveWithRetry(Planning planning, int retryCount) {
        try {
            // Test connection first
            if (!planningDAO.testConnection()) {
                // Force reconnection
                MaConnexion.getInstance().reconnect();
            }
            
            // Now save the planning
            planningDAO.savePlanning(planning);
            showAlert("Succès", "Planning ajouté avec succès", Alert.AlertType.INFORMATION);

            // Rafraîchir la vue parent
            if (parentController != null) {
                parentController.refreshData();
            }

            closeWindow();
        } catch (SQLException e) {
            System.err.println("Error in saveWithRetry: " + e.getMessage() + " (try " + (retryCount + 1) + ")");
            if (retryCount < MAX_RETRIES) {
                // Try reconnecting
                try {
                    MaConnexion.getInstance().reconnect();
                    // Wait a little before retrying
                    Thread.sleep(500);
                    // Recursive retry
                    saveWithRetry(planning, retryCount + 1);
                } catch (Exception ex) {
                    showAlert("Erreur de connexion", 
                              "Impossible de se reconnecter à la base de données: " + ex.getMessage(), 
                              Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Erreur", "Échec après " + MAX_RETRIES + " tentatives: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateFields() {
        if (cmbJour.getValue() == null || cmbJour.getValue().trim().isEmpty()) {
            showAlert("Validation", "Le jour est obligatoire", Alert.AlertType.ERROR);
            return false;
        }

        if (cmbHeureDebut.getValue() == null || cmbHeureFin.getValue() == null) {
            showAlert("Validation", "Les heures sont obligatoires", Alert.AlertType.ERROR);
            return false;
        }

        LocalTime debut = LocalTime.parse(cmbHeureDebut.getValue());
        LocalTime fin = LocalTime.parse(cmbHeureFin.getValue());

        if (fin.isBefore(debut) || fin.equals(debut)) {
            showAlert("Validation", "L'heure de fin doit être après l'heure de début", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void closeWindow() {
        ((Stage) btnAnnuler.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }
}