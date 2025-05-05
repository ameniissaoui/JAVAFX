package org.example.controllers;

import org.example.models.historique_traitement;
import org.example.models.suivie_medical;
import org.example.services.SuivServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Constants for validation
    private static final int MIN_COMMENTAIRE_LENGTH = 10;
    private static final int MAX_COMMENTAIRE_LENGTH = 1000;
    private static final int MAX_DAYS_IN_FUTURE = 30; // Maximum days allowed in the future
    private static final int MAX_DAYS_IN_PAST = 365 * 5; // Maximum days allowed in the past (5 years)

    private suivie_medical suiviMedical;
    private historique_traitement historiqueTraitement;
    private Runnable refreshCallback;
    private SuivServices suivServices = new SuivServices();

    /**
     * Initializes the controller
     */
    public void initialize() {
        // Set up listeners for validation feedback
        setupValidationListeners();
    }

    /**
     * Sets up listeners for immediate validation feedback
     */
    private void setupValidationListeners() {
        // Validate date selection
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate(newValue);
        });

        // Validate commentaire as user types
        commentaireArea.textProperty().addListener((observable, oldValue, newValue) -> {
            validateCommentaire(newValue);
        });

        // Add date format validation
        datePicker.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            validateDate(selectedDate);
        });
    }

    /**
     * Validates the selected date
     * @param date The date to validate
     * @return True if the date is valid, false otherwise
     */
    private boolean validateDate(LocalDate date) {
        if (date == null) {
            updateDatePickerStyle(false);
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate maxFutureDate = today.plusDays(MAX_DAYS_IN_FUTURE);
        LocalDate maxPastDate = today.minusDays(MAX_DAYS_IN_PAST);

        // Check if date is too far in the future
        if (date.isAfter(maxFutureDate)) {
            updateDatePickerStyle(false);
            showTooltip(datePicker, "La date ne peut pas être plus de " + MAX_DAYS_IN_FUTURE + " jours dans le futur.");
            return false;
        }

        // Check if date is too far in the past
        if (date.isBefore(maxPastDate)) {
            updateDatePickerStyle(false);
            showTooltip(datePicker, "La date ne peut pas être plus de " + (MAX_DAYS_IN_PAST / 365) + " ans dans le passé.");
            return false;
        }

        // Date is valid
        updateDatePickerStyle(true);
        hideTooltip(datePicker);
        return true;
    }

    /**
     * Validates the commentaire text
     * @param commentaire The commentaire to validate
     * @return True if the commentaire is valid, false otherwise
     */
    private boolean validateCommentaire(String commentaire) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            updateCommentaireStyle(false);
            showTooltip(commentaireArea, "Le commentaire ne peut pas être vide.");
            return false;
        }

        if (commentaire.trim().length() < MIN_COMMENTAIRE_LENGTH) {
            updateCommentaireStyle(false);
            showTooltip(commentaireArea, "Le commentaire doit contenir au moins " + MIN_COMMENTAIRE_LENGTH + " caractères.");
            return false;
        }

        if (commentaire.trim().length() > MAX_COMMENTAIRE_LENGTH) {
            updateCommentaireStyle(false);
            showTooltip(commentaireArea, "Le commentaire ne peut pas dépasser " + MAX_COMMENTAIRE_LENGTH + " caractères.");
            return false;
        }

        // Check for potentially dangerous content (SQL injection, scripts, etc.)
        if (containsDangerousContent(commentaire)) {
            updateCommentaireStyle(false);
            showTooltip(commentaireArea, "Le commentaire contient des caractères non autorisés.");
            return false;
        }

        // Commentaire is valid
        updateCommentaireStyle(true);
        hideTooltip(commentaireArea);
        return true;
    }

    /**
     * Checks if text contains potentially dangerous content
     * @param text The text to check
     * @return True if dangerous content is found, false otherwise
     */
    private boolean containsDangerousContent(String text) {
        // Check for SQL injection attempts
        String sqlPattern = "(?i).*(select|insert|update|delete|drop|alter|create|exec|union|where|from).*";
        if (text.matches(sqlPattern)) {
            return true;
        }

        // Check for script tags or other HTML/JavaScript injection
        if (text.contains("<script") || text.contains("javascript:") || text.contains("</script>")) {
            return true;
        }

        // Check for common special characters that might indicate injection attempts
        if (text.contains("--") || text.contains("/*") || text.contains("*/")) {
            return true;
        }

        return false;
    }

    /**
     * Shows a tooltip for a control
     * @param control The control to show the tooltip for
     * @param message The tooltip message
     */
    private void showTooltip(javafx.scene.control.Control control, String message) {
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(message);
        control.setTooltip(tooltip);
    }

    /**
     * Hides the tooltip for a control
     * @param control The control to hide the tooltip for
     */
    private void hideTooltip(javafx.scene.control.Control control) {
        control.setTooltip(null);
    }

    /**
     * Updates the style of the date picker based on validation
     * @param isValid True if the date is valid, false otherwise
     */
    private void updateDatePickerStyle(boolean isValid) {
        if (isValid) {
            datePicker.setStyle("-fx-border-color: green;");
        } else {
            datePicker.setStyle("-fx-border-color: red;");
        }
    }

    /**
     * Updates the style of the commentaire area based on validation
     * @param isValid True if the commentaire is valid, false otherwise
     */
    private void updateCommentaireStyle(boolean isValid) {
        if (isValid) {
            commentaireArea.setStyle("-fx-border-color: green;");
        } else {
            commentaireArea.setStyle("-fx-border-color: red;");
        }
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
                validateDate(date); // Validate the date after setting it
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.WARNING, "Avertissement",
                        "La date n'est pas dans un format valide. Utilisation de la date actuelle.");
                datePicker.setValue(LocalDate.now());
                validateDate(LocalDate.now()); // Validate the current date
            }

            // Validate the commentaire after setting it
            validateCommentaire(suiviMedical.getCommentaire());
        } else {
            // If no suivi medical is provided, set default values
            datePicker.setValue(LocalDate.now());
            commentaireArea.setText("");
            validateDate(LocalDate.now());
            validateCommentaire("");
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
            // Sanitize data before displaying
            String nom = sanitizeText(historiqueTraitement.getNom());
            String prenom = sanitizeText(historiqueTraitement.getPrenom());
            String maladies = sanitizeText(historiqueTraitement.getMaladies());

            patientInfoText.setText("Patient: " + nom + " " + prenom + " | Maladie: " + maladies);
        }
    }

    /**
     * Sanitizes text to prevent XSS attacks
     * @param text The text to sanitize
     * @return The sanitized text
     */
    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }

        // Replace HTML special characters with their entity equivalents
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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
            // Perform comprehensive validation before saving
            if (!validateAllFields()) {
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

            // Log the update operation
            logOperation("Modification de suivi médical pour patient: " +
                    (historiqueTraitement != null ? historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom() : "Inconnu"));

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

            // Log the error
            logError("Erreur lors de la modification du suivi médical: " + e.getMessage());
        }
    }

    /**
     * Validates all fields before saving
     * @return True if all fields are valid, false otherwise
     */
    private boolean validateAllFields() {
        boolean isDateValid = validateDate(datePicker.getValue());
        boolean isCommentaireValid = validateCommentaire(commentaireArea.getText());

        if (!isDateValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez sélectionner une date valide. La date ne peut pas être trop éloignée dans le passé ou le futur.");
            return false;
        }

        if (!isCommentaireValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez saisir un commentaire valide d'au moins " + MIN_COMMENTAIRE_LENGTH +
                            " caractères et ne dépassant pas " + MAX_COMMENTAIRE_LENGTH + " caractères.");
            return false;
        }

        return true;
    }

    /**
     * Logs an operation for audit purposes
     * @param message The operation message to log
     */
    private void logOperation(String message) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("[" + timestamp + "] " + message);

        // In a real application, this would write to a log file or database
    }

    /**
     * Logs an error for debugging purposes
     * @param message The error message to log
     */
    private void logError(String message) {
        LocalDate now = LocalDate.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.err.println("[" + timestamp + "] ERROR: " + message);

        // In a real application, this would write to an error log file or database
    }

    /**
     * Handles the cancel button click
     */
    @FXML
    private void annuler(ActionEvent event) {
        // Check if there are unsaved changes
        boolean hasChanges = false;

        if (suiviMedical != null) {
            // Check if date has changed
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate originalDate = LocalDate.parse(suiviMedical.getDate(), formatter);
                if (!originalDate.equals(datePicker.getValue())) {
                    hasChanges = true;
                }
            } catch (DateTimeParseException e) {
                // Ignore parsing errors on cancel
            }

            // Check if commentaire has changed
            if (!suiviMedical.getCommentaire().equals(commentaireArea.getText())) {
                hasChanges = true;
            }
        }

        // If there are unsaved changes, ask for confirmation
        if (hasChanges) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Modifications non enregistrées");
            confirmAlert.setContentText("Vous avez des modifications non enregistrées. Êtes-vous sûr de vouloir quitter sans enregistrer?");

            if (confirmAlert.showAndWait().get().getButtonData().isCancelButton()) {
                return; // User canceled the close operation
            }
        }

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

