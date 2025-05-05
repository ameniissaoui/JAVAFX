package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.User;
import org.example.models.Admin;
import org.example.models.Patient;
import org.example.models.Medecin;
import org.example.services.UserService;
import org.example.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.prefs.Preferences;

public abstract class BaseProfileController implements Initializable {
    @FXML protected Label usernameField;
    @FXML protected TextField nomField;
    @FXML protected TextField prenomField;
    @FXML protected TextField emailField;
    @FXML protected PasswordField passwordField;
    @FXML protected PasswordField confirmPasswordField;
    @FXML protected DatePicker dateNaissancePicker;
    @FXML protected TextField telephoneField;
    @FXML protected Label messageLabel;
    @FXML protected Label messageLabell;
    @FXML public Button submitButton;
    @FXML public Label fullNameLabel;
    @FXML private Button historique;
    @FXML private Label nomDisplayLabel;
    @FXML private Label prenomDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Label telephoneDisplayLabel;
    @FXML private Label dateNaissanceDisplayLabel;
    @FXML protected ImageView profileImageView; // Added for profile picture
    protected User currentUser;
    protected String userType;
    @FXML protected PasswordField currentPasswordField;
    protected UserService<?> userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing BaseProfileController...");
        System.out.println("nomField: " + nomField);
        System.out.println("prenomField: " + prenomField);
        System.out.println("emailField: " + emailField);
        System.out.println("currentPasswordField: " + currentPasswordField);
        System.out.println("passwordField: " + passwordField);
        System.out.println("confirmPasswordField: " + confirmPasswordField);
        System.out.println("dateNaissancePicker: " + dateNaissancePicker);
        System.out.println("telephoneField: " + telephoneField);
        System.out.println("messageLabel: " + messageLabel);
        System.out.println("profileImageView: " + profileImageView);

        // Check if user is already in session
        if (SessionManager.getInstance().isLoggedIn()) {
            this.currentUser = SessionManager.getInstance().getCurrentUser();
            this.userType = SessionManager.getInstance().getUserType();
            loadUserData();
        }

        historique.setOnAction(event -> handleHistoRedirect());
        if (messageLabell != null) {
            messageLabell.setVisible(false);
        }

