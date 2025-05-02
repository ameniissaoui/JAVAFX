package org.example.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.suivie_medical;
import org.example.services.SuivServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatsSuiviController implements Initializable {

    @FXML private BarChart<String, Number> suiviBarChart;
    @FXML private PieChart patientPieChart;
    @FXML private Label totalSuivisLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label thisPeriodLabel;
    @FXML private ComboBox<String> periodeComboBox;
    @FXML private Button backButton;
    @FXML private Button exportStatsButton;
    @FXML private Button refreshButton;

    // Navigation buttons
    @FXML private Button buttoncommande;
    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button tablesButton;
    @FXML private Button eventButton;
    @FXML private Button acceuil;
    @FXML
    private Button reservationButton;
    @FXML
    private Button statistiqueButton;
    private SuivServices suivServices;
    private List<suivie_medical> suiviList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        suivServices = new SuivServices();
        suiviList = suivServices.afficher();

        // Initialize period combo box
        initializePeriodeComboBox();

        // Setup chart data
        loadChartData();

        // Initialize summary statistics
        updateSummaryStats();

        // Setup event handlers
        setupEventHandlers();

        // Setup refresh button
        setupRefreshButton();
        // Configuration des boutons d'action
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

    private void initializePeriodeComboBox() {
        ObservableList<String> periodes = FXCollections.observableArrayList(
                "Tous", "Dernier mois", "Dernière semaine", "Par mois", "Par semaine"
        );
        periodeComboBox.setItems(periodes);
        periodeComboBox.setValue("Tous");

        // Add listener to update charts when period changes
        periodeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            loadChartData();
            updateSummaryStats();
        });
    }

    private void loadChartData() {
        suiviBarChart.getData().clear();
        patientPieChart.getData().clear();

        String selectedPeriode = periodeComboBox.getValue();
        List<suivie_medical> filteredList = filterByPeriod(suiviList, selectedPeriode);

        // Load bar chart data
        loadBarChartData(filteredList, selectedPeriode);

        // Load pie chart data
        loadPieChartData(filteredList);
    }

    private List<suivie_medical> filterByPeriod(List<suivie_medical> list, String period) {
        if (period == null || period.equals("Tous")) {
            return list;
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = now;

        switch (period) {
            case "Dernier mois":
                startDate = now.minusMonths(1);
                break;
            case "Dernière semaine":
                startDate = now.minusWeeks(1);
                break;
            default:
                return list;
        }

        final LocalDate filterStartDate = startDate;
        return list.stream()
                .filter(suivi -> {
                    try {
                        LocalDate suiviDate = LocalDate.parse(suivi.getDate(), dateFormatter);
                        return !suiviDate.isBefore(filterStartDate) && !suiviDate.isAfter(now);
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private void loadBarChartData(List<suivie_medical> list, String period) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de suivis");

        Map<String, Integer> dataMap = new LinkedHashMap<>();

        if (period.equals("Par mois")) {
            Map<String, Long> countByMonth = list.stream()
                    .filter(suivi -> {
                        try {
                            LocalDate.parse(suivi.getDate(), dateFormatter);
                            return true;
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.groupingBy(
                            suivi -> {
                                LocalDate date = LocalDate.parse(suivi.getDate(), dateFormatter);
                                return date.getYear() + "-" + date.getMonthValue();
                            },
                            Collectors.counting()
                    ));

            // Sort by year-month
            countByMonth.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> dataMap.put(formatYearMonth(entry.getKey()), entry.getValue().intValue()));

        } else if (period.equals("Par semaine")) {
            Map<Integer, Long> countByWeek = list.stream()
                    .filter(suivi -> {
                        try {
                            LocalDate.parse(suivi.getDate(), dateFormatter);
                            return true;
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.groupingBy(
                            suivi -> LocalDate.parse(suivi.getDate(), dateFormatter).getDayOfYear() / 7 + 1,
                            Collectors.counting()
                    ));

            // Sort by week number
            countByWeek.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> dataMap.put("Semaine " + entry.getKey(), entry.getValue().intValue()));

        } else {
            // Group by day for default view
            Map<String, Long> countByDay = list.stream()
                    .filter(suivi -> {
                        try {
                            LocalDate.parse(suivi.getDate(), dateFormatter);
                            return true;
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.groupingBy(
                            suivie_medical::getDate,
                            Collectors.counting()
                    ));

            // Sort by date
            countByDay.entrySet().stream()
                    .sorted((e1, e2) -> {
                        try {
                            return LocalDate.parse(e1.getKey(), dateFormatter)
                                    .compareTo(LocalDate.parse(e2.getKey(), dateFormatter));
                        } catch (DateTimeParseException e) {
                            return 0;
                        }
                    })
                    .forEach(entry -> dataMap.put(formatDate(entry.getKey()), entry.getValue().intValue()));
        }

        // Add data to series
        dataMap.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        suiviBarChart.getData().add(series);
    }

    private String formatYearMonth(String yearMonth) {
        String[] parts = yearMonth.split("-");
        if (parts.length != 2) return yearMonth;

        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        String[] monthNames = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};

        return monthNames[month - 1] + " " + year;
    }

    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }

    private void loadPieChartData(List<suivie_medical> list) {
        // Count suivi by historique_traitement id
        Map<Integer, Long> countByHistorique = list.stream()
                .collect(Collectors.groupingBy(
                        suivie_medical::getHistoriqueId,
                        Collectors.counting()
                ));

        // Convert to pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        countByHistorique.forEach((historiqueId, count) ->
                pieChartData.add(new PieChart.Data("Traitement " + historiqueId, count))
        );

        patientPieChart.setData(pieChartData);
        patientPieChart.setTitle("Répartition des suivis par traitement");
    }

    private void updateSummaryStats() {
        String selectedPeriode = periodeComboBox.getValue();
        List<suivie_medical> filteredList = filterByPeriod(suiviList, selectedPeriode);

        // Update total suivis for period
        int totalSuivisCount = filteredList.size();
        totalSuivisLabel.setText(String.valueOf(totalSuivisCount));

        // Count unique historiques/traitements
        long uniqueHistoriquesCount = filteredList.stream()
                .map(suivie_medical::getHistoriqueId)
                .distinct()
                .count();
        totalPatientsLabel.setText(String.valueOf(uniqueHistoriquesCount));

        // Update period label
        updatePeriodLabel(selectedPeriode);
    }

    private void updatePeriodLabel(String period) {
        String periodText;
        LocalDate now = LocalDate.now();

        switch (period) {
            case "Dernier mois":
                LocalDate lastMonth = now.minusMonths(1);
                periodText = formatDate(lastMonth.format(dateFormatter)) + " - " + formatDate(now.format(dateFormatter));
                break;
            case "Dernière semaine":
                LocalDate lastWeek = now.minusWeeks(1);
                periodText = formatDate(lastWeek.format(dateFormatter)) + " - " + formatDate(now.format(dateFormatter));
                break;
            case "Par mois":
                periodText = "Par mois";
                break;
            case "Par semaine":
                periodText = "Par semaine";
                break;
            default:
                periodText = "Tous";
                break;
        }

        thisPeriodLabel.setText(periodText);
    }

    private void setupEventHandlers() {
        // Back button
        backButton.setOnAction(event -> navigateBack());

        // Export stats button
        exportStatsButton.setOnAction(event -> exportStatsToPDF());

        // Navigation buttons
        acceuil.setOnAction(event -> navigateTo("Acceuil.fxml"));
        profileButton.setOnAction(event -> navigateTo("Profile.fxml"));
        suivi.setOnAction(event -> navigateTo("Suivi.fxml"));
        historique.setOnAction(event -> navigateTo("Historique.fxml"));
        tablesButton.setOnAction(event -> navigateTo("Tables.fxml"));
        eventButton.setOnAction(event -> navigateTo("Event.fxml"));
        buttoncommande.setOnAction(event -> navigateTo("Commande.fxml"));
    }

    private void setupRefreshButton() {
        refreshButton.setOnAction(event -> {
            suiviList = suivServices.afficher();
            loadChartData();
            updateSummaryStats();
            showAlert(Alert.AlertType.INFORMATION, "Rafraîchissement",
                    "Les données ont été rafraîchies avec succès.");
        });
    }

    private void navigateBack() {
        try {
            // Use SceneManager to navigate back to the previous page
            SceneManager.loadScene("/fxml/liste_suivi_back.fxml", new ActionEvent(backButton, null));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de retourner à la page précédente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void navigateTo(String fxmlFile) {
        try {
            // Use SceneManager to navigate to the specified page
            // Create an ActionEvent using the acceuil button as the source
            SceneManager.loadScene("/org/example/views/" + fxmlFile, new ActionEvent(acceuil, null));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de naviguer vers " + fxmlFile + ": " + e.getMessage());
        }
    }
    private void exportStatsToPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer les statistiques");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(exportStatsButton.getScene().getWindow());

            if (file != null) {
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
                Paragraph title = new Paragraph("Rapport de Statistiques des Suivis Médicaux", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Add date
                Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
                Paragraph dateP = new Paragraph("Date du rapport: " + LocalDate.now().format(dateFormatter), dateFont);
                dateP.setAlignment(Element.ALIGN_RIGHT);
                dateP.setSpacingAfter(20);
                document.add(dateP);

                // Add period info
                Font periodFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
                Paragraph periodP = new Paragraph("Période: " + thisPeriodLabel.getText(), periodFont);
                periodP.setSpacingAfter(15);
                document.add(periodP);

                // Add summary statistics
                Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
                Paragraph summaryP = new Paragraph();
                summaryP.add(new Chunk("Nombre total de suivis: " + totalSuivisLabel.getText() + "\n", summaryFont));
                summaryP.add(new Chunk("Nombre total de traitements: " + totalPatientsLabel.getText(), summaryFont));
                summaryP.setSpacingAfter(20);
                document.add(summaryP);

                // Add detailed statistics table
                String selectedPeriode = periodeComboBox.getValue();
                List<suivie_medical> filteredList = filterByPeriod(suiviList, selectedPeriode);

                // Create table
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);

                // Table headers
                PdfPCell cell1 = new PdfPCell(new Phrase("Date", tableHeaderFont));
                cell1.setBackgroundColor(BaseColor.DARK_GRAY);
                cell1.setPadding(5);

                PdfPCell cell2 = new PdfPCell(new Phrase("Traitement ID", tableHeaderFont));
                cell2.setBackgroundColor(BaseColor.DARK_GRAY);
                cell2.setPadding(5);

                PdfPCell cell3 = new PdfPCell(new Phrase("ID Suivi", tableHeaderFont));
                cell3.setBackgroundColor(BaseColor.DARK_GRAY);
                cell3.setPadding(5);

                PdfPCell cell4 = new PdfPCell(new Phrase("Commentaire", tableHeaderFont));
                cell4.setBackgroundColor(BaseColor.DARK_GRAY);
                cell4.setPadding(5);

                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);
                table.addCell(cell4);

                // Table data
                for (suivie_medical suivi : filteredList) {
                    table.addCell(formatDate(suivi.getDate()));
                    table.addCell(String.valueOf(suivi.getHistoriqueId()));
                    table.addCell(String.valueOf(suivi.getId()));

                    // Limit details length for table display
                    String commentaire = suivi.getCommentaire();
                    if (commentaire != null && commentaire.length() > 50) {
                        commentaire = commentaire.substring(0, 47) + "...";
                    }
                    table.addCell(commentaire != null ? commentaire : "");
                }

                document.add(table);

                // Add footer
                Font italicFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
                Paragraph footer = new Paragraph("Ce rapport a été généré automatiquement par le système de gestion des suivis médicaux.",
                        italicFont);
                footer.setAlignment(Element.ALIGN_CENTER);
                footer.setSpacingBefore(20);
                document.add(footer);

                document.close();

                showAlert(Alert.AlertType.INFORMATION, "Export réussi",
                        "Les statistiques ont été exportées avec succès vers " + file.getName());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'exportation",
                    "Impossible d'exporter les statistiques: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
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

}