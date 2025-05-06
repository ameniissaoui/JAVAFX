package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.controllers.AjouterHistController;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.NotificationManager;
import org.example.services.ReminderService;
import org.example.util.Carousel;
import org.example.util.ServiceCard;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    @FXML private ImageView logoImageView;
    @FXML private Carousel heroCarousel;
    @FXML private FlowPane featuredServicesContainer;
    @FXML private Label cartCountLabel;
    @FXML private VBox diseaseContent;
    @FXML private ReminderService reminderService;
    @FXML private StackPane notificationCountContainer;
    @FXML private Label notificationCountLabel;
    @FXML private Label welcomeUserLabel; // Add this to show user's name
    @FXML private Button loginButton; // Button to show/hide based on session
    @FXML private Button profileButton; // Button to navigate to profile
    @FXML private HBox userControls; // Container for user-specific buttons
    @FXML private HBox adminControls; // Container for admin-specific buttons
    @FXML private HBox doctorControls; // Container for doctor-specific buttons
    @FXML private Button historique; // Added missing Button declaration

    private Button selectedTabButton;
    private SessionManager sessionManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize SessionManager
        sessionManager = SessionManager.getInstance();

        // Load logo
        try {
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
            logoImageView.setFitHeight(78.0);
            logoImageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Could not load logo image: " + e.getMessage());
            logoImageView.setImage(new Image("file:src/main/resources/images/placeholder.png"));
        }

        // Debug print to check if heroCarousel is null
        System.out.println("heroCarousel: " + (heroCarousel != null ? "initialized" : "null"));

        setupHeroCarousel();
        setupFeaturedServices();
        Platform.runLater(() -> {
            selectDiabetesTab();
            updateUIBasedOnSession();
        });
        reminderService = new ReminderService();
        setupNotificationBadge();
    }

    /**
     * Updates UI elements based on current user session
     */
    private void updateUIBasedOnSession() {
        if (sessionManager.isLoggedIn()) {
            // User is logged
            if (welcomeUserLabel != null) {
                String userName = sessionManager.getCurrentUser().getNom(); // Assuming User has getNom method
                welcomeUserLabel.setText("Bienvenue, " + userName);
                welcomeUserLabel.setVisible(true);
            }

            // Show/hide login button
            if (loginButton != null) {
                loginButton.setText("Déconnexion");
                loginButton.setOnAction(this::handleLogout);
            }

            // Show appropriate controls based on user type
            if (userControls != null) userControls.setVisible(sessionManager.isPatient());
            if (adminControls != null) adminControls.setVisible(sessionManager.isAdmin());
            if (doctorControls != null) doctorControls.setVisible(sessionManager.isMedecin());

            // For patients, we might want to load specific information
            if (sessionManager.isPatient()) {
                Patient patient = sessionManager.getCurrentPatient();
                loadPatientSpecificContent(patient);
            } else if (sessionManager.isMedecin()) {
                Medecin medecin = sessionManager.getCurrentMedecin();
                loadDoctorSpecificContent(medecin);
            }

            // Enable profile button
            if (profileButton != null) {
                profileButton.setVisible(true);
            }
        } else {
            // User is not logged in
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setVisible(false);
            }

            // Show login button
            if (loginButton != null) {
                loginButton.setText("Connexion");
                loginButton.setOnAction(this::handleLogin);
            }

            // Hide user-specific controls
            if (userControls != null) userControls.setVisible(false);
            if (adminControls != null) adminControls.setVisible(false);
            if (doctorControls != null) doctorControls.setVisible(false);

            // Disable profile button
            if (profileButton != null) {
                profileButton.setVisible(false);
            }
        }
    }

    /**
     * Load patient-specific content when a patient is logged in
     */
    private void loadPatientSpecificContent(Patient patient) {
        // Example: Load patient-specific reminders, appointments, etc.
        if (patient != null) {
            // This would depend on your Patient class implementation
            System.out.println("Loading content for patient: " + patient.getNom());
            // You could load upcoming appointments, reminders, etc.
        }
    }

    /**
     * Load doctor-specific content when a doctor is logged in
     */
    private void loadDoctorSpecificContent(Medecin medecin) {
        // Example: Load doctor's schedule, patient list, etc.
        if (medecin != null) {
            System.out.println("Loading content for doctor: " + medecin.getNom());
            // You could load today's appointments, patient alerts, etc.
        }
    }

    /**
     * Handle logout action
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        sessionManager.clearSession();
        showAlert(Alert.AlertType.INFORMATION, "Déconnexion", "Déconnexion réussie",
                "Vous avez été déconnecté avec succès.");
        updateUIBasedOnSession();

        // Optional: Redirect to login page
        // SceneManager.loadScene("/fxml/login.fxml", event);
    }

    /**
     * Handle login button action
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        // Navigate to login page
        SceneManager.loadScene("/fxml/login.fxml", event);
    }

    private void setupNotificationBadge() {
        if (notificationCountLabel != null && notificationCountContainer != null) {
            // Bind notification count to the label
            NotificationManager notificationManager = NotificationManager.getInstance();
            notificationCountLabel.textProperty().bind(notificationManager.unreadCountProperty().asString());

            // Show/hide notification badge based on count
            notificationCountContainer.visibleProperty().bind(
                    notificationManager.unreadCountProperty().greaterThan(0));

            // Refresh the notification count
            notificationManager.refreshUnreadCount();
        }
    }

    private void setupHeroCarousel() {
        try {
            System.out.println("Setting up hero carousel");

            // Check if heroCarousel is null
            if (heroCarousel == null) {
                System.err.println("heroCarousel is null, cannot set up slides");
                return;
            }

            // Try to load images and add slides
            Image image1 = new Image(getClass().getResourceAsStream("/images/hero-carousel/hero-carousel-1.jpg"));
            Image image2 = new Image(getClass().getResourceAsStream("/images/hero-carousel/hero-carousel-2.jpg"));
            Image image3 = new Image(getClass().getResourceAsStream("/images/hero-carousel/hero-carousel-3.jpg"));

            System.out.println("Image 1 loaded: " + !image1.isError());
            System.out.println("Image 2 loaded: " + !image2.isError());
            System.out.println("Image 3 loaded: " + !image3.isError());

            // First Slide - matches the example in the image
            heroCarousel.addSlide(
                    "Bienvenue sur ChronoSerna",
                    "ChronoSerna est votre partenaire pour mieux vivre avec une maladie chronique. Accédez à des outils de suivi personnalisés, rejoignez une communauté bienveillante et bénéficiez de ressources éducatives pour prendre le contrôle de votre santé.",
                    image1,
                    "En savoir plus"
            );

            // Additional slides
            heroCarousel.addSlide(
                    "Suivi personnalisé",
                    "Notre plateforme vous permet de suivre vos symptômes, vos traitements et vos progrès. Visualisez vos données de santé et partagez-les avec votre équipe médicale pour une meilleure prise en charge.",
                    image2,
                    "En savoir plus"
            );
            heroCarousel.addSlide(
                    "Communauté et soutien",
                    "Rejoignez une communauté de patients et de professionnels de santé pour échanger des conseils, partager des expériences et trouver du soutien. Vous n'êtes plus seul face à votre maladie.",
                    image3,
                    "En savoir plus"
            );

            System.out.println("Slides added successfully");
        } catch (Exception e) {
            System.err.println("Error setting up carousel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupFeaturedServices() {
        // Pass the complete icon name including the prefix
        addServiceCard("Gestion des Utilisateurs", "Gérez les profils des patients et des professionnels de santé pour un suivi personnalisé.", "bi-person");
        addServiceCard("Gestion des Rendez-vous", "Planifiez et suivez les rendez-vous médicaux pour une meilleure organisation.", "bi-calendar");
        addServiceCard("Gestion des Traitements", "Suivez et ajustez les traitements médicaux pour une prise en charge optimale.", "fas-pills");
        addServiceCard("Gestion du Mode de Vie", "Adoptez des habitudes saines pour mieux vivre avec une maladie chronique.", "bi-heart");
        addServiceCard("Gestion des Événements", "Organisez et participez à des événements éducatifs sur les maladies chroniques.", "bi-calendar-event");
        addServiceCard("Gestion des Produits", "Découvrez et gérez les produits de santé adaptés à vos besoins.", "fas-prescription-bottle");
    }

    private void addServiceCard(String title, String description, String fullIconName) {
        ServiceCard serviceCard = new ServiceCard(title, description, fullIconName);
        featuredServicesContainer.getChildren().add(serviceCard);
    }

    private Parent createDiseaseContent(String title, String subtitle, String description, String imagePath) {
        // Use HBox as the main container to place text and image side by side
        HBox content = new HBox(20);
        content.getStyleClass().add("disease-content");
        content.setPadding(new javafx.geometry.Insets(20, 30, 20, 30));
        content.setAlignment(Pos.CENTER);

        // Text content in a VBox
        VBox textContent = new VBox(15);
        textContent.setMaxWidth(500);
        textContent.setPrefWidth(500);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("disease-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("disease-subtitle");
        subtitleLabel.setWrapText(true);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("disease-description");
        descLabel.setWrapText(true);

        textContent.getChildren().addAll(titleLabel, subtitleLabel, descLabel);

        // Image on the right
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Could not load image: " + e.getMessage());
            imageView.setImage(new Image("file:src/main/resources/images/placeholder.jpg"));
        }

        // Add both components to the HBox
        content.getChildren().addAll(textContent, imageView);

        return content;
    }

    @FXML
    private void selectDiabetesTab() {
        updateTabContent("Diabète",
                "Le diabète est une maladie chronique qui affecte la façon dont votre corps traite le glucose.",
                "Il existe deux types principaux de diabète : le type 1 et le type 2. Une gestion appropriée comprend une alimentation saine, de l'exercice régulier et des médicaments si nécessaire. Apprenez-en plus sur les stratégies de prévention et de traitement.",
                "/images/departments-1.jpg",
                (Button) diseaseContent.lookup("#diabèteTabButton"));
    }

    @FXML
    private void selectHypertensionTab() {
        updateTabContent("Hypertension",
                "L'hypertension artérielle est un facteur de risque majeur pour les maladies cardiaques.",
                "Une pression artérielle élevée peut être contrôlée grâce à des changements de mode de vie comme une alimentation équilibrée, une activité physique régulière et la réduction du stress. Des médicaments peuvent également être prescrits pour aider à maintenir une pression artérielle normale.",
                "/images/departments-2.jpg",
                (Button) diseaseContent.lookup("#hypertensionTabButton"));
    }

    @FXML
    private void selectAsthmaTab() {
        updateTabContent("Asthme",
                "L'asthme est une maladie chronique des voies respiratoires qui provoque une inflammation et un rétrécissement des bronches.",
                "Les symptômes incluent la toux, la respiration sifflante et l'essoufflement. Ils peuvent être gérés avec des médicaments et en évitant les déclencheurs comme les allergènes, la pollution et le tabac. Un plan d'action personnalisé est essentiel pour gérer efficacement l'asthme.",
                "/images/departments-3.jpg",
                (Button) diseaseContent.lookup("#asthmeTabButton"));
    }

    @FXML
    private void selectHeartDiseaseTab() {
        updateTabContent("Maladies Cardiaques",
                "Les maladies cardiaques sont l'une des principales causes de décès dans le monde.",
                "Elles incluent des conditions comme les maladies coronariennes, l'insuffisance cardiaque et les arythmies. La prévention passe par une alimentation saine, l'exercice régulier, l'arrêt du tabac et la gestion du stress. Un suivi médical régulier est essentiel pour les personnes à risque.",
                "/images/departments-4.jpg",
                (Button) diseaseContent.lookup("#maladiescardiaquesTabButton"));
    }

    private void updateTabContent(String title, String subtitle, String description, String imagePath, Button clickedButton) {
        diseaseContent.getChildren().clear();
        diseaseContent.getChildren().add(createDiseaseContent(title, subtitle, description, imagePath));

        // Update selected tab button style
        if (selectedTabButton != null) {
            selectedTabButton.getStyleClass().remove("tab-button-selected");
        }
        if (clickedButton != null) {
            clickedButton.getStyleClass().add("tab-button-selected");
            selectedTabButton = clickedButton;
        }
    }

    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        // Check if user is logged in before navigating to profile
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder à votre profil.");
            return;
        }

        // Route to appropriate profile page based on user type
        if (sessionManager.isPatient()) {
            SceneManager.loadScene("/fxml/patient_profile.fxml", event);
        } else if (sessionManager.isMedecin()) {
            SceneManager.loadScene("/fxml/doctor_profile.fxml", event);
        } else if (sessionManager.isAdmin()) {
            SceneManager.loadScene("/fxml/admin_profile.fxml", event);
        }
    }

    @FXML
    private void handleCartButtonClick() {
        // Check if user is logged in before accessing cart
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder au panier.");
            return;
        }
        System.out.println("Open cart");
    }

    @FXML
    private void handleAppointmentButtonClick() {
        // Check if user is logged in before accessing appointments
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder aux rendez-vous.");
            return;
        }
        System.out.println("Navigate to appointments");
    }

    ////////////////////////////////////////NAVIGATION////////////////////////////////////////////////////////////////////////////
    @FXML
    private void navigateToDemande(ActionEvent event) {
        // Check login status before navigation
        if (!checkLoginForNavigation()) return;
        SceneManager.loadScene("/fxml/DemandeDashboard.fxml", event);
    }

    @FXML
    private void navigateToProduit(ActionEvent event) {
        // Products might be viewable without login
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

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

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
    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        SceneManager.loadScene("/fxml/rendez-vous-view.fxml", event);
    }
    @FXML public void redirectToEvents(ActionEvent event) {
        SceneManager.loadScene("/fxml/eventFront.fxml", event);
    }
    @FXML
    public void redirectToCalendar(ActionEvent event) {
        try {
            // Check login status before navigation
            if (!checkLoginForNavigation()) return;
            SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
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
    public void refreshView() {
        updateUIBasedOnSession();
    }
}