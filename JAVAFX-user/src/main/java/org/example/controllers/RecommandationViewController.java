package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.models.Patient;
import org.example.models.Recommandation;
import org.example.services.DemandeDAO;
import org.example.services.PatientService;
import org.example.util.EmailSender;
import org.example.util.MainFX;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class RecommandationViewController implements Initializable {

    private RecommandationController recommandationController;
    private DemandeController demandeController;
    private PatientService patientService;
    private DemandeDAO demandeDAO;

    // Table View for displaying Recommandation records
    @FXML
    private TableView<Recommandation> tableView;
    
    @FXML
    private TableColumn<Recommandation, Integer> idColumn;
    
    @FXML
    private TableColumn<Recommandation, Integer> demandeIdColumn;
    
    @FXML
    private TableColumn<Recommandation, String> petitDejeunerColumn;
    
    @FXML
    private TableColumn<Recommandation, String> dejeunerColumn;
    
    @FXML
    private TableColumn<Recommandation, String> dinerColumn;
    
    @FXML
    private TableColumn<Recommandation, String> activityColumn;
    
    @FXML
    private TableColumn<Recommandation, String> caloriesColumn;
    
    @FXML
    private TableColumn<Recommandation, String> dureeColumn;
    
    @FXML
    private TableColumn<Recommandation, String> supplementsColumn;
    
    @FXML
    private TableColumn<Recommandation, String> patientNameColumn;
    
    // ComboBox for Demande selection
    @FXML
    private ComboBox<Demande> demandeComboBox;
    
    // Form fields for Create/Update with ComboBoxes
    @FXML
    private ComboBox<String> petitDejeunerField;
    
    @FXML
    private ComboBox<String> dejeunerField;
    
    @FXML
    private ComboBox<String> dinerField;
    
    @FXML
    private ComboBox<String> activityField;
    
    @FXML
    private ComboBox<String> caloriesField;
    
    @FXML
    private ComboBox<String> dureeField;
    
    @FXML
    private ComboBox<String> supplementsField;
    
    // Search field
    @FXML
    private TextField searchIdField;
    
    // Navigation buttons
    @FXML
    private Button backButton;
    
    @FXML
    private Button switchRoleButton;
    
    @FXML
    private Button generateMealPlanButton;
    
    // Currently selected Recommandation
    private Recommandation selectedRecommandation;
    
    // Maps to store display value to actual value mappings
    private Map<String, Float> caloriesMapping;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recommandationController = new RecommandationController();
        demandeController = new DemandeController();
        patientService = new PatientService();
        demandeDAO = new DemandeDAO();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        demandeIdColumn.setCellValueFactory(new PropertyValueFactory<>("demande_id"));
        petitDejeunerColumn.setCellValueFactory(new PropertyValueFactory<>("petit_dejeuner"));
        dejeunerColumn.setCellValueFactory(new PropertyValueFactory<>("dejeuner"));
        dinerColumn.setCellValueFactory(new PropertyValueFactory<>("diner"));
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activity"));
        caloriesColumn.setCellValueFactory(cellData -> {
            Float calories = cellData.getValue().getCalories();
            return new SimpleStringProperty(calories != null ? calories.toString() : "N/A");
        });
        dureeColumn.setCellValueFactory(cellData -> {
            Float duree = cellData.getValue().getDuree();
            return new SimpleStringProperty(duree != null ? duree.toString() : "N/A");
        });
        supplementsColumn.setCellValueFactory(new PropertyValueFactory<>("supplements"));
        
        // Initialize patient name column
        patientNameColumn.setCellValueFactory(cellData -> {
            try {
                int demandeId = cellData.getValue().getDemande_id();
                Demande demande = demandeDAO.getById(demandeId);
                if (demande != null) {
                    int patientId = demande.getPatient_id();
                    Patient patient = patientService.getOne(patientId);
                    if (patient != null) {
                        return new SimpleStringProperty(patient.getPrenom() + " " + patient.getNom());
                    }
                }
                return new SimpleStringProperty("N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("Erreur");
            }
        });
        
        // Initialize ComboBoxes with initial values
        initializeComboBoxes();
        
        // Set up table selection listener
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedRecommandation = newSelection;
                populateForm(selectedRecommandation);
                
                // Set the selected demande in the combobox
                for (Demande demande : demandeComboBox.getItems()) {
                    if (demande.getId() == selectedRecommandation.getDemande_id()) {
                        demandeComboBox.setValue(demande);
                        break;
                    }
                }
            }
        });
        
        // Load Demandes into ComboBox
        loadDemandesIntoComboBox();
        
        // Set ComboBox cell factory to display Demande ID
        demandeComboBox.setCellFactory(param -> new ListCell<Demande>() {
            @Override
            protected void updateItem(Demande item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Demande #" + item.getId());
                }
            }
        });
        
        // Set ComboBox button cell to display Demande ID
        demandeComboBox.setButtonCell(new ListCell<Demande>() {
            @Override
            protected void updateItem(Demande item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Demande #" + item.getId());
                }
            }
        });
        
        // Add listener to demandeComboBox to update form fields based on demande selection
        demandeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateFormBasedOnDemande(newVal);
            }
        });
        
        // Load data
        loadAllRecommandations();
    }

    // Initialize ComboBoxes with predefined options
    private void initializeComboBoxes() {
        // Initialize caloriesMapping
        caloriesMapping = new HashMap<>();
        caloriesMapping.put("1500-1700 kcal", 1600f);
        caloriesMapping.put("1800-2000 kcal", 1900f);
        caloriesMapping.put("2100-2300 kcal", 2200f);
        caloriesMapping.put("2400-2600 kcal", 2500f);
        caloriesMapping.put("2700-3000 kcal", 2850f);
        
        // Set default ComboBox options (will be updated based on Demande selection)
        initializePetitDejeunerOptions();
        initializeDejeunerOptions();
        initializeDinerOptions();
        initializeActivityOptions();
        initializeCaloriesOptions();
        initializeDureeOptions(false); // Default is not "aucune"
        initializeSupplementsOptions();
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
        petitDejeunerField.setItems(options);
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
        dejeunerField.setItems(options);
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
        dinerField.setItems(options);
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
        activityField.setItems(options);
        
        // Add listener to update duree options when activity changes
        activityField.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAucune = "aucune".equalsIgnoreCase(newVal);
            initializeDureeOptions(isAucune);
        });
    }
    
    private void initializeCaloriesOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "1500-1700 kcal",
            "1800-2000 kcal",
            "2100-2300 kcal",
            "2400-2600 kcal",
            "2700-3000 kcal"
        );
        caloriesField.setItems(options);
    }
    
    private void initializeDureeOptions(boolean isAucune) {
        ObservableList<String> options;
        if (isAucune) {
            options = FXCollections.observableArrayList("15", "30", "45");
        } else {
            options = FXCollections.observableArrayList("60", "75", "90", "120");
        }
        dureeField.setItems(options);
    }
    
    private void initializeSupplementsOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
            "Mélatonine",
            "Thé à la camomille",
            "Magnésium",
            "Glycine",
            "Ashwagandha"
        );
        supplementsField.setItems(options);
    }
    
    // Update form fields based on selected Demande
    private void updateFormBasedOnDemande(Demande demande) {
        if (demande == null) return;
        
        // Get values from demande
        int nbrRepas = demande.getNbr_repas();
        String activityValue = demande.getActivity() != null ? demande.getActivity().toLowerCase() : "aucune";
        String sommeilValue = demande.getSommeil() != null ? demande.getSommeil().toLowerCase() : "bon";
        
        // Update meal fields based on nbrRepas
        updateMealFieldsVisibility(nbrRepas);
        
        // Update activity options
        boolean isAucune = "aucune".equalsIgnoreCase(activityValue);
        initializeDureeOptions(isAucune);
        
        // Update supplements visibility based on sommeil
        boolean showSupplements = "moyen".equalsIgnoreCase(sommeilValue) || "mauvais".equalsIgnoreCase(sommeilValue);
        supplementsField.setDisable(!showSupplements);
        supplementsField.setVisible(showSupplements);
    }
    
    // Update meal ComboBoxes visibility based on nbrRepas
    private void updateMealFieldsVisibility(int nbrRepas) {
        switch (nbrRepas) {
            case 1:
                petitDejeunerField.setDisable(true);
                petitDejeunerField.setVisible(false);
                dejeunerField.setDisable(false);
                dejeunerField.setVisible(true);
                dinerField.setDisable(true);
                dinerField.setVisible(false);
                break;
            case 2:
                petitDejeunerField.setDisable(true);
                petitDejeunerField.setVisible(false);
                dejeunerField.setDisable(false);
                dejeunerField.setVisible(true);
                dinerField.setDisable(false);
                dinerField.setVisible(true);
                break;
            case 3:
            default:
                petitDejeunerField.setDisable(false);
                petitDejeunerField.setVisible(true);
                dejeunerField.setDisable(false);
                dejeunerField.setVisible(true);
                dinerField.setDisable(false);
                dinerField.setVisible(true);
                break;
        }
    }
    
    // Load all demandes into the combobox
    private void loadDemandesIntoComboBox() {
        List<Demande> demandes = demandeController.getAllDemandes();
        ObservableList<Demande> demandeList = FXCollections.observableArrayList(demandes);
        demandeComboBox.setItems(demandeList);
    }
    
    // Load all recommandations
    @FXML
    private void loadAllRecommandations() {
        List<Recommandation> recommandations = recommandationController.getAllRecommandations();
        ObservableList<Recommandation> recommandationList = FXCollections.observableArrayList(recommandations);
        tableView.setItems(recommandationList);
    }
    
    // Create new recommandation
    @FXML
    private void handleCreateButton(ActionEvent event) {
        try {
            Demande selectedDemande = demandeComboBox.getValue();
            if (selectedDemande == null) {
                showAlert(Alert.AlertType.WARNING, "Aucune demande sélectionnée", "Veuillez sélectionner une demande d'abord");
                return;
            }
            
            int demandeId = selectedDemande.getId();
            
            // Get meal values based on visibility
            String petit_dejeuner = petitDejeunerField.isVisible() ? petitDejeunerField.getValue() : null;
            String dejeuner = dejeunerField.isVisible() ? dejeunerField.getValue() : null;
            String diner = dinerField.isVisible() ? dinerField.getValue() : null;
            
            String activity = activityField.getValue();
            
            // Get calories from selected calorie range
            String caloriesText = caloriesField.getValue();
            Float calories = null;
            if (caloriesText != null && !caloriesText.isEmpty()) {
                calories = caloriesMapping.get(caloriesText);
            }
            
            // Get duree
            String dureeText = dureeField.getValue();
            Float duree = dureeText == null || dureeText.isEmpty() ? null : Float.parseFloat(dureeText);
            
            // Get supplements if visible
            String supplements = supplementsField.isVisible() ? supplementsField.getValue() : null;
            
            boolean success = recommandationController.createRecommandation(
                demandeId, petit_dejeuner, dejeuner, diner, 
                activity, calories, duree, supplements
            );
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Recommandation créée avec succès");
                
                // Get the newly created recommendation to send an email
                Recommandation newRecommandation = recommandationController.getRecommandationByDemandeId(demandeId);
                if (newRecommandation != null) {
                    // Get demande to find patient
                    Demande demande = demandeController.getDemande(demandeId);
                    if (demande != null) {
                        // Send email notification to patient in a separate thread
                        new Thread(() -> {
                            boolean emailSent = EmailSender.sendRecommandationNotification(
                                    demande.getPatient_id(), newRecommandation);
                            
                            if (emailSent) {
                                System.out.println("Email notification sent successfully");
                            } else {
                                System.err.println("Failed to send email notification");
                            }
                        }).start();
                    }
                }
                
                clearForm();
                loadAllRecommandations();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création de la recommandation");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Entrée invalide", "Veuillez entrer des valeurs numériques valides");
        }
    }
    
    // Update selected recommandation
    @FXML
    private void handleUpdateButton(ActionEvent event) {
        if (selectedRecommandation == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une recommandation à mettre à jour");
            return;
        }
        
        try {
            Demande selectedDemande = demandeComboBox.getValue();
            if (selectedDemande == null) {
                showAlert(Alert.AlertType.WARNING, "Aucune demande sélectionnée", "Veuillez sélectionner une demande");
                return;
            }
            
            int demandeId = selectedDemande.getId();
            
            // Get meal values based on visibility
            String petit_dejeuner = petitDejeunerField.isVisible() ? petitDejeunerField.getValue() : null;
            String dejeuner = dejeunerField.isVisible() ? dejeunerField.getValue() : null;
            String diner = dinerField.isVisible() ? dinerField.getValue() : null;
            
            String activity = activityField.getValue();
            
            // Get calories from selected calorie range
            String caloriesText = caloriesField.getValue();
            Float calories = null;
            if (caloriesText != null && !caloriesText.isEmpty()) {
                calories = caloriesMapping.get(caloriesText);
            }
            
            // Get duree
            String dureeText = dureeField.getValue();
            Float duree = dureeText == null || dureeText.isEmpty() ? null : Float.parseFloat(dureeText);
            
            // Get supplements if visible
            String supplements = supplementsField.isVisible() ? supplementsField.getValue() : null;
            
            boolean success = recommandationController.updateRecommandation(
                selectedRecommandation.getId(), demandeId, petit_dejeuner, dejeuner, diner, 
                activity, calories, duree, supplements
            );
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Recommandation mise à jour avec succès");
                clearForm();
                loadAllRecommandations();
                selectedRecommandation = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour de la recommandation");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Entrée invalide", "Veuillez entrer des valeurs numériques valides");
        }
    }
    
    // Delete selected recommandation
    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedRecommandation == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une recommandation à supprimer");
            return;
        }
        
        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, 
                "Êtes-vous sûr de vouloir supprimer cette recommandation ?", 
                ButtonType.YES, ButtonType.NO);
        confirmDialog.showAndWait();
        
        if (confirmDialog.getResult() == ButtonType.YES) {
            boolean success = recommandationController.deleteRecommandation(selectedRecommandation.getId());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Recommandation supprimée avec succès");
                clearForm();
                loadAllRecommandations();
                selectedRecommandation = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de la recommandation");
            }
        }
    }
    
    // Clear form and selection
    @FXML
    private void handleClearButton(ActionEvent event) {
        clearForm();
        tableView.getSelectionModel().clearSelection();
        selectedRecommandation = null;
        demandeComboBox.setValue(null);
    }
    
    // Search by ID
    @FXML
    private void handleSearchIdButton(ActionEvent event) {
        try {
            String searchText = searchIdField.getText().trim();
            if (searchText.isEmpty()) {
                loadAllRecommandations();
                return;
            }
            
            int id = Integer.parseInt(searchText);
            Recommandation recommandation = recommandationController.getRecommandation(id);
            
            if (recommandation != null) {
                ObservableList<Recommandation> searchResult = FXCollections.observableArrayList(recommandation);
                tableView.setItems(searchResult);
            } else {
                tableView.setItems(FXCollections.emptyObservableList());
                showAlert(Alert.AlertType.INFORMATION, "Aucun résultat", "Aucune recommandation trouvée avec l'ID : " + id);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Entrée invalide", "Veuillez entrer un numéro d'ID valide");
        }
    }
    
    // Search by Demande ID from combobox
    @FXML
    private void handleSearchDemandeButton(ActionEvent event) {
        Demande selectedDemande = demandeComboBox.getValue();
        if (selectedDemande == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une demande d'abord");
            return;
        }
        
        int demandeId = selectedDemande.getId();
        Recommandation recommandation = recommandationController.getRecommandationByDemandeId(demandeId);
        
        if (recommandation != null) {
            ObservableList<Recommandation> searchResult = FXCollections.observableArrayList(recommandation);
            tableView.setItems(searchResult);
        } else {
            tableView.setItems(FXCollections.emptyObservableList());
            showAlert(Alert.AlertType.INFORMATION, "Aucun résultat", "Aucune recommandation trouvée pour la demande ID : " + demandeId);
        }
    }
    
    // Populate form with selected recommandation data
    private void populateForm(Recommandation recommandation) {
        petitDejeunerField.setValue(recommandation.getPetit_dejeuner());
        dejeunerField.setValue(recommandation.getDejeuner());
        dinerField.setValue(recommandation.getDiner());
        activityField.setValue(recommandation.getActivity());
        
        // Set calories value by finding the matching key in caloriesMapping
        Float calories = recommandation.getCalories();
        if (calories != null) {
            for (Map.Entry<String, Float> entry : caloriesMapping.entrySet()) {
                if (entry.getValue().equals(calories)) {
                    caloriesField.setValue(entry.getKey());
                    break;
                }
            }
        } else {
            caloriesField.setValue(null);
        }
        
        Float duree = recommandation.getDuree();
        dureeField.setValue(duree != null ? duree.toString() : null);
        
        supplementsField.setValue(recommandation.getSupplements());
    }
    
    // Clear form fields
    private void clearForm() {
        petitDejeunerField.setValue(null);
        dejeunerField.setValue(null);
        dinerField.setValue(null);
        activityField.setValue(null);
        caloriesField.setValue(null);
        dureeField.setValue(null);
        supplementsField.setValue(null);
    }
    
    // Handle back button
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Close the current window
            javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer en arrière");
            e.printStackTrace();
        }
    }
    
    // Handle switch role button
    @FXML
    private void handleSwitchRole(ActionEvent event) {
        try {
            // Close the current window
            javafx.stage.Stage currentStage = (javafx.stage.Stage) switchRoleButton.getScene().getWindow();
            currentStage.close();
            
            // Since switchUserRole is not directly accessible, restart the application
            javafx.stage.Stage primaryStage = new javafx.stage.Stage();
            MainFX mainApp = new MainFX();
            mainApp.start(primaryStage);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de changer de rôle");
            e.printStackTrace();
        }
    }
    
    // Show alert dialog
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleGenerateMealPlanButton(ActionEvent event) {
        Recommandation selectedRecommandation = tableView.getSelectionModel().getSelectedItem();
        if (selectedRecommandation == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une recommandation pour générer un plan alimentaire");
            return;
        }
        
        try {
            // Load the MealPlanGenerator view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MealPlanGenerator.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the recommendation ID
            MealPlanGeneratorController controller = loader.getController();
            controller.loadRecommandation(selectedRecommandation.getId());
            
            // Create new scene and show it in a new stage
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Générateur de Plan Alimentaire");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le générateur de plan alimentaire: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 