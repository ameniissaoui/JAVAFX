package org.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.util.SessionManager;
import org.example.util.MainFX;

public class DemandeListViewController implements Initializable {

    @FXML
    private TableView<Demande> demandeTableView;
    
    @FXML
    private TableColumn<Demande, Integer> idColumn;
    
    @FXML
    private TableColumn<Demande, java.time.LocalDateTime> dateColumn;
    
    @FXML
    private TableColumn<Demande, Integer> patientColumn;
    
    @FXML
    private TableColumn<Demande, Float> eauColumn;
    
    @FXML
    private TableColumn<Demande, Integer> repasColumn;
    
    @FXML
    private TableColumn<Demande, Boolean> snacksColumn;
    
    @FXML
    private TableColumn<Demande, String> activityColumn;
    
    @FXML
    private TableColumn<Demande, Float> caloriesColumn;
    
    @FXML
    private TableColumn<Demande, String> statutColumn;
    
    @FXML
    private Button viewDemandeButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button switchRoleButton;
    
    @FXML
    private Button recommandationsButton;
    
    @FXML
    private Label statusLabel;
    
    private DemandeDAO demandeDAO;
    private ObservableList<Demande> demandesList = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        demandeDAO = new DemandeDAO();
        
        // Configure the table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> {
            return new TableCell<Demande, java.time.LocalDateTime>() {
                @Override
                protected void updateItem(java.time.LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
        
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patient_id"));
        eauColumn.setCellValueFactory(new PropertyValueFactory<>("eau"));
        repasColumn.setCellValueFactory(new PropertyValueFactory<>("nbr_repas"));
        
        snacksColumn.setCellValueFactory(new PropertyValueFactory<>("snacks"));
        snacksColumn.setCellFactory(column -> {
            return new TableCell<Demande, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item ? "Oui" : "Non");
                    }
                }
            };
        });
        
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activity"));
        caloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
        
        // Status column for recommendations (placeholder, to be implemented with recommendations)
        statutColumn.setCellValueFactory(cellData -> {
            // This is a placeholder. In a real implementation, you would check if a recommendation
            // exists for this demande and return "Avec recommandation" or "Sans recommandation"
            return javafx.beans.binding.Bindings.createStringBinding(() -> "En attente");
        });
        
        // Load all demandes
        loadAllDemandes();
        
        // Disable the view button until a demande is selected
        viewDemandeButton.setDisable(true);
        
        // Add listener to enable view button when a demande is selected
        demandeTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    viewDemandeButton.setDisable(newSelection == null);
                    if (newSelection != null) {
                        statusLabel.setText("Demande " + newSelection.getId() + " sélectionnée - Cliquez sur 'Voir Détails' pour ajouter une recommandation");
                    } else {
                        statusLabel.setText("Sélectionnez une demande pour voir les détails et ajouter une recommandation.");
                    }
                }
        );
    }
    
    private void loadAllDemandes() {
        try {
            // Get all demandes
            List<Demande> allDemandes = demandeDAO.getAll();
            
            // Clear current list and add all demandes
            demandesList.clear();
            demandesList.addAll(allDemandes);
            
            // Set the items to the table view
            demandeTableView.setItems(demandesList);
            
            statusLabel.setText("Total de " + demandesList.size() + " demandes chargées.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les demandes", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewDemande(ActionEvent event) {
        Demande selectedDemande = demandeTableView.getSelectionModel().getSelectedItem();
        if (selectedDemande != null) {
            try {
                // Load the DemandeDetailView.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDetailView.fxml"));
                Parent root = loader.load();
                
                // Get the controller and pass the selected demande
                DemandeDetailViewController controller = loader.getController();
                controller.loadDemandeDetails(selectedDemande.getId());
                
                // Create a new stage
                Stage stage = new Stage();
                stage.setTitle("Détails de la Demande #" + selectedDemande.getId());
                stage.setScene(new Scene(root, 800, 600));
                stage.setResizable(true);
                
                // Show the stage
                stage.showAndWait();
                
                // After closing, refresh the list to show any changes
                loadAllDemandes();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails de la demande", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadAllDemandes();
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Close the window
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner en arrière", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSwitchRole(ActionEvent event) {
        try {
            // Close current stage
            Stage currentStage = (Stage) switchRoleButton.getScene().getWindow();
            currentStage.close();
            
            // Restart the application to switch roles
            Stage primaryStage = new Stage();
            MainFX mainApp = new MainFX();
            mainApp.start(primaryStage);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de changer de rôle", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRecommandations(ActionEvent event) {
        try {
            // Load the RecommandationView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecommandationView.fxml"));
            Parent root = loader.load();
            
            // Create a new stage
            Stage stage = new Stage();
            stage.setTitle("Recommandations Médicales");
            stage.setScene(new Scene(root, 1200, 800));
            stage.setResizable(true);
            
            // Show the stage
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les recommandations", e.getMessage());
            e.printStackTrace();
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