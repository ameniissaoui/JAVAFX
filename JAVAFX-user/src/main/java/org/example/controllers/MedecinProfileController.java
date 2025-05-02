package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
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
    @FXML private Label cartCountLabel;
    @FXML private Label cartCountLabel1;

    private final MedecinService medecinService = new MedecinService();
    private File selectedDiplomaFile;
    private Medecin currentMedecin;

    @FXML
    void initialize() {
        // Initialize any necessary components
        updateCartCount();

        // Maximize the stage when the view is loaded
        javafx.application.Platform.runLater(() -> {
            if (cartCountLabel != null && cartCountLabel.getScene() != null) {
                Stage stage = (Stage) cartCountLabel.getScene().getWindow();
                maximizeStage(stage);
            }
        });
    }

    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }

    private void updateCartCount() {
        // This would typically fetch the cart count from a service
        // For now, just set it to 0
        if (cartCountLabel != null) {
            cartCountLabel.setText("0");
        }
        if (cartCountLabel1 != null) {
            cartCountLabel1.setText("0");
        }
    }

    // Navigation methods from SuccessController
    @FXML
    void navigateToHome() {
        navigateTo("/fxml/front/home.fxml");
    }

    @FXML
    void navigateToHistoriques() {
        navigateTo("/fxml/historiques.fxml");
    }

    @FXML
    void redirectToDemande() {
        navigateTo("/fxml/DemandeDashboard.fxml");
    }

    @FXML
    void redirectToRendezVous() {
        navigateTo("/fxml/rendez-vous-view.fxml");
    }

    @FXML
    void redirectProduit() {
        navigateTo("/fxml/front/showProduit.fxml");
    }

    @FXML
    void navigateToTraitement() {
        navigateTo("/fxml/traitement.fxml");
    }

    @FXML
    void viewDoctors() {
        navigateTo("/fxml/DoctorList.fxml");
    }

    @FXML
    void navigateToContact() {
        navigateTo("/fxml/front/contact.fxml");
    }

    @FXML
    void navigateToProfile() {
        navigateTo("/fxml/front/profile.fxml");
    }

    @FXML
    void navigateToFavorites() {
        navigateTo("/fxml/front/favoris.fxml");
    }

    @FXML
    void commande() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToCommandes() {
        navigateTo("/fxml/front/ShowCommande.fxml");
    }

    @FXML
    void navigateToShop() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            System.out.println("Attempting to navigate to " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException(fxmlPath + " resource not found");
            }
            Parent root = loader.load();

            // Get the current stage
            Stage stage = null;
            if (cartCountLabel != null && cartCountLabel.getScene() != null) {
                stage = (Stage) cartCountLabel.getScene().getWindow();
            } else if (cartCountLabel1 != null && cartCountLabel1.getScene() != null) {
                stage = (Stage) cartCountLabel1.getScene().getWindow();
            }

            if (stage == null) {
                throw new RuntimeException("Cannot get current stage");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
            System.out.println("Successfully navigated to " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page", e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during navigation: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

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

        // Initialize cart count
        updateCartCount();
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
    private void navigateToRecommendations() {
        navigateTo("/fxml/MedecinRecommendations.fxml");
    }

    @FXML
    private void navigateToPlanning() {
        navigateTo("/fxml/planning-view.fxml");
    }
}