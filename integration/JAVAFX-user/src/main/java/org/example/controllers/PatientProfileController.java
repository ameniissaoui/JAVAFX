package org.example.controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.PatientService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PatientProfileController extends BaseProfileController {
    private PatientService patientService;
    @FXML
    private AnchorPane sidebarContainer;
    private boolean sidebarOpen = false;

    @FXML
    private Button mesTraitementsButton;

    @FXML
    private Button mesReservationsButton;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isPatient()) {
            showMessage("Erreur: Accès non autorisé", "danger");
            handleLogout();
            return;
        }

        Patient patient = SessionManager.getInstance().getCurrentPatient();
        if (patient != null) {
            currentUser = patient;
            loadUserData();
        }

        patientService = new PatientService();
        setUserService();
        super.initialize(url, resourceBundle);
        // Make sure sidebar is initially hidden
        if (sidebarContainer != null) {
            sidebarContainer.setVisible(false);
            sidebarContainer.setPrefWidth(0);
        }

        // Check if sidebar buttons are properly initialized before setting event handlers
        if (mesTraitementsButton != null) {
            mesTraitementsButton.setOnAction(event -> {
                // Handle mes traitements button click
                System.out.println("Mes Traitements clicked");
                // Add your navigation logic here
            });
        }

        if (mesReservationsButton != null) {
            mesReservationsButton.setOnAction(event -> {
                // Handle mes reservations button click
                System.out.println("Mes Réservations clicked");
                // Add your navigation logic here
            });
        }
    }

    @Override
    protected void setUserService() {
        this.userService = patientService;
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Patient) {
            super.setUser(user, "patient");
            setUserService();
        } else {
            throw new IllegalArgumentException("User must be a Patient");
        }
    }

    public void setPatient(Patient patient) {
        this.currentUser = patient;
        this.userType = "patient";
        loadUserData();
        setUserService();
        if (patient != null) {
            SessionManager.getInstance().setCurrentUser(patient, "patient");
        }
    }

    @Override
    protected void saveUser() {
        if (currentUser instanceof Patient) {
            try {
                patientService.update((Patient) currentUser);
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                updateDisplayLabels();
                showMessage("Profil patient mis à jour avec succès", "success");
            } catch (Exception e) {
                showMessage("Erreur lors de la mise à jour du profil: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        }
    }


    @FXML
    public void redirectProduit(ActionEvent event) {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des produits: " + e.getMessage());
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
    @FXML
    public void toggleSidebar() {
        if (sidebarOpen) {
            // Close sidebar with animation
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(sidebarContainer.prefWidthProperty(), 0, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(250), kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(e -> sidebarContainer.setVisible(false)); // Hide after animation completes
            timeline.play();
            sidebarOpen = false;
        } else {
            // Make visible before opening
            sidebarContainer.setVisible(true);
            sidebarContainer.setPrefWidth(0);

            // Open sidebar with animation
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(sidebarContainer.prefWidthProperty(), 220, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(250), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
            sidebarOpen = true;
        }
    }


///////////////////////////////////NAVIGATION///////////////////////////////////////////////////////
    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_patient.fxml", event);
    }
    @FXML
    public void redirectToDemande(ActionEvent event) {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de demande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rendez-vous-view.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
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
    public void redirectToHistorique(ActionEvent event) {
            SceneManager.loadScene("/fxml/ajouter_historique.fxml", event);

    }
    @FXML
    public void redirectToCalendrier(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_calendar.fxml", event);

    }
}