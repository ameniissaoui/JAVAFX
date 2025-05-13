package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.models.Patient;
import org.example.models.RendezVous;
import org.example.services.RendezVousDAO;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class RendezVousViewController implements Initializable {

    @FXML private ListView<RendezVous> rendezVousListView;
    @FXML private Button btnAjouter;
    @FXML private TextField txtRecherche;
    @FXML private Button btnRetour;

    private Patient currentPatient;
    private SessionManager sessionManager;
    private final RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private final ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();
    private FilteredList<RendezVous> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        loadRendezVous();
        setupButtonHandlers();
        configurerRecherche();
        if (btnRetour != null) {
            btnRetour.setOnAction(e -> handleBack());
        }
    }

    private void configureListView() {
        rendezVousListView.setCellFactory(param -> new RendezVousListCell());
        rendezVousListView.setItems(rendezVousList);
    }

    private void loadRendezVous() {
        try {
            rendezVousList.clear();
            rendezVousList.addAll(rendezVousDAO.getAllRendezVous());
        } catch (SQLException e) {
            showAlert("Erreur de chargement", "Impossible de charger les rendez-vous : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupButtonHandlers() {
        btnAjouter.setOnAction(e -> addRendezVous());
    }

    private void addRendezVous() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter-rendez-vous.fxml"));
            Parent root = loader.load();

            AjouterRendezVousController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Créer un nouveau rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void addRendezVousToList(RendezVous rdv) {
        if (!rendezVousList.contains(rdv)) {
            rendezVousList.add(rdv);
        }
    }

    public void updateRendezVousInList(RendezVous updatedRdv) {
        for (int i = 0; i < rendezVousList.size(); i++) {
            if (rendezVousList.get(i).getId() == updatedRdv.getId()) {
                rendezVousList.set(i, updatedRdv);
                break;
            }
        }
        rendezVousListView.refresh();
    }

    public void removeRendezVousFromList(RendezVous rdv) {
        rendezVousList.removeIf(r -> r.getId() == rdv.getId());
    }

    private void viewRendezVous(RendezVous rdv) {
        showAlert("Détails du rendez-vous",
                "Date et Heure : " + rdv.getFormattedDateTime() + "\n" +
                        "Statut : " + rdv.getStatut() + "\n" +
                        "Description : " + rdv.getDescription() + "\n" +
                        "Planning : " + rdv.getPlanningInfo(),
                Alert.AlertType.INFORMATION);
    }

    private void editRendezVous(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter-rendez-vous.fxml"));
            Parent root = loader.load();

            AjouterRendezVousController controller = loader.getController();
            controller.setRendezVous(rdv);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier un rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void confirmDelete(RendezVous rdv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le rendez-vous");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le rendez-vous du " + rdv.getFormattedDateTime() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                rendezVousDAO.deleteRendezVous(rdv.getId());
                removeRendezVousFromList(rdv);
                showAlert("Succès", "Rendez-vous supprimé avec succès.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer le rendez-vous : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void configurerRecherche() {
        filteredData = new FilteredList<>(rendezVousList, p -> true);
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            String filtre = newValue.toLowerCase();
            filteredData.setPredicate(rdv -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return rdv.getFormattedDateTime().toLowerCase().contains(filtre)
                        || rdv.getStatut().toLowerCase().contains(filtre)
                        || rdv.getDescription().toLowerCase().contains(filtre)
                        || rdv.getPlanningInfo().toLowerCase().contains(filtre);
            });
        });
        rendezVousListView.setItems(new SortedList<>(filteredData));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private class RendezVousListCell extends ListCell<RendezVous> {
        @Override
        protected void updateItem(RendezVous rdv, boolean empty) {
            super.updateItem(rdv, empty);
            if (empty || rdv == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            // Main card container
            VBox card = new VBox();
            card.getStyleClass().add("appointment-card");
            card.setSpacing(10);
            card.setPadding(new Insets(20));

            // Header with time and status
            HBox header = new HBox();
            header.getStyleClass().add("appointment-header");
            header.setAlignment(Pos.CENTER_LEFT);

            Label timeLabel = new Label(rdv.getFormattedDateTime());
            timeLabel.getStyleClass().add("appointment-time");

            Label statusLabel = new Label(rdv.getStatut());
            statusLabel.getStyleClass().add("appointment-status");
            switch (rdv.getStatut().toLowerCase()) {
                case "confirmé":
                    statusLabel.getStyleClass().add("status-confirmed");
                    break;
                case "en attente":
                    statusLabel.getStyleClass().add("status-pending");
                    break;
                case "annulé":
                    statusLabel.getStyleClass().add("status-cancelled");
                    break;
            }

            header.getChildren().addAll(timeLabel, statusLabel);

            // Details section
            VBox details = new VBox();
            details.getStyleClass().add("appointment-details");
            details.setSpacing(8);

            Label descLabel = new Label(rdv.getDescription());
            descLabel.getStyleClass().add("appointment-description");
            descLabel.setWrapText(true);

            Label doctorLabel = new Label("👨‍⚕️ " + rdv.getPlanningInfo());
            doctorLabel.getStyleClass().add("appointment-doctor");

            details.getChildren().addAll(descLabel, doctorLabel);

            // Action buttons
            HBox actions = new HBox();
            actions.getStyleClass().add("appointment-actions");
            actions.setSpacing(10);

            Button viewBtn = new Button("Voir détails");
            viewBtn.getStyleClass().addAll("action-button", "view-button");
            viewBtn.setOnAction(e -> viewRendezVous(rdv));

            Button editBtn = new Button("Modifier");
            editBtn.getStyleClass().addAll("action-button", "edit-button");
            editBtn.setOnAction(e -> editRendezVous(rdv));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.getStyleClass().addAll("action-button", "delete-button");
            deleteBtn.setOnAction(e -> confirmDelete(rdv));

            actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

            // Assemble the card
            card.getChildren().addAll(header, details, actions);
            setGraphic(card);
        }
    }

    public void refreshData() {
        loadRendezVous();
        rendezVousListView.refresh();
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_view_patient.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner au menu principal : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Other navigation methods remain the same...
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de navigation",
                    "Impossible de charger la page des produits: " + e.getMessage());
        }
    }

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        try {
            if (!checkLoginForNavigation()) return;
            SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de navigation",
                    "Impossible de charger le calendrier: " + e.getMessage());
        }
    }

    private boolean checkLoginForNavigation() {
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder à cette fonctionnalité.");
            return false;
        }
        return true;
    }

    @FXML
    private void showNotifications(ActionEvent event) {
        Alert notificationsAlert = new Alert(Alert.AlertType.INFORMATION);
        notificationsAlert.setTitle("Notifications");
        notificationsAlert.setHeaderText("Vos notifications");

        VBox notificationsContent = new VBox(10);
        notificationsContent.getChildren().addAll(
                new Label("Rappel: Prendre médicament à 14:00"),
                new Label("Rendez-vous demain à 10:30")
        );

        notificationsAlert.getDialogPane().setContent(notificationsContent);
        notificationsAlert.showAndWait();
    }

    public void redirectToHistorique(ActionEvent event) {
        SceneManager.loadScene("/fxml/ajouter_historique.fxml", event);
    }

    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_patient.fxml", event);
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        try {
            if (currentPatient != null) {
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
        }
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        try {
            if (currentPatient != null) {
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
            SceneManager.loadScene("/fxml/DoctorList.fxml", event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des médecins: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}