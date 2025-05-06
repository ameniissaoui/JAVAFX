package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.util.Carousel;
import org.example.util.ServiceCard;
import org.example.util.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewMedecinController implements Initializable {

    @FXML private ImageView logoImageView;
    @FXML private Carousel heroCarousel;
    @FXML private FlowPane featuredServicesContainer;
    @FXML private Label cartCountLabel;
    @FXML private VBox diseaseContent;

    private Button selectedTabButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        Platform.runLater(this::selectDiabetesTab);
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
    private void redirectToEvents(ActionEvent event) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Accès refusé");
            alert.setHeaderText("Vous n'êtes pas connecté");
            alert.setContentText("Veuillez vous connecter pour accéder aux événements.");
            alert.showAndWait();
            return;
        }

        try {
            SceneManager.loadScene("/fxml/eventFront.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible d'accéder aux événements");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
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
        SceneManager.loadScene("/fxml/medecin_profile.fxml", event);
    }
    @FXML
    private void handleAppointmentButtonClick() {
        System.out.println("Navigate to appointments");
    }

    ////////////////////////////////////////NAVIGATION////////////////////////////////////////////////////////////////////////////
    @FXML
    private void navigateToRecom(ActionEvent event) {
        SceneManager.loadScene("/fxml/MedecinRecommendations.fxml", event);
    }
    @FXML
    private void navigateToProduit(ActionEvent event) {
        SceneManager.loadScene("/fxml/front/showProduit.fxml", event);
    }

    @FXML
    public void redirectToSuivi(ActionEvent event) {
        SceneManager.loadScene("/fxml/liste_historique.fxml", event);

    }

    @FXML
    public void redirectToPlanning(ActionEvent event) {
        SceneManager.loadScene("/fxml/planning-view.fxml", event);

    }
}