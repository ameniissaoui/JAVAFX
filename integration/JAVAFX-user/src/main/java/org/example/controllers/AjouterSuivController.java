package org.example.controllers;

import javafx.animation.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.example.models.Medecin;
import org.example.models.historique_traitement;
import org.example.models.suivie_medical;
import org.example.services.SuivServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.util.SessionManager;
import org.example.services.GeminiAIService;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

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
    private GeminiAIService geminiService;
    @FXML
    private Button analyzeWithAIButton;
    @FXML
    private ProgressIndicator aiProgressIndicator;
    @FXML
    private HBox aiProgressContainer;

    @FXML
    private Label aiProgressLabel;

    private historique_traitement historiqueTraitement;
    private Runnable onSuiviAddedCallback;
    private SuivServices suivServices = new SuivServices();
    private final String storagePath = "src/main/resources/images/";

    // Constantes pour la validation
    private static final int COMMENTAIRE_MIN_LENGTH = 10;
    private static final int COMMENTAIRE_MAX_LENGTH = 500;
    private static final int COMMENTAIRE_WARNING_LENGTH = 450;
    private static final Pattern COMMENTAIRE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s.,;:!?()\\-'\"@#$%&*+=<>/\\[\\]{}|_~`]+$");

    public void initialize() {
        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Désactiver les dates passées
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        // Configurer le listener pour le commentaire avec validation en temps réel
        setupCommentaireValidator();

        // Initialize Gemini API service
        geminiService = GeminiAIService.getInstance();

        // Initialize UI elements
        aiProgressContainer.setVisible(false);

        // Set tooltips for better UX
        analyzeWithAIButton.setTooltip(new Tooltip("Analyser le bilan avec l'intelligence artificielle"));

        // Add hover animations to buttons
        addButtonHoverAnimation(ajouterButton);
        addButtonHoverAnimation(retourButton);
        addButtonHoverAnimation(analyzeWithAIButton);
    }

    // Helper method to add hover animation to buttons
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

    @FXML
    private void analyzeWithAI(ActionEvent event) {
        if (historiqueTraitement == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun historique de traitement sélectionné.");
            return;
        }

        String bilanFileName = historiqueTraitement.getBilan();
        if (bilanFileName == null || bilanFileName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun bilan disponible pour analyse.");
            return;
        }

        File bilanFile = new File(storagePath + bilanFileName);
        if (!geminiService.isFileReadable(bilanFile)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier bilan est introuvable ou illisible.");
            return;
        }

        if (!geminiService.isPdfFile(bilanFile)) {
            showAlert(Alert.AlertType.WARNING, "Format non supporté",
                    "L'analyse AI n'est disponible que pour les fichiers PDF. " +
                            "Format détecté: " + geminiService.getFileExtension(bilanFile));
            return;
        }

        analyzeWithAIButton.setVisible(false);
        aiProgressContainer.setVisible(true);

        createPulseAnimation(aiProgressLabel);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                try {
                    updateProgress(0.1, 1.0);
                    updateMessage("Extraction du texte...");

                    String pdfText = geminiService.extractTextFromPDF(bilanFile);

                    updateProgress(0.4, 1.0);
                    updateMessage("Analyse du contenu...");

                    String patientName = historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom();
                    String diagnosis = historiqueTraitement.getMaladies() != null ?
                            historiqueTraitement.getMaladies() : "Non spécifié";

                    updateProgress(0.6, 1.0);
                    updateMessage("Génération du commentaire...");

                    String result = geminiService.generateMedicalComment(pdfText, patientName, diagnosis).get();

                    updateProgress(1.0, 1.0);
                    updateMessage("Terminé!");

                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            aiProgressLabel.setText(newMsg);
        });

        task.setOnSucceeded(e -> {
            String aiGeneratedComment = task.getValue();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), aiProgressContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event2 -> {
                aiProgressContainer.setVisible(false);
                analyzeWithAIButton.setVisible(true);
            });
            fadeOut.play();

            commentaireArea.clear();
            animateTextInsertion(commentaireArea, aiGeneratedComment);

            showAlert(Alert.AlertType.INFORMATION, "Analyse terminée",
                    "L'analyse du bilan a été effectuée avec succès. Un commentaire a été généré.");
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            showAlert(Alert.AlertType.ERROR, "Erreur d'analyse",
                    "L'analyse du bilan a échoué: " + (exception != null ? exception.getMessage() : "Erreur inconnue"));

            aiProgressContainer.setVisible(false);
            analyzeWithAIButton.setVisible(true);
        });

        new Thread(task).start();
    }

    private void createPulseAnimation(Label label) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), label);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.7);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
    }

    private void animateTextInsertion(TextArea textArea, String text) {
        final int[] i = {0};
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(15),
                event -> {
                    if (i[0] < text.length()) {
                        textArea.appendText(String.valueOf(text.charAt(i[0])));
                        i[0]++;
                    }
                }
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(text.length());
        timeline.play();
    }

    private void setupCommentaireValidator() {
        commentaireArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > COMMENTAIRE_MAX_LENGTH) {
                commentaireArea.setText(oldValue);
                showAlert(Alert.AlertType.WARNING, "Limite de caractères",
                        "Le commentaire est limité à " + COMMENTAIRE_MAX_LENGTH + " caractères.");
                return;
            }

            if (newValue.length() >= COMMENTAIRE_WARNING_LENGTH && oldValue.length() < COMMENTAIRE_WARNING_LENGTH) {
                showAlert(Alert.AlertType.INFORMATION, "Information",
                        "Vous approchez de la limite de " + COMMENTAIRE_MAX_LENGTH + " caractères pour le commentaire.");
            }

            if (!newValue.isEmpty() && !COMMENTAIRE_PATTERN.matcher(newValue).matches()) {
                commentaireArea.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            } else {
                commentaireArea.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
            }

            updateCommentaireIndicators(newValue);
        });
    }

    private void updateCommentaireIndicators(String text) {
        if (text.length() >= COMMENTAIRE_WARNING_LENGTH) {
            commentaireArea.setStyle("-fx-background-color: #FFFACD; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        } else if (text.length() < COMMENTAIRE_MIN_LENGTH) {
            commentaireArea.setStyle("-fx-background-color: #FFE4E1; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        } else {
            commentaireArea.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        }
    }

    public void setHistoriqueTraitement(historique_traitement historiqueTraitement) {
        this.historiqueTraitement = historiqueTraitement;

        if (historiqueTraitement != null) {
            nomPatientLabel.setText(historiqueTraitement.getNom());
            prenomPatientLabel.setText(historiqueTraitement.getPrenom());

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
                                Image placeholderIcon = new Image(getClass().getResourceAsStream("/images/error_icon.png"));
                                if (placeholderIcon != null) {
                                    bilanImageView.setImage(placeholderIcon);
                                }
                            } catch (Exception ex) {
                                // Ignore
                            }
                        }
                    } else {
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

    public void setOnSuiviAddedCallback(Runnable callback) {
        this.onSuiviAddedCallback = callback;
    }

    private boolean validateInputs() {
        StringBuilder erreurs = new StringBuilder();

        if (datePicker.getValue() == null) {
            erreurs.append("- Veuillez sélectionner une date.\n");
        } else {
            LocalDate selectedDate = datePicker.getValue();
            LocalDate today = LocalDate.now();
            LocalDate maxFutureDate = today.plusYears(1);

            if (selectedDate.isBefore(today)) {
                erreurs.append("- La date ne peut pas être antérieure à aujourd'hui.\n");
            } else if (selectedDate.isAfter(maxFutureDate)) {
                erreurs.append("- La date ne peut pas être fixée à plus d'un an dans le futur.\n");
            }
        }

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

            if (commentaire.contains("<script") || commentaire.contains("javascript:")) {
                erreurs.append("- Le commentaire contient des caractères potentiellement dangereux.\n");
            }

            if (!commentaire.matches(".*[.!?].*")) {
                erreurs.append("- Le commentaire doit contenir au moins une phrase complète terminée par un point, un point d'exclamation ou un point d'interrogation.\n");
            }
        }

        if (historiqueTraitement == null) {
            erreurs.append("- Aucun historique de traitement sélectionné.\n");
        }

        if (erreurs.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreurs de validation", erreurs.toString());
            return false;
        }

        return true;
    }

    private String sanitizeCommentaire(String commentaire) {
        if (commentaire == null) {
            return "";
        }

        String sanitized = commentaire.replaceAll("<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll(" {2,}", " ");

        if (!sanitized.isEmpty()) {
            sanitized = Character.toUpperCase(sanitized.charAt(0)) + sanitized.substring(1);
            if (!sanitized.matches(".*[.!?]$")) {
                sanitized = sanitized + ".";
            }
        }

        return sanitized;
    }

    @FXML
    private void ajouterSuivi(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            String dateStr = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String commentaireNettoye = sanitizeCommentaire(commentaireArea.getText().trim());

            Medecin currentMedecin = SessionManager.getInstance().getCurrentMedecin();
            if (currentMedecin == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun médecin connecté. Veuillez vous connecter en tant que médecin.");
                return;
            }

            suivie_medical nouveauSuivi = new suivie_medical(
                    dateStr,
                    commentaireNettoye,
                    historiqueTraitement,
                    currentMedecin
            );
            nouveauSuivi.setUser(currentMedecin);
            suivServices.add(nouveauSuivi);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Suivi médical ajouté avec succès pour le patient " +
                    historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom());

            redirectToListeSuivi();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de l'ajout du suivi médical: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToListeSuivi() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/liste_suivi.fxml"));
            Parent root = loader.load();

            // Configure the controller
            ListeSuiviController controller = loader.getController();
            controller.setHistoriqueTraitement(historiqueTraitement);

            // Get the current stage
            Stage stage = (Stage) datePicker.getScene().getWindow();

            // Set the title
            stage.setTitle("Suivis Médicaux - " + historiqueTraitement.getNom() + " " + historiqueTraitement.getPrenom());

            // Create a scene with full screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Set the scene
            stage.setScene(scene);

            // Ensure it's maximized
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la liste des suivis médicaux: " + e.getMessage());
        }
    }
    @FXML
    private void annuler(ActionEvent event) {
        try {
            if (!commentaireArea.getText().trim().isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation");
                confirmAlert.setHeaderText("Êtes-vous sûr de vouloir annuler ?");
                confirmAlert.setContentText("Les données saisies seront perdues.");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if (onSuiviAddedCallback != null) {
                            onSuiviAddedCallback.run();
                        }
                    }
                });
            } else {
                if (onSuiviAddedCallback != null) {
                    onSuiviAddedCallback.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (onSuiviAddedCallback != null) {
                onSuiviAddedCallback.run();
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}