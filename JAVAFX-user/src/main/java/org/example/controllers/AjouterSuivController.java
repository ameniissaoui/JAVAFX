package org.example.controllers;

import org.example.models.historique_traitement;
import org.example.models.suivie_medical;
import org.example.services.SuivServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class AjouterSuivController {
    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea commentaireArea;

    @FXML
    private Button ajouterButton;

    @FXML
    private Button retourButton;

    @FXML
    private Label nomPatientLabel;

    @FXML
    private Label prenomPatientLabel;

    @FXML
    private ImageView bilanImageView;

    private historique_traitement historiqueTraitement;
    private Runnable onSuiviAddedCallback;
    private SuivServices suivServices = new SuivServices();
    private final String storagePath = "src/main/resources/images/";

    // Constantes pour la validation
    private static final int COMMENTAIRE_MIN_LENGTH = 10;
    private static final int COMMENTAIRE_MAX_LENGTH = 500;
    private static final Pattern COMMENTAIRE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s.,;:!?()\\-'\"]+$");
    private static final int COMMENTAIRE_WARNING_LENGTH = 450; // Pour notifier l'utilisateur qu'il approche de la limite

    /**
     * Initializes the controller
     */
    public void initialize() {
        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Ajout d'un DateCell factory pour désactiver les dates passées
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        // Configurer le listener pour le commentaire avec validation en temps réel
        setupCommentaireValidator();
    }

    /**
     * Configure le listener pour la validation en temps réel du commentaire
     */
    private void setupCommentaireValidator() {
        commentaireArea.textProperty().addListener((observable, oldValue, newValue) -> {
            // Vérifier la longueur maximale
            if (newValue.length() > COMMENTAIRE_MAX_LENGTH) {
                commentaireArea.setText(oldValue);
                showAlert(Alert.AlertType.WARNING, "Limite de caractères",
                        "Le commentaire est limité à " + COMMENTAIRE_MAX_LENGTH + " caractères.");
                return;
            }

            // Avertir quand on approche de la limite
            if (newValue.length() >= COMMENTAIRE_WARNING_LENGTH && oldValue.length() < COMMENTAIRE_WARNING_LENGTH) {
                showAlert(Alert.AlertType.INFORMATION, "Information",
                        "Vous approchez de la limite de " + COMMENTAIRE_MAX_LENGTH + " caractères pour le commentaire.");
            }

            // Vérifier la présence de caractères non autorisés (optionnel, selon les besoins)
            if (!newValue.isEmpty() && !COMMENTAIRE_PATTERN.matcher(newValue).matches()) {
                commentaireArea.setStyle("-fx-border-color: red;");
            } else {
                commentaireArea.setStyle("");
            }

            // Mettre à jour l'interface avec des indicateurs visuels
            updateCommentaireIndicators(newValue);
        });
    }

    /**
     * Met à jour les indicateurs visuels pour le commentaire
     */
    private void updateCommentaireIndicators(String text) {
        // Exemple d'indication visuelle de la longueur du texte
        // Vous pourriez ajouter un Label pour afficher le nombre de caractères utilisés

        // Changer la couleur de fond en fonction de la longueur
        if (text.length() >= COMMENTAIRE_WARNING_LENGTH) {
            commentaireArea.setStyle("-fx-background-color: #FFFACD;"); // Jaune pâle
        } else if (text.length() < COMMENTAIRE_MIN_LENGTH) {
            commentaireArea.setStyle("-fx-background-color: #FFE4E1;"); // Rose pâle
        } else {
            commentaireArea.setStyle("-fx-background-color: white;");
        }
    }

    /**
     * Sets the historique_traitement for this suivi medical
     *
     * @param historiqueTraitement the historique_traitement to set
     */
    public void setHistoriqueTraitement(historique_traitement historiqueTraitement) {
        this.historiqueTraitement = historiqueTraitement;

        // Mettre à jour l'interface avec les informations du patient
        if (historiqueTraitement != null) {
            // Afficher les informations du patient
            nomPatientLabel.setText(historiqueTraitement.getNom());
            prenomPatientLabel.setText(historiqueTraitement.getPrenom());

            // Afficher le bilan s'il existe
            if (historiqueTraitement.getBilan() != null && !historiqueTraitement.getBilan().isEmpty()) {
                File bilanFile = new File(storagePath + historiqueTraitement.getBilan());
                if (bilanFile.exists()) {
                    String extension = getFileExtension(bilanFile).toLowerCase();
                    if (isImageFile(extension)) {
                        try {
                            Image image = new Image(bilanFile.toURI().toString());
                            bilanImageView.setImage(image);
                        } catch (Exception e) {
                            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                            try {
                                // Charger une image par défaut en cas d'erreur
                                Image placeholderIcon = new Image(getClass().getResourceAsStream("/images/error_icon.png"));
                                if (placeholderIcon != null) {
                                    bilanImageView.setImage(placeholderIcon);
                                }
                            } catch (Exception ex) {
                                // Si même l'image d'erreur ne se charge pas, ignorer
                            }
                        }
                    } else {
                        // Si ce n'est pas une image, afficher un message ou une icône
                        try {
                            Image placeholderIcon = new Image(getClass().getResourceAsStream("/images/file_icon.png"));
                            if (placeholderIcon != null) {
                                bilanImageView.setImage(placeholderIcon);
                            }
                        } catch (Exception e) {
                            System.err.println("Icône de fichier non disponible: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Vérifie si le fichier est une image
     */
    private boolean isImageFile(String extension) {
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("gif") || extension.equals("bmp");
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    /**
     * Sets a callback to be executed when a suivi is successfully added
     *
     * @param callback the callback to execute
     */
    public void setOnSuiviAddedCallback(Runnable callback) {
        this.onSuiviAddedCallback = callback;
    }

    /**
     * Validates all input fields
     */
    private boolean validateInputs() {
        StringBuilder erreurs = new StringBuilder();

        // Vérifier la date
        if (datePicker.getValue() == null) {
            erreurs.append("- Veuillez sélectionner une date.\n");
        } else {
            LocalDate selectedDate = datePicker.getValue();
            LocalDate today = LocalDate.now();
            LocalDate maxFutureDate = today.plusYears(1); // Limite d'un an dans le futur

            if (selectedDate.isBefore(today)) {
                erreurs.append("- La date ne peut pas être antérieure à aujourd'hui.\n");
            } else if (selectedDate.isAfter(maxFutureDate)) {
                erreurs.append("- La date ne peut pas être fixée à plus d'un an dans le futur.\n");
            }
        }

        // Vérifier le commentaire
        String commentaire = commentaireArea.getText().trim();
        if (commentaire.isEmpty()) {
            erreurs.append("- Veuillez saisir un commentaire.\n");
        } else {
            if (commentaire.length() < COMMENTAIRE_MIN_LENGTH) {
                erreurs.append("- Le commentaire doit contenir au moins " + COMMENTAIRE_MIN_LENGTH + " caractères.\n");
            }

            if (commentaire.length() > COMMENTAIRE_MAX_LENGTH) {
                erreurs.append("- Le commentaire ne doit pas dépasser " + COMMENTAIRE_MAX_LENGTH + " caractères.\n");
            }

            // Vérifier les caractères autorisés
            if (!COMMENTAIRE_PATTERN.matcher(commentaire).matches()) {
                erreurs.append("- Le commentaire contient des caractères non autorisés.\n");
            }

            // Vérifier la présence d'au moins une phrase complète
            if (!commentaire.matches(".*[.!?].*")) {
                erreurs.append("- Le commentaire doit contenir au moins une phrase complète terminée par un point, un point d'exclamation ou un point d'interrogation.\n");
            }
        }

        // Vérifier l'historique
        if (historiqueTraitement == null) {
            erreurs.append("- Aucun historique de traitement sélectionné.\n");
        }

        // Afficher les erreurs s'il y en a
        if (erreurs.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreurs de validation", erreurs.toString());
            return false;
        }

        return true;
    }

    /**
     * Nettoie et sanitize le commentaire avant enregistrement
     */
    private String sanitizeCommentaire(String commentaire) {
        if (commentaire == null) {
            return "";
        }

        // Supprimer les caractères potentiellement dangereux
        String sanitized = commentaire.replaceAll("[<>]", "");

        // Normaliser les espaces multiples
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        // S'assurer que la première lettre est en majuscule et que le commentaire se termine par un point
        if (!sanitized.isEmpty()) {
            sanitized = Character.toUpperCase(sanitized.charAt(0)) + sanitized.substring(1);
            if (!sanitized.matches(".*[.!?]$")) {
                sanitized = sanitized + ".";
            }
        }

        return sanitized;
    }

    /**
     * Handles the save button click
     */
    @FXML
    private void ajouterSuivi(ActionEvent event) {
        try {
            // Validate fields
            if (!validateInputs()) {
                return;
            }

            // Format date as string
            String dateStr = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Nettoyer le commentaire avant de le sauvegarder
            String commentaireNettoye = sanitizeCommentaire(commentaireArea.getText().trim());

            // Create and save the suivi medical
            suivie_medical nouveauSuivi = new suivie_medical(
                    dateStr,
                    commentaireNettoye,
                    historiqueTraitement  // Passer l'objet historique directement
            );

            suivServices.add(nouveauSuivi);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Suivi médical ajouté avec succès pour le patient " +
                    historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom());

            // Si le callback est défini, l'executer pour rafraîchir la liste des suivis
            if (onSuiviAddedCallback != null) {
                onSuiviAddedCallback.run();
            }

            // Rediriger vers la liste des suivis médicaux
            redirectToListeSuivi();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de l'ajout du suivi médical: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rediriger vers la page de liste des suivis médicaux
     */
    private void redirectToListeSuivi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liste_suivi.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            ListeSuiviController controller = loader.getController();
            controller.setHistoriqueTraitement(historiqueTraitement);

            // Remplacer la scène actuelle
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.setTitle("Suivis Médicaux - " + historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom());
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la liste des suivis médicaux: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the cancel button click
     */
    @FXML
    private void annuler(ActionEvent event) {
        try {
            // Confirmation si un commentaire a été saisi
            if (!commentaireArea.getText().trim().isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation");
                confirmAlert.setHeaderText("Êtes-vous sûr de vouloir annuler ?");
                confirmAlert.setContentText("Les données saisies seront perdues.");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        // Rediriger vers la liste des suivis
                        navigateToListeSuivi();
                    }
                });
            } else {
                // Si aucun commentaire, rediriger directement
                navigateToListeSuivi();
            }
        } catch (Exception e) {
            // En cas d'erreur, simplement fermer la fenêtre
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Navigation vers la liste des suivis
     */
    private void navigateToListeSuivi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liste_suivi.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            ListeSuiviController controller = loader.getController();
            controller.setHistoriqueTraitement(historiqueTraitement);

            // Remplacer la scène actuelle
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.setTitle("Suivis Médicaux - " + historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom());
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            // En cas d'erreur, simplement fermer la fenêtre
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.close();
            e.printStackTrace();
        }
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