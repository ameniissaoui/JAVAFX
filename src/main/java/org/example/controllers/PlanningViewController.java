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
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.example.models.Planning;
import org.example.models.RendezVous;
import org.example.services.PlanningDAO;
import org.example.services.RendezVousDAO;
import org.example.services.GoogleCalendarService;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PlanningViewController implements Initializable {

    @FXML private ListView<Planning> planningListView;
    @FXML private Button btnAjouter;
    @FXML private Button btnExporterPDF;
    @FXML public DatePicker datePicker;
    @FXML public ComboBox<Planning> cmbPlanning;
    @FXML private Button btnRetour;

    private final PlanningDAO planningDAO = new PlanningDAO();
    private final ObservableList<Planning> planningList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        loadPlannings();
        setupButtonHandlers();
        if (btnRetour != null) {
            btnRetour.setOnAction(e -> handleBack());
        }
    }

    public DatePicker getDatePicker() { return datePicker; }
    public ComboBox<Planning> getCmbPlanning() { return cmbPlanning; }

    public void refreshData() { loadPlannings(); }

    private void configureListView() {
        planningListView.setCellFactory(param -> new PlanningListCell());
        planningListView.setItems(planningList);
    }

    private void setupButtonHandlers() {
        btnAjouter.setOnAction(e -> showAddEditDialog(null));
        btnExporterPDF.setOnAction(e -> exporterPlanningVersPDF());
    }

    public void loadPlannings() {
        try {
            planningList.clear();
            List<Planning> plannings = planningDAO.getAllPlannings();
            planningList.addAll(plannings);
        } catch (SQLException e) {
            showAlert("Erreur de chargement", "Impossible de charger les plannings : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAddEditDialog(Planning planning) {
        try {
            FXMLLoader loader;
            Parent root;
            Stage stage = new Stage();

            if (planning == null) {
                loader = new FXMLLoader(getClass().getResource("/fxml/ajouter-planning.fxml"));
                root = loader.load();
                AjouterPlanningController controller = loader.getController();
                controller.setParentController(this);
                stage.setTitle("Ajouter un planning");
            } else {
                loader = new FXMLLoader(getClass().getResource("/fxml/planning-edit-view.fxml"));
                root = loader.load();
                PlanningEditController controller = loader.getController();
                controller.setParentController(this);
                controller.setPlanning(planning);
                stage.setTitle("Modifier le planning");
            }

            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'édition : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exporterPlanningVersPDF() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("planning.pdf"));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Planning du Médecin", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new PdfPCell(new Phrase("Jour", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Heure de début", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Heure de fin", headerFont)));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            for (Planning planning : planningList) {
                table.addCell(planning.getJour());
                table.addCell(planning.getHeuredebut().format(formatter));
                table.addCell(planning.getHeurefin().format(formatter));
            }

            document.add(table);
            document.close();

            showAlert("Export PDF", "Le fichier PDF a bien été généré.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Échec de l'export PDF : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void confirmDelete(Planning planning) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression d'un planning");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText("Voulez-vous supprimer le planning du " + planning.getJour() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                planningDAO.deletePlanning(planning.getId());
                refreshData();
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key")) {
                    showAlert("Erreur", "Ce planning est associé à des rendez-vous. Supprimez-les d'abord.", Alert.AlertType.WARNING);
                } else {
                    showAlert("Erreur", "Erreur de suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private class PlanningListCell extends ListCell<Planning> {
        @Override
        protected void updateItem(Planning planning, boolean empty) {
            super.updateItem(planning, empty);

            if (empty || planning == null) {
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

            // En-tête de la carte avec le jour
            HBox header = new HBox();
            header.setSpacing(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label labelJour = new Label(planning.getJour());
            labelJour.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #495057;");

            // Ajouter l'icône de notification
            Button btnNotification = new Button("🔔");
            btnNotification.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;");
            btnNotification.setTooltip(new Tooltip("Voir les rendez-vous de ce jour"));

            // Ajouter un compteur de rendez-vous
            try {
                RendezVousDAO rendezVousDAO = new RendezVousDAO();
                int nombreRendezVous = rendezVousDAO.getRendezVousByPlanningId(planning.getId()).size();

                if (nombreRendezVous > 0) {
                    btnNotification.setText("🔔 " + nombreRendezVous);
                    btnNotification.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 15;");
                } else {
                    btnNotification.setText("🔔");
                    btnNotification.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;");
                }
            } catch (SQLException e) {
                btnNotification.setText("🔔");
                System.err.println("Erreur lors du comptage des rendez-vous: " + e.getMessage());
            }

            // Événement de clic sur l'icône
            btnNotification.setOnAction(event -> afficherRendezVousDuJour(planning));

            header.getChildren().addAll(labelJour, btnNotification);

            // Détails des heures
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            Label labelHoraire = new Label("Horaires: " + planning.getHeuredebut().format(formatter) +
                    " - " + planning.getHeurefin().format(formatter));
            labelHoraire.setStyle("-fx-text-fill: #6c757d;");

            // Boutons d'action
            Button btnModifier = new Button("✏️ Modifier");
            btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-font-weight: bold; -fx-padding: 5 10;");

            Button btnSupprimer = new Button("🗑️ Supprimer");
            btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");

            HBox actionsContainer = new HBox(10, btnModifier, btnSupprimer);
            actionsContainer.setAlignment(Pos.CENTER_RIGHT);

            // Configurer les événements des boutons
            btnModifier.setOnAction(event -> {
                showAddEditDialog(planning);
            });

            btnSupprimer.setOnAction(event -> {
                confirmDelete(planning);
            });

            // Assembler la carte
            card.getChildren().addAll(header, labelHoraire, actionsContainer);

            // Ajouter un peu d'espace entre les cartes
            setGraphic(card);
            setPadding(new Insets(5));
        }
    }

    private void afficherRendezVousDuJour(Planning planning) {
        try {
            RendezVousDAO rendezVousDAO = new RendezVousDAO();
            List<RendezVous> rendezVousDuPlanning = rendezVousDAO.getRendezVousByPlanningId(planning.getId());

            // Créer une nouvelle fenêtre pour afficher les rendez-vous
            Stage stage = new Stage();
            stage.setTitle("Rendez-vous du " + planning.getJour());
            stage.initModality(Modality.APPLICATION_MODAL);

            if (rendezVousDuPlanning.isEmpty()) {
                // Message si aucun rendez-vous
                VBox emptyContent = new VBox(15);
                emptyContent.setAlignment(Pos.CENTER);
                emptyContent.setPadding(new Insets(40));
                emptyContent.setStyle("-fx-background-color: white;");

                Label iconLabel = new Label("📅");
                iconLabel.setStyle("-fx-font-size: 48px;");

                Label msgTitle = new Label("Aucun rendez-vous trouvé");
                msgTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #495057;");

                Label msgSubtitle = new Label("Il n'y a pas encore de rendez-vous programmés pour ce jour.");
                msgSubtitle.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

                emptyContent.getChildren().addAll(iconLabel, msgTitle, msgSubtitle);

                Scene scene = new Scene(emptyContent, 400, 300);
                stage.setScene(scene);
            } else {
                // Afficher les rendez-vous existants
                VBox root = new VBox(15);
                root.setPadding(new Insets(20));
                root.setStyle("-fx-background-color: white;");

                // En-tête avec jour et horaires du planning
                HBox header = new HBox();
                header.setAlignment(Pos.CENTER_LEFT);
                header.setSpacing(10);

                VBox titleBox = new VBox(5);

                Label titleLabel = new Label("Rendez-vous du " + planning.getJour());
                titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #212529;");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                Label planningInfoLabel = new Label("Horaires du médecin: " +
                        planning.getHeuredebut().format(formatter) + " - " +
                        planning.getHeurefin().format(formatter));
                planningInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

                titleBox.getChildren().addAll(titleLabel, planningInfoLabel);

                Button btnClose = new Button("✖");
                btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c757d; -fx-font-size: 16px;");
                btnClose.setOnAction(e -> stage.close());

                HBox.setHgrow(titleBox, Priority.ALWAYS);
                header.getChildren().addAll(titleBox, btnClose);

                // Ligne séparatrice
                Separator separator = new Separator();
                separator.setStyle("-fx-background-color: #dee2e6;");

                // ScrollPane pour la liste des rendez-vous
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
                scrollPane.setFitToWidth(true);
                scrollPane.setPannable(true);

                VBox cardsContainer = new VBox(15);
                cardsContainer.setPadding(new Insets(10, 5, 10, 5));

                // Créer une carte pour chaque rendez-vous
                for (RendezVous rdv : rendezVousDuPlanning) {
                    VBox card = new VBox(8);
                    card.setPadding(new Insets(15));
                    card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8;");

                    // Ajouter une ombre à la carte
                    DropShadow shadow = new DropShadow();
                    shadow.setRadius(3.0);
                    shadow.setOffsetX(1.0);
                    shadow.setOffsetY(1.0);
                    shadow.setColor(Color.color(0, 0, 0, 0.1));
                    card.setEffect(shadow);

                    // Date et heure du rendez-vous
                    Label dateLabel = new Label(rdv.getFormattedDateTime());
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #212529;");

                    // Statut avec badge coloré
                    Label statusLabel = new Label(rdv.getStatut());
                    statusLabel.setPadding(new Insets(3, 8, 3, 8));
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

                    // Description
                    Label descLabel = new Label(rdv.getDescription());
                    descLabel.setStyle("-fx-text-fill: #495057;");
                    descLabel.setWrapText(true);

                    // Ligne des boutons d'action
                    HBox actions = new HBox(10);
                    actions.setAlignment(Pos.CENTER_RIGHT);

                    Button btnSupprimer = new Button("Supprimer");
                    btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px;");
                    btnSupprimer.setOnAction(e -> {
                        if (confirmerSuppressionRendezVous(rdv)) {
                            stage.close();
                            // Rafraîchir après suppression
                            afficherRendezVousDuJour(planning);
                        }
                    });

                    actions.getChildren().add(btnSupprimer);

                    // Assembler la carte
                    card.getChildren().addAll(dateLabel, statusLabel, descLabel, actions);
                    cardsContainer.getChildren().add(card);
                }

                scrollPane.setContent(cardsContainer);

                root.getChildren().addAll(header, separator, scrollPane);

                Scene scene = new Scene(root, 500, 600);
                stage.setScene(scene);
            }

            // Personnaliser l'apparence de la fenêtre
            stage.setMinWidth(400);
            stage.setMinHeight(300);

            // Afficher la fenêtre
            stage.showAndWait();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de récupérer les rendez-vous: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean confirmerSuppressionRendezVous(RendezVous rdv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr?");
        alert.setContentText("Voulez-vous vraiment supprimer ce rendez-vous du " + rdv.getFormattedDateTime() + " ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprimer de Google Calendar si un ID existe
                if (rdv.getGoogleEventId() != null && !rdv.getGoogleEventId().isEmpty()) {
                    try {
                        GoogleCalendarService googleCalendarService = new GoogleCalendarService();
                        googleCalendarService.deleteEvent(rdv.getGoogleEventId());
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la suppression de l'événement Google Calendar: " + e.getMessage());
                        // Continuer la suppression en base de données même si la suppression Google échoue
                    }
                }

                // Supprimer de la base de données
                RendezVousDAO rendezVousDAO = new RendezVousDAO();
                rendezVousDAO.deleteRendezVous(rdv.getId());
                return true;
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer le rendez-vous: " + e.getMessage(), Alert.AlertType.ERROR);
                return false;
            }
        }

        return false;
    }

    @FXML
    public void handleAddPlanning() {
        showAddEditDialog(null);
    }

    @FXML
    public void handleExportPDF() {
        exporterPlanningVersPDF();
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_view_medecin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner au menu principal : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}