package org.example.controllers;

import com.itextpdf.text.DocumentException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.models.Patient;
import org.example.models.Recommandation;
import org.example.services.DemandeDAO;
import org.example.services.PatientService;
import org.example.services.RecommandationDAO;
import org.example.util.AIMealPlanGenerator;
import org.example.util.PDFGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MealPlanGeneratorController implements Initializable {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label recommandationIdLabel;
    
    @FXML
    private Label demandeIdLabel;
    
    @FXML
    private Label patientNameLabel;
    
    @FXML
    private Label breakfastLabel;
    
    @FXML
    private Label lunchLabel;
    
    @FXML
    private Label dinnerLabel;
    
    @FXML
    private Label activityLabel;
    
    @FXML
    private Label caloriesLabel;
    
    @FXML
    private Label supplementsLabel;
    
    @FXML
    private RadioButton standardRadioButton;
    
    @FXML
    private RadioButton aiRadioButton;
    
    @FXML
    private ToggleGroup generatorToggleGroup;
    
    @FXML
    private Button generateButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button openPdfButton;
    
    @FXML
    private ProgressIndicator progressIndicator;
    
    @FXML
    private Label statusLabel;
    
    private Recommandation recommandation;
    private Patient patient;
    private Demande demande;
    private File generatedFile;
    
    private RecommandationDAO recommandationDAO;
    private DemandeDAO demandeDAO;
    private PatientService patientService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recommandationDAO = new RecommandationDAO();
        demandeDAO = new DemandeDAO();
        patientService = new PatientService();
        
        // Initialize UI state
        openPdfButton.setDisable(true);
        progressIndicator.setVisible(false);
        statusLabel.setText("");
        
        standardRadioButton.setSelected(true);
    }
    
    /**
     * Load the recommendation data to generate a meal plan for
     * @param recommandationId The ID of the recommendation
     */
    public void loadRecommandation(int recommandationId) {
        this.recommandation = recommandationDAO.getById(recommandationId);
        
        if (recommandation == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Recommandation non trouvée", 
                    "La recommandation avec l'ID " + recommandationId + " n'a pas été trouvée.");
            return;
        }
        
        // Load demande data
        int demandeId = recommandation.getDemande_id();
        demande = demandeDAO.getById(demandeId);
        
        if (demande == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Demande non trouvée", 
                    "La demande associée à cette recommandation n'a pas été trouvée.");
            return;
        }
        
        // Load patient data
        int patientId = demande.getPatient_id();
        patient = patientService.getOne(patientId);
        
        if (patient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Patient non trouvé", 
                    "Le patient associé à cette demande n'a pas été trouvé.");
            return;
        }
        
        // Update UI with loaded data
        updateUI();
    }
    
    /**
     * Update UI with the loaded recommendation data
     */
    private void updateUI() {
        recommandationIdLabel.setText("Recommandation #" + recommandation.getId());
        demandeIdLabel.setText("Demande #" + recommandation.getDemande_id());
        patientNameLabel.setText(patient.getPrenom() + " " + patient.getNom());
        
        breakfastLabel.setText(recommandation.getPetit_dejeuner());
        lunchLabel.setText(recommandation.getDejeuner());
        dinnerLabel.setText(recommandation.getDiner());
        
        if (recommandation.getActivity() != null && !recommandation.getActivity().isEmpty()) {
            activityLabel.setText(recommandation.getActivity());
            if (recommandation.getDuree() != null) {
                activityLabel.setText(activityLabel.getText() + " (" + recommandation.getDuree() + " min)");
            }
        } else {
            activityLabel.setText("Aucune activité spécifiée");
        }
        
        if (recommandation.getCalories() != null) {
            caloriesLabel.setText(recommandation.getCalories() + " kcal");
        } else {
            caloriesLabel.setText("Non spécifié");
        }
        
        if (recommandation.getSupplements() != null && !recommandation.getSupplements().isEmpty()) {
            supplementsLabel.setText(recommandation.getSupplements());
        } else {
            supplementsLabel.setText("Aucun supplément");
        }
    }
    
    @FXML
    private void handleGenerateButton(ActionEvent event) {
        if (recommandation == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune recommandation", 
                    "Aucune recommandation n'a été chargée.");
            return;
        }
        
        // Get the selected output directory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir le dossier de destination");
        File outputDir = directoryChooser.showDialog(generateButton.getScene().getWindow());
        
        if (outputDir == null) {
            // User cancelled the operation
            return;
        }
        
        // Generate a unique filename based on patient name and current date/time
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String fileName = "plan-alimentaire-" + patient.getNom().toLowerCase() + "-" + timestamp + ".pdf";
        String outputPath = outputDir.getAbsolutePath() + File.separator + fileName;
        
        // Show progress indicator
        progressIndicator.setVisible(true);
        statusLabel.setText("Génération du PDF en cours...");
        generateButton.setDisable(true);
        openPdfButton.setDisable(true);
        
        // Use a separate thread for PDF generation to keep the UI responsive
        Thread pdfThread = new Thread(() -> {
            try {
                if (aiRadioButton.isSelected()) {
                    // Generate AI-powered meal plan
                    generatedFile = AIMealPlanGenerator.generateAIWeeklyMealPlan(recommandation, outputPath);
                } else {
                    // Generate standard meal plan
                    generatedFile = PDFGenerator.generateWeeklyMealPlan(recommandation, outputPath);
                }
                
                // Update UI on the JavaFX application thread
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText("PDF généré avec succès: " + fileName);
                    generateButton.setDisable(false);
                    openPdfButton.setDisable(false);
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF généré", 
                            "Le plan alimentaire a été généré avec succès à l'emplacement suivant:\n" + outputPath);
                });
            } catch (IOException | DocumentException e) {
                // Handle errors on the JavaFX application thread
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Erreur lors de la génération du PDF: " + e.getMessage());
                    generateButton.setDisable(false);
                    
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la génération du PDF", 
                            "Une erreur est survenue lors de la génération du PDF: " + e.getMessage());
                });
            }
        });
        
        pdfThread.setDaemon(true);
        pdfThread.start();
    }
    
    @FXML
    private void handleOpenPdfButton(ActionEvent event) {
        if (generatedFile != null && generatedFile.exists()) {
            try {
                // Open the PDF with the system's default PDF viewer
                java.awt.Desktop.getDesktop().open(generatedFile);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le PDF", 
                        "Impossible d'ouvrir le fichier PDF: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun PDF", 
                    "Aucun PDF n'a été généré ou le fichier n'existe plus.");
        }
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecommandationView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation", 
                    "Impossible de revenir à la vue des recommandations: " + e.getMessage());
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