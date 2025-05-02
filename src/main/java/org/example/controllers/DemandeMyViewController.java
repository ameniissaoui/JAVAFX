package org.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
        profile.setOnAction(event -> handleProfileRedirect());
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
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/patient_profile.fxml"));
            Stage stage = (Stage) profile.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
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
        try {
            // Load the DemandeCreateView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeCreateView.fxml"));
            Parent root = loader.load();

            // Get the controller (no need to explicitly set patient ID thanks to SessionManager)
            DemandeCreateViewController controller = loader.getController();

            // Create new scene and show it
            Scene scene = new Scene(root);
            Stage stage = (Stage) createDemandeButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire de création", e.getMessage());
        }
    }

    @FXML
    private void handleEditDemande(ActionEvent event) {
        try {
            // Load the DemandeEditView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeEditView.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current demande
            DemandeEditViewController controller = loader.getController();
            controller.setDemande(currentDemande);

            // Create new scene and show it
            Scene scene = new Scene(root);
            Stage stage = (Stage) editDemandeButton.getScene().getWindow();
            stage.setScene(scene);
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
}