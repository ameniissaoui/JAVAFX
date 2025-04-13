package Controllers;

import Model.historique_traitement;
import Model.suivie_medical;
import Services.SuivServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ModifierSuiviController {
    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea commentaireArea;

    @FXML
    private Button enregistrerButton;

    @FXML
    private Button annulerButton;

    @FXML
    private Text patientInfoText;

    private suivie_medical suiviMedical;
    private historique_traitement historiqueTraitement;
    private Runnable refreshCallback;
    private SuivServices suivServices = new SuivServices();

    /**
     * Initializes the controller
     */
    public void initialize() {
        // Will be populated when suiviMedical is set
    }

    /**
     * Sets the suivi medical to edit
     * @param suiviMedical the suivi medical to edit
     */
    public void setSuiviMedical(suivie_medical suiviMedical) {
        this.suiviMedical = suiviMedical;

        if (suiviMedical != null) {
            // Populate fields with data
            commentaireArea.setText(suiviMedical.getCommentaire());

            // Convert String date to LocalDate
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(suiviMedical.getDate(), formatter);
                datePicker.setValue(date);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.WARNING, "Avertissement",
                        "La date n'est pas dans un format valide. Utilisation de la date actuelle.");
                datePicker.setValue(LocalDate.now());
            }
        }
    }

    /**
     * Sets the historique_traitement for this suivi medical
     * @param historiqueTraitement the historique_traitement to set
     */
    public void setHistoriqueTraitement(historique_traitement historiqueTraitement) {
        this.historiqueTraitement = historiqueTraitement;

        // Update UI with patient info if available
        if (historiqueTraitement != null && patientInfoText != null) {
            patientInfoText.setText("Patient: " + historiqueTraitement.getNom() + " " +
                    historiqueTraitement.getPrenom() + " | Maladie: " +
                    historiqueTraitement.getMaladies());
        }
    }

    /**
     * Sets a callback to be executed when changes are saved
     * @param callback the callback to execute
     */
    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    /**
     * Handles the save button click
     */
    @FXML
    private void enregistrerModifications(ActionEvent event) {
        try {
            // Validate fields
            if (datePicker.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une date.");
                return;
            }

            if (commentaireArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir un commentaire.");
                return;
            }

            if (suiviMedical == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun suivi médical sélectionné pour modification.");
                return;
            }

            // Format date as string
            String dateStr = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Update the suiviMedical object
            suiviMedical.setDate(dateStr);
            suiviMedical.setCommentaire(commentaireArea.getText().trim());

            // Save changes
            suivServices.update(suiviMedical);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Suivi médical modifié avec succès.");

            // Close the window
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.close();

            // Execute callback if exists
            if (refreshCallback != null) {
                refreshCallback.run();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la modification du suivi médical: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the cancel button click
     */
    @FXML
    private void annuler(ActionEvent event) {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}