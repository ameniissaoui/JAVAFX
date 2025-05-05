package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.models.Demande;
import org.example.models.Recommandation;
import org.example.services.DemandeDAO;
import org.example.services.RecommandationDAO;
import org.example.util.NotificationUtil;
import org.example.util.SessionManager;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Controller for the Médecin Recommendations view.
 * Allows doctors to view patient demands and create recommendations.
 */
public class MedecinRecommendationsController implements Initializable {

    @FXML
    private TableView<Demande> demandeTable;
    
    @FXML
    private TableColumn<Demande, Integer> idColumn;
    
    @FXML
    private TableColumn<Demande, Float> eauColumn;
    
    @FXML
    private TableColumn<Demande, Integer> nbrRepasColumn;
    
    @FXML
    private TableColumn<Demande, Boolean> snacksColumn;
    
    @FXML
    private VBox detailsPane;
    
    @FXML
    private HBox petitDejeunerContainer;
    
    @FXML
    private HBox dejeunerContainer;
    
    @FXML
    private HBox dinerContainer;
    
    @FXML
    private HBox supplementsContainer;
    
    @FXML
    private VBox mealRecommendationsContainer;
    
    @FXML
    private ComboBox<String> petitDejeunerComboBox;
    
    @FXML
    private ComboBox<String> dejeunerComboBox;
    
    @FXML
    private ComboBox<String> dinerComboBox;
    
    @FXML
    private ComboBox<String> activityComboBox;
    
    @FXML
    private ComboBox<Float> caloriesComboBox;
    
    @FXML
    private ComboBox<Integer> dureeComboBox;
    
    @FXML
    private ComboBox<String> supplementsComboBox;
    
    @FXML
    private Label patientNameLabel;
    
    @FXML
    private Label eauDetailLabel;
    
    @FXML
    private Label repasDetailLabel;
    
    @FXML
    private Label snacksDetailLabel;
    
    @FXML
    private Label caloriesDetailLabel;
    
    @FXML
    private Label activityDetailLabel;
    
    @FXML
    private Label dureeDetailLabel;
    
    @FXML
    private Label sommeilDetailLabel;
    @FXML private Button profile;

    private DemandeDAO demandeDAO;
    private RecommandationDAO recommandationDAO;
    private Demande selectedDemande;
    
    // Predefined options for meal recommendations
    private final Map<String, String> petitDejOptions = new LinkedHashMap<>();
    private final Map<String, String> dejeunerOptions = new LinkedHashMap<>();
    private final Map<String, String> dinerOptions = new LinkedHashMap<>();
    private final Map<String, String> activityOptions = new LinkedHashMap<>();
    private final Map<String, String> supplementOptions = new LinkedHashMap<>();
    private final Map<String, Float> calorieOptions = new LinkedHashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        demandeDAO = new DemandeDAO();
        recommandationDAO = new RecommandationDAO();
        profile.setOnAction(event -> handleProfileRedirect());

        // Initialize predefined options
        initializePredefinedOptions();
        
        // Check if a doctor is logged in
        if (!SessionManager.getInstance().isLoggedIn()) {
            NotificationUtil.showError("Erreur", "Utilisateur non connecté. Vous devez être connecté pour accéder à cette page.");
            redirectToLogin();
            return;
        }
        
        // Set up table columns
        initializeTable();
        
        // Load demandes
        loadDemandes();
        
