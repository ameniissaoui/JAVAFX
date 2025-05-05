package org.example.controllers;

import com.sun.javafx.menu.MenuItemBase;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Medecin;
import org.example.services.MedecinService;
import org.example.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.UUID;

public class MedecinProfileController extends BaseProfileController {
    @FXML private TextField specialiteField;
    @FXML private Label diplomaPathLabel;
    @FXML private ImageView diplomaImageView;
    @FXML private Label specialiteErrorLabel;
    @FXML private Button downloadDiplomaButton;
    private final MedecinService medecinService = new MedecinService();
    private File selectedDiplomaFile;
    private Medecin currentMedecin;
    @FXML
    private AnchorPane sidebarContainer;
    private boolean sidebarOpen = false;
    @FXML
    private Button mesTraitementsButton;

    @FXML
    private Button mesReservationsButton;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isMedecin()) {
            showMessage("Erreur: Accès non autorisé", "danger");
            handleLogout();
            return;
        }

        Medecin medecin = SessionManager.getInstance().getCurrentMedecin();
        if (medecin != null) {
            currentMedecin = medecin;
            currentUser = medecin;
            loadUserData();
            loadMedecinData();
        }

        setUserService();
        super.initialize(url, resourceBundle);
        // Make sure sidebar is initially hidden
        if (sidebarContainer != null) {
            sidebarContainer.setVisible(false);
            sidebarContainer.setPrefWidth(0);
        }

        // Check if sidebar buttons are properly initialized before setting event handlers
        if (mesTraitementsButton != null) {
            mesTraitementsButton.setOnAction(event -> {
                // Handle mes traitements button click
                System.out.println("Mes Traitements clicked");
            });
        }
        mesTraitementsButton.setOnAction(this::goToListeSuivi);
        if (mesReservationsButton != null) {
            mesReservationsButton.setOnAction(event -> {
                // Handle mes reservations button click
                System.out.println("Mes Réservations clicked");
            });
        }
    }

    @Override
    protected void setUserService() {
        this.userService = medecinService;
    }

    public void setMedecin(Medecin medecin) {
        this.currentMedecin = medecin;
        super.setUser(medecin, "medecin");
        loadMedecinData();
        setUserService();
    }

    private void loadMedecinData() {
        if (currentMedecin != null) {
            specialiteField.setText(currentMedecin.getSpecialite());

            String diplomaPath = currentMedecin.getDiploma();
            if (diplomaPath != null && !diplomaPath.isEmpty()) {
                diplomaPathLabel.setText(Paths.get(diplomaPath).getFileName().toString());

                try {
                    if (diplomaPath.toLowerCase().endsWith(".png") ||
                            diplomaPath.toLowerCase().endsWith(".jpg") ||
                            diplomaPath.toLowerCase().endsWith(".jpeg")) {
                        Image diplomaImage = new Image(new File(diplomaPath).toURI().toString());
                        diplomaImageView.setImage(diplomaImage);
                    } else {
                        Image pdfIcon = new Image(getClass().getResourceAsStream("/images/pdf-icon.png"));
                        diplomaImageView.setImage(pdfIcon);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading diploma image: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void handleBrowseDiploma() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner votre diplôme");
        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter("Documents PDF (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().addAll(pdfFilter, imageFilter);

        File file = fileChooser.showOpenDialog(specialiteField.getScene().getWindow());
        if (file != null) {
            long fileSize = file.length();
            long maxSize = 5 * 1024 * 1024;

            if (fileSize > maxSize) {
                showMessage("Le fichier est trop volumineux (max 5MB)", "danger");
                return;
            }

            selectedDiplomaFile = file;
            diplomaPathLabel.setText(file.getName());

            if (file.getName().toLowerCase().endsWith(".png") ||
                    file.getName().toLowerCase().endsWith(".jpg") ||
                    file.getName().toLowerCase().endsWith(".jpeg")) {
                try {
                    Image newImage = new Image(file.toURI().toString());
                    diplomaImageView.setImage(newImage);
                } catch (Exception e) {
                    System.err.println("Error loading preview: " + e.getMessage());
                }
            } else {
                try {
                    Image pdfIcon = new Image(getClass().getResourceAsStream("/images/pdf-icon.png"));
                    diplomaImageView.setImage(pdfIcon);
                } catch (Exception e) {
                    System.err.println("Error loading PDF icon: " + e.getMessage());
                }
            }
        }
    }

    @Override
    protected boolean validateFields() {
        boolean baseFieldsValid = super.validateFields();
        boolean medecinFieldsValid = validateMedecinFields();
        return baseFieldsValid && medecinFieldsValid;
    }

    private boolean validateMedecinFields() {
        boolean isValid = true;

        if (specialiteField.getText() == null || specialiteField.getText().trim().isEmpty()) {
            specialiteField.setStyle("-fx-border-color: red;");
            if (specialiteErrorLabel != null) {
                specialiteErrorLabel.setVisible(true);
            }
            isValid = false;
        } else {
            specialiteField.setStyle("");
            if (specialiteErrorLabel != null) {
                specialiteErrorLabel.setVisible(false);
            }
        }

        return isValid;
    }

    @Override
    protected void updateUserData() {
        super.updateUserData();

        if (currentMedecin != null) {
            currentMedecin.setSpecialite(specialiteField.getText().trim());

            if (selectedDiplomaFile != null) {
                try {
                    String diplomaPath = saveDiplomaFile();
                    currentMedecin.setDiploma(diplomaPath);
                } catch (IOException e) {
                    showMessage("Erreur lors de l'enregistrement du diplôme: " + e.getMessage(), "danger");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void saveUser() {
        if (currentMedecin != null) {
            try {
                medecinService.update(currentMedecin);
                SessionManager.getInstance().setCurrentUser(currentMedecin, "medecin");
                updateDisplayLabels();
                showMessage("Profil médecin mis à jour avec succès", "success");
            } catch (Exception e) {
                showMessage("Erreur lors de la mise à jour du profil: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        }
    }

    private String saveDiplomaFile() throws IOException {
        if (selectedDiplomaFile == null) {
            return currentMedecin.getDiploma();
        }

        Path uploadDir = Paths.get("uploads", "diplomas");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = selectedDiplomaFile.getName();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        Path targetPath = uploadDir.resolve(uniqueFilename);
        Files.copy(selectedDiplomaFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    @FXML
    private void showDiplomaDetails() {
        if (currentMedecin != null && currentMedecin.getDiploma() != null) {
            try {
                File diplomaFile = new File(currentMedecin.getDiploma());
                if (diplomaFile.exists()) {
                    java.awt.Desktop.getDesktop().open(diplomaFile);
                } else {
                    showMessage("Le fichier du diplôme est introuvable", "danger");
                }
            } catch (IOException e) {
                showMessage("Impossible d'ouvrir le fichier: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        } else {
            showMessage("Aucun diplôme disponible", "info");
        }
    }

    @FXML
    private void navigateToRecom(ActionEvent event) {
        SceneManager.loadScene("/fxml/MedecinRecommendations.fxml", event);
    }
    @FXML
    private void navigateToProduit(ActionEvent event) {
        SceneManager.loadScene("/fxml/front/showProduit.fxml", event);
    }

    @FXML
    public void redirectToSuivi(ActionEvent event) {
        try {
            // Check if user is logged in and is a medecin
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert("Vous devez être connecté pour accéder à cette page.", "error");
                return;
            }

            if (!SessionManager.getInstance().isMedecin()) {
                showAlert("Seuls les médecins peuvent accéder à cette fonctionnalité.", "error");
                return;
            }

            // Get the current medecin
            Medecin medecin = SessionManager.getInstance().getCurrentMedecin();
            if (medecin == null) {
                showAlert("Impossible de récupérer les informations du médecin.", "error");
                return;
            }

            // Use SceneManager to load the liste_historique page in full screen
            String fxmlPath = "/fxml/liste_historique.fxml";
            SceneManager.loadScene(fxmlPath, event);

            // Get the controller and pass the medecin if needed
            try {
                ListeHistoriqueController controller = SceneManager.getController(fxmlPath);
                // If needed, set the medecin in the controller
                // controller.setMedecin(medecin);
            } catch (IOException e) {
                System.err.println("Error getting controller: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Error during navigation to liste historique: " + e.getMessage());
            e.printStackTrace();
            showAlert("Impossible de charger la page de liste d'historique: " + e.getMessage(), "error");
        }
    }

    @FXML
    public void redirectToPlanning(ActionEvent event) {
        SceneManager.loadScene("/fxml/planning-view.fxml", event);

    }
    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_medecin.fxml", event);
    }
    @FXML
    public void toggleSidebar() {
        if (sidebarOpen) {
            // Close sidebar with animation
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(sidebarContainer.prefWidthProperty(), 0, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(250), kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(e -> sidebarContainer.setVisible(false)); // Hide after animation completes
            timeline.play();
            sidebarOpen = false;
        } else {
            // Make visible before opening
            sidebarContainer.setVisible(true);
            sidebarContainer.setPrefWidth(0);

            // Open sidebar with animation
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(sidebarContainer.prefWidthProperty(), 220, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(250), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
            sidebarOpen = true;
        }
    }

    @FXML
    private void goToListeSuivi(ActionEvent event) {
        try {
            // Spécifier le chemin correct vers le fichier FXML de la liste des suivis
            // Assurez-vous que ce chemin est correct par rapport à la structure de votre projet
            String fxmlPath = "/fxml/liste_historique.fxml";
            // ou "/org/example/fxml/liste_suivi.fxml" selon votre structure

            // Vérifier si la ressource existe
            URL location = getClass().getResource(fxmlPath);
            if (location == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de chargement");
                alert.setHeaderText("Fichier FXML introuvable");
                alert.setContentText("Le fichier liste_suivi.fxml n'a pas été trouvé dans le chemin spécifié.");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent listeSuiviView = loader.load();

            // Obtenir la scène actuelle
            Scene currentScene = ((Node) event.getSource()).getScene();

            // Remplacer le contenu de la scène par la vue de la liste des suivis
            currentScene.setRoot(listeSuiviView);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page des suivis");
            alert.setContentText("Une erreur s'est produite: " + e.getMessage());
            alert.showAndWait();
        }
    }
}