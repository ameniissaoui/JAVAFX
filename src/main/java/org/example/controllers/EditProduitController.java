package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.Produit;
import org.example.services.ProduitServices;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

public class EditProduitController {

    // Keep idField as a private field but remove it from the UI
    private int productId;

    @FXML private TextField nom;
    @FXML private TextField description;
    @FXML private TextField prix;
    @FXML private TextField stock_quantite;
    @FXML private DatePicker date;
    @FXML private TextField image;
    @FXML private Button browseButton;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    private Produit currentProduct;
    private final String IMAGE_DIR = "src/resources/images/";
    private ProduitServices ps = new ProduitServices();

    @FXML
    void initialize() {
        // Make sure the image directory exists
        File directory = new File(IMAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Set current date as default if not set
        if (date.getValue() == null) {
            date.setValue(LocalDate.now());
        }
    }

    public void initData(Produit product) {
        this.currentProduct = product;
        this.productId = product.getId(); // Store ID internally

        // Fill the form with product data
        nom.setText(product.getNom());
        description.setText(product.getDescription());
        prix.setText(String.format("%.2f", product.getPrix()));
        stock_quantite.setText(String.valueOf(product.getStock_quantite()));

        if (product.getDate() != null) {
            date.setValue(product.getDate().toLocalDate());
        } else {
            date.setValue(LocalDate.now());
        }

        image.setText(product.getImage());
    }

    @FXML
    void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");

        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
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
                image.setText(uniqueFileName);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Image importée avec succès");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Échec de l'importation de l'image");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void save(ActionEvent event) {
        if (validateInput()) {
            try {
                float prixValue = Float.parseFloat(prix.getText().replace(',', '.'));
                int stockValue = Integer.parseInt(stock_quantite.getText());
                Date sqlDate = Date.valueOf(date.getValue());

                // Update the current product with new values
                currentProduct.setNom(nom.getText());
                currentProduct.setDescription(description.getText());
                currentProduct.setPrix(prixValue);
                currentProduct.setStock_quantite(stockValue);
                currentProduct.setDate(sqlDate);
                currentProduct.setImage(image.getText());

                ps.editProduit(currentProduct);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Produit mis à jour avec succès");
                navigateBack();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Échec de la mise à jour du produit: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    void retour(ActionEvent event) {
        navigateBack();
    }

    private boolean validateInput() {
        // Validate input
        if (nom.getText().isEmpty() || description.getText().isEmpty() ||
                prix.getText().isEmpty() || stock_quantite.getText().isEmpty() ||
                date.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Informations manquantes", "Veuillez remplir tous les champs obligatoires");
            return false;
        }

        try {
            Float.parseFloat(prix.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Prix invalide", "Veuillez entrer un prix valide");
            return false;
        }

        try {
            Integer.parseInt(stock_quantite.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Stock invalide", "Veuillez entrer une quantité valide");
            return false;
        }

        return true;
    }

    private void navigateBack() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/showProduit.fxml"));
            Parent root = loader.load();

            // Get the stage from any available control
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de retourner à la liste des produits.");

            // Attempt to find the FXML file in alternative locations
            tryAlternativeNavigation();
        }
    }

    private void tryAlternativeNavigation() {
        // Try alternative FXML paths if the primary one failed
        String[] possiblePaths = {
                "showProduit.fxml",
                "../showProduit.fxml",
                "/showProduit.fxml",
                "/views/showProduit.fxml",
                "/view/showProduit.fxml",
                "/fxml/showProduit.fxml"
        };

        for (String path : possiblePaths) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Parent root = loader.load();
                Stage stage = (Stage) nom.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                return; // Successfully navigated
            } catch (Exception e) {
                // Continue trying other paths
                System.out.println("Failed to load: " + path);
            }
        }

        // If all navigation attempts failed, show a more detailed error
        showAlert(Alert.AlertType.ERROR, "Erreur critique de navigation",
                "Impossible de naviguer vers la liste des produits. Veuillez redémarrer l'application.");
    }

    @FXML
    void showHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de naviguer vers l'accueil");
        }
    }

    @FXML
    void showProducts() {
        navigateBack(); // Navigate to products list
    }

    @FXML
    void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de se déconnecter");
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