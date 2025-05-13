// eventFrontController.java

package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.Event;
import org.example.models.Patient;
import org.example.services.EventService;
import org.example.services.ReservationService;
import org.example.util.SessionManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.example.util.NotificationUtil.showAlert;

public class eventFrontController {
    @FXML
    private GridPane eventGridPane;

    @FXML
    private ScrollPane scrollPane;

    private final EventService eventService = new EventService();
    private final ReservationService reservationService = new ReservationService();
    private final String API_KEY = "294172e55663c6d252179c242db11ad0";
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    @FXML private Button historique; // Added missing Button declaration

    @FXML
    public void initialize() {

        Platform.runLater(this::loadEvents);
    }

    private void loadEvents() {
        List<Event> events = eventService.afficher();

        eventGridPane.getChildren().clear();
        eventGridPane.setAlignment(Pos.CENTER);
        eventGridPane.setHgap(20);
        eventGridPane.setVgap(20);

        int column = 0, row = 0;
        for (Event event : events) {
            VBox card = createEventCard(event);
            eventGridPane.add(card, column, row);
            if (++column == 3) {
                column = 0;
                row++;
            }
        }

        scrollPane.setFitToWidth(true);
        scrollPane.setContent(eventGridPane);
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox();
        card.setPadding(new Insets(10));
        card.setSpacing(8);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setAlignment(Pos.CENTER);

        // Charger l'image
        String imagePath = "file:src/main/resources/com/example/sante/images/" + event.getImage();
        ImageView imageView = new ImageView(new Image(imagePath));
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // ---- Badge de la date en bleu ----
        String formattedDate = formatDate(event.getDateevent()); // Convertir la date
        Label dateBadge = new Label(formattedDate);
        dateBadge.setStyle("-fx-background-color: #3fbbc0; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 8px; -fx-background-radius: 10px;");

        StackPane badgeContainer = new StackPane();
        badgeContainer.getChildren().add(dateBadge);
        StackPane.setAlignment(dateBadge, Pos.TOP_RIGHT);  // Alignement à droite

        // Labels
        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3fbbc0;");

        Label location = new Label(event.getLieu());
        location.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        Label description = new Label(event.getDiscription().length() > 80 ? event.getDiscription().substring(0, 80) + "..." : event.getDiscription());
        description.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        description.setWrapText(true);

        // Boutons avec icônes
        ImageView detailsIcon = new ImageView(new Image("file:src/main/resources/com/example/sante/images/details.png"));
        detailsIcon.setFitWidth(16);
        detailsIcon.setFitHeight(16);

        Button detailsButton = new Button("Voir Détails", detailsIcon);
        detailsButton.setStyle("-fx-background-color: #3fbbc0; -fx-text-fill: white; -fx-font-size: 12px;");
        detailsButton.setOnAction(e -> showEventDetails(event));

        ImageView reserveIcon = new ImageView(new Image("file:src/main/resources/com/example/sante/images/reservation.png"));
        reserveIcon.setFitWidth(16);
        reserveIcon.setFitHeight(16);

        // Icône de localisation
        ImageView locationIcon = new ImageView(new Image("file:src/main/resources/com/example/sante/images/location.png"));
        locationIcon.setFitWidth(16);
        locationIcon.setFitHeight(16);

        // Bouton de localisation
        Button locationButton = new Button("Localisation", locationIcon);
        locationButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-size: 12px;");
        locationButton.setOnAction(e -> showEventLocation(event));

        // Icône météo (assurez-vous d'avoir cette image dans votre répertoire de ressources)
        ImageView weatherIcon;
        try {
            weatherIcon = new ImageView(new Image("file:src/main/resources/com/example/sante/images/weather.png"));
        } catch (Exception e) {
            // Utilisez une autre image disponible si weather.png n'existe pas
            weatherIcon = new ImageView(new Image("file:src/main/resources/com/example/sante/images/details.png"));
        }
        weatherIcon.setFitWidth(16);
        weatherIcon.setFitHeight(16);

        // Bouton météo
        Button weatherButton = new Button("Météo", weatherIcon);
        weatherButton.setStyle("-fx-background-color: #4a6fa5; -fx-text-fill: white; -fx-font-size: 12px;");
        weatherButton.setOnAction(e -> showWeatherForEvent(event));

        // Vérifier si l'événement est complet
        int totalReserves = new ReservationService().getTotalReservedForEvent(event.getId());
        boolean isComplet = totalReserves >= event.getNbplace();

        Button reserveButton = new Button("Réserver", reserveIcon);
        reserveButton.setStyle("-fx-background-color: #3fbbc0; -fx-text-fill: white; -fx-font-size: 12px;");
        reserveButton.setOnAction(e -> redirectToAddReservation(event));

        if (isComplet) {
            reserveButton.setDisable(true);
            reserveButton.setText("Complet");
            reserveButton.setStyle("-fx-background-color: #ccc; -fx-text-fill: #888;");
        }

        // Regrouper les boutons
        HBox buttonBox = new HBox(10, detailsButton, reserveButton, locationButton, weatherButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Ajouter les éléments à la carte
        StackPane imageContainer = new StackPane(imageView, badgeContainer);
        StackPane.setAlignment(badgeContainer, Pos.TOP_RIGHT);  // Positionner le badge à droite

        card.getChildren().addAll(imageContainer, title, location, description, buttonBox);

        // Effet au survol
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #3fbbc0; -fx-effect: dropshadow(three-pass-box, rgba(63,187,192,0.2), 10, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));

        return card;
    }

    // Méthode pour récupérer les données météo
    private JSONObject getWeather(String city) {
        try {
            String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { // Erreur HTTP
                throw new RuntimeException("Erreur HTTP : " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return new JSONObject(response.toString());

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la météo : " + e.getMessage());
            return null; // ou gérer une réponse vide
        }
    }

    // Nouvelle méthode pour afficher la météo de l'événement
    private void showWeatherForEvent(Event event) {
        try {
            // Extraire la ville du lieu de l'événement (supposons que le lieu contient la ville)
            String location = event.getLieu();
            // Si le lieu contient plusieurs informations, extraire la ville
            // Par exemple: "Salle XYZ, Paris, France" -> nous voulons "Paris"
            String city = extractCityFromLocation(location);

            // Récupérer les données météo
            JSONObject weatherData = getWeather(city);

            if (weatherData != null) {
                // Extraire les informations principales
                JSONObject main = weatherData.getJSONObject("main");
                double temperature = main.getDouble("temp");
                int humidity = main.getInt("humidity");
                JSONObject weatherDetails = weatherData.getJSONArray("weather").getJSONObject(0);
                String weatherDescription = weatherDetails.getString("description");

                // Créer une fenêtre d'alerte pour afficher les informations météo
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Météo pour " + city);
                alert.setHeaderText("Prévisions météo pour l'événement: " + event.getTitre());

                String content = String.format(
                        "🌡️ Température: %.1f°C\n" +
                                "💧 Humidité: %d%%\n" +
                                "☁️ Conditions: %s",
                        temperature, humidity, weatherDescription);

                alert.setContentText(content);
                alert.showAndWait();
            } else {
                // En cas d'erreur
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur météo");
                alert.setHeaderText("Impossible de récupérer les données météo");
                alert.setContentText("Veuillez vérifier votre connexion internet ou essayer à nouveau plus tard.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur imprévue
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur est survenue");
            alert.setContentText("Impossible de récupérer les informations météo: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Extraire la ville du lieu de l'événement
    private String extractCityFromLocation(String location) {
        // Simplification: on suppose que le lieu est structuré comme "lieu, ville"
        // ou que la ville est déjà indiquée directement
        if (location.contains(",")) {
            String[] parts = location.split(",");
            return parts[parts.length - 1].trim(); // Prendre la dernière partie après la virgule
        } else {
            return location.trim(); // Utiliser tout le texte comme nom de ville
        }
    }

    // Méthode pour afficher la localisation de l'événement
    private void showEventLocation(Event event) {
        try {
            // Charger le FXML pour la carte
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventMap.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre pour la carte
            Stage mapStage = new Stage();
            mapStage.setTitle("Localisation: " + event.getTitre());
            mapStage.setScene(new Scene(root, 800, 600));

            // Récupérer le contrôleur
            EventMapController mapController = loader.getController();

            // Initialiser la carte avec le lieu de l'événement
            mapController.initializeMap(event);

            // Afficher la fenêtre
            mapStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---- Fonction pour formater la date en "14Feb" ----
    private String formatDate(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            String day = parts[2]; // Jour
            String month = parts[1]; // Mois (numérique)

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int monthIndex = Integer.parseInt(month) - 1; // Convertir en index

            return day + months[monthIndex]; // Exemple : "14Feb"
        } catch (Exception e) {
            return dateStr; // En cas d'erreur, afficher la date originale
        }
    }

    private void showEventDetails(Event event) {
        System.out.println("Afficher les détails de : " + event.getTitre());
        // Implement a detailed view if needed
    }

    private void redirectToAddReservation(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addReservation.fxml"));
            Parent root = loader.load();

            // Get the controller and set the event ID
            reservationController controller = loader.getController();
            controller.setEventId(event.getId());

            // Update the scene
            Scene scene = new Scene(root, 1200, 700);
            Stage stage = (Stage) eventGridPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Navigation methods from SuccessController
    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigate(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showError("Erreur de navigation", e.getMessage());
        }
    }
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        try {
            // Check login status before navigation
            if (!checkLoginForNavigation()) return;
            SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de navigation");
            alert.setContentText("Impossible de charger le calendrier: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean checkLoginForNavigation() {
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder à cette fonctionnalité.");
            return false;
        }
        return true;
    }

    @FXML
    void navigateToProfile(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }


    // NAVIGATION METHODS
    @FXML void navigateToHome(ActionEvent event) { SceneManager.loadScene("/fxml/main_view_patient.fxml", event); }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void redirectToHistorique(ActionEvent event) {
        try {
            // Enhanced logging
            System.out.println("Starting history redirect...");
            System.out.println("SessionManager.isLoggedIn(): " + SessionManager.getInstance().isLoggedIn());
            System.out.println("SessionManager.isPatient(): " + SessionManager.getInstance().isPatient());

            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showErrorDialog("Erreur", "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Check if the user is a patient
            if (!SessionManager.getInstance().isPatient()) {
                showErrorDialog("Erreur", "Seuls les patients peuvent accéder à cette fonctionnalité.");
                return;
            }

            // Get the patient from the session
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            System.out.println("Patient from SessionManager: " + (patient != null ?
                    "ID=" + patient.getId() + ", Nom=" + patient.getNom() : "NULL"));

            if (patient == null) {
                System.out.println("Error: SessionManager.getCurrentPatient() returned null!");
                // Remove reference to non-existent currentUser variable
                showErrorDialog("Erreur", "Impossible de récupérer les informations du patient.");
                return;
            }

            // Load the historique page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_historique.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the patient
            AjouterHistController controller = loader.getController();
            controller.setPatient(patient);
            System.out.println("Patient passed to controller: ID=" + patient.getId() +
                    ", Nom=" + patient.getNom() + ", Prénom=" + patient.getPrenom());

            // Show the new scene with maximum size
            Stage stage = (Stage) historique.getScene().getWindow();

            // Get screen dimensions for maximum size
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error during navigation to historique: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur", "Impossible de charger la page d'ajout d'historique: " + e.getMessage());
        }
    }
    @FXML void redirectToDemande(ActionEvent event) { SceneManager.loadScene("/fxml/DemandeDashboard.fxml", event); }
    @FXML void redirectToRendezVous(ActionEvent event) {
        SceneManager.loadScene("/fxml/rendez-vous-view.fxml", event);
    }
    @FXML
    void redirectProduit(ActionEvent event) {
        SceneManager.loadScene("/fxml/front/showProduit.fxml", event);
    }
    @FXML
    public void viewDoctors(ActionEvent event) {
        try {
            if (!sessionManager.isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Use SceneManager to load the DoctorList.fxml in full screen
            SceneManager.loadScene("/fxml/DoctorList.fxml", event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des médecins: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