        // Set up selection listener
        demandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedDemande = newSelection;
                showDemandeDetails(selectedDemande);
            } else {
                hideDemandeDetails();
            }
        });
        
        // Initialize ComboBoxes
        initializeComboBoxes();
        
        // Initially hide details or load the first demande
        if (!demandeTable.getItems().isEmpty()) {
            demandeTable.getSelectionModel().selectFirst();
        } else {
            hideDemandeDetails();
        }
    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void handleProfileRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/medecin_profile.fxml"));
            Stage stage = (Stage) profile.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize predefined options based on the requirements
     */
    private void initializePredefinedOptions() {
        // Petit déjeuner options
        petitDejOptions.put("Croissant et café", "Croissant et café");
        petitDejOptions.put("Pain complet et confiture", "Pain complet et confiture");
        petitDejOptions.put("Omelette et jus d'orange", "Omelette et jus d'orange");
        petitDejOptions.put("Yaourt et fruits", "Yaourt et fruits");
        petitDejOptions.put("Céréales et lait", "Céréales et lait");
        petitDejOptions.put("Smoothie aux fruits", "Smoothie aux fruits");
        petitDejOptions.put("Toast avocat", "Toast avocat");
        petitDejOptions.put("Porridge aux baies", "Porridge aux baies");
        petitDejOptions.put("Muesli et yaourt", "Muesli et yaourt");
        petitDejOptions.put("Pancakes légers", "Pancakes légers");
        
        // Déjeuner options
        dejeunerOptions.put("Salade de quinoa", "Salade de quinoa");
        dejeunerOptions.put("Poulet grillé et légumes", "Poulet grillé et légumes");
        dejeunerOptions.put("Poisson et riz complet", "Poisson et riz complet");
        dejeunerOptions.put("Sandwich au poulet", "Sandwich au poulet");
        dejeunerOptions.put("Soupe de légumes", "Soupe de légumes");
        dejeunerOptions.put("Wrap végétarien", "Wrap végétarien");
        dejeunerOptions.put("Pâtes intégrales", "Pâtes intégrales");
        dejeunerOptions.put("Bol de poke", "Bol de poke");
        dejeunerOptions.put("Salade César", "Salade César");
        dejeunerOptions.put("Riz sauté aux légumes", "Riz sauté aux légumes");
        
        // Dîner options
        dinerOptions.put("Steak et légumes", "Steak et légumes");
        dinerOptions.put("Saumon et quinoa", "Saumon et quinoa");
        dinerOptions.put("Ragoût de légumes", "Ragoût de légumes");
        dinerOptions.put("Curry de poulet", "Curry de poulet");
        dinerOptions.put("Pâtes à la bolognaise", "Pâtes à la bolognaise");
        dinerOptions.put("Pizza aux légumes", "Pizza aux légumes");
        dinerOptions.put("Tacos de poisson", "Tacos de poisson");
        dinerOptions.put("Boeuf bourguignon", "Boeuf bourguignon");
        dinerOptions.put("Risotto aux champignons", "Risotto aux champignons");
        dinerOptions.put("Gratin de courgettes", "Gratin de courgettes");
        
        // Activity options
        activityOptions.put("Marche rapide", "Marche rapide");
        activityOptions.put("Natation", "Natation");
        activityOptions.put("Cyclisme", "Cyclisme");
        activityOptions.put("Yoga", "Yoga");
        activityOptions.put("Musculation légère", "Musculation légère");
        activityOptions.put("Pilates", "Pilates");
        activityOptions.put("Danse", "Danse");
        activityOptions.put("Tennis", "Tennis");
        activityOptions.put("Course à pied", "Course à pied");
        activityOptions.put("Aviron", "Aviron");
        
        // Supplements options
        supplementOptions.put("Mélatonine", "Mélatonine");
        supplementOptions.put("Thé à la camomille", "Thé à la camomille");
        supplementOptions.put("Magnésium", "Magnésium");
        supplementOptions.put("Glycine", "Glycine");
        supplementOptions.put("Ashwagandha", "Ashwagandha");
        
        // Calories options
        calorieOptions.put("1500-1700 kcal", 1600.0f);
        calorieOptions.put("1800-2000 kcal", 1900.0f);
        calorieOptions.put("2100-2300 kcal", 2200.0f);
        calorieOptions.put("2400-2600 kcal", 2500.0f);
        calorieOptions.put("2700-3000 kcal", 2850.0f);
    }
    
    /**
     * Initialize ComboBoxes with predefined options
     */
    private void initializeComboBoxes() {
        // Populate meal ComboBoxes
        petitDejeunerComboBox.setItems(FXCollections.observableArrayList(petitDejOptions.keySet()));
        dejeunerComboBox.setItems(FXCollections.observableArrayList(dejeunerOptions.keySet()));
        dinerComboBox.setItems(FXCollections.observableArrayList(dinerOptions.keySet()));
        
        // Populate activity ComboBox
        activityComboBox.setItems(FXCollections.observableArrayList(activityOptions.keySet()));
        
        // Populate supplements ComboBox
        supplementsComboBox.setItems(FXCollections.observableArrayList(supplementOptions.keySet()));
        
        // Populate calories ComboBox
        caloriesComboBox.setItems(FXCollections.observableArrayList(new ArrayList<>(calorieOptions.values())));
        
        // Set a custom string converter for the calories ComboBox
        caloriesComboBox.setConverter(new StringConverter<Float>() {
            @Override
            public String toString(Float calories) {
                if (calories == null) return null;
                for (Map.Entry<String, Float> entry : calorieOptions.entrySet()) {
                    if (Objects.equals(entry.getValue(), calories)) {
                        return entry.getKey();
                    }
                }
                return calories.toString();
            }
            
            @Override
            public Float fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return calorieOptions.getOrDefault(string, null);
            }
        });
    }
    
    /**
     * Initialize the table columns
     */
    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        eauColumn.setCellValueFactory(new PropertyValueFactory<>("eau"));
        nbrRepasColumn.setCellValueFactory(new PropertyValueFactory<>("nbr_repas"));
        snacksColumn.setCellValueFactory(new PropertyValueFactory<>("snacks"));
    }
    
    /**
     * Load all demandes from the database
     */
    private void loadDemandes() {
        try {
            List<Demande> demandes = demandeDAO.getAll();
            ObservableList<Demande> demandesList = FXCollections.observableArrayList(demandes);
            demandeTable.setItems(demandesList);
        } catch (Exception e) {
            NotificationUtil.showError("Erreur", "Impossible de charger les demandes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show the details for a selected demande
     * @param demande The selected demande
     */
    private void showDemandeDetails(Demande demande) {
        // Show details pane, hide no selection pane
        detailsPane.setVisible(true);
        
        // Set patient name
        int patientId = demande.getPatient_id();
        patientNameLabel.setText("Patient " + patientId);
        
        // Set demande details
        eauDetailLabel.setText("Eau: " + demande.getEau() + "L");
        repasDetailLabel.setText("Repas: " + demande.getNbr_repas());
        snacksDetailLabel.setText("Snacks: " + (demande.isSnacks() ? "Oui" : "Non"));
        
        String caloriesText = demande.getCalories() != null ? demande.getCalories() + " kcal" : "Non spécifié";
        caloriesDetailLabel.setText("Calories: " + caloriesText);
        
        String activityText = demande.getActivity() != null && !demande.getActivity().isEmpty() ? 
                demande.getActivity() : "Non spécifié";
        activityDetailLabel.setText("Activité: " + activityText);
        
        String dureeText = demande.getDuree_activite() > 0 ? 
                demande.getDuree_activite() + " minutes" : "Non spécifié";
        dureeDetailLabel.setText("Durée: " + dureeText);
        
        String sommeilText = demande.getSommeil() != null && !demande.getSommeil().isEmpty() ? 
                demande.getSommeil() : "Non spécifié";
        sommeilDetailLabel.setText("Sommeil: " + sommeilText);
        
        // Configure the recommendation form based on demande properties
        configureRecommendationForm(demande);
        
        // Check if a recommendation already exists for this demande
        Recommandation existingRecommendation = recommandationDAO.getByDemandeId(demande.getId());
        if (existingRecommendation != null) {
            loadExistingRecommendation(existingRecommendation);
        } else {
            clearRecommendationForm();
        }
    }
    
    /**
     * Configure the recommendation form based on demande data
     * @param demande The demande to base the configuration on
     */
    private void configureRecommendationForm(Demande demande) {
        int nbrRepas = demande.getNbr_repas();
        String activityValue = demande.getActivity() != null ? demande.getActivity() : "aucune";
        String sommeilValue = demande.getSommeil() != null ? demande.getSommeil().toLowerCase() : "bon";
        
        // Configure meal fields based on nbrRepas
        petitDejeunerContainer.setVisible(nbrRepas == 3);
        petitDejeunerContainer.setManaged(nbrRepas == 3);
        
        dejeunerContainer.setVisible(nbrRepas >= 1);
        dejeunerContainer.setManaged(nbrRepas >= 1);
        
        dinerContainer.setVisible(nbrRepas >= 2);
        dinerContainer.setManaged(nbrRepas >= 2);
        
        // Configure duration options based on activity
        List<Integer> durationOptions;
        if (activityValue.equals("aucune")) {
            durationOptions = Arrays.asList(15, 30, 45);
        } else {
            durationOptions = Arrays.asList(60, 75, 90, 120);
        }
        dureeComboBox.setItems(FXCollections.observableArrayList(durationOptions));
        
        // Configure supplements visibility based on sleep quality
        boolean showSupplements = sommeilValue.equals("moyen") || sommeilValue.equals("mauvais");
        supplementsContainer.setVisible(showSupplements);
        supplementsContainer.setManaged(showSupplements);
    }
    
    /**
     * Load existing recommendation data into the form
     * @param recommandation The existing recommendation
     */
    private void loadExistingRecommendation(Recommandation recommandation) {
        // Set meal values
        if (recommandation.getPetit_dejeuner() != null && !recommandation.getPetit_dejeuner().isEmpty()) {
            petitDejeunerComboBox.setValue(getKeyByValue(petitDejOptions, recommandation.getPetit_dejeuner()));
        } else {
            petitDejeunerComboBox.setValue(null);
        }
        
        if (recommandation.getDejeuner() != null && !recommandation.getDejeuner().isEmpty()) {
            dejeunerComboBox.setValue(getKeyByValue(dejeunerOptions, recommandation.getDejeuner()));
        } else {
            dejeunerComboBox.setValue(null);
        }
        
        if (recommandation.getDiner() != null && !recommandation.getDiner().isEmpty()) {
            dinerComboBox.setValue(getKeyByValue(dinerOptions, recommandation.getDiner()));
        } else {
            dinerComboBox.setValue(null);
        }
        
        // Set activity
        if (recommandation.getActivity() != null && !recommandation.getActivity().isEmpty()) {
            activityComboBox.setValue(getKeyByValue(activityOptions, recommandation.getActivity()));
        } else {
            activityComboBox.setValue(null);
        }
        
        // Set calories
        if (recommandation.getCalories() != null) {
            caloriesComboBox.setValue(recommandation.getCalories());
        } else {
            caloriesComboBox.setValue(null);
        }
        
        // Set duration
        if (recommandation.getDuree() != null) {
            Integer duration = Math.round(recommandation.getDuree() * 60); // Convert hours to minutes
            if (dureeComboBox.getItems().contains(duration)) {
                dureeComboBox.setValue(duration);
            } else {
                dureeComboBox.setValue(null);
            }
        } else {
            dureeComboBox.setValue(null);
        }
        
        // Set supplements
        if (recommandation.getSupplements() != null && !recommandation.getSupplements().isEmpty()) {
            supplementsComboBox.setValue(getKeyByValue(supplementOptions, recommandation.getSupplements()));
        } else {
            supplementsComboBox.setValue(null);
        }
    }
    
    /**
     * Helper method to get a key by its value in a map
     * @param map The map to search in
     * @param value The value to search for
     * @return The key corresponding to the value, or null if not found
     */
    private <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Hide demande details when no demande is selected
     */
    private void hideDemandeDetails() {
        // Since we no longer have a noSelectionPane, we'll handle this case differently
        // We could either:
        // 1. Show a message in the details pane
        // 2. Keep the details pane empty
        // 3. Select the first demande in the table
        
        // Let's go with approach 3 - select the first demande if available
        if (!demandeTable.getItems().isEmpty()) {
            demandeTable.getSelectionModel().selectFirst();
        } else {
            // If no demandes are available, clear the form
            clearRecommendationForm();
            // Set default texts
            patientNameLabel.setText("Aucun patient");
            eauDetailLabel.setText("Eau: --");
            repasDetailLabel.setText("Repas: --");
            snacksDetailLabel.setText("Snacks: --");
            caloriesDetailLabel.setText("Calories: --");
            activityDetailLabel.setText("Activité: --");
            dureeDetailLabel.setText("Durée: --");
            sommeilDetailLabel.setText("Sommeil: --");
        }
    }
    
    /**
     * Clear the recommendation form
     */
    private void clearRecommendationForm() {
        petitDejeunerComboBox.setValue(null);
        dejeunerComboBox.setValue(null);
        dinerComboBox.setValue(null);
        activityComboBox.setValue(null);
        caloriesComboBox.setValue(null);
        dureeComboBox.setValue(null);
        supplementsComboBox.setValue(null);
    }
    
    /**
     * Handle saving the recommendation
     */
    @FXML
    private void handleSaveRecommendation() {
        if (selectedDemande == null) {
            NotificationUtil.showWarning("Attention", "Veuillez sélectionner une demande d'abord.");
            return;
        }
        
        // Validate fields - at least one meal recommendation is required
        boolean hasMealRecommendation = false;
        
        if (selectedDemande.getNbr_repas() == 3 && petitDejeunerComboBox.getValue() != null) {
            hasMealRecommendation = true;
        }
        
        if (dejeunerComboBox.getValue() != null) {
            hasMealRecommendation = true;
        }
        
        if (selectedDemande.getNbr_repas() >= 2 && dinerComboBox.getValue() != null) {
            hasMealRecommendation = true;
        }
        
        if (!hasMealRecommendation && activityComboBox.getValue() == null) {
            NotificationUtil.showWarning("Champs incomplets", 
                    "Veuillez remplir au moins une recommandation de repas ou d'activité.");
            return;
        }
        
        try {
            // Get values from form
            String petitDejeuner = petitDejeunerComboBox.getValue() != null ? 
                    petitDejOptions.get(petitDejeunerComboBox.getValue()) : null;
            
            String dejeuner = dejeunerComboBox.getValue() != null ? 
                    dejeunerOptions.get(dejeunerComboBox.getValue()) : null;
            
            String diner = dinerComboBox.getValue() != null ? 
                    dinerOptions.get(dinerComboBox.getValue()) : null;
            
            String activity = activityComboBox.getValue() != null ? 
                    activityOptions.get(activityComboBox.getValue()) : null;
            
            Float calories = caloriesComboBox.getValue();
            
            Float duree = null;
            if (dureeComboBox.getValue() != null) {
                duree = dureeComboBox.getValue() / 60.0f; // Convert minutes to hours
            }
            
            String supplements = supplementsComboBox.getValue() != null ? 
                    supplementOptions.get(supplementsComboBox.getValue()) : null;
            
            // Check if there's an existing recommendation
            Recommandation existingRecommandation = recommandationDAO.getByDemandeId(selectedDemande.getId());
            
            if (existingRecommandation != null) {
                // Update existing recommendation
                existingRecommandation.setPetit_dejeuner(petitDejeuner);
                existingRecommandation.setDejeuner(dejeuner);
                existingRecommandation.setDiner(diner);
                existingRecommandation.setActivity(activity);
                existingRecommandation.setCalories(calories);
                existingRecommandation.setDuree(duree);
                existingRecommandation.setSupplements(supplements);
                
                boolean updated = recommandationDAO.update(existingRecommandation);
                if (updated) {
                    NotificationUtil.showSuccess("Succès", "La recommandation a été mise à jour avec succès.");
                } else {
                    NotificationUtil.showError("Erreur", "Impossible de mettre à jour la recommandation.");
                }
            } else {
                // Create new recommendation
                Recommandation newRecommandation = new Recommandation(
                        selectedDemande.getId(),
                        petitDejeuner,
                        dejeuner,
                        diner,
                        activity,
                        calories,
                        duree,
                        supplements
                );
                
                boolean inserted = recommandationDAO.insert(newRecommandation);
                if (inserted) {
                    NotificationUtil.showSuccess("Succès", "La recommandation a été créée avec succès.");
                } else {
                    NotificationUtil.showError("Erreur", "Impossible de créer la recommandation.");
                }
            }
        } catch (Exception e) {
            NotificationUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate back to the medecin profile
     */
    @FXML
    private void navigateBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/medecin_profile.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            NotificationUtil.showError("Erreur", "Erreur de Navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Redirects to the login page
     */
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            
            // Close the current window
            if (demandeTable != null && demandeTable.getScene() != null) {
                ((Stage) demandeTable.getScene().getWindow()).close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * View all recommendations with clickable items and action buttons
     */
    @FXML
    private void viewAllRecommendations() {
        try {
            // Get all recommendations
            List<Recommandation> recommendations = recommandationDAO.getAll();
            
            if (recommendations.isEmpty()) {
                NotificationUtil.showInfo("Information", "Aucune recommandation n'a été trouvée.");
                return;
            }
            
            // Create a custom dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Toutes les recommandations");
            dialog.setHeaderText("Recommandations enregistrées");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Create a VBox to hold all recommendations
            VBox recommendationsContainer = new VBox(10);
            recommendationsContainer.setStyle("-fx-padding: 10px;");
            
            // Create a ScrollPane to make the content scrollable
            ScrollPane scrollPane = new ScrollPane(recommendationsContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(500);
            scrollPane.setPrefWidth(700);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            // Add each recommendation as a clickable item with action buttons
            for (Recommandation rec : recommendations) {
                // Create a container for this recommendation
                VBox recItem = new VBox(5);
                recItem.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px; -fx-background-color: #f9f9f9;");
                
                // Get associated demande data if available
                final Demande demande;
                try {
                    demande = demandeDAO.getById(rec.getDemande_id());
                } catch (Exception e) {
                    // Demande might not exist anymore
                    continue; // Skip this recommendation if the demande doesn't exist
                }
                
                if (demande == null) {
                    continue; // Skip this recommendation if the demande doesn't exist
                }
                
                // Add recommendation content
                Label titleLabel = new Label("Recommandation #" + rec.getId());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                GridPane detailsGrid = new GridPane();
                detailsGrid.setHgap(15);
                detailsGrid.setVgap(5);
                
                // Add details to grid
                int row = 0;
                
                // Demande info
                Label demandeLabel = new Label("Demande ID: " + rec.getDemande_id());
                detailsGrid.add(demandeLabel, 0, row++, 2, 1);
                
                // Patient info if available
                if (demande != null) {
                    Label patientLabel = new Label("Patient ID: " + demande.getPatient_id());
                    detailsGrid.add(patientLabel, 0, row++, 2, 1);
                }
                
                // Meals
                if (rec.getPetit_dejeuner() != null && !rec.getPetit_dejeuner().isEmpty()) {
                    detailsGrid.add(new Label("Petit déjeuner:"), 0, row);
                    detailsGrid.add(new Label(rec.getPetit_dejeuner()), 1, row++);
                }
                
                if (rec.getDejeuner() != null && !rec.getDejeuner().isEmpty()) {
                    detailsGrid.add(new Label("Déjeuner:"), 0, row);
                    detailsGrid.add(new Label(rec.getDejeuner()), 1, row++);
                }
                
                if (rec.getDiner() != null && !rec.getDiner().isEmpty()) {
                    detailsGrid.add(new Label("Dîner:"), 0, row);
                    detailsGrid.add(new Label(rec.getDiner()), 1, row++);
                }
                
                // Activity
                if (rec.getActivity() != null && !rec.getActivity().isEmpty()) {
                    detailsGrid.add(new Label("Activité:"), 0, row);
                    detailsGrid.add(new Label(rec.getActivity()), 1, row++);
                }
                
                if (rec.getDuree() > 0) {
                    detailsGrid.add(new Label("Durée:"), 0, row);
                    detailsGrid.add(new Label(rec.getDuree() + " min"), 1, row++);
                }
                
                // Other recommendations
                if (rec.getCalories() > 0) {
                    detailsGrid.add(new Label("Calories:"), 0, row);
                    detailsGrid.add(new Label(rec.getCalories() + " kcal"), 1, row++);
                }
                
                if (rec.getSupplements() != null && !rec.getSupplements().isEmpty()) {
                    detailsGrid.add(new Label("Supplément:"), 0, row);
                    detailsGrid.add(new Label(rec.getSupplements()), 1, row++);
                }
                
                // Create action buttons
                Button editButton = new Button("Modifier");
                editButton.setStyle("-fx-background-color: #4ECDC4; -fx-text-fill: white;");
                
                Button deleteButton = new Button("Supprimer");
                deleteButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white;");
                
                HBox buttonsBox = new HBox(10, editButton, deleteButton);
                buttonsBox.setStyle("-fx-padding: 10px 0 0 0;");
                
                // Add the entire recommendation item to the container
                recItem.getChildren().addAll(titleLabel, detailsGrid, buttonsBox);
                
                // Make the item clickable
                recItem.setOnMouseClicked(event -> {
                    // Load the associated demande for editing
                    if (demande != null) {
                        // Select the demande in the table
                        demandeTable.getSelectionModel().select(demande);
                        
                        // Set up form with existing recommendation
                        showDemandeDetails(demande);
                        loadExistingRecommendation(rec);
                        
                        // Close the dialog
                        dialog.close();
                    } else {
                        NotificationUtil.showWarning("Avertissement", 
                            "La demande associée à cette recommandation n'existe plus.");
                    }
                });
                
                // Set up edit button action
                editButton.setOnAction(event -> {
                    if (demande != null) {
                        demandeTable.getSelectionModel().select(demande);
                        showDemandeDetails(demande);
                        loadExistingRecommendation(rec);
                        dialog.close();
                    } else {
                        NotificationUtil.showWarning("Avertissement", 
                            "La demande associée à cette recommandation n'existe plus.");
                    }
                });
                
                // Set up delete button action
                deleteButton.setOnAction(event -> {
                    boolean confirmed = NotificationUtil.showConfirmation("Confirmation", 
                        "Êtes-vous sûr de vouloir supprimer cette recommandation ?");
                    
                    if (confirmed) {
                        try {
                            recommandationDAO.delete(rec.getId());
                            NotificationUtil.showSuccess("Succès", "Recommandation supprimée avec succès.");
                            
                            // Remove from the view
                            recommendationsContainer.getChildren().remove(recItem);
                            
                            // If no more recommendations, close the dialog
                            if (recommendationsContainer.getChildren().isEmpty()) {
                                NotificationUtil.showInfo("Information", "Toutes les recommandations ont été supprimées.");
                                dialog.close();
                            }
                        } catch (Exception e) {
                            NotificationUtil.showError("Erreur", "Impossible de supprimer la recommandation: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
                
                // Add this recommendation to the container
                recommendationsContainer.getChildren().add(recItem);
            }
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.showAndWait();
            
        } catch (Exception e) {
            NotificationUtil.showError("Erreur", "Impossible de charger les recommandations: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 