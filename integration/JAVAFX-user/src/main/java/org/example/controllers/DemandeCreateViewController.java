package org.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.example.util.MainFX;
import org.example.services.DemandeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.util.SessionManager;

public class DemandeCreateViewController implements Initializable {

    @FXML
    private TextField eauField;
    
    @FXML
    private ToggleGroup repasToggleGroup;
    
    @FXML
    private RadioButton repas1RadioButton;
    
    @FXML
    private RadioButton repas2RadioButton;
    
    @FXML
    private RadioButton repas3RadioButton;
    
    @FXML
    private ToggleGroup snacksToggleGroup;
    
    @FXML
    private RadioButton snacksOuiRadioButton;
    
    @FXML
    private RadioButton snacksNonRadioButton;
    
    @FXML
    private ComboBox<String> caloriesComboBox;
    
    @FXML
    private ComboBox<String> activityComboBox;
    
    @FXML
    private TextField dureeField;
    
    @FXML
    private ComboBox<String> sommeilComboBox;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button switchRoleButton;
    
    private DemandeDAO demandeDAO;
    
    // Maps to store display value to actual value mappings
    private Map<String, Float> caloriesMapping;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        demandeDAO = new DemandeDAO();
        
        // Initialize combo boxes
        initializeCaloriesComboBox();
        initializeActivityComboBox();
        initializeSommeilComboBox();
    }
    
    private void initializeCaloriesComboBox() {
        ObservableList<String> caloriesOptions = FXCollections.observableArrayList(
            "1500-1700 kcal",
            "1800-2000 kcal",
            "2100-2300 kcal",
            "2400-2600 kcal",
            "2700-3000 kcal"
        );
        caloriesComboBox.setItems(caloriesOptions);
        
        // Map display values to actual values
        caloriesMapping = new HashMap<>();
        caloriesMapping.put("1500-1700 kcal", 1600f);
        caloriesMapping.put("1800-2000 kcal", 1900f);
        caloriesMapping.put("2100-2300 kcal", 2200f);
        caloriesMapping.put("2400-2600 kcal", 2500f);
        caloriesMapping.put("2700-3000 kcal", 2850f);
    }
    
    private void initializeActivityComboBox() {
        ObservableList<String> activityOptions = FXCollections.observableArrayList(
            "Aucune",
            "Marche",
            "Course à pied",
            "Cyclisme",
            "Natation",
            "Yoga",
            "Musculation",
            "Danse",
            "Escalade",
            "Gymnastique",
            "Autre"
        );
        activityComboBox.setItems(activityOptions);
    }
    
    private void initializeSommeilComboBox() {
        ObservableList<String> sommeilOptions = FXCollections.observableArrayList(
            "Très bon",
            "Bon",
            "Moyen",
            "Mauvais"
        );
        sommeilComboBox.setItems(sommeilOptions);
    }
    
    @FXML
    private void handleSubmitButton(ActionEvent event) {
        if (validateFields()) {
            try {
                // Get patient ID from session
                //int patientId = SessionManager.getCurrentPatientId();
                
                // Create new Demande object from form data
                Demande demande = new Demande();
                
                // Set the current date and time automatically
                demande.setDate(LocalDateTime.now());
                
                // Set the required fields
                demande.setEau(Float.parseFloat(eauField.getText()));
                
                // Get number of meals from radio buttons
                RadioButton selectedRepasRadio = (RadioButton) repasToggleGroup.getSelectedToggle();
                if (selectedRepasRadio == repas1RadioButton) {
                    demande.setNbr_repas(1);
                } else if (selectedRepasRadio == repas2RadioButton) {
                    demande.setNbr_repas(2);
                } else {
                    demande.setNbr_repas(3);
                }
                
                // Get snacks value from radio buttons
                RadioButton selectedSnacksRadio = (RadioButton) snacksToggleGroup.getSelectedToggle();
                demande.setSnacks(selectedSnacksRadio == snacksOuiRadioButton);
                
                // Set optional fields
                String selectedCalories = caloriesComboBox.getValue();
                if (selectedCalories != null) {
                    demande.setCalories(caloriesMapping.get(selectedCalories));
                }
                
                String selectedActivity = activityComboBox.getValue();
                if (selectedActivity != null) {
                    demande.setActivity(selectedActivity);
                }
                
                if (!dureeField.getText().isEmpty()) {
                    demande.setDuree_activite(Float.parseFloat(dureeField.getText()));
                }
                
                String selectedSommeil = sommeilComboBox.getValue();
                if (selectedSommeil != null) {
                    demande.setSommeil(selectedSommeil);
                }
                
                // Set the patient ID from the session
               // demande.setPatient_id(patientId);
                
                //System.out.println("Creating demande with patient ID: " + patientId);
                
                // Save the demande to the database
                boolean saved = demandeDAO.insert(demande);
                
                if (saved) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande créée", "Votre demande a été créée avec succès !");
                    
                    // Navigate back to DemandeMyView
                    navigateToDemandeMyView();
                } else {
                   showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création",
                            "La demande n'a pas pu être créée. Veuillez vérifier que l'ID patient (" + //patientId +
                            ") existe dans la base de données utilisateur.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de Format", "Format invalide", 
                        "Veuillez entrer des valeurs numériques valides pour les champs eau et durée.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création", 
                        "Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleCancelButton(ActionEvent event) {
        // Navigate back to DemandeMyView without saving
        navigateToDemandeMyView();
    }
    
    @FXML
    private void handleSwitchRole(ActionEvent event) {
        try {
            // Close the current window
            Stage currentStage = (Stage) switchRoleButton.getScene().getWindow();
            currentStage.close();
            
            // Restart the application to switch roles
            Stage primaryStage = new Stage();
            MainFX mainApp = new MainFX();
            mainApp.start(primaryStage);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de changer de rôle", 
                    "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateFields() {
        // Check required fields
        if (eauField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Champ manquant", "Veuillez entrer la consommation d'eau.");
            return false;
        }
        
        // Validate eau field (must be a positive number)
        try {
            float eau = Float.parseFloat(eauField.getText());
            if (eau <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Valeur invalide", 
                        "La consommation d'eau doit être un nombre positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Format invalide", 
                    "Veuillez entrer une valeur numérique pour la consommation d'eau.");
            return false;
        }
        
        // Validate duree field if not empty
        if (!dureeField.getText().isEmpty()) {
            try {
                float duree = Float.parseFloat(dureeField.getText());
                if (duree <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Valeur invalide", 
                            "La durée doit être un nombre positif.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Format invalide", 
                        "Veuillez entrer une valeur numérique pour la durée.");
                return false;
            }
        }
        
        return true;
    }
    
    private void navigateToDemandeMyView() {
        try {
            // Load the DemandeMyView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeMyView.fxml"));
            Parent root = loader.load();
            
            // Create new scene and show it
            Scene scene = new Scene(root);
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation", "Impossible de revenir à la vue principale: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 