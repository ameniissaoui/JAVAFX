package Controllers;

import Services.HisServices;
import Model.historique_traitement;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.UUID;

public class ModifierHistController implements Initializable {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    private TextField txtMaladie;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtType;

    @FXML
    private Label lblFichierBilan;

    @FXML
    private Button btnChoisirBilan;

    @FXML
    private ImageView imgPreviewBilan;

    @FXML
    private Button btnEnregistrer;

    @FXML
    private Button btnAnnuler;

    private HisServices hisServices;
    private historique_traitement historiqueActuel;
    private Runnable refreshCallback;
    private File selectedBilanFile;
    private boolean bilanFileChanged = false;
    private String storagePath = "src/main/resources/uploads/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hisServices = new HisServices();

        // S'assurer que le dossier d'uploads existe
        try {
            Files.createDirectories(Paths.get(storagePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialiser l'aperçu d'image comme invisible par défaut
        if (imgPreviewBilan != null) {
            imgPreviewBilan.setVisible(false);
            imgPreviewBilan.setFitWidth(200);
            imgPreviewBilan.setPreserveRatio(true);
        }
    }

    @FXML
    private void choisirFichierBilan() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier bilan");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File file = fileChooser.showOpenDialog(btnChoisirBilan.getScene().getWindow());
        if (file != null) {
            selectedBilanFile = file;
            bilanFileChanged = true;
            lblFichierBilan.setText(file.getName());

            // Afficher une prévisualisation si c'est une image
            try {
                String extension = getFileExtension(file).toLowerCase();
                if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")
                        || extension.equals("gif") || extension.equals("bmp")) {
                    if (imgPreviewBilan != null) {
                        Image image = new Image(file.toURI().toString());
                        imgPreviewBilan.setImage(image);
                        imgPreviewBilan.setVisible(true);
                    }
                } else {
                    if (imgPreviewBilan != null) {
                        imgPreviewBilan.setVisible(false);
                    }
                }
            } catch (Exception e) {
                if (imgPreviewBilan != null) {
                    imgPreviewBilan.setVisible(false);
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * Définit l'historique à modifier et remplit le formulaire
     */
    public void setHistoriqueTraitement(historique_traitement historique) {
        this.historiqueActuel = historique;
        remplirFormulaire();
    }

    /**
     * Définit un callback pour rafraîchir la liste après modification
     */
    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void remplirFormulaire() {
        if (historiqueActuel != null) {
            txtNom.setText(historiqueActuel.getNom());
            txtPrenom.setText(historiqueActuel.getPrenom());
            txtMaladie.setText(historiqueActuel.getMaladies());
            txtDescription.setText(historiqueActuel.getDescription());
            txtType.setText(historiqueActuel.getType_traitement());

            // Gérer l'affichage du bilan existant
            if (historiqueActuel.getBilan() != null && !historiqueActuel.getBilan().isEmpty()) {
                lblFichierBilan.setText(historiqueActuel.getBilan());

                // Tenter de charger l'image si c'est un format supporté
                try {
                    File bilanFile = new File(storagePath + historiqueActuel.getBilan());
                    if (bilanFile.exists()) {
                        String extension = getFileExtension(bilanFile).toLowerCase();
                        if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")
                                || extension.equals("gif") || extension.equals("bmp")) {
                            if (imgPreviewBilan != null) {
                                Image image = new Image(bilanFile.toURI().toString());
                                imgPreviewBilan.setImage(image);
                                imgPreviewBilan.setVisible(true);
                            }
                        } else {
                            if (imgPreviewBilan != null) {
                                imgPreviewBilan.setVisible(false);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (imgPreviewBilan != null) {
                        imgPreviewBilan.setVisible(false);
                    }
                }
            } else {
                lblFichierBilan.setText("Aucun fichier");
            }
        }
    }

    @FXML
    private void enregistrerModifications() {
        if (validerFormulaire()) {
            try {
                String bilanPath = historiqueActuel.getBilan(); // Conserver l'ancien chemin par défaut

                // Si un nouveau fichier a été sélectionné
                if (bilanFileChanged && selectedBilanFile != null) {
                    // Générer un nom de fichier unique
                    String uniqueFileName = UUID.randomUUID().toString() + "." + getFileExtension(selectedBilanFile);
                    Path destination = Paths.get(storagePath + uniqueFileName);

                    // Copier le fichier
                    Files.copy(selectedBilanFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                    bilanPath = uniqueFileName;

                    // Supprimer l'ancien fichier si nécessaire
                    if (historiqueActuel.getBilan() != null && !historiqueActuel.getBilan().isEmpty()) {
                        try {
                            Files.deleteIfExists(Paths.get(storagePath + historiqueActuel.getBilan()));
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la suppression de l'ancien fichier: " + e.getMessage());
                        }
                    }
                }

                // Mettre à jour l'objet historique
                historiqueActuel.setNom(txtNom.getText().trim());
                historiqueActuel.setPrenom(txtPrenom.getText().trim());
                historiqueActuel.setMaladies(txtMaladie.getText().trim());
                historiqueActuel.setDescription(txtDescription.getText().trim());
                historiqueActuel.setType_traitement(txtType.getText().trim());
                historiqueActuel.setBilan(bilanPath);

                // Mettre à jour dans la base de données
                hisServices.update(historiqueActuel);

                // Afficher un message de succès
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Les modifications ont été enregistrées avec succès.");

                // Exécuter le callback de rafraîchissement si disponible
                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                // Fermer la fenêtre
                fermerFenetre();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la mise à jour: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (txtNom.getText().trim().isEmpty()) {
            erreurs.append("- Le nom est requis\n");
        }

        if (txtPrenom.getText().trim().isEmpty()) {
            erreurs.append("- Le prénom est requis\n");
        }

        if (txtMaladie.getText().trim().isEmpty()) {
            erreurs.append("- La maladie est requise\n");
        }

        if (erreurs.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Formulaire incomplet",
                    "Veuillez corriger les erreurs suivantes:\n" + erreurs.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }
}