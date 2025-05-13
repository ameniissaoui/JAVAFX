package org.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.example.services.DemandeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.Demande;
import org.example.models.Patient;
import org.example.util.SessionManager;
import javafx.scene.Node;

public class DemandeMyViewController implements Initializable {

    @FXML
    private VBox noDemandeView;

    @FXML
    private VBox existingDemandeView;

    @FXML
    private Button createDemandeButton;

    @FXML
    private Button editDemandeButton;
    @FXML
    private Button historique;

    @FXML
    private Button deleteDemandeButton;

    @FXML
    private Button profile;

    @FXML
    private Label dateLabel;

    @FXML
    private Label eauLabel;

    @FXML
    private Label nbrRepasLabel;

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

    @FXML
    private Label patientIdLabel;

    private DemandeDAO demandeDAO;
    private Demande currentDemande;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        demandeDAO = new DemandeDAO();
        loadPatientDemande();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleProfileRedirect() {
        // Updated to use SceneManager - we'll need an ActionEvent for this
        // Since this method doesn't seem to be connected to any FXML button directly,
        // keeping it as a reference implementation
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/patient_profile.fxml"));
            Stage stage = (Stage) profile.getScene().getWindow();

            // Create scene with screen dimensions for full screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene tableScene = new Scene(tableRoot, screenBounds.getWidth(), screenBounds.getHeight());

            // Preserve stylesheets if any
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                tableScene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            stage.setScene(tableScene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPatientDemande() {
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            // Check if a user is logged in
            if (!sessionManager.isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                redirectToLogin();
                return;
            }

            if (!sessionManager.isPatient()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Accès non autorisé",
                        "Seuls les patients peuvent accéder à cette page.");

                redirectToLogin();
                return;
            }

            Patient currentPatient = sessionManager.getCurrentPatient();

            if (currentPatient == null) {
                throw new IllegalStateException("Patient non trouvé dans la session");
            }

            int patientId = currentPatient.getId();
            System.out.println("Loading demandes for patient ID: " + patientId);

            // Get all demandes for the patient
            List<Demande> demandes = demandeDAO.getByPatientId(patientId);

            // Sort demandes by date in descending order (newest first)
            demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));

            // Get the latest demande (first in the sorted list)
            Optional<Demande> latestDemande = demandes.stream().findFirst();

            if (latestDemande.isPresent()) {
                // If demande exists, show the existing demande view
                currentDemande = latestDemande.get();
                displayDemande(currentDemande);

                // Show existing demande view, hide no demande view
                existingDemandeView.setVisible(true);
                noDemandeView.setVisible(false);
            } else {
                // If no demande exists, show the no demande view
                existingDemandeView.setVisible(false);
                noDemandeView.setVisible(true);
            }
        } catch (Exception e) {
            // Show error message
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger les demandes: " + e.getMessage());
            e.printStackTrace();

            // Default to no demande view
            existingDemandeView.setVisible(false);
            noDemandeView.setVisible(true);
        }
    }

    /**
     * Redirects to the login page
     */
    private void redirectToLogin() {
        try {
            // Load the Login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // Create new scene and show it
            Scene scene = new Scene(root);
            Stage stage = (Stage) createDemandeButton.getScene().getWindow();

            // Handle case where the scene might not be attached yet
            if (stage == null) {
                stage = new Stage();
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayDemande(Demande demande) {
        // Format and display all demande fields
        dateLabel.setText(demande.getDate().format(formatter));
        eauLabel.setText(String.valueOf(demande.getEau()));
        nbrRepasLabel.setText(String.valueOf(demande.getNbr_repas()));
        snacksLabel.setText(demande.isSnacks() ? "Oui" : "Non");

        // Handle nullable fields
        caloriesLabel.setText(demande.getCalories() != null ? String.valueOf(demande.getCalories()) : "Non spécifié");
        activityLabel.setText(demande.getActivity() != null ? demande.getActivity() : "Non spécifié");
        dureeLabel.setText(String.valueOf(demande.getDuree_activite()));
        sommeilLabel.setText(demande.getSommeil() != null ? demande.getSommeil() : "Non spécifié");

        patientIdLabel.setText(String.valueOf(demande.getPatient_id()));
    }

    @FXML
    private void handleCreateDemande(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeCreateView.fxml", event);
    }

    @FXML
    private void handleEditDemande(ActionEvent event) {
        try {
            // We need custom handling here to set the demande in controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeEditView.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current demande
            DemandeEditViewController controller = loader.getController();
            controller.setDemande(currentDemande);

            // Using SceneManager's approach for fullscreen support
            Stage stage = (Stage) editDemandeButton.getScene().getWindow();

            // Create scene with screen dimensions for full screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Preserve stylesheets if any
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire de modification", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteDemande(ActionEvent event) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la demande");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette demande ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // If confirmed, delete the demande
                boolean deleted = demandeDAO.delete(currentDemande.getId());

                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande supprimée", "La demande a été supprimée avec succès.");

                    // Refresh the view
                    loadPatientDemande();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression", "La demande n'a pas pu être supprimée.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression",
                        "La demande n'a pas pu être supprimée: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
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
                showErrorDialog("Erreur", "Impossible de récupérer les informations du patient.");
                return;
            }

            // Since we need to pass patient to controller, we need custom loading
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

            // Preserve stylesheets if any
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

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

    @FXML
    public void navigateToEvent(ActionEvent event) {
        SceneManager.loadScene("/fxml/eventFront.fxml", event);
    }

    @FXML
    private void redirectProduit(ActionEvent event) {
        SceneManager.loadScene("/fxml/front/showProduit.fxml", event);
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