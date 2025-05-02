package controller;

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
import model.RendezVous;
import service.RendezVousDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class RendezVousViewController implements Initializable {

    @FXML
    private ListView<RendezVous> rendezVousListView;

    @FXML
    private Button btnAjouter;

    @FXML
    private TextField txtRecherche;

    private RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();
    private FilteredList<RendezVous> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        loadRendezVous();
        setupButtonHandlers();
        configurerRecherche();
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
            showAlert("Erreur de chargement",
                    "Impossible de charger les rendez-vous : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void setupButtonHandlers() {
        btnAjouter.setOnAction(e -> addRendezVous());
    }

    private void addRendezVous() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter-rendez-vous.fxml"));
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

    // Méthode pour ajouter un rendez-vous directement à la liste sans recharger
    public void addRendezVousToList(RendezVous rdv) {
        if (!rendezVousList.contains(rdv)) {
            rendezVousList.add(rdv);
            // La liste étant filtrable, le filtre s'applique automatiquement
        }
    }

    // Méthode pour mettre à jour un rendez-vous directement dans la liste
    public void updateRendezVousInList(RendezVous updatedRdv) {
        for (int i = 0; i < rendezVousList.size(); i++) {
            if (rendezVousList.get(i).getId() == updatedRdv.getId()) {
                rendezVousList.set(i, updatedRdv);
                break;
            }
        }
        // Rafraîchir la vue pour refléter les changements
        rendezVousListView.refresh();
    }

    // Méthode pour supprimer un rendez-vous directement de la liste
    public void removeRendezVousFromList(RendezVous rdv) {
        rendezVousList.removeIf(r -> r.getId() == rdv.getId());
    }

    private void viewRendezVous(RendezVous rdv) {
        // Implémentation de la visualisation du rendez-vous
        showAlert("Détails du rendez-vous",
                "Date et Heure: " + rdv.getFormattedDateTime() + "\n" +
                        "Statut: " + rdv.getStatut() + "\n" +
                        "Description: " + rdv.getDescription() + "\n" +
                        "Planning: " + rdv.getPlanningInfo(),
                Alert.AlertType.INFORMATION);
    }

    private void editRendezVous(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter-rendez-vous.fxml"));
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
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void confirmDelete(RendezVous rdv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le rendez-vous");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce rendez-vous du " + rdv.getFormattedDateTime() + " ?");

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                rendezVousDAO.deleteRendezVous(rdv.getId());
                // Supprimer directement de la liste sans recharger
                removeRendezVousFromList(rdv);
                showAlert("Succès", "Rendez-vous supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer le rendez-vous: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    public void refreshData() {
        // Cette méthode est conservée pour compatibilité, mais on préférera
        // les méthodes spécifiques ci-dessus pour des mises à jour ponctuelles
        loadRendezVous();
    }

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

    private void configurerRecherche() {
        // Filtrage intelligent basé sur la recherche textuelle
        filteredData = new FilteredList<>(rendezVousList, p -> true);

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(rdv -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filtre = newValue.toLowerCase();

                return rdv.getFormattedDateTime().toLowerCase().contains(filtre)
                        || rdv.getStatut().toLowerCase().contains(filtre)
                        || rdv.getDescription().toLowerCase().contains(filtre)
                        || rdv.getPlanningInfo().toLowerCase().contains(filtre);
            });
        });

        SortedList<RendezVous> sortedData = new SortedList<>(filteredData);

        rendezVousListView.setItems(sortedData);
    }

    /**
     * Classe interne pour créer des cellules personnalisées dans la ListView
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

            // Créer la carte (card)
            VBox card = new VBox();
            card.setSpacing(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8;");

            // Ajouter une ombre à la carte
            DropShadow shadow = new DropShadow();
            shadow.setRadius(5.0);
            shadow.setOffsetX(1.0);
            shadow.setOffsetY(1.0);
            shadow.setColor(Color.color(0, 0, 0, 0.2));
            card.setEffect(shadow);

            // En-tête avec la date et l'heure
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            header.setSpacing(15);

            // Date et heure
            Label labelDateTime = new Label(rdv.getFormattedDateTime());
            labelDateTime.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #495057;");

            // Badge de statut
            Label statusLabel = new Label(rdv.getStatut());
            statusLabel.setPadding(new Insets(5, 10, 5, 10));
            statusLabel.setTextFill(Color.WHITE);

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

            header.getChildren().addAll(labelDateTime, statusLabel);

            // Description du rendez-vous
            Label labelDescription = new Label(rdv.getDescription());
            labelDescription.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
            labelDescription.setWrapText(true);

            // Infos du planning
            Label labelPlanning = new Label("📅 " + rdv.getPlanningInfo());
            labelPlanning.setStyle("-fx-text-fill: #007bff; -fx-font-style: italic;");

            // Boutons d'action
            Button btnVoir = new Button("👁️ Voir");
            btnVoir.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");

            Button btnModifier = new Button("✏️ Modifier");
            btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-font-weight: bold; -fx-padding: 5 10;");

            Button btnSupprimer = new Button("🗑️ Supprimer");
            btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");

            HBox actionsContainer = new HBox(10, btnVoir, btnModifier, btnSupprimer);
            actionsContainer.setAlignment(Pos.CENTER_RIGHT);

            // Configurer les événements des boutons
            btnVoir.setOnAction(event -> viewRendezVous(rdv));
            btnModifier.setOnAction(event -> editRendezVous(rdv));
            btnSupprimer.setOnAction(event -> confirmDelete(rdv));

            // Assembler la carte
            card.getChildren().addAll(header, labelDescription, labelPlanning, actionsContainer);

            // Ajouter un peu d'espace entre les cartes
            setGraphic(card);
            setPadding(new Insets(5));
        }
    }
}