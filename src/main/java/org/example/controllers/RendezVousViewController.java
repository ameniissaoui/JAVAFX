package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.models.RendezVous;
import org.example.services.RendezVousDAO;

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
        alert.setContentText("Supprimer le rendez-vous du " + rdv.getFormattedDateTime() + " ?");

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

    /**
     * Classe ListCell personnalisée
     */
    private class RendezVousListCell extends ListCell<RendezVous> {
        @Override
        protected void updateItem(RendezVous rdv, boolean empty) {
            super.updateItem(rdv, empty);
            if (empty || rdv == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            VBox card = new VBox(10);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");
            card.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.2)));

            Label dateTime = new Label(rdv.getFormattedDateTime());
            dateTime.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            // Status label based on the 'statut' of the RendezVous
            Label statusLabel = new Label(rdv.getStatut());
            statusLabel.setPadding(new Insets(5, 10, 5, 10));
            statusLabel.setTextFill(Color.WHITE);

            // Check the status of the RendezVous and apply the corresponding style
            switch (rdv.getStatut().toLowerCase()) {
                case "confirmé":
                    statusLabel.setStyle("-fx-background-color: #28a745; -fx-background-radius: 3;");
                    break;
                case "en attente":
                    statusLabel.setStyle("-fx-background-color: #ffc107; -fx-background-radius: 3;");
                    statusLabel.setTextFill(Color.BLACK);
                    break;
                case "annulé":
                    statusLabel.setStyle("-fx-background-color: #dc3545; -fx-background-radius: 3;");
                    break;
                default:
                    statusLabel.setStyle("-fx-background-color: #6c757d; -fx-background-radius: 3;");
                    break;
            }

            Label desc = new Label(rdv.getDescription());
            desc.setWrapText(true);

            Label planning = new Label("📅 " + rdv.getPlanningInfo());
            planning.setStyle("-fx-font-style: italic;");

            HBox boutons = new HBox(10);
            boutons.setAlignment(Pos.CENTER_RIGHT);

            Button voir = new Button("👁️ Voir");
            voir.setOnAction(e -> viewRendezVous(rdv));

            Button modifier = new Button("✏️ Modifier");
            modifier.setOnAction(e -> editRendezVous(rdv));

            Button supprimer = new Button("🗑️ Supprimer");
            supprimer.setOnAction(e -> confirmDelete(rdv));

            boutons.getChildren().addAll(voir, modifier, supprimer);
            card.getChildren().addAll(dateTime, statusLabel, desc, planning, boutons);  // Add statusLabel here
            setGraphic(card);
        }
    }

    public void refreshData() {
        loadRendezVous();                // recharge depuis base de données via DAO (déjà existante)
        rendezVousListView.refresh();    // rafraîchir interface si besoin
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
}
