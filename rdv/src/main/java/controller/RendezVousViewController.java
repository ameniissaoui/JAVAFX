package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.RendezVous;
import service.RendezVousDAO;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;


import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class RendezVousViewController implements Initializable {

    @FXML
    private TableView<RendezVous> rendezVousTable;

    @FXML
    private TableColumn<RendezVous, LocalDateTime> colDateHeure;

    @FXML
    private TableColumn<RendezVous, String> colStatut;

    @FXML
    private TableColumn<RendezVous, String> colDescription;

    @FXML
    private TableColumn<RendezVous, String> colPlanning;

    @FXML
    private TableColumn<RendezVous, Void> colActions;

    @FXML
    private Button btnAjouter;

    @FXML private TextField txtRecherche;

    private RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        loadRendezVous();
        setupButtonHandlers();
        configurerRecherche();
    }

    private void configureTableColumns() {
        // Configuration de la colonne Date et Heure
        colDateHeure.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateheure()));
        colDateHeure.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });

        // Configuration de la colonne Statut
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item);
                    statusLabel.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
                    statusLabel.setTextFill(Color.WHITE);

                    switch (item.toLowerCase()) {
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

                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });

        // Configuration de la colonne Description
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Configuration de la colonne Planning
        colPlanning.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPlanningInfo()));

        // Configuration de la colonne Actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox actionsContainer = new HBox(5, btnVoir, btnModifier, btnSupprimer);

            {
                // Styliser les boutons
                btnVoir.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-font-weight: bold; -fx-padding: 5 10;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");

                actionsContainer.setAlignment(Pos.CENTER);

                // Configuration des événements
                btnVoir.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    viewRendezVous(rdv);
                });

                btnModifier.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    editRendezVous(rdv);
                });

                btnSupprimer.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    confirmDelete(rdv);
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

    private void loadRendezVous() {
        try {
            rendezVousList.clear();
            rendezVousList.addAll(rendezVousDAO.getAllRendezVous());
            rendezVousTable.setItems(rendezVousList);
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
                refreshData();
                showAlert("Succès", "Rendez-vous supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer le rendez-vous: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    public void refreshData() {
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
        FilteredList<RendezVous> filteredData = new FilteredList<>(rendezVousList, p -> true);

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(rdv -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filtre = newValue.toLowerCase();

                // Tu peux adapter ces conditions selon tes besoins
                return rdv.getFormattedDateTime().toLowerCase().contains(filtre)
                        || rdv.getStatut().toLowerCase().contains(filtre)
                        || rdv.getDescription().toLowerCase().contains(filtre)
                        || rdv.getPlanningInfo().toLowerCase().contains(filtre);
            });
        });

        SortedList<RendezVous> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(rendezVousTable.comparatorProperty());

        rendezVousTable.setItems(sortedData);
    }
}
