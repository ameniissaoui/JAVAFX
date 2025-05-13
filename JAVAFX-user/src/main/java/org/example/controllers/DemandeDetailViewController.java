package org.example.controllers;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.models.Recommandation;
import org.example.util.MainFX;

public class DemandeDetailViewController implements Initializable {

    // Patient info labels
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label patientIdLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label demandeIdLabel;
    
    // Lifestyle data labels
    @FXML
    private Label eauLabel;
    
    @FXML
    private Label repasLabel;
    
    @FXML
    private Label snacksLabel;
    
    @FXML
    private Label caloriesLabel;
    
    @FXML
    private Label activityLabel;
    
    @FXML
    private Label dureeLabel;
    
    @FXML
    private Label sommeilLabel;
    
    // Recommendation form fields
    @FXML
    private ComboBox<String> petitDejCombo;
    
    @FXML
    private ComboBox<String> dejeunerCombo;
    
    @FXML
    private ComboBox<String> dinerCombo;
    
    @FXML
    private ComboBox<String> activityCombo;
    
    @FXML
    private ComboBox<String> caloriesCombo;
    
    @FXML
    private ComboBox<String> dureeCombo;
    
    @FXML
    private ComboBox<String> supplementsCombo;
    
    // For showing/hiding form elements
    @FXML
    private javafx.scene.layout.GridPane petitDejGrid;
    
    @FXML
    private javafx.scene.layout.GridPane dejeunerGrid;
    
    @FXML
    private javafx.scene.layout.GridPane dinerGrid;
    
    @FXML
    private javafx.scene.layout.GridPane supplementsGrid;
    
    // Navigation buttons
    @FXML
    private Button backButton;
    
    @FXML
    private Button roleButton;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    // Controllers
    private DemandeController demandeController;
    private RecommandationController recommandationController;
    
    // Current demande
    private Demande currentDemande;
    
    // Maps to store display value to actual value mappings
    private Map<String, Float> caloriesMapping;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeController = new DemandeController();
        recommandationController = new RecommandationController();
        
        // Initialize caloriesMapping
        initializeCaloriesMapping();
        
