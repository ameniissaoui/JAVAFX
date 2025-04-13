package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Produit;
import services.ProduitServices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

public class AddProduitController {

    @FXML private TextField nom;
    @FXML private TextField description;
    @FXML private TextField prix;
    @FXML private TextField stock_quantite;
    @FXML private DatePicker date;
    @FXML private TextField image;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Error labels
    @FXML private Label nomError;
    @FXML private Label descriptionError;
    @FXML private Label prixError;
    @FXML private Label stockError;
    @FXML private Label dateError;
    @FXML private Label imageError;

    // Define the image directory path
    private final String IMAGE_DIR = "src/resources/images/";
    private ProduitServices ps = new ProduitServices();

    @FXML
    void initialize() {
        // Make sure the image directory exists
        File directory = new File(IMAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Set current date as default
        date.setValue(LocalDate.now());

        // Add real-time validation listeners
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Validate name field
        nom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                showFieldError(nom, nomError, "Le nom est obligatoire");
            } else if (newValue.length() < 3) {
                showFieldError(nom, nomError, "Le nom doit contenir au moins 3 caractères");
            } else {
                hideFieldError(nom, nomError);
            }
        });

        // Validate description field
        description.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                showFieldError(description, descriptionError, "La description est obligatoire");
            } else if (newValue.length() < 10) {
                showFieldError(description, descriptionError, "La description doit contenir au moins 10 caractères");
            } else {
                hideFieldError(description, descriptionError);
            }
        });

        // Validate prix field
        prix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                showFieldError(prix, prixError, "Le prix est obligatoire");
            } else {
                try {
                    float price = Float.parseFloat(newValue.replace(',', '.'));
                    if (price <= 0) {
                        showFieldError(prix, prixError, "Le prix doit être supérieur à 0");
                    } else {
                        hideFieldError(prix, prixError);
                    }
                } catch (NumberFormatException e) {
                    showFieldError(prix, prixError, "Format de prix invalide");
                }
            }
        });

        // Validate stock field
        stock_quantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                showFieldError(stock_quantite, stockError, "La quantité est obligatoire");
            } else {
                try {
                    int stock = Integer.parseInt(newValue);
                    if (stock < 0) {
                        showFieldError(stock_quantite, stockError, "La quantité ne peut pas être négative");
                    } else {
                        hideFieldError(stock_quantite, stockError);
                    }
                } catch (NumberFormatException e) {
                    showFieldError(stock_quantite, stockError, "La quantité doit être un nombre entier");
                }
            }
        });

        // Validate date field
        date.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                showFieldError(date, dateError, "La date est obligatoire");
            } else if (newValue.isBefore(LocalDate.now())) {
                showFieldError(date, dateError, "La date ne peut pas être dans le passé");
            } else {
                hideFieldError(date, dateError);
            }
        });

        // Add filter for number fields
        prix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prix.setText(oldValue);
            }
        });

        stock_quantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stock_quantite.setText(oldValue);
            }
        });

        // Add listener for image field
        image.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                showFieldError(image, imageError, "Veuillez sélectionner une image");
            } else {
                hideFieldError(image, imageError);
            }
        });
    }

    private void showFieldError(Control field, Label errorLabel, String message) {
        field.setStyle("-fx-border-color: #dc3545; -fx-border-width: 1px;");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideFieldError(Control field, Label errorLabel) {
        field.setStyle("");
        errorLabel.setVisible(false);
    }

    @FXML
    void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Fichiers Image", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String uniqueFileName = UUID.randomUUID().toString() +
                        selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));

                Path sourcePath = selectedFile.toPath();
                Path targetPath = Paths.get(IMAGE_DIR + uniqueFileName);

                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Store only the filename - we'll build the full path when loading
                image.setText(uniqueFileName);
                hideFieldError(image, imageError);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Image importée avec succès");
            } catch (IOException e) {
                showFieldError(image, imageError, "Échec de l'importation de l'image");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void save() {
        boolean isValid = validateForm();

        if (isValid) {
            try {
                float prixValue = Float.parseFloat(prix.getText().replace(',', '.'));
                int stockValue = Integer.parseInt(stock_quantite.getText());
                Date sqlDate = Date.valueOf(date.getValue());

                Produit produit = new Produit(
                        nom.getText(),
                        description.getText(),
                        prixValue,
                        stockValue,
                        sqlDate,
                        image.getText()  // Store just the filename
                );

                ps.addProduit(produit);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès");
                navigateBack();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du produit: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate nom
        if (nom.getText().trim().isEmpty()) {
            showFieldError(nom, nomError, "Le nom est obligatoire");
            isValid = false;
        } else if (nom.getText().length() < 3) {
            showFieldError(nom, nomError, "Le nom doit contenir au moins 3 caractères");
            isValid = false;
        }

        // Validate description
        if (description.getText().trim().isEmpty()) {
            showFieldError(description, descriptionError, "La description est obligatoire");
            isValid = false;
        } else if (description.getText().length() < 10) {
            showFieldError(description, descriptionError, "La description doit contenir au moins 10 caractères");
            isValid = false;
        }

        // Validate prix
        if (prix.getText().trim().isEmpty()) {
            showFieldError(prix, prixError, "Le prix est obligatoire");
            isValid = false;
        } else {
            try {
                float price = Float.parseFloat(prix.getText().replace(',', '.'));
                if (price <= 0) {
                    showFieldError(prix, prixError, "Le prix doit être supérieur à 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError(prix, prixError, "Format de prix invalide");
                isValid = false;
            }
        }

        // Validate stock
        if (stock_quantite.getText().trim().isEmpty()) {
            showFieldError(stock_quantite, stockError, "La quantité est obligatoire");
            isValid = false;
        } else {
            try {
                int stock = Integer.parseInt(stock_quantite.getText());
                if (stock < 0) {
                    showFieldError(stock_quantite, stockError, "La quantité ne peut pas être négative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError(stock_quantite, stockError, "La quantité doit être un nombre entier");
                isValid = false;
            }
        }

        // Validate date
        if (date.getValue() == null) {
            showFieldError(date, dateError, "La date est obligatoire");
            isValid = false;
        } else if (date.getValue().isBefore(LocalDate.now())) {
            showFieldError(date, dateError, "La date ne peut pas être dans le passé");
            isValid = false;
        }

        // Validate image
        if (image.getText().trim().isEmpty()) {
            showFieldError(image, imageError, "Veuillez sélectionner une image");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    void cancel() {
        navigateBack();
    }

    private void navigateBack() {
        try {
            // Get the resource URL correctly
            URL resourceUrl = getClass().getResource("/back/showProduit.fxml");
            if (resourceUrl == null) {
                throw new IOException("Resource not found: /back/showProduit.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de naviguer vers la page précédente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void showHome() {
        try {
            URL resourceUrl = getClass().getResource("/back/Home.fxml");
            if (resourceUrl == null) {
                throw new IOException("Resource not found: /back/Home.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de naviguer vers la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void showProducts() {
        try {
            URL resourceUrl = getClass().getResource("/back/showProduit.fxml");
            if (resourceUrl == null) {
                throw new IOException("Resource not found: /back/showProduit.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de naviguer vers la liste des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void logout() {
        try {
            URL resourceUrl = getClass().getResource("/front/Login.fxml");
            if (resourceUrl == null) {
                throw new IOException("Resource not found: /front/Login.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Déconnexion",
                    "Impossible de se déconnecter: " + e.getMessage());
            e.printStackTrace();
        }
    }
}