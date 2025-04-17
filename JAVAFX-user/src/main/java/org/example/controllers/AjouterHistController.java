package org.example.controllers;

import org.example.models.historique_traitement;
import org.example.services.HisServices;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

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
    private String storagePath = "src/main/resources/images/";

    // Taille maximale du fichier bilan en bytes (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Extensions autorisées pour les fichiers
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("png", "jpg", "jpeg", "gif", "bmp", "pdf")
    );

    // Pattern pour validation de noms (lettres, espaces, tirets et apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} \\-']{2,50}$");

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

        // Ajouter des listeners pour validation en temps réel
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Validation du nom (lettres, espaces, tirets uniquement)
        tfNom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !NAME_PATTERN.matcher(newValue).matches()) {
                tfNom.setStyle("-fx-border-color: red;");
            } else {
                tfNom.setStyle("");
            }
        });

        // Validation du prénom (lettres, espaces, tirets uniquement)
        tfPrenom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !NAME_PATTERN.matcher(newValue).matches()) {
                tfPrenom.setStyle("-fx-border-color: red;");
            } else {
                tfPrenom.setStyle("");
            }
        });

        // Validation des champs obligatoires avec longueur minimale
        tfMaladie.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && newValue.length() < 3) {
                tfMaladie.setStyle("-fx-border-color: red;");
            } else {
                tfMaladie.setStyle("");
            }
        });

        // Validation de la description (longueur)
        tfDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && newValue.length() < 10) {
                tfDescription.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 500) {
                tfDescription.setText(oldValue);
                afficherAlert(Alert.AlertType.WARNING, "Attention",
                        "Taille maximale", "La description ne peut pas dépasser 500 caractères.");
            } else {
                tfDescription.setStyle("");
            }
        });

        // Validation du type de traitement
        tfTypeTraitement.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && newValue.length() < 3) {
                tfTypeTraitement.setStyle("-fx-border-color: red;");
            } else {
                tfTypeTraitement.setStyle("");
            }
        });
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
            // Valider la taille du fichier
            if (file.length() > MAX_FILE_SIZE) {
                afficherAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier trop volumineux",
                        "Le fichier sélectionné dépasse la taille maximale autorisée de 5MB.");
                return;
            }

            // Valider l'extension du fichier
            String extension = getFileExtension(file).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                afficherAlert(Alert.AlertType.ERROR, "Erreur",
                        "Format de fichier non autorisé",
                        "Seuls les formats PNG, JPG, JPEG, GIF, BMP et PDF sont acceptés.");
                return;
            }

            selectedBilanFile = file;
            lblFichierBilan.setText(file.getName());

            // Afficher une prévisualisation si c'est une image
            try {
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
                afficherAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur de prévisualisation",
                        "Impossible de charger la prévisualisation: " + e.getMessage());
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
                historique.setNom(tfNom.getText().trim());
                historique.setPrenom(tfPrenom.getText().trim());
                historique.setMaladies(tfMaladie.getText().trim());
                historique.setDescription(tfDescription.getText().trim());
                historique.setType_traitement(tfTypeTraitement.getText().trim());
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

        // Validation du nom
        String nom = tfNom.getText().trim();
        if (nom.isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        } else if (!NAME_PATTERN.matcher(nom).matches()) {
            errors.append("- Le nom ne doit contenir que des lettres, espaces, tirets ou apostrophes (2-50 caractères)\n");
        }

        // Validation du prénom
        String prenom = tfPrenom.getText().trim();
        if (prenom.isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
        } else if (!NAME_PATTERN.matcher(prenom).matches()) {
            errors.append("- Le prénom ne doit contenir que des lettres, espaces, tirets ou apostrophes (2-50 caractères)\n");
        }

        // Validation de la maladie
        String maladie = tfMaladie.getText().trim();
        if (maladie.isEmpty()) {
            errors.append("- La maladie est obligatoire\n");
        } else if (maladie.length() < 3) {
            errors.append("- La maladie doit contenir au moins 3 caractères\n");
        } else if (maladie.length() > 100) {
            errors.append("- La maladie ne doit pas dépasser 100 caractères\n");
        }

        // Validation de la description
        String description = tfDescription.getText().trim();
        if (description.isEmpty()) {
            errors.append("- La description est obligatoire\n");
        } else if (description.length() < 10) {
            errors.append("- La description doit contenir au moins 10 caractères\n");
        } else if (description.length() > 500) {
            errors.append("- La description ne doit pas dépasser 500 caractères\n");
        }

        // Validation du type de traitement
        String typeTraitement = tfTypeTraitement.getText().trim();
        if (typeTraitement.isEmpty()) {
            errors.append("- Le type de traitement est obligatoire\n");
        } else if (typeTraitement.length() < 3) {
            errors.append("- Le type de traitement doit contenir au moins 3 caractères\n");
        } else if (typeTraitement.length() > 100) {
            errors.append("- Le type de traitement ne doit pas dépasser 100 caractères\n");
        }

        // Validation du fichier bilan
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

        // Réinitialiser les styles
        tfNom.setStyle("");
        tfPrenom.setStyle("");
        tfMaladie.setStyle("");
        tfDescription.setStyle("");
        tfTypeTraitement.setStyle("");
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