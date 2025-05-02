package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.Medecin;
import org.example.models.Planning;
import org.example.services.PlanningDAO;
import org.example.util.MaConnexion;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class PlanningViewController implements Initializable {

    @FXML
    private TableView<Planning> planningTable;

    @FXML
    private TableColumn<Planning, String> colJour;

    @FXML
    private TableColumn<Planning, LocalTime> colHeureDebut;

    @FXML
    private TableColumn<Planning, LocalTime> colHeureFin;

    @FXML
    private TableColumn<Planning, Void> colActions;

    @FXML
    private Button btnAjouter;
    
    @FXML
    private Label medecinNameLabel;

    private PlanningDAO planningDAO = new PlanningDAO();
    private ObservableList<Planning> planningList = FXCollections.observableArrayList();
    private Medecin currentMedecin;
    private static final int MAX_RETRIES = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if a user is logged in and is a Medecin
        if (SessionManager.getInstance().isLoggedIn() && 
            SessionManager.getInstance().getCurrentUser() instanceof Medecin) {
            currentMedecin = (Medecin) SessionManager.getInstance().getCurrentUser();
            
            // Display medecin name if label exists
            if (medecinNameLabel != null) {
                medecinNameLabel.setText("Dr. " + currentMedecin.getPrenom() + " " + currentMedecin.getNom());
            }
        } else {
            showAlert("Accès refusé", "Vous devez être connecté en tant que médecin pour accéder à cette page.", Alert.AlertType.ERROR);
            // We continue anyway to show the UI, but operations might be limited
        }
        
        configureTableColumns();
        loadPlannings();
        setupButtonHandlers();
    }

    /**
     * Configure les colonnes de la TableView
     */
    private void configureTableColumns() {
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        colHeureDebut.setCellValueFactory(new PropertyValueFactory<>("heuredebut"));
        colHeureFin.setCellValueFactory(new PropertyValueFactory<>("heurefin"));

        // Affichage formaté des heures
        colHeureDebut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        });

        colHeureFin.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        });

        // Configuration de la colonne Actions avec boutons
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox actionsContainer = new HBox(10, btnModifier, btnSupprimer);

            {
                // Styliser les boutons
                btnModifier.getStyleClass().add("button-modifier");
                btnSupprimer.getStyleClass().add("button-supprimer");

                // Style supplémentaire pour les boutons
                btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-font-weight: bold; -fx-padding: 5 10;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");

                actionsContainer.setAlignment(Pos.CENTER);

                // Configuration des événements pour les boutons
                btnModifier.setOnAction(event -> {
                    Planning planning = getTableView().getItems().get(getIndex());
                    showAddEditDialog(planning);
                });

                btnSupprimer.setOnAction(event -> {
                    Planning planning = getTableView().getItems().get(getIndex());
                    confirmDelete(planning);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionsContainer);
                }
            }
        });
    }

    /**
     * Charge les plannings depuis la base de données avec mécanisme de retry
     */
    private void loadPlannings() {
        loadPlanningsWithRetry(0);
    }
    
    /**
     * Implémentation récursive du chargement avec retry
     */
    private void loadPlanningsWithRetry(int retryCount) {
        try {
            // Test connection first
            if (!planningDAO.testConnection()) {
                System.out.println("Connection test failed. Attempting reconnection...");
                MaConnexion.getInstance().reconnect();
            }
            
            planningList.clear();
            planningList.addAll(planningDAO.getAllPlannings());
            planningTable.setItems(planningList);
            
            // If we get here, it worked - good!
            System.out.println("Plannings loaded successfully");
        } catch (SQLException e) {
            System.err.println("Error loading plannings: " + e.getMessage() + " (attempt " + (retryCount+1) + " of " + MAX_RETRIES + ")");
            
            if (retryCount < MAX_RETRIES) {
                // Try reconnecting
                try {
                    System.out.println("Attempting to reconnect...");
                    MaConnexion.getInstance().reconnect();
                    // Wait a little before retrying
                    Thread.sleep(500);
                    // Recursive retry
                    loadPlanningsWithRetry(retryCount + 1);
                } catch (Exception ex) {
                    showAlert("Erreur de connexion", 
                            "Impossible de se reconnecter à la base de données: " + ex.getMessage(), 
                            Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Erreur de chargement",
                        "Impossible de charger les plannings après " + MAX_RETRIES + " tentatives: " + e.getMessage(),
                        Alert.AlertType.ERROR);
                
                // Show the error message, but at least provide an empty list so the UI works
                planningList.clear();
                planningTable.setItems(planningList);
            }
        }
    }

    /**
     * Configure les gestionnaires d'événements des boutons
     */
    private void setupButtonHandlers() {
        btnAjouter.setOnAction(e -> openAjouterPlanningWindow());
    }

    /**
     * Ouvre la fenêtre d'ajout de planning
     */
    private void openAjouterPlanningWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter-planning.fxml"));
            Parent root = loader.load();

            AjouterPlanningController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un planning");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Affiche une boîte de dialogue pour modifier un planning
     * @param planning Le planning à modifier
     */
    private void showAddEditDialog(Planning planning) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/planning-edit-view.fxml"));
            Parent root = loader.load();

            PlanningEditController controller = loader.getController();
            controller.setPlanning(planning);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier un planning");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'édition: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Demande confirmation avant de supprimer un planning
     * @param planning Le planning à supprimer
     */
    private void confirmDelete(Planning planning) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le planning");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le planning du " + planning.getJour() + " ?");

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deletePlanningWithRetry(planning.getId(), 0);
        }
    }
    
    /**
     * Delete planning with retry mechanism
     */
    private void deletePlanningWithRetry(int planningId, int retryCount) {
        try {
            // Test connection
            if (!planningDAO.testConnection()) {
                MaConnexion.getInstance().reconnect();
            }
            
            planningDAO.deletePlanning(planningId);
            refreshData();
            showAlert("Succès", "Planning supprimé avec succès", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            System.err.println("Error deleting planning: " + e.getMessage() + " (attempt " + (retryCount+1) + ")");
            
            if (retryCount < MAX_RETRIES) {
                // Try reconnecting
                try {
                    MaConnexion.getInstance().reconnect();
                    // Wait a bit before retrying
                    Thread.sleep(500);
                    // Recursive retry
                    deletePlanningWithRetry(planningId, retryCount + 1);
                } catch (Exception ex) {
                    showAlert("Erreur de connexion", 
                            "Impossible de se reconnecter à la base de données: " + ex.getMessage(), 
                            Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Erreur", "Impossible de supprimer le planning après " + MAX_RETRIES + 
                        " tentatives: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Return to the doctor profile page
     */
    @FXML
    private void returnToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/medecin_profile.fxml"));
            Parent root = loader.load();
            
            MedecinProfileController controller = loader.getController();
            if (currentMedecin != null) {
                controller.setMedecin(currentMedecin);
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) planningTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner au profil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Rafraîchit les données de la table
     * Cette méthode est appelée depuis les contrôleurs enfants après sauvegarde
     */
    public void refreshData() {
        loadPlannings();
    }

    /**
     * Affiche une boîte de dialogue d'alerte
     * @param title Titre de l'alerte
     * @param content Contenu du message
     * @param type Type d'alerte
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }
}