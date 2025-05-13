package org.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
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
import org.example.models.Patient;

public class DemandeCreateViewController implements Initializable {

    @FXML
    private TextField eauField;
    
    @FXML
    private ToggleGroup repasToggleGroup;
    @FXML private Button historique; // Added missing Button declaration
    @FXML private Button profileButton;
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
                Patient currentPatient = SessionManager.getInstance().getCurrentPatient();
                if (currentPatient == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté", 
                            "Vous devez être connecté en tant que patient pour créer une demande.");
                    return;
                }
                int patientId = currentPatient.getId();
                
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
                demande.setPatient_id(patientId);
                
                System.out.println("Creating demande with patient ID: " + patientId);
                
                // Save the demande to the database
                boolean saved = demandeDAO.insert(demande);
                
                if (saved) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande créée", "Votre demande a été créée avec succès !");
                    
                    // Navigate back to DemandeMyView
                    navigateToDemandeMyView(event);
                } else {
                   showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création",
                            "La demande n'a pas pu être créée. Veuillez vérifier que l'ID patient (" + patientId +
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
        navigateToDemandeMyView(event);
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
    
    private void navigateToDemandeMyView(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeMyView.fxml", event);
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void redirectToHistorique(ActionEvent event) {
        try {
            // Enhanced logging
            System.out.println("Starting history redirect...");
            System.out.println("SessionManager.isLoggedIn(): " + SessionManager.getInstance().isLoggedIn());
            System.out.println("SessionManager.isPatient(): " + SessionManager.getInstance().isPatient());

            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showErrorDialog("Erreur", "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Check if the user is a patient
            if (!SessionManager.getInstance().isPatient()) {
                showErrorDialog("Erreur", "Seuls les patients peuvent accéder à cette fonctionnalité.");
                return;
            }

            // Get the patient from the session
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            System.out.println("Patient from SessionManager: " + (patient != null ?
                    "ID=" + patient.getId() + ", Nom=" + patient.getNom() : "NULL"));

            if (patient == null) {
                System.out.println("Error: SessionManager.getCurrentPatient() returned null!");
                // Remove reference to non-existent currentUser variable
                showErrorDialog("Erreur", "Impossible de récupérer les informations du patient.");
                return;
            }

            // Load the historique page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_historique.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the patient
            AjouterHistController controller = loader.getController();
            controller.setPatient(patient);
            System.out.println("Patient passed to controller: ID=" + patient.getId() +
                    ", Nom=" + patient.getNom() + ", Prénom=" + patient.getPrenom());

            // Show the new scene with maximum size
            Stage stage = (Stage) historique.getScene().getWindow();

            // Get screen dimensions for maximum size
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error during navigation to historique: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur", "Impossible de charger la page d'ajout d'historique: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_patient.fxml", event);
    }
    @FXML
    public void redirectToDemande(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeDashboard.fxml", event);
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        SceneManager.loadScene("/fxml/rendez-vous-view.fxml", event);
    }
    @FXML
    public void viewDoctors(ActionEvent event) {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Use SceneManager to load the DoctorList.fxml in full screen
            SceneManager.loadScene("/fxml/DoctorList.fxml", event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des médecins: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void navigateToEvent(ActionEvent event) {
        SceneManager.loadScene("/fxml/eventFront.fxml", event);

    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void redirectProduit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur de navigation",
                    "Impossible de charger la page des produits: " + e.getMessage());
        }
    }
    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }
    @FXML
    public void redirectToCalendar(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
    }
} 