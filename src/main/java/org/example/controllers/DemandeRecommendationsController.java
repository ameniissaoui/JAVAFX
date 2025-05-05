package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import org.example.models.Demande;
import org.example.models.Patient;
import org.example.models.Recommandation;
import org.example.services.DemandeDAO;
import org.example.util.SessionManager;
import org.example.util.AIMealPlanGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

// PDF related imports
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Controller for the Recommendations view.
 * Displays recommendations based on user's demande data.
 */
public class DemandeRecommendationsController implements Initializable {

    @FXML
    private VBox recommendationsContainer;

    @FXML
    private VBox noRecommendationsView;

    @FXML
    private VBox recommendationsListView;
    
    @FXML
    private Button downloadPdfButton;
    
    @FXML
    private Button generateAiPdfButton;

    private DemandeDAO demandeDAO;
    private Demande currentDemande;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        demandeDAO = new DemandeDAO();

        // Check if a user is logged in
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                    "Vous devez être connecté pour accéder à cette page.");

            // Redirect to login after a short delay
            redirectToLogin();
            return;
        }

        // Check if the logged in user is a patient
        if (!SessionManager.getInstance().isPatient()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Accès non autorisé",
                    "Seuls les patients peuvent accéder à cette page.");

            // Redirect to login after a short delay
            redirectToLogin();
            return;
        }

        loadRecommendations();
        
        // Disable PDF buttons if no recommendations are available
        updatePdfButtonsState();
    }
    
    /**
     * Updates the state of PDF buttons based on whether recommendations are available
     */
    private void updatePdfButtonsState() {
        boolean hasRecommendations = recommendationsListView.isVisible();
        downloadPdfButton.setDisable(!hasRecommendations);
        generateAiPdfButton.setDisable(!hasRecommendations);
    }

    /**
     * Load recommendations based on user's demande data
     */
    private void loadRecommendations() {
        try {
            // Get current patient from session
            Patient currentPatient = SessionManager.getInstance().getCurrentPatient();

            if (currentPatient == null) {
                throw new IllegalStateException("Patient non trouvé dans la session");
            }

            int patientId = currentPatient.getId(); // Assuming Patient has getId() method

            // Get latest demande for this patient
            Optional<Demande> latestDemande = demandeDAO.getByPatientId(patientId).stream().findFirst();

            if (latestDemande.isPresent()) {
                // If demande exists, show recommendations
                currentDemande = latestDemande.get();

                // For demonstration, we'll just show sample recommendations
                // In a real app, you would generate recommendations based on the demande data
                // For example, if water intake is low, recommend drinking more water

                // Toggle visibility
                noRecommendationsView.setVisible(false);
                recommendationsListView.setVisible(true);

                // Generate recommendations based on demande data
                // generateRecommendations(currentDemande);
            } else {
                // If no demande exists, show the no recommendations view
                noRecommendationsView.setVisible(true);
                recommendationsListView.setVisible(false);
            }
            
            // Update PDF buttons state
            updatePdfButtonsState();
        } catch (Exception e) {
            // Show error message
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger les recommandations: " + e.getMessage());
            e.printStackTrace();

            // Default to no recommendations view
            noRecommendationsView.setVisible(true);
            recommendationsListView.setVisible(false);
            
            // Update PDF buttons state
            updatePdfButtonsState();
        }
    }

    /**
     * Generate a recommendation card and add it to the view
     *
     * @param title The recommendation title
     * @param content The recommendation content
     */
    private void addRecommendation(String title, String content) {
        VBox card = new VBox();
        card.getStyleClass().add("recommendation-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("recommendation-title");
        titleLabel.setWrapText(true);

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("recommendation-content");
        contentLabel.setWrapText(true);

        titleLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setMaxWidth(Double.MAX_VALUE);

        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));

        card.getChildren().addAll(titleLabel, contentLabel);
        recommendationsListView.getChildren().add(card);
    }
    
    /**
     * Handles the download PDF button action
     * Generates a PDF from current recommendations and allows the user to save it
     *
     * @param event The action event
     */
    @FXML
    public void downloadPdf(ActionEvent event) {
        try {
            if (currentDemande == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune demande",
                        "Aucune demande disponible pour générer un PDF.");
                return;
            }
            
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "Recommandations_" + patient.getNom() + "_" + patient.getPrenom() + "_" + timestamp + ".pdf";
            
            // Create file chooser dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer les recommandations");
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
            
            if (file != null) {
                generatePdf(file, false);
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF généré",
                        "Le PDF a été généré avec succès et enregistré à:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Génération PDF échouée",
                    "Impossible de générer le PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the generate AI PDF button action
     * Generates an AI-enhanced PDF from recommendations and allows the user to save it
     *
     * @param event The action event
     */
    @FXML
    public void generateAiPdf(ActionEvent event) {
        try {
            if (currentDemande == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune demande",
                        "Aucune demande disponible pour générer un PDF.");
                return;
            }
            
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "Recommandations_AI_" + patient.getNom() + "_" + patient.getPrenom() + "_" + timestamp + ".pdf";
            
            // Create file chooser dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer les recommandations générées par IA");
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
            
            if (file != null) {
                // Show loading message
                showAlert(Alert.AlertType.INFORMATION, "Traitement", "Génération en cours",
                        "Génération du PDF avec l'aide de l'IA. Veuillez patienter...");
                        
                generatePdf(file, true);
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF généré par IA",
                        "Le PDF a été généré avec succès avec l'aide de l'IA et enregistré à:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Génération PDF AI échouée",
                    "Impossible de générer le PDF avec l'IA: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a PDF file with recommendations
     *
     * @param file The file to save the PDF to
     * @param useAI Whether to use AI to enhance the recommendations
     */
    private void generatePdf(File file, boolean useAI) throws IOException {
        try {
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            
            if (useAI) {
                // Use AIMealPlanGenerator for AI-enhanced PDF
                System.out.println("DemandeRecommendationsController: Generating AI-enhanced PDF...");
                
                // Create a recommendation object with patient's data
                Recommandation recommandation = new Recommandation();
                
                // Set basic recommendations from currentDemande
                recommandation.setPetit_dejeuner("Un petit-déjeuner équilibré avec des protéines et des fibres");
                recommandation.setDejeuner("Un déjeuner riche en légumes et protéines maigres");
                recommandation.setDiner("Un dîner léger avec des protéines et légumes");
                
                // Set activity if available
                if (currentDemande.getActivity() != null && !currentDemande.getActivity().isEmpty()) {
                    recommandation.setActivity(currentDemande.getActivity());
                    
                    // Set duration if available
                    Float duration = currentDemande.getDuree_activite();
                    recommandation.setDuree(duration);
                }
                
                // Set calorie goal if available
                if (currentDemande.getCalories() != null) {
                    recommandation.setCalories(currentDemande.getCalories());
                } else {
                    // Default calorie goal if none set
                    recommandation.setCalories(2000.0f);
                }
                
                // Set supplements if needed
                recommandation.setSupplements("Hydratation régulière tout au long de la journée");
                
                // Generate the AI meal plan PDF
                AIMealPlanGenerator.generateAIWeeklyMealPlan(recommandation, file.getAbsolutePath());
                
                System.out.println("DemandeRecommendationsController: AI-enhanced PDF generated successfully at: " + file.getAbsolutePath());
            } else {
                // Generate a standard PDF with basic recommendations
                try (PDDocument document = new PDDocument()) {
                    PDPage page = new PDPage();
                    document.addPage(page);
                    
                    // Use more consistent margins
                    float topMargin = 770;
                    float leftMargin = 60;
                    float rightMargin = 550;
                    float lineHeight = 20;
                    
                    PDPageContentStream contentStream = new PDPageContentStream(document, page);
                    
                    // Define ChronoSerena brand color
                    java.awt.Color brandTeal = new java.awt.Color(78, 205, 196); // #4ECDC4
                    java.awt.Color lightGray = new java.awt.Color(245, 245, 245);
                    
                    // Add a teal header bar
                    contentStream.setNonStrokingColor(brandTeal);
                    contentStream.addRect(leftMargin - 10, topMargin, rightMargin - leftMargin + 20, 40);
                    contentStream.fill();
                    
                    // Add header text
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.WHITE);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    contentStream.newLineAtOffset(leftMargin, topMargin + 15);
                    contentStream.showText("ChronoSerena - Recommandations Personnalisées");
                    contentStream.endText();
                    
                    // Current Y position for content
                    float yPosition = topMargin - 50;
                    
                    // Add title
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(brandTeal);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Plan de Recommandations");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 2;
                    
                    // Add patient information
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.BLACK);
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Patient: " + patient.getPrenom() + " " + patient.getNom());
                    contentStream.endText();
                    
                    yPosition -= lineHeight;
                    
                    // Add date
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Date: " + java.time.LocalDate.now().toString());
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 1.5f;
                    
                    // Add horizontal separator
                    contentStream.setStrokingColor(brandTeal);
                    contentStream.setLineWidth(2f);
                    contentStream.moveTo(leftMargin, yPosition);
                    contentStream.lineTo(rightMargin, yPosition);
                    contentStream.stroke();
                    
                    yPosition -= lineHeight * 2;
                    
                    // Nutrition section
                    // Add section header with teal background
                    contentStream.setNonStrokingColor(brandTeal);
                    contentStream.addRect(leftMargin - 10, yPosition - 5, rightMargin - leftMargin + 20, 30);
                    contentStream.fill();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.WHITE);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("RECOMMANDATIONS NUTRITIONNELLES");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 2;
                    
                    // Nutrition content
                    contentStream.setNonStrokingColor(lightGray);
                    contentStream.addRect(leftMargin - 10, yPosition - 85, rightMargin - leftMargin + 20, 90);
                    contentStream.fill();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.BLACK);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Petit-déjeuner:");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 120, yPosition);
                    contentStream.showText("Un petit-déjeuner équilibré avec des protéines et des fibres");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 1.5f;
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Déjeuner:");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 120, yPosition);
                    contentStream.showText("Un déjeuner riche en légumes et protéines maigres");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 1.5f;
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Dîner:");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 120, yPosition);
                    contentStream.showText("Un dîner léger avec des protéines et légumes");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 3;
                    
                    // Physical activity section
                    // Add section header with teal background
                    contentStream.setNonStrokingColor(brandTeal);
                    contentStream.addRect(leftMargin - 10, yPosition - 5, rightMargin - leftMargin + 20, 30);
                    contentStream.fill();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.WHITE);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("ACTIVITÉ PHYSIQUE");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 2;
                    
                    // Activity content
                    contentStream.setNonStrokingColor(lightGray);
                    contentStream.addRect(leftMargin - 10, yPosition - 55, rightMargin - leftMargin + 20, 60);
                    contentStream.fill();
                    
                    // Add activity based on demande data if available
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.BLACK);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Activité recommandée:");
                    contentStream.endText();
                    
                    String activityText = currentDemande.getActivity() != null && !currentDemande.getActivity().isEmpty() 
                            ? currentDemande.getActivity() 
                            : "30 minutes de marche quotidienne";
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 170, yPosition);
                    contentStream.showText(activityText);
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 1.5f;
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Durée recommandée:");
                    contentStream.endText();
                    
                    String durationText = "30 minutes par jour";
                    if (currentDemande.getDuree_activite() > 0) {
                        durationText = currentDemande.getDuree_activite() + " minutes par jour";
                    }
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 170, yPosition);
                    contentStream.showText(durationText);
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 3;
                    
                    // General tips section
                    // Add section header with teal background
                    contentStream.setNonStrokingColor(brandTeal);
                    contentStream.addRect(leftMargin - 10, yPosition - 5, rightMargin - leftMargin + 20, 30);
                    contentStream.fill();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.WHITE);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("CONSEILS GÉNÉRAUX");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 2;
                    
                    // Tips content
                    contentStream.setNonStrokingColor(lightGray);
                    contentStream.addRect(leftMargin - 10, yPosition - 85, rightMargin - leftMargin + 20, 90);
                    contentStream.fill();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.BLACK);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Hydratation:");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 120, yPosition);
                    contentStream.showText("Boire au moins 1,5 à 2 litres d'eau par jour");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 1.5f;
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Sommeil:");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 120, yPosition);
                    contentStream.showText("Viser 7 à 8 heures de sommeil par nuit");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 1.5f;
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("• Stress:");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(leftMargin + 120, yPosition);
                    contentStream.showText("Pratiquer des activités relaxantes comme la méditation");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 4;
                    
                    // Add a motivational quote
                    contentStream.setNonStrokingColor(brandTeal);
                    contentStream.addRect(leftMargin + 25, yPosition - 30, rightMargin - leftMargin - 50, 35);
                    contentStream.setNonStrokingColor(new java.awt.Color(240, 250, 250));
                    contentStream.fill();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.DARK_GRAY);
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                    contentStream.newLineAtOffset(leftMargin + 35, yPosition - 10);
                    contentStream.showText("« Le succès n'est pas définitif, l'échec n'est pas fatal : c'est");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                    contentStream.newLineAtOffset(leftMargin + 35, yPosition - 25);
                    contentStream.showText("le courage de continuer qui compte. »");
                    contentStream.endText();
                    
                    yPosition -= lineHeight * 5;
                    
                    // Add disclaimer
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                    contentStream.setNonStrokingColor(java.awt.Color.DARK_GRAY);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Ces recommandations sont générées sur la base de vos informations.");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition - 15);
                    contentStream.showText("Pour des conseils médicaux spécifiques, veuillez consulter un professionnel de santé.");
                    contentStream.endText();
                    
                    // Add footer
                    // Add footer separator line
                    contentStream.setStrokingColor(java.awt.Color.LIGHT_GRAY);
                    contentStream.setLineWidth(0.5f);
                    contentStream.moveTo(leftMargin, 70);
                    contentStream.lineTo(rightMargin, 70);
                    contentStream.stroke();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(java.awt.Color.DARK_GRAY);
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.newLineAtOffset(leftMargin, 50);
                    contentStream.showText("ChronoSerena - Recommandations personnalisées");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(brandTeal);
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.newLineAtOffset(rightMargin - 150, 50);
                    contentStream.showText("www.chronoserena.com");
                    contentStream.endText();
                    
                    contentStream.close();
                    document.save(file);
                }
                
                System.out.println("DemandeRecommendationsController: Standard PDF generated successfully at: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Navigate back to the demande dashboard
     *
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir le tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the demande view
     *
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToDemande(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeMyView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de demande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows an alert dialog with the specified parameters
     *
     * @param type The type of alert
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Redirects to the login page
     */
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) recommendationsContainer.getScene().getWindow();

            // Handle case where the scene might not be attached yet
            if (stage == null) {
                stage = new Stage();
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a random fun fact about chronic diseases for the AI-enhanced PDF
     * 
     * @return A string containing a fun fact or health tip
     */
    private String generateChronicDiseaseFunFact() {
        String[] funFacts = {
            "1,2,3,4,5,6,7,8,9,10"
        };
        
        // Return a random fun fact
        return funFacts[(int)(Math.random() * funFacts.length)];
    }
}