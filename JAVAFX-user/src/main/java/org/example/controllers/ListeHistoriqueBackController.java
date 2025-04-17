package org.example.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.historique_traitement;
import org.example.services.HisServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeHistoriqueBackController implements Initializable {

    @FXML
    private ListView<historique_traitement> listViewHistorique;
    @FXML private Button suivi;

    @FXML
    private Label titleLabel;

    private HisServices hisServices;
    private ObservableList<historique_traitement> historiqueList;
    @FXML private ComboBox<String> filterComboBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hisServices = new HisServices();

        setupListView();
        addHeaderToListView();
        loadData();
        suivi.setOnAction(event -> handleSuiviRedirect());

    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleSuiviRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_suivi_back.fxml"));
            Stage stage = (Stage) suivi.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupListView() {
        listViewHistorique.setCellFactory(lv -> new TableLikeListCell());
    }

    private void addHeaderToListView() {
        // Create header container that matches the same width as the list items
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(10);
        headerGrid.setPrefWidth(980);
        headerGrid.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 8; -fx-border-color: #ddd; -fx-border-width: 0 0 2 0; -fx-font-weight: bold;");

        // Add the same column constraints as the cell grid
        for (int i = 0; i < 6; i++) {
            javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();

            if (i == 0 || i == 1) {
                column.setPrefWidth(120); // Nom, Prenom
            } else if (i == 2) {
                column.setPrefWidth(120); // Maladie
            } else if (i == 3) {
                column.setPrefWidth(280); // Description
            } else if (i == 4) {
                column.setPrefWidth(150); // Type
            } else if (i == 5) {
                column.setPrefWidth(150); // Bilan
            }

            headerGrid.getColumnConstraints().add(column);
        }

        // Create header labels
        Label nomHeaderLabel = new Label("Nom");
        Label prenomHeaderLabel = new Label("Prenom");
        Label maladieHeaderLabel = new Label("Maladie");
        Label descriptionHeaderLabel = new Label("Description");
        Label typeHeaderLabel = new Label("Type Traitement");
        Label bilanHeaderLabel = new Label("Bilan");

        // Add header labels to grid
        headerGrid.add(nomHeaderLabel, 0, 0);
        headerGrid.add(prenomHeaderLabel, 1, 0);
        headerGrid.add(maladieHeaderLabel, 2, 0);
        headerGrid.add(descriptionHeaderLabel, 3, 0);
        headerGrid.add(typeHeaderLabel, 4, 0);
        headerGrid.add(bilanHeaderLabel, 5, 0);

        // Wrap the ListView in a parent container and add the header above it
        ListView<historique_traitement> oldListView = listViewHistorique;

        // Since we can't directly modify the FXML structure, we add the header grid as the
        // first item in the ListView but make it non-selectable and style it as a header
        if (listViewHistorique.getParent() instanceof javafx.scene.layout.Pane) {
            javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) listViewHistorique.getParent();
            int indexOfListView = parent.getChildren().indexOf(listViewHistorique);

            parent.getChildren().add(indexOfListView, headerGrid);

            // Adjust the ListView's top margin to make room for the header
            listViewHistorique.setStyle(listViewHistorique.getStyle() + "-fx-border-radius: 0; -fx-background-radius: 0;");
        }
    }

    public void loadData() {
        List<historique_traitement> histories = hisServices.afficher();
        historiqueList = FXCollections.observableArrayList(histories);
        listViewHistorique.setItems(historiqueList);
    }

    // Custom ListCell to display items in a table-like format
    private class TableLikeListCell extends ListCell<historique_traitement> {
        private final GridPane gridPane = new GridPane();
        private final Label nomLabel = new Label();
        private final Label prenomLabel = new Label();
        private final Label maladieLabel = new Label();
        private final Label descriptionLabel = new Label();
        private final Label typeLabel = new Label();
        private final Label bilanLabel = new Label();

        public TableLikeListCell() {
            // Configure grid pane
            gridPane.setHgap(10);
            gridPane.setPrefWidth(980);
            gridPane.getStyleClass().add("table-row");
            gridPane.setStyle("-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

            // Add column constraints
            for (int i = 0; i < 6; i++) {
                javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();

                // Set different widths for columns
                if (i == 0 || i == 1) {
                    column.setPrefWidth(120); // Nom, Prenom
                } else if (i == 2) {
                    column.setPrefWidth(120); // Maladie
                } else if (i == 3) {
                    column.setPrefWidth(280); // Description
                } else if (i == 4) {
                    column.setPrefWidth(150); // Type
                } else if (i == 5) {
                    column.setPrefWidth(150); // Bilan
                }

                gridPane.getColumnConstraints().add(column);
            }

            // Configure text elements
            nomLabel.setWrapText(true);
            prenomLabel.setWrapText(true);
            maladieLabel.setWrapText(true);
            descriptionLabel.setWrapText(true);
            typeLabel.setWrapText(true);
            bilanLabel.setWrapText(true);

            // Add elements to grid
            gridPane.add(nomLabel, 0, 0);
            gridPane.add(prenomLabel, 1, 0);
            gridPane.add(maladieLabel, 2, 0);
            gridPane.add(descriptionLabel, 3, 0);
            gridPane.add(typeLabel, 4, 0);
            gridPane.add(bilanLabel, 5, 0);
        }

        @Override
        protected void updateItem(historique_traitement historique, boolean empty) {
            super.updateItem(historique, empty);

            if (empty || historique == null) {
                setGraphic(null);
            } else {
                // Set data to labels
                nomLabel.setText(historique.getNom());
                prenomLabel.setText(historique.getPrenom());
                maladieLabel.setText(historique.getMaladies());
                descriptionLabel.setText(historique.getDescription());
                typeLabel.setText(historique.getType_traitement());
                bilanLabel.setText(historique.getBilan());

                setGraphic(gridPane);
            }
        }
    }
}