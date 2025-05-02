package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Demande;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class DemandeViewController implements Initializable {

    private DemandeController demandeController;

    // Table View for displaying Demande records
    @FXML
    private TableView<Demande> tableView;
    
    @FXML
    private TableColumn<Demande, Integer> idColumn;
    
    @FXML
    private TableColumn<Demande, String> dateColumn;
    
    @FXML
    private TableColumn<Demande, Float> eauColumn;
    
    @FXML
    private TableColumn<Demande, Integer> nbrRepasColumn;
    
    @FXML
    private TableColumn<Demande, String> snacksColumn;
    
    @FXML
    private TableColumn<Demande, String> caloriesColumn;
    
    @FXML
    private TableColumn<Demande, String> activityColumn;
    
    @FXML
    private TableColumn<Demande, String> sommeilColumn;
    
    @FXML
    private TableColumn<Demande, Float> dureeColumn;
    
    @FXML
    private TableColumn<Demande, Integer> patientIdColumn;
    
    // Form fields for Create/Update
    @FXML
    private TextField eauField;
    
    @FXML
    private TextField nbrRepasField;
    
    @FXML
    private CheckBox snacksCheckBox;
    
    @FXML
    private TextField caloriesField;
    
    @FXML
    private TextField activityField;
    
    @FXML
    private TextField sommeilField;
    
    @FXML
    private TextField dureeField;
    
    @FXML
    private TextField patientIdField;
    
    // Search fields
    @FXML
    private TextField searchIdField;
    
    @FXML
    private TextField searchPatientField;
    
    // Currently selected Demande
    private Demande selectedDemande;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeController = new DemandeController();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDate();
            return new SimpleStringProperty(date != null ? date.toString() : "");
        });
        eauColumn.setCellValueFactory(new PropertyValueFactory<>("eau"));
        nbrRepasColumn.setCellValueFactory(new PropertyValueFactory<>("nbr_repas"));
        snacksColumn.setCellValueFactory(cellData -> {
            boolean snacks = cellData.getValue().isSnacks();
            return new SimpleStringProperty(snacks ? "Yes" : "No");
        });
        caloriesColumn.setCellValueFactory(cellData -> {
            Float calories = cellData.getValue().getCalories();
            return new SimpleStringProperty(calories != null ? calories.toString() : "N/A");
        });
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activity"));
        sommeilColumn.setCellValueFactory(cellData -> {
            String sommeil = cellData.getValue().getSommeil();
            return new SimpleStringProperty(sommeil != null ? sommeil : "N/A");
        });
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree_activite"));
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patient_id"));
        
        // Set up table selection listener
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedDemande = newSelection;
                populateForm(selectedDemande);
            }
        });
        
        // Load data
        loadAllDemandes();
    }
    
    // Load all demandes
    @FXML
    private void loadAllDemandes() {
        List<Demande> demandes = demandeController.getAllDemandes();
        ObservableList<Demande> demandeList = FXCollections.observableArrayList(demandes);
        tableView.setItems(demandeList);
    }
    
    // Create new demande
    @FXML
    private void handleCreateButton(ActionEvent event) {
        try {
            float eau = Float.parseFloat(eauField.getText());
            int nbrRepas = Integer.parseInt(nbrRepasField.getText());
            boolean snacks = snacksCheckBox.isSelected();
            
            String caloriesText = caloriesField.getText();
            Float calories = caloriesText.isEmpty() ? null : Float.parseFloat(caloriesText);
            
            String activity = activityField.getText();
            String sommeil = sommeilField.getText();
            if (sommeil.isEmpty()) sommeil = null;
            
            float duree = Float.parseFloat(dureeField.getText());
            int patientId = Integer.parseInt(patientIdField.getText());
            
            boolean success = demandeController.createDemande(
                eau, nbrRepas, snacks, calories, activity, sommeil, duree, patientId
            );
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Demande created successfully");
                clearForm();
                loadAllDemandes();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create demande");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers");
        }
    }
    
    // Update selected demande
    @FXML
    private void handleUpdateButton(ActionEvent event) {
        if (selectedDemande == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a demande to update");
            return;
        }
        
        try {
            float eau = Float.parseFloat(eauField.getText());
            int nbrRepas = Integer.parseInt(nbrRepasField.getText());
            boolean snacks = snacksCheckBox.isSelected();
            
            String caloriesText = caloriesField.getText();
            Float calories = caloriesText.isEmpty() ? null : Float.parseFloat(caloriesText);
            
            String activity = activityField.getText();
            String sommeil = sommeilField.getText();
            if (sommeil.isEmpty()) sommeil = null;
            
            float duree = Float.parseFloat(dureeField.getText());
            int patientId = Integer.parseInt(patientIdField.getText());
            
            boolean success = demandeController.updateDemande(
                selectedDemande.getId(), selectedDemande.getDate(), eau, nbrRepas, 
                snacks, calories, activity, sommeil, duree, patientId
            );
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Demande updated successfully");
                clearForm();
                loadAllDemandes();
                selectedDemande = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update demande");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers");
        }
    }
    
    // Delete selected demande
    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedDemande == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a demande to delete");
            return;
        }
        
        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, 
                "Are you sure you want to delete this demande?", 
                ButtonType.YES, ButtonType.NO);
        confirmDialog.showAndWait();
        
        if (confirmDialog.getResult() == ButtonType.YES) {
            boolean success = demandeController.deleteDemande(selectedDemande.getId());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Demande deleted successfully");
                clearForm();
                loadAllDemandes();
                selectedDemande = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete demande");
            }
        }
    }
    
    // Clear form and selection
    @FXML
    private void handleClearButton(ActionEvent event) {
        clearForm();
        tableView.getSelectionModel().clearSelection();
        selectedDemande = null;
    }
    
    // Search by ID
    @FXML
    private void handleSearchIdButton(ActionEvent event) {
        try {
            String searchText = searchIdField.getText().trim();
            if (searchText.isEmpty()) {
                loadAllDemandes();
                return;
            }
            
            int id = Integer.parseInt(searchText);
            Demande demande = demandeController.getDemande(id);
            
            if (demande != null) {
                ObservableList<Demande> searchResult = FXCollections.observableArrayList(demande);
                tableView.setItems(searchResult);
            } else {
                tableView.setItems(FXCollections.emptyObservableList());
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No demande found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid ID number");
        }
    }
    
    // Search by Patient ID
    @FXML
    private void handleSearchPatientButton(ActionEvent event) {
        try {
            String searchText = searchPatientField.getText().trim();
            if (searchText.isEmpty()) {
                loadAllDemandes();
                return;
            }
            
            int patientId = Integer.parseInt(searchText);
            List<Demande> demandes = demandeController.getDemandesByPatient(patientId);
            
            ObservableList<Demande> searchResult = FXCollections.observableArrayList(demandes);
            tableView.setItems(searchResult);
            
            if (demandes.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No demandes found for patient ID: " + patientId);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid patient ID number");
        }
    }
    
    // Populate form with selected demande data
    private void populateForm(Demande demande) {
        eauField.setText(String.valueOf(demande.getEau()));
        nbrRepasField.setText(String.valueOf(demande.getNbr_repas()));
        snacksCheckBox.setSelected(demande.isSnacks());
        
        Float calories = demande.getCalories();
        caloriesField.setText(calories != null ? calories.toString() : "");
        
        activityField.setText(demande.getActivity());
        
        String sommeil = demande.getSommeil();
        sommeilField.setText(sommeil != null ? sommeil : "");
        
        dureeField.setText(String.valueOf(demande.getDuree_activite()));
        patientIdField.setText(String.valueOf(demande.getPatient_id()));
    }
    
    // Clear form fields
    private void clearForm() {
        eauField.clear();
        nbrRepasField.clear();
        snacksCheckBox.setSelected(false);
        caloriesField.clear();
        activityField.clear();
        sommeilField.clear();
        dureeField.clear();
        patientIdField.clear();
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