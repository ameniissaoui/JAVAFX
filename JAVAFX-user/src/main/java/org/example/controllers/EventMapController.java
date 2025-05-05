package org.example.controllers;

import com.sothawo.mapjfx.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Event;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EventMapController {

    @FXML private BorderPane mapContainer;
    @FXML private Label titleLabel;
    @FXML private Label addressLabel;
    @FXML private Button closeButton;
    @FXML private Button showMapButton;

    private MapView mapView;
    private Coordinate currentCoord;
    private Marker currentMarker;
    private String originalAddress;
    private Timeline pulseAnimation;

    @FXML
    public void initialize() {
        mapView = new MapView();
        mapView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        mapView.initialize(Configuration.builder()
                .projection(Projection.WEB_MERCATOR)
                .showZoomControls(true)
                .build());

        // Lorsque la carte est prête, afficher le marker si coordonnées disponibles
        mapView.initializedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (currentCoord == null) {
                    currentCoord = new Coordinate(36.8065, 10.1815); // Tunis fallback
                }
                updateMapAndMarker();
            }
        });

        mapContainer.setCenter(mapView);
    }

    public void initializeMap(Event event) {
        this.titleLabel.setText(event.getTitre());
        this.addressLabel.setText("Chargement de l'adresse...");
        this.originalAddress = event.getLieu();
        getCoordinatesFromAddress(event.getLieu());
    }

    private void getCoordinatesFromAddress(String address) {
        new Thread(() -> {
            try {
                // Première tentative: recherche exacte de l'adresse
                tryGeocodingWithQuery(address);
            } catch (Exception e) {
                e.printStackTrace();
                fallbackToTunis("Erreur de géolocalisation: " + e.getMessage());
            }
        }).start();
    }

    private void tryGeocodingWithQuery(String query) {
        try {
            // D'abord essayer sans ajouter ", Tunisie"
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlStr = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";

            JSONArray result = performGeocodingRequest(urlStr);

            if (result.length() == 0) {
                // Si rien n'est trouvé, essayer avec ", Tunisie"
                encoded = URLEncoder.encode(query + ", Tunisie", StandardCharsets.UTF_8);
                urlStr = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";
                result = performGeocodingRequest(urlStr);

                if (result.length() == 0) {
                    // Si toujours rien, essayer de rechercher juste la ville ou région principale
                    String[] parts = query.split(",");
                    if (parts.length > 0) {
                        encoded = URLEncoder.encode(parts[0].trim() + ", Tunisie", StandardCharsets.UTF_8);
                        urlStr = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";
                        result = performGeocodingRequest(urlStr);
                    }

                    if (result.length() == 0) {
                        fallbackToTunis("Adresse introuvable: " + originalAddress);
                        return;
                    }
                }
            }

            // Traiter le résultat
            JSONObject obj = result.getJSONObject(0);
            double lat = obj.getDouble("lat");
            double lon = obj.getDouble("lon");
            Coordinate coord = new Coordinate(lat, lon);

            Platform.runLater(() -> {
                currentCoord = coord;
                addressLabel.setText(obj.optString("display_name", originalAddress));
                updateMapAndMarker();
            });

        } catch (Exception e) {
            e.printStackTrace();
            fallbackToTunis("Erreur de géolocalisation: " + e.getMessage());
        }
    }

    private JSONArray performGeocodingRequest(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "JavaFXApp/1.0");
        conn.setConnectTimeout(5000); // 5 secondes timeout

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        return new JSONArray(response.toString());
    }

    private void updateMapAndMarker() {
        if (currentCoord == null || !mapView.getInitialized()) return;

        mapView.setCenter(currentCoord);
        mapView.setZoom(14);

        // Supprimer l'ancien marqueur s'il existe
        if (currentMarker != null) {
            mapView.removeMarker(currentMarker);
            if (pulseAnimation != null) {
                pulseAnimation.stop();
            }
        }

        // Créer le nouveau marqueur
        currentMarker = Marker.createProvided(Marker.Provided.RED)
                .setPosition(currentCoord)
                .setVisible(true);

        mapView.addMarker(currentMarker);

        // Démarrer l'animation de pulsation
        startSimplePulseAnimation();

        System.out.println("📍 Marker animé ajouté à : " + currentCoord.getLatitude() + ", " + currentCoord.getLongitude());
    }

    private void startSimplePulseAnimation() {
        // Créer une animation simple qui fait clignoter le marqueur
        pulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    currentMarker.setVisible(true);
                }),
                new KeyFrame(Duration.seconds(0.6), event -> {
                    currentMarker.setVisible(false);
                }),
                new KeyFrame(Duration.seconds(0.8), event -> {
                    currentMarker.setVisible(true);
                })
        );

        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.play();
    }

    private void fallbackToTunis(String message) {
        Platform.runLater(() -> {
            currentCoord = new Coordinate(36.8065, 10.1815);
            addressLabel.setText(message + " Affichage par défaut : Tunis.");
            updateMapAndMarker();
        });
    }

    @FXML
    void onShowMapClicked() {
        if (currentCoord != null) {
            mapView.setCenter(currentCoord);
            mapView.setZoom(14);
        }
    }

    @FXML
    void closeMap() {
        // Arrêter l'animation au moment de fermer la fenêtre
        if (pulseAnimation != null) {
            pulseAnimation.stop();
        }

        Stage stage = (Stage) closeButton.getScene().getWindow();
        if (stage != null) stage.close();
    }
}