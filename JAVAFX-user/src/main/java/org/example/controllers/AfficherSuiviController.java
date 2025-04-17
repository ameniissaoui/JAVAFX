package org.example.controllers;

import org.example.models.historique_traitement;
import org.example.models.suivie_medical;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AfficherSuiviController implements Initializable {

    @FXML
    private Label idLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label patientLabel;

    @FXML
    private TextArea commentaireTextArea;

    @FXML
    private Button fermerButton;

    private suivie_medical suiviMedical;
    private historique_traitement historiqueTraitement;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the controller
        System.out.println("AfficherSuiviController initialized");

        // Set up the fermer (close) button action
        fermerButton.setOnAction(event -> {
            System.out.println("Fermer button clicked");
            Stage stage = (Stage) fermerButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setSuiviMedical(suivie_medical suiviMedical) {
        try {
            this.suiviMedical = suiviMedical;

            // Update UI with suivi_medical data
            if (suiviMedical != null) {
                System.out.println("Setting suiviMedical data: ID=" + suiviMedical.getId());

                // Définir l'ID
                if (idLabel != null) {
                    idLabel.setText(String.valueOf(suiviMedical.getId()));
                } else {
                    System.err.println("Error: idLabel is null");
                }

                // Définir la date
                if (dateLabel != null) {
                    dateLabel.setText(suiviMedical.getDate());
                } else {
                    System.err.println("Error: dateLabel is null");
                }

                // Définir le commentaire
                if (commentaireTextArea != null) {
                    commentaireTextArea.setText(suiviMedical.getCommentaire());
                    commentaireTextArea.setEditable(false); // Make it read-only
                } else {
                    System.err.println("Error: commentaireTextArea is null");
                }

                // Si nous avons déjà l'historique dans l'objet suivi_medical
                if (suiviMedical.getHistorique() != null && patientLabel != null) {
                    historique_traitement historique = suiviMedical.getHistorique();
                    patientLabel.setText(historique.getNom() + " " + historique.getPrenom());
                }
                // Sinon, utiliser l'historique défini séparément si disponible
                else if (historiqueTraitement != null && patientLabel != null) {
                    patientLabel.setText(historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom());
                }
                else {
                    System.err.println("Warning: Cannot set patient name, missing references");
                    if (patientLabel != null) {
                        patientLabel.setText("Information non disponible");
                    }
                }
            } else {
                System.err.println("Error: suiviMedical is null");
            }
        } catch (Exception e) {
            System.err.println("Exception in setSuiviMedical: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setHistoriqueTraitement(historique_traitement historiqueTraitement) {
        try {
            this.historiqueTraitement = historiqueTraitement;

            if (historiqueTraitement != null && patientLabel != null && suiviMedical == null) {
                patientLabel.setText(historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom());
            }
        } catch (Exception e) {
            System.err.println("Exception in setHistoriqueTraitement: " + e.getMessage());
            e.printStackTrace();
        }
    }
}