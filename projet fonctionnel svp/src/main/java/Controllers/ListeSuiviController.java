package Controllers;

import Model.historique_traitement;
import Model.suivie_medical;
import Services.HisServices;
import Services.SuivServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeSuiviController implements Initializable {

    @FXML
    private ListView<suivie_medical> listViewSuivi;

    @FXML
    private Label patientInfoLabel;

    @FXML
    private Button retourButton;

    @FXML
    private Button ajouterButton;

    private SuivServices suivServices;
    private HisServices hisServices;
    private ObservableList<suivie_medical> suiviList;
    private historique_traitement historiqueTraitement;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suivServices = new SuivServices();
        hisServices = new HisServices();

        // Setup custom cell factory for the ListView
        setupListView();

        // Set placeholder message when no data
        listViewSuivi.setPlaceholder(new Label("Aucun suivi disponible"));
    }

    private void setupListView() {
        listViewSuivi.setCellFactory(listView -> new TableLikeListCell());
    }

    public void setHistoriqueTraitement(historique_traitement historiqueTraitement) {
        this.historiqueTraitement = historiqueTraitement;

        if (historiqueTraitement != null) {
            // Load patient's medical follow-ups
            loadData();
        } else {
            System.err.println("Erreur: historiqueTraitement est null");
        }
    }

    public void loadData() {
        if (historiqueTraitement != null && listViewSuivi != null) {
            System.out.println("Chargement des suivis pour l'historique ID: " + historiqueTraitement.getId());

            try {
                // Get follow-ups by historique ID
                List<suivie_medical> suivis = suivServices.getSuiviByHistoriqueId(historiqueTraitement.getId());
                System.out.println("Nombre de suivis trouvés: " + suivis.size());

                // Debug: Display found follow-ups
                for (suivie_medical s : suivis) {
                    System.out.println("Suivi ID: " + s.getId() + ", Date: " + s.getDate() + ", Commentaire: " + s.getCommentaire());
                }

                // Set data to list view
                suiviList = FXCollections.observableArrayList(suivis);
                listViewSuivi.getItems().clear(); // Clear existing items first
                listViewSuivi.setItems(suiviList);
                listViewSuivi.refresh();
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement des données: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                        "Impossible de charger les suivis médicaux: " + e.getMessage());
            }
        } else {
            System.err.println("Impossible de charger les données: historiqueTraitement ou listViewSuivi est null");
        }
    }

    @FXML
    private void afficherSuivi(suivie_medical suivi) {
        try {
            System.out.println("Tentative d'affichage du suivi ID: " + suivi.getId());

            // Vérifier que le fichier FXML existe
            URL url = getClass().getResource("/views/afficher_suivi.fxml");
            if (url == null) {
                System.err.println("Le fichier FXML n'a pas été trouvé!");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier FXML n'a pas été trouvé!");
                return;
            }
            System.out.println("URL du fichier FXML: " + url);

            // Load the display view
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            System.out.println("FXML chargé avec succès");

            // Get controller and initialize data
            AfficherSuiviController controller = loader.getController();
            controller.setSuiviMedical(suivi);
            controller.setHistoriqueTraitement(historiqueTraitement);

            System.out.println("Contrôleur initialisé");

            // Create and display new window
            Stage stage = new Stage();
            stage.setTitle("Détails du Suivi Médical");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            System.out.println("Fenêtre affichée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue d'affichage: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception inattendue: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterNouveauSuivi() {
        try {
            // Load add follow-up view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ajouter_suivi.fxml"));
            Parent root = loader.load();

            // Get controller and initialize data
            AjouterSuivController controller = loader.getController();
            controller.setHistoriqueTraitement(historiqueTraitement);

            // Set a callback to refresh data after adding
            controller.setOnSuiviAddedCallback(this::loadData);

            // Create and display new window
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Suivi Médical");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload data after closing add window
            loadData();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void retourVersList() {
        try {
            // Load historique list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/liste_historique.fxml"));
            Parent root = loader.load();

            // Replace current scene
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setTitle("Liste des Historiques de Traitement");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de retourner à la liste des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void modifierSuivi(suivie_medical suivi) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modifier_suivi.fxml"));
            Parent root = loader.load();

            // Get controller and initialize data
            ModifierSuiviController controller = loader.getController();
            controller.setSuiviMedical(suivi);
            controller.setHistoriqueTraitement(historiqueTraitement);

            // Set a callback to refresh data after modification
            controller.setRefreshCallback(this::loadData);

            // Create and display new window
            Stage stage = new Stage();
            stage.setTitle("Modifier Suivi Médical");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload data after closing window
            loadData();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerSuivi(suivie_medical suivi) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer ce suivi médical ?");
        confirmDialog.setContentText("Date: " + suivi.getDate() + "\nCommentaire: " + suivi.getCommentaire());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                suivServices.remove(suivi);
                loadData(); // Refresh list after deletion

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Suppression réussie");
                alert.setContentText("Le suivi médical a été supprimé avec succès.");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur de suppression");
                alert.setContentText("Une erreur est survenue lors de la suppression: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Custom ListCell to display items in a table-like format
    private class TableLikeListCell extends ListCell<suivie_medical> {
        private final GridPane gridPane = new GridPane();
        private final Label idLabel = new Label();
        private final Label dateLabel = new Label();
        private final Label commentaireLabel = new Label();
        private final HBox actionsBox = new HBox(5);

        private final Button btnModifier = new Button("Modifier");
        private final Button btnSupprimer = new Button("Supprimer");
        private final Button btnAfficher = new Button("Afficher");

        public TableLikeListCell() {
            // Configure grid pane
            gridPane.setHgap(10);
            gridPane.setPrefWidth(780);
            gridPane.getStyleClass().add("table-row");
            gridPane.setStyle("-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

            // Add column constraints
            for (int i = 0; i < 4; i++) {
                javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();

                // Set different widths for columns
                if (i == 0) {
                    column.setPrefWidth(50);  // ID
                } else if (i == 1) {
                    column.setPrefWidth(150); // Date
                } else if (i == 2) {
                    column.setPrefWidth(400); // Commentaire
                } else if (i == 3) {
                    column.setPrefWidth(170); // Actions
                }

                gridPane.getColumnConstraints().add(column);
            }

            // Configure text elements
            idLabel.setWrapText(false);
            dateLabel.setWrapText(true);
            commentaireLabel.setWrapText(true);

            // Add elements to grid
            gridPane.add(idLabel, 0, 0);
            gridPane.add(dateLabel, 1, 0);
            gridPane.add(commentaireLabel, 2, 0);
            gridPane.add(actionsBox, 3, 0);

            // Style buttons
            btnModifier.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
            btnSupprimer.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white;");
            btnAfficher.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white;");

            // Configure buttons
            configureButtons();
        }

        private void configureButtons() {
            btnModifier.setOnAction(event -> {
                suivie_medical suivi = getItem();
                if (suivi != null) {
                    modifierSuivi(suivi);
                }
            });

            btnSupprimer.setOnAction(event -> {
                suivie_medical suivi = getItem();
                if (suivi != null) {
                    supprimerSuivi(suivi);
                }
            });

            btnAfficher.setOnAction(event -> {
                suivie_medical suivi = getItem();
                if (suivi != null) {
                    afficherSuivi(suivi);
                }
            });
        }

        @Override
        protected void updateItem(suivie_medical suivi, boolean empty) {
            super.updateItem(suivi, empty);

            if (empty || suivi == null) {
                setGraphic(null);
            } else {
                // Set data to labels
                idLabel.setText(String.valueOf(suivi.getId()));
                dateLabel.setText(suivi.getDate());
                commentaireLabel.setText(suivi.getCommentaire());

                // Configure actions buttons
                actionsBox.getChildren().clear();
                actionsBox.getChildren().addAll(btnAfficher, btnModifier, btnSupprimer);

                setGraphic(gridPane);
            }
        }
    }

}