        // Load profile picture if available
        if (currentUser != null && currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getImage());
                if (imageFile.exists() && profileImageView != null) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);

                    // Apply circular clip to the image view
                    Circle clip = new Circle(profileImageView.getFitWidth() / 2,
                            profileImageView.getFitHeight() / 2,
                            Math.min(profileImageView.getFitWidth(), profileImageView.getFitHeight()) / 2);
                    profileImageView.setClip(clip);

                    // Ensure the image preserves ratio and fits properly
                    profileImageView.setPreserveRatio(true);
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        } else if (profileImageView != null) {
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/avatar_paceholder.jpg"));
                profileImageView.setImage(defaultImage);

                // Apply circular clip to the default image view
                Circle clip = new Circle(profileImageView.getFitWidth() / 2,
                        profileImageView.getFitHeight() / 2,
                        Math.min(profileImageView.getFitWidth(), profileImageView.getFitHeight()) / 2);
                profileImageView.setClip(clip);

                // Ensure the image preserves ratio and fits properly
                profileImageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Could not load default profile image: " + e.getMessage());
            }
        }
    }

    public void handleHistoRedirect() {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showErrorDialog("Erreur", "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_historique.fxml"));
            Stage stage = (Stage) historique.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected abstract void setUserService();

    public void setUser(User user) {
        this.currentUser = user;

        if (user instanceof Admin) {
            this.userType = "admin";
        } else if (user instanceof Patient) {
            this.userType = "patient";
        } else if (user instanceof Medecin) {
            this.userType = "medecin";
        }

        loadUserData();
        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user, this.userType);
        }
    }

    @FXML
    protected void handleChangePassword() {
        if (validatePasswordFields()) {
            try {
                userService.updatePassword(currentUser.getId(), passwordField.getText());
                currentPasswordField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                showMessage("Mot de passe mis à jour avec succès. Veuillez vous reconnecter.", "success");
                handleLogout();
            } catch (Exception e) {
                showMessage("Erreur lors de la mise à jour du mot de passe: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void handleSavePassword() {
        if (validatePasswordFields()) {
            currentUser.setMotDePasse(passwordField.getText());
            saveUser();

            SessionManager.getInstance().setCurrentUser(currentUser, userType);

            currentPasswordField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

            showMessage("Mot de passe mis à jour avec succès", "success");
        }
    }

    protected boolean validatePasswordFields() {
        boolean isValid = true;

        if (currentPasswordField.getText() == null || currentPasswordField.getText().isEmpty()) {
            currentPasswordField.setStyle("-fx-border-color: red;");
            showMessage("Veuillez entrer votre mot de passe actuel", "danger");
            isValid = false;
        } else if (!userService.verifyPassword(currentUser.getId(), currentPasswordField.getText())) {
            currentPasswordField.setStyle("-fx-border-color: red;");
            showMessage("Mot de passe actuel incorrect", "danger");
            isValid = false;
        } else {
            currentPasswordField.setStyle("");
        }

        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            passwordField.setStyle("-fx-border-color: red;");
            showMessage("Veuillez entrer un nouveau mot de passe", "danger");
            isValid = false;
        } else if (passwordField.getText().length() < 8) {
            passwordField.setStyle("-fx-border-color: red;");
            showMessage("Le nouveau mot de passe doit contenir au moins 8 caractères", "danger");
            isValid = false;
        } else if (!passwordField.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            passwordField.setStyle("-fx-border-color: red;");
            showMessage("Le mot de passe doit contenir des lettres majuscules, minuscules, chiffres et caractères spéciaux", "danger");
            isValid = false;
        } else {
            passwordField.setStyle("");
        }

        if (confirmPasswordField.getText() == null || confirmPasswordField.getText().isEmpty()) {
            confirmPasswordField.setStyle("-fx-border-color: red;");
            showMessage("Veuillez confirmer votre nouveau mot de passe", "danger");
            isValid = false;
        } else if (!confirmPasswordField.getText().equals(passwordField.getText())) {
            confirmPasswordField.setStyle("-fx-border-color: red;");
            showMessage("Les mots de passe ne correspondent pas", "danger");
            isValid = false;
        } else {
            confirmPasswordField.setStyle("");
        }

        if (!isValid && messageLabell != null && !messageLabell.isVisible()) {
            showMessage("Veuillez corriger les champs en rouge", "danger");
        }

        return isValid;
    }

    public void setUser(User user, String userType) {
        this.currentUser = user;
        this.userType = userType;
        loadUserData();
        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user, userType);
        }
    }

    protected void loadUserData() {
        if (currentUser != null) {
            String nom = currentUser.getNom();
            String prenom = currentUser.getPrenom();

            nomField.setText(nom);
            prenomField.setText(prenom);
            emailField.setText(currentUser.getEmail());
            nomDisplayLabel.setText(nom);
            prenomDisplayLabel.setText(prenom);
            emailDisplayLabel.setText(currentUser.getEmail());
            telephoneDisplayLabel.setText(currentUser.getTelephone());

            if (currentUser.getDateNaissance() != null) {
                LocalDate localDate;
                if (currentUser.getDateNaissance() instanceof java.sql.Date) {
                    localDate = ((java.sql.Date) currentUser.getDateNaissance()).toLocalDate();
                } else {
                    localDate = currentUser.getDateNaissance().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                }
                dateNaissancePicker.setValue(localDate);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateNaissanceDisplayLabel.setText(localDate.format(formatter));
            }
            telephoneField.setText(currentUser.getTelephone());
            fullNameLabel.setText(prenom + " " + nom);
        }
    }

    protected void updateDisplayLabels() {
        nomDisplayLabel.setText(currentUser.getNom());
        prenomDisplayLabel.setText(currentUser.getPrenom());
        emailDisplayLabel.setText(currentUser.getEmail());
        telephoneDisplayLabel.setText(currentUser.getTelephone());
        fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        if (currentUser.getDateNaissance() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dateNaissanceDisplayLabel.setText(sdf.format(currentUser.getDateNaissance()));
        }
    }

    @FXML private StackPane contentArea;

    @FXML
    private void handleEdit() {
        VBox editContent = new VBox();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(editContent);
    }

    @FXML
    protected void handleSave() {
        if (validateFields()) {
            updateUserData();
            saveUser();
            SessionManager.getInstance().setCurrentUser(currentUser, userType);
            showMessage("Profil mis à jour avec succès", "success");
            updateDisplayLabels();
        }
    }

    protected boolean validateFields() {
        boolean isValid = true;

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            nomField.setStyle("");
        }

        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            prenomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            prenomField.setStyle("");
        }

        if (emailField.getText() == null || !emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            emailField.setStyle("");
        }

        if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
            if (passwordField.getText().length() < 8) {
                passwordField.setStyle("-fx-border-color: red;");
                isValid = false;
            } else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                passwordField.setStyle("-fx-border-color: red;");
                confirmPasswordField.setStyle("-fx-border-color: red;");
                isValid = false;
            } else {
                passwordField.setStyle("");
                confirmPasswordField.setStyle("");
            }
        }

        if (dateNaissancePicker.getValue() == null) {
            dateNaissancePicker.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            dateNaissancePicker.setStyle("");
        }

        if (telephoneField.getText() == null || !telephoneField.getText().matches("^\\d{8}$")) {
            telephoneField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            telephoneField.setStyle("");
        }

        if (!isValid) {
            showMessage("Veuillez corriger les champs en rouge", "warning");
        }

        return isValid;
    }

    protected void updateUserData() {
        if (currentUser != null) {
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());

            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                currentUser.setMotDePasse(passwordField.getText());
            }

            LocalDate localDate = dateNaissancePicker.getValue();
            if (localDate != null) {
                try {
                    java.util.Date utilDate = java.util.Date.from(
                            localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    currentUser.setDateNaissance(utilDate);
                } catch (Exception e) {
                    try {
                        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
                        currentUser.setDateNaissance(sqlDate);
                    } catch (Exception ex) {
                        showMessage("Erreur lors de la conversion de la date", "danger");
                    }
                }
            }
            currentUser.setTelephone(telephoneField.getText().trim());
        }
    }

    protected abstract void saveUser();

    @FXML
    void redirectProduit(ActionEvent event) {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert("Vous devez être connecté pour accéder à cette page.", "error");
                return;
            }

            URL fxmlLocation = getClass().getResource("/fxml/front/showProduit.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found!");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Commandes");
            newStage.setMaximized(true);
            newStage.show();

            Stage currentStage = (Stage) nomField.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening commandes view: " + e.getMessage(), "error");
        }
    }

    private void showAlert(String message, String type) {
        Alert alert = new Alert(type.equals("error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(type.equals("error") ? "Erreur" : "Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleLogout() {
        SessionManager.getInstance().clearSession();

        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        prefs.remove("savedEmail");
        prefs.remove("savedPassword");
        prefs.putBoolean("rememberMe", false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

            Stage currentStage = (Stage) nomField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during logout: " + e.getMessage());
        }
    }

    protected void showMessage(String message, String type) {
        messageLabel.setText(message);

        switch (type) {
            case "danger":
                messageLabel.setStyle("-fx-text-fill: #d63031;");
                break;
            case "warning":
                messageLabel.setStyle("-fx-text-fill: #856404;");
                break;
            case "success":
                messageLabel.setStyle("-fx-text-fill: #155724;");
                break;
        }

        messageLabel.setVisible(true);
    }

    @FXML
    protected void handleUploadProfilePicture() {
        if (profileImageView == null) {
            showMessage("Erreur: Interface utilisateur non initialisée", "danger");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            long fileSize = file.length();
            long maxSize = 2 * 1024 * 1024; // 2MB

            if (fileSize > maxSize) {
                showMessage("La photo est trop volumineuse (max 2MB)", "danger");
                return;
            }

            try {
                Path uploadDir = Paths.get("uploads", "profiles");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String originalFilename = file.getName();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

                Path targetPath = uploadDir.resolve(uniqueFilename);
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                currentUser.setImage(targetPath.toString());
                saveUser();

                Image profileImage = new Image(file.toURI().toString());
                profileImageView.setImage(profileImage);
                // Better centering and clipping
                double size = Math.min(profileImageView.getFitWidth(), profileImageView.getFitHeight());
                Circle clip = new Circle(size/2, size/2, size/2);
                profileImageView.setClip(clip);

                // Ensure image fills the circle properly
                profileImageView.setPreserveRatio(false);
                showMessage("Photo de profil mise à jour", "success");
            } catch (IOException e) {
                showMessage("Erreur lors de l'enregistrement de la photo: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        }
    }
}