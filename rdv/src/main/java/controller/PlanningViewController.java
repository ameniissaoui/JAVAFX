package controller;

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
import model.Planning;
import service.PlanningDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import java.io.File;
import java.awt.Color;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.FileOutputStream;
import java.awt.Desktop;

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
    private Button btnExporterPDF;

    private PlanningDAO planningDAO = new PlanningDAO();
    private ObservableList<Planning> planningList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        loadPlannings();
        setupButtonHandlers();
        btnExporterPDF.setOnAction(e -> {
            exporterPlanningVersPDF();
        });
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
     * Charge les plannings depuis la base de données
     */
    private void loadPlannings() {
        try {
            planningList.clear();
            planningList.addAll(planningDAO.getAllPlannings());
            planningTable.setItems(planningList);
        } catch (SQLException e) {
            showAlert("Erreur de chargement",
                    "Impossible de charger les plannings : " + e.getMessage(),
                    Alert.AlertType.ERROR);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterplanning.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/planning-edit-view.fxml"));
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
            try {
                planningDAO.deletePlanning(planning.getId());
                refreshData();
                showAlert("Succès", "Planning supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer le planning: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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

    private void exporterPlanningVersPDF() {
        try {
            // Crée le dossier exports/ s'il n'existe pas
            File dossier = new File("exports");
            if (!dossier.exists()) {
                dossier.mkdirs();
            }

            // Générateur du PDF
            File fichierPDF = new File(dossier, "planning.pdf");
            Document document = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
            PdfWriter.getInstance(document, new FileOutputStream(fichierPDF));
            document.open();

            // Titre
            Font fontTitre = new Font(Font.HELVETICA, 20, Font.BOLD);
            Paragraph titre = new Paragraph("Planning des Médecins", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            document.add(Chunk.NEWLINE);

            // Tableau
            PdfPTable table = new PdfPTable(3);
            table.setWidths(new float[]{3, 3, 3});
            table.setWidthPercentage(100);

            // Couleurs
            Color headerColor = new Color(0, 102, 204); // Bleu foncé
            Color rowEvenColor = new Color(230, 240, 255); // Bleu clair
            Color rowOddColor = Color.WHITE;

            Font fontHeader = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Font fontCell = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);

            // En-têtes
            String[] headers = {"Jour", "Heure début", "Heure fin"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Remplir les lignes avec alternance de couleurs
            boolean isEven = true;
            for (Planning planning : planningList) {
                Color bgColor = isEven ? rowEvenColor : rowOddColor;
                isEven = !isEven;

                table.addCell(createStyledCell(planning.getJour(), fontCell, bgColor));
                table.addCell(createStyledCell(planning.getHeuredebut().toString(), fontCell, bgColor));
                table.addCell(createStyledCell(planning.getHeurefin().toString(), fontCell, bgColor));
            }

            document.add(table);
            document.close();

            // S'ouvre automatiquement
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(fichierPDF);
            }

            showAlert("Export Réussi", "Le fichier 'planning.pdf' a été généré avec succès dans le dossier 'exports'.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur PDF", "Une erreur est survenue :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private PdfPCell createStyledCell(String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    // Méthode utilitaire pour styliser les cellules
    private void addCellToTable(PdfPTable table, String content) {
        addCellToTable(table, content, false);
    }
    private void addCellToTable(PdfPTable table, String content, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(content));
        cell.setPadding(8);
        if (isHeader) {
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPhrase(new Phrase(content, new Font(Font.HELVETICA, 12, Font.BOLD)));
        }
        table.addCell(cell);
    }
}