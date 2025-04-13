package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Commande;
import models.Produit;
import services.CommandeServices;

public class AddCommandeController implements Initializable {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField adresseField;
    @FXML
    private Button submitButton;
    @FXML
    private Button submitButton1;
    @FXML
    private Label nomError;
    @FXML
    private Label prenomError;
    @FXML
    private Label emailError;
    @FXML
    private Label phoneError;
    @FXML
    private Label adresseError;
    @FXML
    private Label productNameLabel;

    private CommandeServices commandeService;
    private Produit selectedProduct;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        commandeService = new CommandeServices();

        // Clear error labels initially
        clearErrors();

        // Apply CSS style to error labels to make them red
        applyErrorLabelStyles();

        // Add focus listeners to clear errors when fields are edited
        setupFieldListeners();
    }

    private void applyErrorLabelStyles() {
        // Apply red text style to all error labels
        String errorStyle = "-fx-text-fill: red; -fx-font-size: 12px;";
        nomError.setStyle(errorStyle);
        prenomError.setStyle(errorStyle);
        emailError.setStyle(errorStyle);
        phoneError.setStyle(errorStyle);
        adresseError.setStyle(errorStyle);
    }

    private void setupFieldListeners() {
        // Clear error when user starts typing in a field
        nomField.textProperty().addListener((observable, oldValue, newValue) -> nomError.setText(""));
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> prenomError.setText(""));
        emailField.textProperty().addListener((observable, oldValue, newValue) -> emailError.setText(""));
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> phoneError.setText(""));
        adresseField.textProperty().addListener((observable, oldValue, newValue) -> adresseError.setText(""));
    }

    public void setSelectedProduct(Produit produit) {
        this.selectedProduct = produit;
        if (produit != null && productNameLabel != null) {
            productNameLabel.setText("Produit: " + produit.getNom() + " - " + String.format("%.2f TND", produit.getPrix()));
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        // Clear previous error messages
        clearErrors();

        // Validate inputs
        boolean isValid = validateInputs();

        if (isValid) {
            try {
                // Create a new Commande object
                Commande commande = new Commande();
                commande.setNom(nomField.getText().trim());
                commande.setPrenom(prenomField.getText().trim());
                commande.setEmail(emailField.getText().trim());
                commande.setAdresse(adresseField.getText().trim());
                commande.setPhone_number(Integer.parseInt(phoneField.getText().trim()));

                // Set the selected product
                commande.setProduit(selectedProduct);

                // Save to database
                commandeService.addProduit(commande);

                // Show success message or close window
                showAlert("Commande enregistrée avec succès!", "success");

                // Clear form or close window
                clearForm();

                // Optionally close the window


            } catch (NumberFormatException e) {
                phoneError.setText("Le numéro de téléphone doit être un nombre valide");
            } catch (Exception e) {
                showAlert("Erreur lors de l'enregistrement: " + e.getMessage(), "error");
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate Nom
        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            nomField.setStyle("");
        }

        // Validate Prénom
        if (prenomField.getText().trim().isEmpty()) {
            prenomError.setText("Le prénom est obligatoire");
            prenomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            prenomField.setStyle("");
        }

        // Validate Email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("L'email est obligatoire");
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailError.setText("Format d'email invalide");
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            emailField.setStyle("");
        }

        // Validate Phone
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            phoneError.setText("Le téléphone est obligatoire");
            phoneField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            try {
                int phoneNumber = Integer.parseInt(phone);
                if (phone.length() != 8) {
                    phoneError.setText("Le téléphone doit avoir 8 chiffres");
                    phoneField.setStyle("-fx-border-color: red;");
                    isValid = false;
                } else {
                    phoneField.setStyle("");
                }
            } catch (NumberFormatException e) {
                phoneError.setText("Le téléphone doit être un nombre");
                phoneField.setStyle("-fx-border-color: red;");
                isValid = false;
            }
        }

        // Validate Adresse
        if (adresseField.getText().trim().isEmpty()) {
            adresseError.setText("L'adresse est obligatoire");
            adresseField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            adresseField.setStyle("");
        }

        // Also validate that a product is selected
        if (selectedProduct == null) {
            showAlert("Aucun produit sélectionné", "error");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        // Clear all error messages
        nomError.setText("");
        prenomError.setText("");
        emailError.setText("");
        phoneError.setText("");
        adresseError.setText("");

        // Reset field styles
        nomField.setStyle("");
        prenomField.setStyle("");
        emailField.setStyle("");
        phoneField.setStyle("");
        adresseField.setStyle("");
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        phoneField.clear();
        adresseField.clear();
    }

    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message, String type) {
        // You can implement this method to show alerts
        // This could use JavaFX Alert or a custom alert dialog
        System.out.println(type.toUpperCase() + ": " + message);

        // Example implementation with JavaFX Alert:
        // Alert alert = new Alert(type.equals("error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        // alert.setTitle(type.equals("error") ? "Erreur" : "Succès");
        // alert.setHeaderText(null);
        // alert.setContentText(message);
        // alert.showAndWait();
    }

    @FXML
    void show(ActionEvent event) {
        try {
            // Make sure to provide the correct path to your FXML file
            URL fxmlLocation = getClass().getResource("/front/showCommande.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found!");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Create a new scene and stage
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Commandes");

            // Set the new stage to maximized
            newStage.setMaximized(true);

            // Show the new stage
            newStage.show();

            // Close the current stage
            Stage currentStage = (Stage) submitButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening commandes view: " + e.getMessage(), "error");
        }
    }
}