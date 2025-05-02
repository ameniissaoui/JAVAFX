package org.example.controllers;

import javafx.application.Platform;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Patient;
import org.example.models.historique_traitement;
import org.example.services.HisServices;

import java.io.File;
import java.io.IOException;
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
    private TextArea tfDescription;

    @FXML
    private TextField tfTypeTraitement;

    @FXML
    private Label lblFichierBilan;

    @FXML
    private Button btnChoisirBilan;

    @FXML
    private ImageView imgPreviewBilan;

    @FXML
    private Button btnAnnuler;

    private Patient patient;
    private HisServices hisServices;
    private File selectedBilanFile;
    private String storagePath = "src/main/resources/images/";

    // Taille maximale du fichier bilan en bytes (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Extensions autorisées pour les fichiers
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("png", "jpg", "jpeg", "gif", "bmp", "pdf")
    );

    // Patterns pour validation (aucun chiffre autorisé)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} \\-']{2,50}$"); // Nom et prénom
    private static final Pattern TEXT_PATTERN = Pattern.compile("^[\\p{L} \\-',;.!?]{3,100}$"); // Maladie, type traitement
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^[\\p{L} \\-',;.!?]{10,500}$"); // Description

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

        // Ajouter des animations 3D après que la scène est initialisée
        Platform.runLater(this::addAnimations);
    }

    private void addAnimations() {
        // Animation 3D pour les champs de texte
        addFieldAnimation(tfNom);
        addFieldAnimation(tfPrenom);
        addFieldAnimation(tfMaladie);
        addFieldAnimation(tfTypeTraitement);
        addTextAreaAnimation(tfDescription);

        // Animation 3D pour les boutons
        addButtonHoverAnimation(btnChoisirBilan);
        addButtonHoverAnimation(btnAnnuler);

        // Ajouter l'animation pour le bouton "Ajouter" (lookup après que la scène est prête)
        Button btnAjouter = (Button) tfNom.getScene().lookup("[text='Ajouter']");
        if (btnAjouter != null) {
            addButtonHoverAnimation(btnAjouter);
        }
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

    public void setPatient(Patient patient) {
        this.patient = patient;
        System.out.println("Patient défini dans AjouterHistController: ID=" + (patient != null ? patient.getId() : "null"));

        if (patient != null) {
            tfNom.setText(patient.getNom());
            tfPrenom.setText(patient.getPrenom());
            tfNom.setEditable(false);
            tfPrenom.setEditable(false);
        } else {
            System.out.println("ATTENTION: Patient null reçu dans setPatient()");
        }
    }

    private void setupValidationListeners() {
        // Validation du nom
        tfNom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && (!NAME_PATTERN.matcher(newValue).matches() || newValue.matches(".*\\d.*"))) {
                tfNom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            } else {
                tfNom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            }
        });

        // Validation du prénom
        tfPrenom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && (!NAME_PATTERN.matcher(newValue).matches() || newValue.matches(".*\\d.*"))) {
                tfPrenom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            } else {
                tfPrenom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            }
        });

        // Validation de la maladie
        tfMaladie.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && (!TEXT_PATTERN.matcher(newValue).matches() || newValue.matches(".*\\d.*"))) {
                tfMaladie.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            } else if (newValue.length() > 100) {
                tfMaladie.setText(oldValue);
                afficherAlert(Alert.AlertType.WARNING, "Attention",
                        "Taille maximale", "La maladie ne peut pas dépasser 100 caractères.");
            } else {
                tfMaladie.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            }
        });

        // Validation de la description
        tfDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && (!DESCRIPTION_PATTERN.matcher(newValue).matches() || newValue.matches(".*\\d.*"))) {
                tfDescription.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            } else if (newValue.length() > 500) {
                tfDescription.setText(oldValue);
                afficherAlert(Alert.AlertType.WARNING, "Attention",
                        "Taille maximale", "La description ne peut pas dépasser 500 caractères.");
            } else {
                tfDescription.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            }
        });

        // Validation du type de traitement
        tfTypeTraitement.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && (!TEXT_PATTERN.matcher(newValue).matches() || newValue.matches(".*\\d.*"))) {
                tfTypeTraitement.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            } else if (newValue.length() > 100) {
                tfTypeTraitement.setText(oldValue);
                afficherAlert(Alert.AlertType.WARNING, "Attention",
                        "Taille maximale", "Le type de traitement ne peut pas dépasser 100 caractères.");
            } else {
                tfTypeTraitement.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
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
            if (file.length() > MAX_FILE_SIZE) {
                afficherAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier trop volumineux",
                        "Le fichier sélectionné dépasse la taille maximale autorisée de 5MB.");
                return;
            }

            String extension = getFileExtension(file).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                afficherAlert(Alert.AlertType.ERROR, "Erreur",
                        "Format de fichier non autorisé",
                        "Seuls les formats PNG, JPG, JPEG, GIF, BMP et PDF sont acceptés.");
                return;
            }

            selectedBilanFile = file;
            lblFichierBilan.setText(file.getName());

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
                if (patient == null) {
                    System.out.println("ERREUR: Le patient est null!");
                    afficherAlert(Alert.AlertType.ERROR, "Erreur",
                            "Patient non défini",
                            "Aucun patient n'est associé à cet historique.");
                    return;
                } else {
                    System.out.println("Patient détails: ID=" + patient.getId() +
                            ", Nom=" + patient.getNom() +
                            ", Prénom=" + patient.getPrenom());
                }

                String bilanPath = "";
                if (selectedBilanFile != null) {
                    String uniqueFileName = UUID.randomUUID().toString() + "." + getFileExtension(selectedBilanFile);
                    Path destination = Paths.get(storagePath + uniqueFileName);
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

                historique.setUser(patient);
                System.out.println("Historique créé avec patient ID: " +
                        (historique.getUser() != null ? historique.getUser().getId() : "NULL"));

                hisServices.add(historique);

                afficherAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Historique ajouté",
                        "L'historique de traitement a été ajouté avec succès.");

                clearFields();

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient_profile.fxml"));
                    Parent root = loader.load();
                    PatientProfileController profileController = loader.getController();
                    profileController.setPatient(patient);
                    Stage stage = (Stage) tfNom.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch (Exception e) {
                    afficherAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur de navigation",
                            "Impossible de charger le profil patient: " + e.getMessage());
                    e.printStackTrace();
                }

            } catch (Exception e) {
                afficherAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de l'ajout",
                        "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
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
    private void onAnnuler(ActionEvent event) {
        retourProfil(new ActionEvent(btnAnnuler, null));
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        String nom = tfNom.getText().trim();
        if (nom.isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        } else if (!NAME_PATTERN.matcher(nom).matches() || nom.matches(".*\\d.*")) {
            errors.append("- Le nom ne doit contenir que des lettres, espaces, tirets ou apostrophes (2-50 caractères, pas de chiffres)\n");
        }

        String prenom = tfPrenom.getText().trim();
        if (prenom.isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
        } else if (!NAME_PATTERN.matcher(prenom).matches() || prenom.matches(".*\\d.*")) {
            errors.append("- Le prénom ne doit contenir que des lettres, espaces, tirets ou apostrophes (2-50 caractères, pas de chiffres)\n");
        }

        String maladie = tfMaladie.getText().trim();
        if (maladie.isEmpty()) {
            errors.append("- La maladie est obligatoire\n");
        } else if (!TEXT_PATTERN.matcher(maladie).matches() || maladie.matches(".*\\d.*")) {
            errors.append("- La maladie ne doit contenir que des lettres, espaces, tirets, apostrophes ou ponctuation (3-100 caractères, pas de chiffres)\n");
        }

        String description = tfDescription.getText().trim();
        if (description.isEmpty()) {
            errors.append("- La description est obligatoire\n");
        } else if (!DESCRIPTION_PATTERN.matcher(description).matches() || description.matches(".*\\d.*")) {
            errors.append("- La description ne doit contenir que des lettres, espaces, tirets, apostrophes ou ponctuation (10-500 caractères, pas de chiffres)\n");
        }

        String typeTraitement = tfTypeTraitement.getText().trim();
        if (typeTraitement.isEmpty()) {
            errors.append("- Le type de traitement est obligatoire\n");
        } else if (!TEXT_PATTERN.matcher(typeTraitement).matches() || typeTraitement.matches(".*\\d.*")) {
            errors.append("- Le type de traitement ne doit contenir que des lettres, espaces, tirets, apostrophes ou ponctuation (3-100 caractères, pas de chiffres)\n");
        }

        if (selectedBilanFile == null) {
            errors.append("- Le fichier bilan est obligatoire\n");
        }

        if (errors.length() > 0) {
            afficherAlert(Alert.AlertType.WARNING, "Validation",
                    "Veuillez corriger les erreurs suivantes",
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

        tfNom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
        tfPrenom.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
        tfMaladie.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
        tfDescription.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
        tfTypeTraitement.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 10 10 0; -fx-border-color: #3fbbc0; -fx-border-width: 1; -fx-border-radius: 0 10 10 0; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
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

    public void handleLogout(ActionEvent actionEvent) {
    }
}