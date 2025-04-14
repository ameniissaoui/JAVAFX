package org.example.controllers;

import org.example.models.suivie_medical;
import org.example.services.SuivServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeSuiviBackController implements Initializable {

    @FXML
    private ListView<suivie_medical> listViewSuivi;

    @FXML
    private Label titleLabel;

    private SuivServices suivServices;
    private ObservableList<suivie_medical> suiviList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suivServices = new SuivServices();

        setupListView();
        addHeaderToListView();
        loadData();
    }

    private void setupListView() {
        listViewSuivi.setCellFactory(listView -> new TableLikeListCell());
    }

    private void addHeaderToListView() {
        // Create header container that matches the same width as the list items
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(10);
        headerGrid.setPrefWidth(780);
        headerGrid.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 8; -fx-border-color: #ddd; -fx-border-width: 0 0 2 0; -fx-font-weight: bold;");

        // Add the same column constraints as the cell grid
        for (int i = 0; i < 4; i++) {
            javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();

            if (i == 0) {
                column.setPrefWidth(50);  // ID
            } else if (i == 1) {
                column.setPrefWidth(120); // Date
            } else if (i == 2) {
                column.setPrefWidth(410); // Commentaire
            } else if (i == 3) {
                column.setPrefWidth(200); // Patient
            }

            headerGrid.getColumnConstraints().add(column);
        }

        // Create header labels
        Label idHeaderLabel = new Label("ID");
        Label dateHeaderLabel = new Label("Date");
        Label commentaireHeaderLabel = new Label("Commentaire");
        Label patientHeaderLabel = new Label("Patient");

        // Add header labels to grid
        headerGrid.add(idHeaderLabel, 0, 0);
        headerGrid.add(dateHeaderLabel, 1, 0);
        headerGrid.add(commentaireHeaderLabel, 2, 0);
        headerGrid.add(patientHeaderLabel, 3, 0);

        // Add the header above the ListView
        if (listViewSuivi.getParent() instanceof javafx.scene.layout.Pane) {
            javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) listViewSuivi.getParent();
            int indexOfListView = parent.getChildren().indexOf(listViewSuivi);

            parent.getChildren().add(indexOfListView, headerGrid);

            // Adjust the ListView's top margin to make room for the header
            listViewSuivi.setStyle(listViewSuivi.getStyle() + "-fx-border-radius: 0; -fx-background-radius: 0;");
        }
    }

    public void loadData() {
        List<suivie_medical> suivis = suivServices.afficher();
        suiviList = FXCollections.observableArrayList(suivis);
        listViewSuivi.setItems(suiviList);
    }

    // Custom ListCell to display items in a table-like format
    private class TableLikeListCell extends ListCell<suivie_medical> {
        private final GridPane gridPane = new GridPane();
        private final Label idLabel = new Label();
        private final Label dateLabel = new Label();
        private final Label commentaireLabel = new Label();
        private final Label patientLabel = new Label();

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
                    column.setPrefWidth(120); // Date
                } else if (i == 2) {
                    column.setPrefWidth(410); // Commentaire
                } else if (i == 3) {
                    column.setPrefWidth(200); // Patient
                }

                gridPane.getColumnConstraints().add(column);
            }

            // Configure text elements
            idLabel.setWrapText(false);
            dateLabel.setWrapText(true);
            commentaireLabel.setWrapText(true);
            patientLabel.setWrapText(true);

            // Add elements to grid
            gridPane.add(idLabel, 0, 0);
            gridPane.add(dateLabel, 1, 0);
            gridPane.add(commentaireLabel, 2, 0);
            gridPane.add(patientLabel, 3, 0);
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

                // Format patient information (nom et prénom)
                if (suivi.getHistorique() != null) {
                    patientLabel.setText(suivi.getHistorique().getNom() + " " + suivi.getHistorique().getPrenom());
                } else {
                    patientLabel.setText("N/A");
                }

                setGraphic(gridPane);
            }
        }
    }
}