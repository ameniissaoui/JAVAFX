package Controllers;

import Services.HisServices;
import Model.historique_traitement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AjouterHistController {
    @FXML
    private TextField tfNom;

    @FXML
    private TextField tfPrenom;

    @FXML
    private TextField tfMaladie;

    @FXML
    private TextField tfDescription;

    @FXML
    private TextField tfTypeTraitement;

    @FXML
    private Label lblFichierBilan;

    @FXML
    private Button btnChoisirBilan;

    @FXML
    private ImageView imgPreviewBilan;

    private HisServices hisServices;
    private File selectedBilanFile;
    private String storagePath = "src/main/resources/uploads/";

    @FXML
    public void initialize() {
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
        }
    }

    @FXML
    private void choisirFichierBilan(ActionEvent event) {
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
                        imgPreviewBilan.setFitWidth(200);
                        imgPreviewBilan.setPreserveRatio(true);
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

    @FXML
    private void onAjouter(ActionEvent event) {
        if (validateInput()) {
            try {
                String bilanPath = "";
                if (selectedBilanFile != null) {
                    // Générer un nom de fichier unique
                    String uniqueFileName = UUID.randomUUID().toString() + "." + getFileExtension(selectedBilanFile);
                    Path destination = Paths.get(storagePath + uniqueFileName);

                    // Copier le fichier
                    Files.copy(selectedBilanFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                    bilanPath = uniqueFileName;
                }

                historique_traitement historique = new historique_traitement();
                historique.setNom(tfNom.getText());
                historique.setPrenom(tfPrenom.getText());
                historique.setMaladies(tfMaladie.getText());
                historique.setDescription(tfDescription.getText());
                historique.setType_traitement(tfTypeTraitement.getText());
                historique.setBilan(bilanPath);

                hisServices.add(historique);

                afficherAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Historique ajouté",
                        "L'historique de traitement a été ajouté avec succès.");

                clearFields();

                // Retourner à la liste
                Stage stage = (Stage) tfNom.getScene().getWindow();
                stage.close();

            } catch (Exception e) {
                afficherAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de l'ajout",
                        "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (tfNom.getText().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }
        if (tfPrenom.getText().isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
        }
        if (tfMaladie.getText().isEmpty()) {
            errors.append("- La maladie est obligatoire\n");
        }
        if (tfDescription.getText().isEmpty()) {
            errors.append("- La description est obligatoire\n");
        }
        if (tfTypeTraitement.getText().isEmpty()) {
            errors.append("- Le type de traitement est obligatoire\n");
        }
        if (selectedBilanFile == null) {
            errors.append("- Le fichier bilan est obligatoire\n");
        }

        if (errors.length() > 0) {
            afficherAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez remplir tous les champs obligatoires",
                    errors.toString());
            return false;
        }

        return true;
    }

    private void clearFields() {
        tfNom.clear();
        tfPrenom.clear();
        tfMaladie.clear();
        tfDescription.clear();
        tfTypeTraitement.clear();
        lblFichierBilan.setText("Aucun fichier sélectionné");
        selectedBilanFile = null;
        if (imgPreviewBilan != null) {
            imgPreviewBilan.setImage(null);
            imgPreviewBilan.setVisible(false);
        }
    }

    private void afficherAlert(Alert.AlertType type, String titre, String header, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(header);
        alert.setContentText(contenu);
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