        // Initialize the UI with options
        initializeFormOptions();
    }
    
    // Initialize options for the form fields
    private void initializeFormOptions() {
        // Initialize meal options
        initializePetitDejeunerOptions();
        initializeDejeunerOptions();
        initializeDinerOptions();
        
        // Initialize activity options
        initializeActivityOptions();
        
        // Initialize calorie options
        initializeCaloriesOptions();
        
        // Initialize supplements options (visibility will be handled later)
        initializeSupplementsOptions();
    }
    
    private void initializeCaloriesMapping() {
        caloriesMapping = new HashMap<>();
        caloriesMapping.put("1500-1700 kcal", 1600f);
        caloriesMapping.put("1800-2000 kcal", 1900f);
        caloriesMapping.put("2100-2300 kcal", 2200f);
        caloriesMapping.put("2400-2600 kcal", 2500f);
        caloriesMapping.put("2700-3000 kcal", 2850f);
    }
    
    private void initializePetitDejeunerOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "Croissant et café",
            "Pain complet et confiture",
            "Omelette et jus d'orange",
            "Yaourt et fruits",
            "Céréales et lait",
            "Smoothie aux fruits",
            "Toast avocat",
            "Porridge aux baies",
            "Muesli et yaourt",
            "Pancakes légers"
        );
        petitDejCombo.setItems(options);
    }
    
    private void initializeDejeunerOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "Salade de quinoa",
            "Poulet grillé et légumes",
            "Poisson et riz complet",
            "Sandwich au poulet",
            "Soupe de légumes",
            "Wrap végétarien",
            "Pâtes intégrales",
            "Bol de poke",
            "Salade César",
            "Riz sauté aux légumes"
        );
        dejeunerCombo.setItems(options);
    }
    
    private void initializeDinerOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "Steak et légumes",
            "Saumon et quinoa",
            "Ragoût de légumes",
            "Curry de poulet",
            "Pâtes à la bolognaise",
            "Pizza aux légumes",
            "Tacos de poisson",
            "Boeuf bourguignon",
            "Risotto aux champignons",
            "Gratin de courgettes"
        );
        dinerCombo.setItems(options);
    }
    
    private void initializeActivityOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "Marche rapide",
            "Natation",
            "Cyclisme",
            "Yoga",
            "Musculation légère",
            "Pilates",
            "Danse",
            "Tennis",
            "Course à pied",
            "Aviron"
        );
        activityCombo.setItems(options);
        
        // Add listener to update duree options when activity changes
        activityCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAucune = "aucune".equalsIgnoreCase(newVal);
            updateDureeOptions(isAucune);
        });
    }
    
    private void updateDureeOptions(boolean isAucune) {
        ObservableList<String> options;
        if (isAucune) {
            options = FXCollections.observableArrayList("15", "30", "45");
        } else {
            options = FXCollections.observableArrayList("60", "75", "90", "120");
        }
        dureeCombo.setItems(options);
    }
    
    private void initializeCaloriesOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "1500-1700 kcal",
            "1800-2000 kcal",
            "2100-2300 kcal",
            "2400-2600 kcal",
            "2700-3000 kcal"
        );
        caloriesCombo.setItems(options);
    }
    
    private void initializeSupplementsOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "Mélatonine",
            "Thé à la camomille",
            "Magnésium",
            "Glycine",
            "Ashwagandha"
        );
        supplementsCombo.setItems(options);
    }
    
    // Load details for a specific demande
    public void loadDemandeDetails(int demandeId) {
        currentDemande = demandeController.getDemande(demandeId);
        if (currentDemande != null) {
            displayDemandeInfo();
            updateFormBasedOnDemande();
            checkForExistingRecommandation();
        }
    }
    
    // Display demande info in the UI
    private void displayDemandeInfo() {
        // Set title
        titleLabel.setText("Détails de la Demande #" + currentDemande.getId());
        
        // Set patient info
        patientIdLabel.setText(String.valueOf(currentDemande.getPatient_id()));
        
        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        dateLabel.setText(currentDemande.getDate().format(formatter));
        
        demandeIdLabel.setText(String.valueOf(currentDemande.getId()));
        
        // Set lifestyle data
        eauLabel.setText(String.valueOf(currentDemande.getEau()) + " L");
        repasLabel.setText(String.valueOf(currentDemande.getNbr_repas()));
        snacksLabel.setText(currentDemande.isSnacks() ? "Oui" : "Non");
        
        Float calories = currentDemande.getCalories();
        caloriesLabel.setText(calories != null ? String.valueOf(calories) : "N/A");
        
        String activity = currentDemande.getActivity();
        activityLabel.setText(activity != null ? activity : "Aucune");
        
        Float duree = currentDemande.getDuree_activite();
        dureeLabel.setText(duree != null ? duree + " heures" : "N/A");
        
        String sommeil = currentDemande.getSommeil();
        sommeilLabel.setText(sommeil != null ? sommeil : "Bon");
    }
    
    // Update form fields based on current demande
    private void updateFormBasedOnDemande() {
        if (currentDemande == null) return;
        
        // Get values from demande
        int nbrRepas = currentDemande.getNbr_repas();
        String activityValue = currentDemande.getActivity() != null ? currentDemande.getActivity().toLowerCase() : "aucune";
        String sommeilValue = currentDemande.getSommeil() != null ? currentDemande.getSommeil().toLowerCase() : "bon";
        
        // Update meal fields based on nbrRepas
        updateMealFieldsVisibility(nbrRepas);
        
        // Update activity options and duree options based on activity
        boolean isAucune = "aucune".equalsIgnoreCase(activityValue);
        updateDureeOptions(isAucune);
        
        // Update supplements visibility based on sommeil
        boolean showSupplements = "moyen".equalsIgnoreCase(sommeilValue) || "mauvais".equalsIgnoreCase(sommeilValue);
        supplementsGrid.setVisible(showSupplements);
        supplementsGrid.setManaged(showSupplements);
    }
    
    // Update meal ComboBoxes visibility based on nbrRepas
    private void updateMealFieldsVisibility(int nbrRepas) {
        switch (nbrRepas) {
            case 1:
                petitDejGrid.setVisible(false);
                petitDejGrid.setManaged(false);
                dejeunerGrid.setVisible(true);
                dejeunerGrid.setManaged(true);
                dinerGrid.setVisible(false);
                dinerGrid.setManaged(false);
                break;
            case 2:
                petitDejGrid.setVisible(false);
                petitDejGrid.setManaged(false);
                dejeunerGrid.setVisible(true);
                dejeunerGrid.setManaged(true);
                dinerGrid.setVisible(true);
                dinerGrid.setManaged(true);
                break;
            case 3:
            default:
                petitDejGrid.setVisible(true);
                petitDejGrid.setManaged(true);
                dejeunerGrid.setVisible(true);
                dejeunerGrid.setManaged(true);
                dinerGrid.setVisible(true);
                dinerGrid.setManaged(true);
                break;
        }
    }
    
    // Check if there's already a recommendation for this demande
    private void checkForExistingRecommandation() {
        Recommandation recommandation = recommandationController.getRecommandationByDemandeId(currentDemande.getId());
        if (recommandation != null) {
            populateFormWithRecommandation(recommandation);
        }
    }
    
    // Populate the form with existing recommendation data
    private void populateFormWithRecommandation(Recommandation recommandation) {
        petitDejCombo.setValue(recommandation.getPetit_dejeuner());
        dejeunerCombo.setValue(recommandation.getDejeuner());
        dinerCombo.setValue(recommandation.getDiner());
        activityCombo.setValue(recommandation.getActivity());
        
        // Set calories value by finding the matching key in caloriesMapping
        Float calories = recommandation.getCalories();
        if (calories != null) {
            for (Map.Entry<String, Float> entry : caloriesMapping.entrySet()) {
                if (entry.getValue().equals(calories)) {
                    caloriesCombo.setValue(entry.getKey());
                    break;
                }
            }
        }
        
        Float duree = recommandation.getDuree();
        if (duree != null) {
            dureeCombo.setValue(duree.toString());
        }
        
        supplementsCombo.setValue(recommandation.getSupplements());
    }
    
    // Save button handler
    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (currentDemande == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune demande sélectionnée");
            return;
        }
        
        try {
            int demandeId = currentDemande.getId();
            
            // Get meal values based on visibility
            String petit_dejeuner = petitDejGrid.isVisible() ? petitDejCombo.getValue() : null;
            String dejeuner = dejeunerGrid.isVisible() ? dejeunerCombo.getValue() : null;
            String diner = dinerGrid.isVisible() ? dinerCombo.getValue() : null;
            
            String activity = activityCombo.getValue();
            
            // Get calories from selected calorie range
            String caloriesText = caloriesCombo.getValue();
            Float calories = null;
            if (caloriesText != null && !caloriesText.isEmpty()) {
                calories = caloriesMapping.get(caloriesText);
            }
            
            // Get duree
            String dureeText = dureeCombo.getValue();
            Float duree = dureeText == null || dureeText.isEmpty() ? null : Float.parseFloat(dureeText);
            
            // Get supplements if visible
            String supplements = supplementsGrid.isVisible() ? supplementsCombo.getValue() : null;
            
            // Check if there's an existing recommendation
            Recommandation existing = recommandationController.getRecommandationByDemandeId(demandeId);
            boolean success;
            
            if (existing != null) {
                // Update existing recommendation
                success = recommandationController.updateRecommandation(
                    existing.getId(), demandeId, petit_dejeuner, dejeuner, diner, 
                    activity, calories, duree, supplements
                );
            } else {
                // Create new recommendation
                success = recommandationController.createRecommandation(
                    demandeId, petit_dejeuner, dejeuner, diner, 
                    activity, calories, duree, supplements
                );
            }
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Recommandation enregistrée avec succès");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement de la recommandation");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Entrée invalide", "Veuillez entrer des valeurs numériques valides");
        }
    }
    
    // Handle back button
    @FXML
    private void handleBackButton(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    
    // Handle role button
    @FXML
    private void handleRoleButton(ActionEvent event) {
        try {
            // Close the current window
            Stage currentStage = (Stage) roleButton.getScene().getWindow();
            currentStage.close();
            
            // Since switchUserRole is not directly accessible, restart the application
            Stage primaryStage = new Stage();
            MainFX mainApp = new MainFX();
            mainApp.start(primaryStage);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de changer de rôle");
            e.printStackTrace();
        }
    }
    
    // Handle cancel button
    @FXML
    private void handleCancelButton(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    // Show alert dialog
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 