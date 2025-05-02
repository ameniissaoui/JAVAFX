package org.example;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

// Importations pour Ikonli
import org.example.controllers.SceneManager;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

public class ChronoSernaApp extends Application {

    private int currentCarouselIndex = 0;
    private List<CarouselItem> carouselItems;
    private Label carouselTitle;
    private Label carouselDescription;
    private Button carouselButton;
    private BorderPane carouselContent;
    private HBox carouselIndicators;
    public void loadNewScene(String fxmlPath, ActionEvent event) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            if (stage.getScene() != null && stage.getScene().getStylesheets() != null) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ChronoSerna");
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Set maximized before showing the stage
        primaryStage.setMaximized(true);

        primaryStage.setScene(scene);
        primaryStage.show();
        startCarousel();
    }
    private VBox createHeader() {
        VBox headerContainer = new VBox();
        headerContainer.getStyleClass().add("header-container");
        // Topbar
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER);
        topBar.setBackground(new Background(new BackgroundFill(Color.web("#4ECDC4"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Créer une icône d'horloge avec Ikonli
        FontIcon clockIcon = new FontIcon(BootstrapIcons.CLOCK);
        clockIcon.setIconColor(Color.WHITE);
        clockIcon.setIconSize(16);

        Label hoursLabel = new Label("Lundi - Samedi, 8h à 22h");
        hoursLabel.setTextFill(Color.WHITE);
        hoursLabel.setGraphic(clockIcon);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Créer une icône de téléphone avec Ikonli
        FontIcon phoneIcon = new FontIcon(BootstrapIcons.TELEPHONE);
        phoneIcon.setIconColor(Color.WHITE);
        phoneIcon.setIconSize(16);

        Label phoneLabel = new Label("Appelez-nous : +1 5589 55488 55");
        phoneLabel.setTextFill(Color.WHITE);
        phoneLabel.setGraphic(phoneIcon);

        topBar.getChildren().addAll(hoursLabel, spacer, phoneLabel);

        // Branding
        HBox branding = new HBox();
        branding.getStyleClass().add("branding");
        branding.setPadding(new Insets(15));
        branding.setSpacing(20);
        branding.setAlignment(Pos.CENTER);

        // Logo - vous pouvez remplacer ceci par votre propre image de logo ou créer un texte stylisé
        Label logoText = new Label(" ");
        logoText.setFont(Font.font("Poppins", FontWeight.BOLD, 15));
        logoText.setTextFill(Color.web("#4ECDC4"));

        ImageView logoImage = new ImageView(getClass().getResource("/images/logo.png").toExternalForm());
        logoImage.setFitHeight(90); // Adjust size as needed
        logoImage.setPreserveRatio(true);

        HBox logo = new HBox(5);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.getChildren().addAll(logoImage, logoText); //
        Region brandingSpacer = new Region();
        HBox.setHgrow(brandingSpacer, Priority.ALWAYS);

        Button loginButton = new Button("Connexion");
        loginButton.getStyleClass().add("cta-button");
        // Icône de connexion
        FontIcon loginIcon = new FontIcon(BootstrapIcons.PERSON);
        loginIcon.setIconColor(Color.WHITE);
        loginButton.setGraphic(loginIcon);
        loginButton.setOnAction(e -> {
            SceneManager.loadScene("/fxml/Login.fxml", e);
        });
        Button registerButton = new Button("Inscription");
        registerButton.getStyleClass().add("cta-button");
        // Icône d'inscription
        FontIcon registerIcon = new FontIcon(BootstrapIcons.PERSON_PLUS);
        registerIcon.setIconColor(Color.WHITE);
        registerButton.setGraphic(registerIcon);
        registerButton.setOnAction(e -> {
            SceneManager.loadScene("/fxml/RoleSelection.fxml", e);
        });
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("Français", "English", "العربية");
        languageSelector.setValue("Français");

        branding.getChildren().addAll(logo, brandingSpacer, loginButton, registerButton, languageSelector);

        headerContainer.getChildren().addAll(topBar, branding);
        return headerContainer;
    }

    private BorderPane createMainContent() {
        BorderPane mainContent = new BorderPane();

        // Hero Section avec carrousel
        VBox heroSection = new VBox();
        heroSection.getStyleClass().add("hero-section");

        // Contenu du carrousel
        carouselContent = new BorderPane();
        carouselContent.setPrefHeight(400);
        carouselContent.getStyleClass().add("carousel-item");

        VBox carouselTextContent = new VBox();
        carouselTextContent.setAlignment(Pos.CENTER);
        carouselTextContent.setSpacing(15);
        carouselTextContent.setPadding(new Insets(30));

        carouselTitle = new Label();
        carouselTitle.setFont(Font.font("Poppins", FontWeight.BOLD, 28));
        carouselTitle.setTextFill(Color.WHITE);

        carouselDescription = new Label();
        carouselDescription.setFont(Font.font("Roboto", 16));
        carouselDescription.setWrapText(true);
        carouselDescription.setMaxWidth(800);
        carouselDescription.setTextFill(Color.WHITE);

        carouselButton = new Button("En savoir plus");
        carouselButton.getStyleClass().add("carousel-button");
        // Icône d'information
        FontIcon infoIcon = new FontIcon(BootstrapIcons.INFO_CIRCLE);
        infoIcon.setIconColor(Color.WHITE);
        carouselButton.setGraphic(infoIcon);

        carouselTextContent.getChildren().addAll(carouselTitle, carouselDescription, carouselButton);
        carouselContent.setCenter(carouselTextContent);

        // Indicateurs du carrousel
        carouselIndicators = new HBox();
        carouselIndicators.setAlignment(Pos.CENTER);
        carouselIndicators.setSpacing(10);
        carouselIndicators.setPadding(new Insets(15));

        // Navigation du carrousel
        HBox carouselNavigation = new HBox();
        carouselNavigation.setAlignment(Pos.CENTER);
        carouselNavigation.setSpacing(700);

        Button prevButton = new Button();
        FontIcon prevIcon = new FontIcon(BootstrapIcons.CHEVRON_LEFT);
        prevIcon.setIconColor(Color.WHITE);
        prevIcon.setIconSize(24);
        prevButton.setGraphic(prevIcon);
        prevButton.getStyleClass().add("carousel-nav-button");
        prevButton.setOnAction(e -> navigateCarousel(-1));

        Button nextButton = new Button();
        FontIcon nextIcon = new FontIcon(BootstrapIcons.CHEVRON_RIGHT);
        nextIcon.setIconColor(Color.WHITE);
        nextIcon.setIconSize(24);
        nextButton.setGraphic(nextIcon);
        nextButton.getStyleClass().add("carousel-nav-button");
        nextButton.setOnAction(e -> navigateCarousel(1));

        carouselNavigation.getChildren().addAll(prevButton, nextButton);

        // Définir le contenu du carrousel
        carouselItems = Arrays.asList(
                new CarouselItem(
                        "/images/carousel-1.jpg",
                        "Bienvenue sur ChronoSerna",
                        "ChronoSerna est votre partenaire pour mieux vivre avec une maladie chronique. Accédez à des outils de suivi personnalisés, rejoignez une communauté bienveillante et bénéficiez de ressources éducatives pour prendre le contrôle de votre santé.",
                        "En savoir plus"
                ),
                new CarouselItem(
                        "/images/carousel-2.jpg",
                        "Suivi personnalisé",
                        "Notre plateforme vous permet de suivre vos symptômes, vos traitements et vos progrès grâce à des outils adaptés à vos besoins. Visualisez vos données et partagez-les avec votre médecin pour un suivi optimal.",
                        "En savoir plus"
                ),
                new CarouselItem(
                        "/images/carousel-3.jpg",
                        "Communauté et soutien",
                        "Rejoignez une communauté de patients et de professionnels de santé pour échanger des conseils, partager des expériences et trouver du soutien. Vous n'êtes plus seul face à votre maladie.",
                        "En savoir plus"
                )
        );

        // Créer les indicateurs
        for (int i = 0; i < carouselItems.size(); i++) {
            Button indicator = new Button();
            indicator.getStyleClass().add("carousel-indicator");
            int index = i;
            indicator.setOnAction(e -> updateCarousel(index));
            carouselIndicators.getChildren().add(indicator);
        }

        VBox carouselContainer = new VBox();
        carouselContainer.getChildren().addAll(carouselContent, carouselNavigation, carouselIndicators);

        heroSection.getChildren().add(carouselContainer);
        mainContent.setCenter(heroSection);

        return mainContent;
    }

    private VBox createFooter() {
        VBox footer = new VBox();
        footer.getStyleClass().add("footer");
        footer.setPadding(new Insets(30, 20, 20, 20));
        footer.setBackground(new Background(new BackgroundFill(Color.rgb(245, 245, 245), CornerRadii.EMPTY, Insets.EMPTY)));

        // Contenu du pied de page
        HBox footerContent = new HBox();
        footerContent.setSpacing(30);

        // Section "À propos"
        VBox aboutSection = new VBox();
        aboutSection.setSpacing(10);

        Label siteName = new Label("ChronoSerna");
        siteName.setFont(Font.font("Poppins", FontWeight.BOLD, 18));

        VBox contactInfo = new VBox();
        contactInfo.setSpacing(5);

        // Étiquette d'adresse avec icône
        HBox addressBox = new HBox(5);
        FontIcon locationIcon = new FontIcon(BootstrapIcons.GEO_ALT);
        locationIcon.setIconColor(Color.web("#555"));
        addressBox.getChildren().addAll(locationIcon, new Label("A108 Adam Street"));

        // Étiquette de ville avec icône
        HBox cityBox = new HBox(5);
        FontIcon cityIcon = new FontIcon(BootstrapIcons.BUILDING);
        cityIcon.setIconColor(Color.web("#555"));
        cityBox.getChildren().addAll(cityIcon, new Label("New York, NY 535022"));

        // Étiquette de téléphone avec icône
        HBox phoneBox = new HBox(5);
        FontIcon footerPhoneIcon = new FontIcon(BootstrapIcons.TELEPHONE);
        footerPhoneIcon.setIconColor(Color.web("#555"));
        phoneBox.getChildren().addAll(footerPhoneIcon, new Label("Téléphone : +1 5589 55488 55"));

        // Étiquette d'email avec icône
        HBox emailBox = new HBox(5);
        FontIcon emailIcon = new FontIcon(BootstrapIcons.ENVELOPE);
        emailIcon.setIconColor(Color.web("#555"));
        emailBox.getChildren().addAll(emailIcon, new Label("Email : info@example.com"));

        contactInfo.getChildren().addAll(addressBox, cityBox, phoneBox, emailBox);

        HBox socialLinks = new HBox();
        socialLinks.setSpacing(10);
        socialLinks.setPadding(new Insets(10, 0, 0, 0));

        // Icônes de réseaux sociaux avec Ikonli
        BootstrapIcons[] socialIcons = {
                BootstrapIcons.TWITTER,
                BootstrapIcons.FACEBOOK,
                BootstrapIcons.INSTAGRAM,
                BootstrapIcons.LINKEDIN
        };

        for (BootstrapIcons icon : socialIcons) {
            Button socialButton = new Button();
            FontIcon socialIcon = new FontIcon(icon);
            socialIcon.setIconColor(Color.WHITE);
            socialIcon.setIconSize(16);
            socialButton.setGraphic(socialIcon);
            socialButton.getStyleClass().add("social-button");
            socialLinks.getChildren().add(socialButton);
        }

        aboutSection.getChildren().addAll(siteName, contactInfo, socialLinks);

        // Sections des liens
        VBox usefulLinks = createLinkSection("Liens utiles",
                new String[]{"Accueil", "À propos", "Services", "Conditions d'utilisation", "Politique de confidentialité"});

        VBox servicesLinks = createLinkSection("Nos services",
                new String[]{"Suivi des symptômes", "Ressources éducatives", "Communauté de soutien", "Conseils personnalisés", "Témoignages de patients"});

        VBox section1Links = createLinkSection("Hic solutasetp",
                new String[]{"Molestiae accusamus iure", "Excepturi dignissimos", "Suscipit distinctio", "Dilecta", "Sit quas consectetur"});

        VBox section2Links = createLinkSection("Nobis illum",
                new String[]{"Ipsam", "Laudantium dolorum", "Dinera", "Trodelas", "Flexo"});

        footerContent.getChildren().addAll(aboutSection, usefulLinks, servicesLinks, section1Links, section2Links);

        // Copyright
        VBox copyright = new VBox();
        copyright.setAlignment(Pos.CENTER);
        copyright.setPadding(new Insets(20, 0, 0, 0));

        Label copyrightText = new Label("© Copyright ChronoSerna Tous droits réservés");
        Label creditsText = new Label("Conçu par BootstrapMade");

        copyright.getChildren().addAll(copyrightText, creditsText);

        footer.getChildren().addAll(footerContent, copyright);
        return footer;
    }

    private VBox createLinkSection(String title, String[] links) {
        VBox section = new VBox();
        section.setSpacing(10);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Poppins", FontWeight.BOLD, 16));

        VBox linkList = new VBox();
        linkList.setSpacing(5);

        for (String link : links) {
            HBox linkContainer = new HBox(5);
            linkContainer.setAlignment(Pos.CENTER_LEFT);

            // Ajouter une icône de flèche pour chaque lien
            FontIcon arrowIcon = new FontIcon(BootstrapIcons.CHEVRON_RIGHT);
            arrowIcon.setIconColor(Color.web("#4ECDC4"));
            arrowIcon.setIconSize(12);

            Hyperlink hyperlink = new Hyperlink(link);

            linkContainer.getChildren().addAll(arrowIcon, hyperlink);
            linkList.getChildren().add(linkContainer);
        }

        section.getChildren().addAll(titleLabel, linkList);
        return section;
    }

    private void updateCarousel(int index) {
        currentCarouselIndex = index;
        CarouselItem item = carouselItems.get(index);

        carouselTitle.setText(item.getTitle());
        carouselDescription.setText(item.getDescription());
        carouselButton.setText(item.getButtonText());

        // Mise à jour de l'arrière-plan
        carouselContent.setStyle("-fx-background-image: url('" + item.getImageUrl() + "'); " +
                "-fx-background-size: cover; " +
                "-fx-background-position: center;");

        // Mise à jour des indicateurs
        for (int i = 0; i < carouselIndicators.getChildren().size(); i++) {
            Button indicator = (Button) carouselIndicators.getChildren().get(i);
            if (i == index) {
                indicator.getStyleClass().add("active");
            } else {
                indicator.getStyleClass().remove("active");
            }
        }
    }

    private void navigateCarousel(int direction) {
        int newIndex = (currentCarouselIndex + direction + carouselItems.size()) % carouselItems.size();
        updateCarousel(newIndex);
    }

    private void startCarousel() {
        // Initialiser le premier élément
        updateCarousel(0);

        // Créer une animation pour le carrousel
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            navigateCarousel(1);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Classe pour représenter un élément du carrousel
    private static class CarouselItem {
        private final String imageUrl;
        private final String title;
        private final String description;
        private final String buttonText;

        public CarouselItem(String imageUrl, String title, String description, String buttonText) {
            this.imageUrl = imageUrl;
            this.title = title;
            this.description = description;
            this.buttonText = buttonText;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getButtonText() {
            return buttonText;
        }
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}