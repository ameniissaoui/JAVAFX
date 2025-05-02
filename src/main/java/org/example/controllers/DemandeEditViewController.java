package org.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.example.services.DemandeDAO;
import org.example.util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.models.Demande;

public class DemandeEditViewController implements Initializable {

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
    private Button updateButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label eauErrorLabel;
    
    @FXML
    private Label dureeErrorLabel;
    
    private DemandeDAO demandeDAO;
    private Demande currentDemande;
    
    // Maps to store display value to actual value mappings
    private Map<String, Float> caloriesMapping;
    // Reverse mapping to convert from calories value to display value
    private Map<Float, String> reverseCaloriesMapping;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        demandeDAO = new DemandeDAO();
        
        // Initialize combo boxes
        initializeCaloriesComboBox();
        initializeActivityComboBox();
        initializeSommeilComboBox();
        
        // Set up button actions
        updateButton.setOnAction(event -> handleUpdateButton());
        cancelButton.setOnAction(event -> handleCancelButton());
        
        // Set up validation listeners
        setupValidationListeners();
    }
    
    /**
     * Set up real-time validation listeners for form fields
     */
    private void setupValidationListeners() {
        // Validation for eauField - must be a positive number
        eauField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                eauErrorLabel.setText("Ce champ est requis");
                eauErrorLabel.setTextFill(Color.RED);
            } else {
                try {
                    float value = Float.parseFloat(newValue);
                    if (value < 0) {
                        eauErrorLabel.setText("La valeur doit être positive");
                        eauErrorLabel.setTextFill(Color.RED);
                    } else {
                        eauErrorLabel.setText("");
                    }
                } catch (NumberFormatException e) {
                    eauErrorLabel.setText("Veuillez entrer un nombre valide");
                    eauErrorLabel.setTextFill(Color.RED);
                }
            }
        });
        
        // Validation for dureeField - must be a positive number if provided
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                dureeErrorLabel.setText("");
            } else {
                try {
                    float value = Float.parseFloat(newValue);
                    if (value < 0) {
                        dureeErrorLabel.setText("La valeur doit être positive");
                        dureeErrorLabel.setTextFill(Color.RED);
                    } else {
                        dureeErrorLabel.setText("");
                    }
                } catch (NumberFormatException e) {
                    dureeErrorLabel.setText("Veuillez entrer un nombre valide");
                    dureeErrorLabel.setTextFill(Color.RED);
                }
            }
        });
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
        
        // Create reverse mapping
        reverseCaloriesMapping = new HashMap<>();
        for (Map.Entry<String, Float> entry : caloriesMapping.entrySet()) {
            reverseCaloriesMapping.put(entry.getValue(), entry.getKey());
        }
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
    
    /**
     * Sets the demande to be edited and populates the form fields
     * Called from the DemandeMyViewController when navigating to this view
     * 
     * @param demande The demande to edit
     */
    public void setDemande(Demande demande) {
        this.currentDemande = demande;
        
        // Populate the form fields with the demande data
        if (demande != null) {
            eauField.setText(String.valueOf(demande.getEau()));
            
            // Set the correct radio button for repas
            switch (demande.getNbr_repas()) {
                case 1:
                    repas1RadioButton.setSelected(true);
                    break;
                case 2:
                    repas2RadioButton.setSelected(true);
                    break;
                case 3:
                default:
                    repas3RadioButton.setSelected(true);
                    break;
            }
            
            // Set the correct radio button for snacks
            if (demande.isSnacks()) {
                snacksOuiRadioButton.setSelected(true);
            } else {
                snacksNonRadioButton.setSelected(true);
            }
            
            // Set calories combobox
            if (demande.getCalories() != null) {
                String caloriesDisplay = reverseCaloriesMapping.get(demande.getCalories());
                if (caloriesDisplay != null) {
                    caloriesComboBox.setValue(caloriesDisplay);
                } else {
                    // Find the closest match if the exact value is not in the mapping
                    caloriesComboBox.setValue(findClosestCaloriesMatch(demande.getCalories()));
                }
            }
            
            // Set activity combobox
            if (demande.getActivity() != null && !demande.getActivity().isEmpty()) {
                activityComboBox.setValue(demande.getActivity());
            }
            
            dureeField.setText(String.valueOf(demande.getDuree_activite()));
            
            // Set sommeil combobox
            if (demande.getSommeil() != null && !demande.getSommeil().isEmpty()) {
                sommeilComboBox.setValue(demande.getSommeil());
            }
        }
    }
    
    /**
     * Find the closest calories match in the mapping
     */
    private String findClosestCaloriesMatch(Float calories) {
        String closestMatch = null;
        float minDifference = Float.MAX_VALUE;
        
        for (Map.Entry<String, Float> entry : caloriesMapping.entrySet()) {
            float difference = Math.abs(entry.getValue() - calories);
            if (difference < minDifference) {
                minDifference = difference;
                closestMatch = entry.getKey();
            }
        }
        
        return closestMatch;
    }
    
    @FXML
    private void handleUpdateButton() {
        if (validateFields()) {
            try {
                // Update the demande object with the form values
                currentDemande.setEau(Float.parseFloat(eauField.getText()));
                
                // Get number of meals from radio buttons
                RadioButton selectedRepasRadio = (RadioButton) repasToggleGroup.getSelectedToggle();
                if (selectedRepasRadio == repas1RadioButton) {
                    currentDemande.setNbr_repas(1);
                } else if (selectedRepasRadio == repas2RadioButton) {
                    currentDemande.setNbr_repas(2);
                } else {
                    currentDemande.setNbr_repas(3);
                }
                
                // Get snacks value from radio buttons
                RadioButton selectedSnacksRadio = (RadioButton) snacksToggleGroup.getSelectedToggle();
                currentDemande.setSnacks(selectedSnacksRadio == snacksOuiRadioButton);
                
                // Set optional fields
                String selectedCalories = caloriesComboBox.getValue();
                if (selectedCalories != null) {
                    currentDemande.setCalories(caloriesMapping.get(selectedCalories));
                }
                
                String selectedActivity = activityComboBox.getValue();
                if (selectedActivity != null) {
                    currentDemande.setActivity(selectedActivity);
                }
                
                if (!dureeField.getText().isEmpty()) {
                    currentDemande.setDuree_activite(Float.parseFloat(dureeField.getText()));
                }
                
                String selectedSommeil = sommeilComboBox.getValue();
                if (selectedSommeil != null) {
                    currentDemande.setSommeil(selectedSommeil);
                }
                
                // Save the updated demande to the database
                boolean updated = demandeDAO.update(currentDemande);
                
                if (updated) {
                    NotificationUtil.showSuccess("Succès", "Votre demande a été mise à jour avec succès !");
                    
                    // Navigate back to DemandeMyView
                    navigateToDemandeMyView();
                } else {
                    NotificationUtil.showError("Échec de la mise à jour", 
                            "La demande n'a pas pu être mise à jour. Veuillez vérifier que l'ID patient (" + 
                            currentDemande.getPatient_id() + ") existe dans la base de données utilisateur.");
                }
            } catch (NumberFormatException e) {
                NotificationUtil.showError("Format invalide", 
                        "Veuillez entrer des valeurs numériques valides pour les champs eau, nombre de repas, calories et durée.");
            } catch (Exception e) {
                NotificationUtil.showError("Échec de la mise à jour", "Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleCancelButton() {
        // Navigate back to DemandeMyView without saving
        navigateToDemandeMyView();
    }
    
    private boolean validateFields() {
        StringBuilder errorMsg = new StringBuilder();
        
        // Check for empty required fields
        if (eauField.getText().isEmpty()) {
            errorMsg.append("Le champ 'Eau consommée' est requis.\n");
        }
        
        // Check if a repas option is selected
        if (repasToggleGroup.getSelectedToggle() == null) {
            errorMsg.append("Veuillez sélectionner le nombre de repas.\n");
        }
        
        // Check if a snacks option is selected
        if (snacksToggleGroup.getSelectedToggle() == null) {
            errorMsg.append("Veuillez indiquer si vous consommez des snacks.\n");
        }
        
        // Validate numeric fields
        try {
            if (!eauField.getText().isEmpty()) {
                float eau = Float.parseFloat(eauField.getText());
                if (eau < 0) {
                    errorMsg.append("La consommation d'eau ne peut pas être négative.\n");
                }
            }
            
            if (!dureeField.getText().isEmpty()) {
                float duree = Float.parseFloat(dureeField.getText());
                if (duree < 0) {
                    errorMsg.append("La durée ne peut pas être négative.\n");
                }
            }
        } catch (NumberFormatException e) {
            errorMsg.append("Veuillez entrer des valeurs numériques valides pour les champs eau et durée.\n");
        }
        
        // If there are errors, show them and return false
        if (errorMsg.length() > 0) {
            NotificationUtil.showWarning("Champs invalides", errorMsg.toString());
            return false;
        }
        
        return true;
    }
    
    private void navigateToDemandeMyView() {
        try {
            // Load the FXML file for DemandeMyView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeMyView.fxml"));
            Parent root = loader.load();
            
            // Create a new scene with the loaded FXML
            Scene scene = new Scene(root);
            
            // Get the stage from the current button
            Stage stage = (Stage) updateButton.getScene().getWindow();
            
            // Set the new scene on the stage
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            NotificationUtil.showError("Erreur de Navigation", 
                    "Impossible de retourner à la page principale des demandes: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 