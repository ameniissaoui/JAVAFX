package org.example.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.services.EventService;

import java.io.IOException;
import java.util.Map;

public class StatistiqueController {

    @FXML
    private BarChart<String, Number> barChart;
    @FXML private Button eventButton;
    @FXML private Button rereservationButton;
    @FXML private Button statistiqueButton;

    private final EventService eventService = new EventService();
    @FXML
    private void navigateToReportDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminReportDashboard.fxml", event);
    }
    @FXML
    public void initialize() {
        Map<String, Long> stats = eventService.getNombreEvenementsParLieu();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'événements");

        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
        eventButton.setOnAction(event -> {
            try {
                handleeventRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        rereservationButton.setOnAction(event -> {
            try {
                handlereservationRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        statistiqueButton.setOnAction(event -> {
            try {
                handlestatRedirect(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void gotoevent(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sante/listEvent.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void gotofront(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sante/eventFront.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void gotoreservation(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sante/listReservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    void statistique(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sante/statistique.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    private void handleStatisticsRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/statistics-view.fxml", event);
    }

    @FXML
    private void handleStatisticsCommandeRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/back/statistics.fxml", event);
    }

    private void handleeventRedirect(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listevent.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    }
    private void handlestatRedirect(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistique.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    }
    private void handlereservationRedirect(ActionEvent event) throws IOException  {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/listreservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

}
