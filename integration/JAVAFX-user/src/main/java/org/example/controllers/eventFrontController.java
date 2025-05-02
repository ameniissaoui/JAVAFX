package org.example.controllers;

import javafx.stage.Screen;
import org.example.models.Event;
import org.example.services.EventService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.services.ReservationService;
import com.sothawo.mapjfx.Configuration;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;
import com.sothawo.mapjfx.Projection;

import java.io.IOException;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
// Weather API imports
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class eventFrontController {
    @FXML
    private GridPane eventGridPane;
    @FXML
    private ScrollPane scrollPane;
    private final EventService eventService = new EventService();
    private final String API_KEY = "294172e55663c6d252179c242db11ad0";
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    @FXML
    void eventfront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void reservationfront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservationFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        List<Event> events = eventService.afficher(); // Récupérer les événements

        eventGridPane.getChildren().clear();
        eventGridPane.setAlignment(Pos.CENTER);
        eventGridPane.setHgap(20); // Espacement horizontal
        eventGridPane.setVgap(20); // Espacement vertical

        int column = 0;
        int row = 0;

        for (Event event : events) {
            VBox card = createEventCard(event);
            eventGridPane.add(card, column, row);
            column++;

            if (column == 3) { // 3 cartes par ligne
                column = 0;
                row++;
            }
        }

        // Assurer un scroll fluide et adapter à la largeur
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
                Alert alert = new Alert(AlertType.INFORMATION);
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
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur météo");
                alert.setHeaderText("Impossible de récupérer les données météo");
                alert.setContentText("Veuillez vérifier votre connexion internet ou essayer à nouveau plus tard.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur imprévue
            Alert alert = new Alert(AlertType.ERROR);
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

    @FXML
    void navigateToHome() {
        navigateTo("/fxml/front/home.fxml");
    }

    @FXML
    void navigateToHistoriques() {
        navigateTo("/fxml/historiques.fxml");
    }

    @FXML
    void redirectToDemande() {
        navigateTo("/fxml/DemandeDashboard.fxml");
    }

    @FXML
    void redirectToRendezVous() {
        navigateTo("/fxml/rendez-vous-view.fxml");
    }

    @FXML
    void redirectProduit() {
        navigateTo("/fxml/front/showProduit.fxml");
    }

    @FXML
    void navigateToTraitement() {
        navigateTo("/fxml/traitement.fxml");
    }

    @FXML
    void viewDoctors() {
        navigateTo("/fxml/DoctorList.fxml");
    }

    @FXML
    void navigateToContact() {
        navigateTo("/fxml/front/contact.fxml");
    }

    @FXML
    void navigateToProfile() {
        navigateTo("/fxml/front/profile.fxml");
    }

    @FXML
    void navigateToFavorites() {
        navigateTo("/fxml/front/favoris.fxml");
    }

    @FXML
    void commande() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToCommandes() {
        navigateTo("/fxml/front/ShowCommande.fxml");
    }

    @FXML
    void navigateToShop() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToEvent() {
        navigateTo("/fxml/eventFront.fxml");
    }
    @FXML
    void navigateToReservation() {
        navigateTo("/fxml/reservationFront.fxml");
    }
    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            System.out.println("Attempting to navigate to " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException(fxmlPath + " resource not found");
            }
            Parent root = loader.load();

            // Get the current stage
            Stage stage = null;
            if (eventGridPane != null && eventGridPane.getScene() != null) {
                stage = (Stage) eventGridPane.getScene().getWindow();
            } else if (eventGridPane != null && eventGridPane.getScene() != null) {
                stage = (Stage) eventGridPane.getScene().getWindow();
            }

            if (stage == null) {
                throw new RuntimeException("Cannot get current stage");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
            System.out.println("Successfully navigated to " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("Unexpected error during navigation: " + e.getMessage());
            e.printStackTrace();

        }
    }
}