package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Planning;
import org.example.services.PlanningDAO;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PlanningEditController implements Initializable {

    @FXML
    private Label headerLabel;

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

    private Planning planning;
    private PlanningDAO planningDAO = new PlanningDAO();
    private PlanningViewController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupButtons();
    }

    public void setPlanning(Planning planning) {
        this.planning = planning;

        if (planning != null) {
            headerLabel.setText("Modifier le Planning");
            cmbJour.setValue(planning.getJour());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            cmbHeureDebut.setValue(planning.getHeuredebut().format(formatter));
            cmbHeureFin.setValue(planning.getHeurefin().format(formatter));
        } else {
            headerLabel.setText("Créer un Nouveau Planning");
            cmbJour.setValue("Lundi");
            cmbHeureDebut.setValue("08:00");
            cmbHeureFin.setValue("17:00");
        }
    }

    public void setParentController(PlanningViewController controller) {
        this.parentController = controller;
    }

    private void setupComboBoxes() {
        // Remplir la liste des jours
        cmbJour.getItems().addAll("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

        // Remplir les heures
        for (int h = 8; h <= 20; h++) {
            cmbHeureDebut.getItems().add(String.format("%02d:00", h));
            cmbHeureDebut.getItems().add(String.format("%02d:30", h));
            cmbHeureFin.getItems().add(String.format("%02d:00", h));
            cmbHeureFin.getItems().add(String.format("%02d:30", h));
        }
        cmbHeureFin.getItems().add("21:00");
    }

    private void setupButtons() {
        btnEnregistrer.setOnAction(e -> savePlanning());
        btnAnnuler.setOnAction(e -> closeWindow());
    }

    private void savePlanning() {
        if (validateFields()) {
            try {
                if (planning == null) {
                    planning = new Planning();
                }

                planning.setJour(cmbJour.getValue());
                planning.setHeuredebut(LocalTime.parse(cmbHeureDebut.getValue()));
                planning.setHeurefin(LocalTime.parse(cmbHeureFin.getValue()));

                if (planning.getId() == 0) {
                    // Nouveau planning
                    planningDAO.savePlanning(planning);
                    showAlert("Succès", "Planning ajouté avec succès", Alert.AlertType.INFORMATION);
                } else {
                    // Mise à jour
                    planningDAO.updatePlanning(planning);
                    showAlert("Succès", "Planning mis à jour avec succès", Alert.AlertType.INFORMATION);
                }

                // Rafraîchir la vue parent
                if (parentController != null) {
                    parentController.refreshData();
                }

                closeWindow();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
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