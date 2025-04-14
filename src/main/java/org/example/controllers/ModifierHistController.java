package org.example.controllers;

import org.example.models.historique_traitement;
import org.example.services.HisServices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
import java.util.regex.Pattern;

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

    // Constantes pour la validation
    private static final int NOM_MAX_LENGTH = 50;
    private static final int PRENOM_MAX_LENGTH = 50;
    private static final int MALADIE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;
    private static final int TYPE_MAX_LENGTH = 100;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 Mo

    // Patterns de validation
    private static final Pattern NOM_PATTERN = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ\\-' ]{2,}$");
    private static final Pattern PRENOM_PATTERN = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ\\-' ]{2,}$");

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

        // Configuration des listeners pour la validation en temps réel
        setupValidationListeners();
    }

    /**
     * Configure les listeners pour la validation en temps réel des champs
     */
    private void setupValidationListeners() {
        // Validation du nom (lettres, espaces, tirets, apostrophes uniquement)
        txtNom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > NOM_MAX_LENGTH) {
                txtNom.setText(oldValue);
                showValidationError("Le nom ne peut pas dépasser " + NOM_MAX_LENGTH + " caractères.");
                return;
            }

            if (!newValue.isEmpty() && !NOM_PATTERN.matcher(newValue).matches()) {
                txtNom.setStyle("-fx-border-color: red;");
            } else {
                txtNom.setStyle("");
            }
        });

        // Validation du prénom (lettres, espaces, tirets, apostrophes uniquement)
        txtPrenom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > PRENOM_MAX_LENGTH) {
                txtPrenom.setText(oldValue);
                showValidationError("Le prénom ne peut pas dépasser " + PRENOM_MAX_LENGTH + " caractères.");
                return;
            }

            if (!newValue.isEmpty() && !PRENOM_PATTERN.matcher(newValue).matches()) {
                txtPrenom.setStyle("-fx-border-color: red;");
            } else {
                txtPrenom.setStyle("");
            }
        });

        // Validation de la maladie (taille max)
        txtMaladie.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MALADIE_MAX_LENGTH) {
                txtMaladie.setText(oldValue);
                showValidationError("Le nom de la maladie ne peut pas dépasser " + MALADIE_MAX_LENGTH + " caractères.");
            }
        });

        // Validation de la description (taille max)
        txtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > DESCRIPTION_MAX_LENGTH) {
                txtDescription.setText(oldValue);
                showValidationError("La description ne peut pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères.");
            }
        });

        // Validation du type de traitement (taille max)
        txtType.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > TYPE_MAX_LENGTH) {
                txtType.setText(oldValue);
                showValidationError("Le type de traitement ne peut pas dépasser " + TYPE_MAX_LENGTH + " caractères.");
            }
        });
    }

    /**
     * Affiche une alerte pour les erreurs de validation en temps réel
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Erreur de saisie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            // Vérifier la taille du fichier
            if (file.length() > MAX_FILE_SIZE) {
                showAlert(Alert.AlertType.ERROR, "Fichier trop volumineux",
                        "Le fichier sélectionné dépasse la taille maximale autorisée (10 Mo).");
                return;
            }

            // Vérifier l'extension du fichier
            String extension = getFileExtension(file).toLowerCase();
            if (!isValidFileExtension(extension)) {
                showAlert(Alert.AlertType.ERROR, "Type de fichier non supporté",
                        "Les types de fichiers supportés sont : PNG, JPG, JPEG, GIF, BMP et PDF.");
                return;
            }

            selectedBilanFile = file;
            bilanFileChanged = true;
            lblFichierBilan.setText(file.getName());

            // Afficher une prévisualisation si c'est une image
            try {
                if (isImageFile(extension)) {
                    if (imgPreviewBilan != null) {
                        Image image = new Image(file.toURI().toString());

                        // Vérifier si l'image est valide
                        if (image.isError()) {
                            throw new Exception("L'image est corrompue ou ne peut pas être chargée.");
                        }

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
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de charger le fichier: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Vérifie si l'extension du fichier est valide
     */
    private boolean isValidFileExtension(String extension) {
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("gif") || extension.equals("bmp") || extension.equals("pdf");
    }

    /**
     * Vérifie si le fichier est une image
     */
    private boolean isImageFile(String extension) {
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("gif") || extension.equals("bmp");
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
                        if (isImageFile(extension)) {
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

                // Nettoyer les données avant la mise à jour
                String nom = sanitizeInput(txtNom.getText().trim());
                String prenom = sanitizeInput(txtPrenom.getText().trim());
                String maladie = sanitizeInput(txtMaladie.getText().trim());
                String description = sanitizeInput(txtDescription.getText().trim());
                String type = sanitizeInput(txtType.getText().trim());

                // Mettre à jour l'objet historique
                historiqueActuel.setNom(nom);
                historiqueActuel.setPrenom(prenom);
                historiqueActuel.setMaladies(maladie);
                historiqueActuel.setDescription(description);
                historiqueActuel.setType_traitement(type);
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

    /**
     * Nettoie une chaîne de caractères pour éviter les injections SQL et les problèmes de sécurité
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        // Remplacer les caractères pouvant causer des problèmes de sécurité
        return input.replaceAll("[;\"'<>]", "");
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        // Validation du nom
        if (txtNom.getText().trim().isEmpty()) {
            erreurs.append("- Le nom est requis\n");
        } else if (!NOM_PATTERN.matcher(txtNom.getText().trim()).matches()) {
            erreurs.append("- Le nom contient des caractères non autorisés (utilisez uniquement des lettres, espaces, tirets ou apostrophes)\n");
        } else if (txtNom.getText().trim().length() < 2) {
            erreurs.append("- Le nom doit comporter au moins 2 caractères\n");
        }

        // Validation du prénom
        if (txtPrenom.getText().trim().isEmpty()) {
            erreurs.append("- Le prénom est requis\n");
        } else if (!PRENOM_PATTERN.matcher(txtPrenom.getText().trim()).matches()) {
            erreurs.append("- Le prénom contient des caractères non autorisés (utilisez uniquement des lettres, espaces, tirets ou apostrophes)\n");
        } else if (txtPrenom.getText().trim().length() < 2) {
            erreurs.append("- Le prénom doit comporter au moins 2 caractères\n");
        }

        // Validation de la maladie
        if (txtMaladie.getText().trim().isEmpty()) {
            erreurs.append("- La maladie est requise\n");
        } else if (txtMaladie.getText().trim().length() < 2) {
            erreurs.append("- Le nom de la maladie doit comporter au moins 2 caractères\n");
        }

        // Validation du type de traitement (si renseigné)
        if (!txtType.getText().trim().isEmpty() && txtType.getText().trim().length() < 2) {
            erreurs.append("- Le type de traitement doit comporter au moins 2 caractères s'il est renseigné\n");
        }

        // Validation de la description (si renseignée)
        if (!txtDescription.getText().trim().isEmpty() && txtDescription.getText().trim().length() < 10) {
            erreurs.append("- La description doit comporter au moins 10 caractères si elle est renseignée\n");
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