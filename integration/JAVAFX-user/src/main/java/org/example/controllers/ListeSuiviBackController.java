package org.example.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.suivie_medical;
import org.example.services.SuivServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ListeSuiviBackController implements Initializable {

    @FXML
    private ListView<suivie_medical> listViewSuivi;

    @FXML
    private Label titleLabel;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshButton;

    @FXML
    private Button addButton;

    @FXML
    private Button exportPdfButton;

    @FXML
    private Button statsButton;

    @FXML
    private Button buttoncommande;

    @FXML
    private Button profileButton;

    @FXML
    private Button historique;

    @FXML
    private Button suivi;

    @FXML
    private Button tablesButton;

    @FXML
    private Button eventButton;

    @FXML
    private Button reservationButton;

    @FXML
    private Button statistiqueButton;

    @FXML
    private Button statisticsButton;

    @FXML
    private Button statisticsButton1;

    @FXML
    private Button acceuil;

    @FXML
    private Button userProfileButton;

    @FXML
    private Button manageReportsButton;

    private SuivServices suivServices;
    private ObservableList<suivie_medical> suiviList;
    private FilteredList<suivie_medical> filteredSuiviList;

    // Format de date pour l'analyse
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suivServices = new SuivServices();

        // Initialize filter options
        initializeFilterOptions();

        // Setup search functionality
        setupSearch();

        // Setup refresh button
        setupRefreshButton();

        // Setup export PDF button
        setupExportPdfButton();

        // Setup add button
        setupAddButton();

        setupListView();
        // Ne plus appeler addHeaderToListView() pour éviter la duplication
        loadData();

        // Configuration des boutons d'action
        statsButton.setOnAction(event -> handleStatsView());
        buttoncommande.setOnAction(event -> handleCommandeRedirect());
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        reservationButton.setOnAction(event -> handleReservationRedirect());
        statistiqueButton.setOnAction(event -> handleStatistiqueRedirect());
        profileButton.setOnAction(event -> handleProfileRedirect());

        // Boutons qui étaient référencés dans le FXML mais manquants dans le contrôleur initial
        if (acceuil != null) {
            acceuil.setOnAction(event -> handleAcceuilRedirect());
        }

    }


    @FXML
    public void navigateToReportDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ReportDashboard.fxml"));
            Stage stage = (Stage) manageReportsButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger le tableau de bord des signalements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStatisticsRedirect(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/UserStatistics.fxml"));
            Stage stage = (Stage) statisticsButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger les statistiques utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStatisticsCommandeRedirect(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/CommandeStatistics.fxml"));
            Stage stage = (Stage) statisticsButton1.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger les statistiques des commandes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleStatsView() {
        try {
            // Replace direct FXMLLoader with SceneManager
            SceneManager.loadScene("/fxml/stats_suivi.fxml", new ActionEvent(statsButton, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupAddButton() {
        if (addButton != null) {
            addButton.setOnAction(event -> handleAddSuivi());
        }
    }

    private void handleAddSuivi() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ajouter_suivi.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un suivi médical");
            stage.setScene(new Scene(root));
            stage.show();

            // Rafraîchir la liste après fermeture de la fenêtre d'ajout
            stage.setOnHidden(event -> loadData());
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger le formulaire d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleReservationRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ReservationList.fxml"));
            Stage stage = (Stage) reservationButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des réservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleStatistiqueRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/EventStatistics.fxml"));
            Stage stage = (Stage) statistiqueButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger les statistiques des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleProfileRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Profile.fxml"));
            Stage stage = (Stage) profileButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupExportPdfButton() {
        if (exportPdfButton != null) {
            exportPdfButton.setOnAction(event -> exportToPdf());
        } else {
            // Try to find the button after the scene is loaded
            listViewSuivi.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    exportPdfButton = (Button) newScene.getRoot().lookup("Button[text=\"Export to PDF\"]");
                    if (exportPdfButton != null) {
                        exportPdfButton.setOnAction(event -> exportToPdf());
                    }
                }
            });
        }
    }

    private void exportToPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("suivis_medicaux.pdf");

        // Show save file dialog
        File file = fileChooser.showSaveDialog(listViewSuivi.getScene().getWindow());

        if (file != null) {
            try {
                // Create PDF document
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(file));

                document.open();

                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
                Paragraph title = new Paragraph("Liste des Suivis Médicaux", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));  // Add some space

                // Add date
                Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
                Paragraph dateParagraph = new Paragraph("Exporté le: " + LocalDate.now().format(dateFormatter), dateFont);
                dateParagraph.setAlignment(Element.ALIGN_RIGHT);
                document.add(dateParagraph);
                document.add(new Paragraph(" "));  // Add some space

                // Create table
                PdfPTable table = new PdfPTable(3);  // 3 columns: Date, Commentaire, Patient
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.5f, 3f, 1.5f});  // Column width ratios

                // Add table headers
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
                BaseColor headerColor = new BaseColor(59, 130, 246);  // #3b82f6 (blue)

                PdfPCell dateHeaderCell = new PdfPCell(new Phrase("Date", headerFont));
                dateHeaderCell.setBackgroundColor(headerColor);
                dateHeaderCell.setPadding(5);
                dateHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell commentaireHeaderCell = new PdfPCell(new Phrase("Commentaire", headerFont));
                commentaireHeaderCell.setBackgroundColor(headerColor);
                commentaireHeaderCell.setPadding(5);
                commentaireHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell patientHeaderCell = new PdfPCell(new Phrase("Patient", headerFont));
                patientHeaderCell.setBackgroundColor(headerColor);
                patientHeaderCell.setPadding(5);
                patientHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                table.addCell(dateHeaderCell);
                table.addCell(commentaireHeaderCell);
                table.addCell(patientHeaderCell);

                // Add data rows
                Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
                BaseColor rowColor1 = new BaseColor(249, 250, 251);  // #f9fafb (light gray)
                BaseColor rowColor2 = BaseColor.WHITE;

                boolean alternateColor = true;

                for (suivie_medical suivi : filteredSuiviList) {
                    BaseColor rowColor = alternateColor ? rowColor1 : rowColor2;
                    alternateColor = !alternateColor;

                    PdfPCell dateCell = new PdfPCell(new Phrase(suivi.getDate(), contentFont));
                    dateCell.setBackgroundColor(rowColor);
                    dateCell.setPadding(5);

                    PdfPCell commentaireCell = new PdfPCell(new Phrase(suivi.getCommentaire(), contentFont));
                    commentaireCell.setBackgroundColor(rowColor);
                    commentaireCell.setPadding(5);

                    // Format patient information
                    String patientInfo = "N/A";
                    if (suivi.getHistorique() != null) {
                        patientInfo = suivi.getHistorique().getNom() + " " + suivi.getHistorique().getPrenom();
                    }

                    PdfPCell patientCell = new PdfPCell(new Phrase(patientInfo, contentFont));
                    patientCell.setBackgroundColor(rowColor);
                    patientCell.setPadding(5);

                    table.addCell(dateCell);
                    table.addCell(commentaireCell);
                    table.addCell(patientCell);
                }

                document.add(table);

                // Add footer
                document.add(new Paragraph(" "));
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
                Paragraph footer = new Paragraph("Ce document a été généré automatiquement par le système de gestion médical.", footerFont);
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(footer);

                document.close();

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exportation réussie");
                alert.setHeaderText(null);
                alert.setContentText("Le fichier PDF a été enregistré avec succès.");
                alert.showAndWait();

            } catch (Exception e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur d'exportation");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur s'est produite lors de l'exportation: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    private void initializeFilterOptions() {
        // Add filter options to the ComboBox
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "Tous",
                "Date récente → ancienne",
                "Date ancienne → récente",
                "Dernier mois",
                "Dernière semaine"
        );
        filterComboBox.setItems(filterOptions);
        filterComboBox.setValue("Tous"); // Default value

        // Add listener to filter the data when selection changes
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void setupSearch() {
        // Add listener to search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void setupRefreshButton() {
        if (refreshButton != null) {
            refreshButton.setOnAction(event -> {
                loadData();
                searchField.clear();
                filterComboBox.setValue("Tous");
            });
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String filterType = filterComboBox.getValue();

        // Si le filtre est basé sur la date, trier la liste d'abord
        if (filterType.contains("Date")) {
            sortByDate(filterType.contains("récente"));
        }

        LocalDate now = LocalDate.now();
        LocalDate oneWeekAgo = now.minusWeeks(1);
        LocalDate oneMonthAgo = now.minusMonths(1);

        filteredSuiviList.setPredicate(suivi -> {
            boolean matchesSearch = true;
            boolean matchesType = true;

            // Apply search filter
            if (searchText != null && !searchText.isEmpty()) {
                matchesSearch = (suivi.getCommentaire() != null && suivi.getCommentaire().toLowerCase().contains(searchText)) ||
                        (suivi.getDate() != null && suivi.getDate().toLowerCase().contains(searchText)) ||
                        (suivi.getHistorique() != null &&
                                ((suivi.getHistorique().getNom() != null && suivi.getHistorique().getNom().toLowerCase().contains(searchText)) ||
                                        (suivi.getHistorique().getPrenom() != null && suivi.getHistorique().getPrenom().toLowerCase().contains(searchText))));
            }

            // Apply type filter
            if (filterType != null && !filterType.equals("Tous")) {
                if (filterType.equals("Patient 'uu II'")) {
                    matchesType = suivi.getHistorique() != null &&
                            "uu II".equals(suivi.getHistorique().getNom() + " " + suivi.getHistorique().getPrenom());
                } else if (filterType.equals("Patient 'dcvv Issaoui'")) {
                    matchesType = suivi.getHistorique() != null &&
                            "dcvv Issaoui".equals(suivi.getHistorique().getNom() + " " + suivi.getHistorique().getPrenom());
                } else if (filterType.equals("Patient 'Ameniii ouerfellixXX'")) {
                    matchesType = suivi.getHistorique() != null &&
                            "Ameniii ouerfellixXX".equals(suivi.getHistorique().getNom() + " " + suivi.getHistorique().getPrenom());
                } else if (filterType.equals("Patient 'N/A'")) {
                    matchesType = suivi.getHistorique() == null;
                } else if (filterType.equals("Dernière semaine")) {
                    matchesType = isDateInRange(suivi.getDate(), oneWeekAgo, now);
                } else if (filterType.equals("Dernier mois")) {
                    matchesType = isDateInRange(suivi.getDate(), oneMonthAgo, now);
                } else if (filterType.contains("Date")) {
                    // Pour les filtres de tri par date, on ne filtre pas, on trie seulement
                    matchesType = true;
                }
            }

            return matchesSearch && matchesType;
        });
    }

    /**
     * Vérifie si une date est dans une plage donnée
     */
    private boolean isDateInRange(String dateStr, LocalDate start, LocalDate end) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            return !date.isBefore(start) && !date.isAfter(end);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Trie la liste par date
     * @param descending true pour trier de la plus récente à la plus ancienne, false pour l'inverse
     */
    private void sortByDate(boolean descending) {
        Comparator<suivie_medical> dateComparator = Comparator.comparing(
                suivi -> {
                    try {
                        return LocalDate.parse(suivi.getDate(), dateFormatter);
                    } catch (DateTimeParseException e) {
                        // En cas d'erreur de format de date, mettre à la fin de la liste
                        return LocalDate.MIN;
                    }
                }
        );

        if (descending) {
            dateComparator = dateComparator.reversed();
        }

        FXCollections.sort(suiviList, dateComparator);
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
            // Use SceneManager to load the scene and ensure it displays maximized
            SceneManager.loadScene("/fxml/liste_suivi_back.fxml", new ActionEvent(suivi, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des suivis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleHistoriqueRedirect() {
        try {
            // Use SceneManager to load the scene and ensure it displays maximized
            SceneManager.loadScene("/fxml/liste_historique_back.fxml", new ActionEvent(historique, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleCommandeRedirect() {
        try {
            SceneManager.loadScene("/fxml/back/showCommande.fxml", new ActionEvent(buttoncommande, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des commandes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTablesRedirect() {
        try {
            SceneManager.loadScene("/fxml/back/showProduit.fxml", new ActionEvent(tablesButton, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleeventRedirect() {
        try {
            SceneManager.loadScene("/fxml/listevent.fxml", new ActionEvent(eventButton, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAcceuilRedirect() {
        try {
            SceneManager.loadScene("/fxml/AdminDashboard.fxml", new ActionEvent(acceuil, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void setupListView() {
        listViewSuivi.setCellFactory(listView -> new ListStyleCell());
        listViewSuivi.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");
    }

    public void loadData() {
        List<suivie_medical> suivis = suivServices.afficher();
        suiviList = FXCollections.observableArrayList(suivis);

        // Create a filtered list wrapper around the original list
        filteredSuiviList = new FilteredList<>(suiviList, p -> true);

        // Set the filtered list as items of the ListView
        listViewSuivi.setItems(filteredSuiviList);
    }

    // Custom ListCell pour afficher les éléments en style liste
    private class ListStyleCell extends ListCell<suivie_medical> {
        private final VBox container = new VBox(5); // Espacement vertical de 5px
        private final HBox infoLine = new HBox(15);
        private final HBox commentLine = new HBox();
        private final Label dateLabel = new Label();
        private final Label commentaireLabel = new Label();
        private final Label patientLabel = new Label();
        private final Label separatorLabel = new Label("|");

        public ListStyleCell() {
            // Configuration du style de la cellule
            container.setPadding(new Insets(10, 5, 10, 5));
            container.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

            // Style des labels
            dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b82f6;"); // Date en bleu et gras
            separatorLabel.setStyle("-fx-text-fill: #cbd5e1;"); // Séparateur en gris clair
            patientLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b;"); // Patient en italique et gris

            // La date et le patient sur la même ligne
            infoLine.getChildren().addAll(dateLabel, separatorLabel, patientLabel);

            // Commentaire sur une ligne séparée avec une indentation
            commentLine.setPadding(new Insets(0, 0, 0, 10)); // Indentation à gauche
            commentaireLabel.setWrapText(true);
            commentLine.getChildren().add(commentaireLabel);

            // Assemblage de la cellule
            container.getChildren().addAll(infoLine, commentLine);
        }

        @Override
        protected void updateItem(suivie_medical suivi, boolean empty) {
            super.updateItem(suivi, empty);

            if (empty || suivi == null) {
                setGraphic(null);
            } else {
                // Configuration des données
                dateLabel.setText("Date: " + suivi.getDate());
                commentaireLabel.setText(suivi.getCommentaire());

                // Format des informations patient
                if (suivi.getHistorique() != null) {
                    patientLabel.setText("Patient: " + suivi.getHistorique().getNom() + " " + suivi.getHistorique().getPrenom());
                } else {
                    patientLabel.setText("Patient: N/A");
                }

                // Appliquer un style différent pour les lignes alternées
                if (getIndex() % 2 == 0) {
                    container.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
                } else {
                    container.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
                }

                setGraphic(container);
            }
        }
    }
    @FXML
    public void refreshList(ActionEvent event) {
        loadData();
        searchField.clear();
        filterComboBox.setValue("Tous");
    }
}