package org.example.controllers;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.historique_traitement;
import org.example.services.HisServices;

import java.io.File;
import java.io.IOException;
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

        // Ajouter des animations 3D aux champs et boutons
        addAnimations();
    }

    private void addAnimations() {
        // Animation 3D pour les champs de texte
        addFieldAnimation(txtNom);
        addFieldAnimation(txtPrenom);
        addFieldAnimation(txtMaladie);
        addFieldAnimation(txtType);
        addTextAreaAnimation(txtDescription);

        // Animation 3D pour les boutons
        addButtonHoverAnimation(btnChoisirBilan);
        addButtonHoverAnimation(btnEnregistrer);
        addButtonHoverAnimation(btnAnnuler);
    }

    private void addFieldAnimation(TextField field) {
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(400), field);
        rotateTransition.setByAngle(5);
        rotateTransition.setCycleCount(2);
        rotateTransition.setAutoReverse(true);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), field);
        scaleTransition.setToX(1.02);
        scaleTransition.setToY(1.02);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);

        ParallelTransition hoverAnimation = new ParallelTransition(rotateTransition, scaleTransition);

        field.setOnMouseEntered(event -> hoverAnimation.play());
    }

    private void addTextAreaAnimation(TextArea textArea) {
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(400), textArea);
        rotateTransition.setByAngle(5);
        rotateTransition.setCycleCount(2);
        rotateTransition.setAutoReverse(true);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), textArea);
        scaleTransition.setToX(1.02);
        scaleTransition.setToY(1.02);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);

        ParallelTransition hoverAnimation = new ParallelTransition(rotateTransition, scaleTransition);

        textArea.setOnMouseEntered(event -> hoverAnimation.play());
    }

    private void addButtonHoverAnimation(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> scaleIn.play());
        button.setOnMouseExited(e -> scaleOut.play());
    }

    private void setupValidationListeners() {
        // Validation du nom
        txtNom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > NOM_MAX_LENGTH) {
                txtNom.setText(oldValue);
                showValidationError("Le nom ne peut pas dépasser " + NOM_MAX_LENGTH + " caractères.");
                return;
            }

            if (!newValue.isEmpty() && !NOM_PATTERN.matcher(newValue).matches()) {
                txtNom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            } else {
                txtNom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            }
        });

        // Validation du prénom
        txtPrenom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > PRENOM_MAX_LENGTH) {
                txtPrenom.setText(oldValue);
                showValidationError("Le prénom ne peut pas dépasser " + PRENOM_MAX_LENGTH + " caractères.");
                return;
            }

            if (!newValue.isEmpty() && !PRENOM_PATTERN.matcher(newValue).matches()) {
                txtPrenom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            } else {
                txtPrenom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            }
        });

        // Validation de la maladie
        txtMaladie.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MALADIE_MAX_LENGTH) {
                txtMaladie.setText(oldValue);
                showValidationError("Le nom de la maladie ne peut pas dépasser " + MALADIE_MAX_LENGTH + " caractères.");
            }
        });

        // Validation de la description
        txtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > DESCRIPTION_MAX_LENGTH) {
                txtDescription.setText(oldValue);
                showValidationError("La description ne peut pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères.");
            }
        });

        // Validation du type de traitement
        txtType.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > TYPE_MAX_LENGTH) {
                txtType.setText(oldValue);
                showValidationError("Le type de traitement ne peut pas dépasser " + TYPE_MAX_LENGTH + " caractères.");
            }
        });
    }

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
            if (file.length() > MAX_FILE_SIZE) {
                showAlert(Alert.AlertType.ERROR, "Fichier trop volumineux",
                        "Le fichier sélectionné dépasse la taille maximale autorisée (10 Mo).");
                return;
            }

            String extension = getFileExtension(file).toLowerCase();
            if (!isValidFileExtension(extension)) {
                showAlert(Alert.AlertType.ERROR, "Type de fichier non supporté",
                        "Les types de fichiers supportés sont : PNG, JPG, JPEG, GIF, BMP et PDF.");
                return;
            }

            selectedBilanFile = file;
            bilanFileChanged = true;
            lblFichierBilan.setText(file.getName());

            try {
                if (isImageFile(extension)) {
                    if (imgPreviewBilan != null) {
                        Image image = new Image(file.toURI().toString());
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

    private boolean isValidFileExtension(String extension) {
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("gif") || extension.equals("bmp") || extension.equals("pdf");
    }

    private boolean isImageFile(String extension) {
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("gif") || extension.equals("bmp");
    }

    public void setHistoriqueTraitement(historique_traitement historique) {
        this.historiqueActuel = historique;
        remplirFormulaire();
    }

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

            if (historiqueActuel.getBilan() != null && !historiqueActuel.getBilan().isEmpty()) {
                lblFichierBilan.setText(historiqueActuel.getBilan());
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
                String bilanPath = historiqueActuel.getBilan();

                if (bilanFileChanged && selectedBilanFile != null) {
                    String uniqueFileName = UUID.randomUUID().toString() + "." + getFileExtension(selectedBilanFile);
                    Path destination = Paths.get(storagePath + uniqueFileName);
                    Files.copy(selectedBilanFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                    bilanPath = uniqueFileName;

                    if (historiqueActuel.getBilan() != null && !historiqueActuel.getBilan().isEmpty()) {
                        try {
                            Files.deleteIfExists(Paths.get(storagePath + historiqueActuel.getBilan()));
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la suppression de l'ancien fichier: " + e.getMessage());
                        }
                    }
                }

                String nom = sanitizeInput(txtNom.getText().trim());
                String prenom = sanitizeInput(txtPrenom.getText().trim());
                String maladie = sanitizeInput(txtMaladie.getText().trim());
                String description = sanitizeInput(txtDescription.getText().trim());
                String type = sanitizeInput(txtType.getText().trim());

                historiqueActuel.setNom(nom);
                historiqueActuel.setPrenom(prenom);
                historiqueActuel.setMaladies(maladie);
                historiqueActuel.setDescription(description);
                historiqueActuel.setType_traitement(type);
                historiqueActuel.setBilan(bilanPath);

                hisServices.update(historiqueActuel);

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Les modifications ont été enregistrées avec succès.");

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                retourProfil(new ActionEvent(btnEnregistrer, null));

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la mise à jour: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[;\"'<>]", "");
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (txtNom.getText().trim().isEmpty()) {
            erreurs.append("- Le nom est requis\n");
        } else if (!NOM_PATTERN.matcher(txtNom.getText().trim()).matches()) {
            erreurs.append("- Le nom contient des caractères non autorisés (utilisez uniquement des lettres, espaces, tirets ou apostrophes)\n");
        } else if (txtNom.getText().trim().length() < 2) {
            erreurs.append("- Le nom doit comporter au moins 2 caractères\n");
        }

        if (txtPrenom.getText().trim().isEmpty()) {
            erreurs.append("- Le prénom est requis\n");
        } else if (!PRENOM_PATTERN.matcher(txtPrenom.getText().trim()).matches()) {
            erreurs.append("- Le prénom contient des caractères non autorisés (utilisez uniquement des lettres, espaces, tirets ou apostrophes)\n");
        } else if (txtPrenom.getText().trim().length() < 2) {
            erreurs.append("- Le prénom doit comporter au moins 2 caractères\n");
        }

        if (txtMaladie.getText().trim().isEmpty()) {
            erreurs.append("- La maladie est requise\n");
        } else if (txtMaladie.getText().trim().length() < 2) {
            erreurs.append("- Le nom de la maladie doit comporter au moins 2 caractères\n");
        }

        if (!txtType.getText().trim().isEmpty() && txtType.getText().trim().length() < 2) {
            erreurs.append("- Le type de traitement doit comporter au moins 2 caractères s'il est renseigné\n");
        }

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
    public void retourProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient_profile.fxml"));
            Parent root = loader.load();
            PatientProfileController controller = loader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Profil Patient");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void annuler() {
        retourProfil(new ActionEvent(btnAnnuler, null));
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

    public void navigateToHome(ActionEvent actionEvent) {
    }

    public void handleHistoRedirect(ActionEvent actionEvent) {
    }

    public void redirectToCalendar(ActionEvent actionEvent) {
    }

    public void redirectToDemande(ActionEvent actionEvent) {
    }

    public void redirectToRendezVous(ActionEvent actionEvent) {
    }

    public void redirectProduit(ActionEvent actionEvent) {
    }

    public void viewDoctors(ActionEvent actionEvent) {
    }

    public void navigateToEvent(ActionEvent actionEvent) {
    }

    public void navigateToReservation(ActionEvent actionEvent) {
    }

    public void navigateToContact(ActionEvent actionEvent) {
    }

    public void showNotifications(ActionEvent actionEvent) {
    }

    public void navigateToProfile(ActionEvent actionEvent) {

    }
}