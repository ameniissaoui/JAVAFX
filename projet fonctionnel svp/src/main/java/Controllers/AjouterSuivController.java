package Controllers;

import Model.historique_traitement;
import Model.suivie_medical;
import Services.SuivServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    private final String storagePath = "src/main/resources/uploads/";

    /**
     * Initializes the controller
     */
    public void initialize() {
        // Set default date to today
        datePicker.setValue(LocalDate.now());
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
                    if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                            extension.equals("gif") || extension.equals("bmp")) {
                        try {
                            Image image = new Image(bilanFile.toURI().toString());
                            bilanImageView.setImage(image);
                        } catch (Exception e) {
                            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
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
     * Handles the save button click
     */
    @FXML
    private void ajouterSuivi(ActionEvent event) {
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

            if (historiqueTraitement == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun historique de traitement sélectionné.");
                return;
            }

            // Format date as string
            String dateStr = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Create and save the suivi medical
            suivie_medical nouveauSuivi = new suivie_medical(
                    dateStr,
                    commentaireArea.getText().trim(),
                    historiqueTraitement
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/liste_suivi.fxml"));
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
            // Rediriger vers la liste des suivis
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/liste_suivi.fxml"));
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