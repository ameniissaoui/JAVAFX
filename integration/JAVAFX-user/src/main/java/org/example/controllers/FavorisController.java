package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.Favoris;
import org.example.models.Produit;
import org.example.models.User;
import org.example.services.FavorisServices;
import org.example.services.ProduitServices;
import org.example.services.CartItemServices;
import org.example.util.SessionManager;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

public class FavorisController {

    private static final Logger LOGGER = Logger.getLogger(FavorisController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TilePane favorisTilePane;

    @FXML
    private Label cartCountLabel;

    @FXML
    private Label cartCountLabel1;

    private FavorisServices favorisServices = new FavorisServices();
    private ProduitServices produitServices = new ProduitServices();
    private CartItemServices cartItemServices;

    private final Color PRIMARY_COLOR = Color.web("#30b4b4");

    @FXML
    void initialize() {
        // Initialize services
        cartItemServices = new CartItemServices();

        // Setup responsive layout immediately
        setupResponsiveLayout();

        // Update cart count
        updateCartCount();

        // Check if a user is logged in before loading favorites
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        if (!(currentUser instanceof User)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non authentifié", "Veuillez vous connecter pour voir vos favoris.");
            navigateToHome();
            return;
        }

        loadFavoris();

        // Maximize the stage
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            maximizeStage(stage);
        });
    }

    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }

    private void setupResponsiveLayout() {
        AnchorPane.setTopAnchor(anchorPane, 0.0);
        AnchorPane.setBottomAnchor(anchorPane, 0.0);
        AnchorPane.setLeftAnchor(anchorPane, 0.0);
        AnchorPane.setRightAnchor(anchorPane, 0.0);

        if (favorisTilePane != null) {
            favorisTilePane.prefWidthProperty().bind(anchorPane.widthProperty().multiply(0.95));
            favorisTilePane.prefHeightProperty().bind(anchorPane.heightProperty().multiply(0.8));

            favorisTilePane.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
                int availableWidth = newVal.intValue();
                int cardWidth = 300;
                int gap = 20;
                int columns = Math.max(1, availableWidth / (cardWidth + gap));
                favorisTilePane.setPrefColumns(columns);
            });
        }

        if (favorisTilePane.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) favorisTilePane.getParent();
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.prefWidthProperty().bind(anchorPane.widthProperty().multiply(0.95));
            scrollPane.prefHeightProperty().bind(anchorPane.heightProperty().multiply(0.8));
        }
    }

    private void loadFavoris() {
        favorisTilePane.getChildren().clear();
        try {
            List<Favoris> favorisList = favorisServices.showProduit();
            if (favorisList.isEmpty()) {
                Label emptyLabel = new Label("Aucun produit dans vos favoris.");
                emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                emptyLabel.setStyle("-fx-text-fill: #666666;");
                favorisTilePane.getChildren().add(emptyLabel);
                LOGGER.info("No favorites found in database for the logged-in user");
            } else {
                for (Favoris favoris : favorisList) {
                    VBox productCard = createProductCard(favoris.getProduit());
                    favorisTilePane.getChildren().add(productCard);
                }
                LOGGER.info("Loaded " + favorisList.size() + " favorites for the logged-in user");
            }
        } catch (RuntimeException e) {
            LOGGER.severe("Error loading favorites: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load favorites", e.getMessage());
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setPrefHeight(450);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        StackPane badgeContainer = new StackPane();
        badgeContainer.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        badgeContainer.setPadding(new Insets(10));

        boolean isNew = false;
        java.sql.Date sqlDate = produit.getDate();
        if (sqlDate != null) {
            LocalDate productDate = sqlDate.toLocalDate();
            isNew = ChronoUnit.DAYS.between(productDate, LocalDate.now()) <= 7;
        }
        boolean isOutOfStock = produit.getStock_quantite() <= 0;

        if (isNew || isOutOfStock) {
            Label badgeLabel = new Label(isNew ? "New" : "Out of Stock");
            badgeLabel.setStyle(
                    "-fx-background-color: " + (isNew ? "#4caf50" : "#f44336") + ";" +
                            "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                            "-fx-padding: 5 10; -fx-background-radius: 12;"
            );
            badgeContainer.getChildren().add(badgeLabel);
        }

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(220);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 6;");

        ImageView imageView = new ImageView();
        String imageName = produit.getImage();
        loadProductImage(imageView, imageName);

        imageView.setFitWidth(200);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageContainer.getChildren().add(imageView);

        VBox infoContainer = new VBox(8);
        infoContainer.setPadding(new Insets(10));

        Label nameLabel = new Label(produit.getNom());
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #222222;");

        String skuText = "SKU: " + String.format("%07d", produit.getId() > 0 ? produit.getId() : (int)(Math.random() * 10000000));
        Label skuLabel = new Label(skuText);
        skuLabel.setFont(Font.font("System", 12));
        skuLabel.setStyle("-fx-text-fill: #666666;");

        HBox priceBox = new HBox(8);
        priceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("%.2f TND", produit.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setStyle("-fx-text-fill: #30b4b4;");

        Label taxLabel = new Label("ex. VAT*");
        taxLabel.setFont(Font.font("System", 12));
        taxLabel.setStyle("-fx-text-fill: #888888;");

        priceBox.getChildren().addAll(priceLabel, taxLabel);

        Button removeButton = new Button("Retirer des favoris");
        removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
        removeButton.setOnAction(e -> removeFromFavoris(produit));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(removeButton);

        infoContainer.getChildren().addAll(nameLabel, skuLabel, priceBox, buttonBox);

        card.getChildren().addAll(badgeContainer, imageContainer, infoContainer);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-border-color: #30b4b4; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        card.setOnMouseClicked(e -> navigateToProductDetails(produit));

        return card;
    }

    private void removeFromFavoris(Produit produit) {
        try {
            favorisServices.removeByProduit(produit);
            loadFavoris();
            showAlert(Alert.AlertType.INFORMATION, "Removed from Favorites", "Product Removed", produit.getNom() + " has been removed from your favorites.");
        } catch (RuntimeException e) {
            LOGGER.severe("Error removing favorite: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove favorite", e.getMessage());
        }
    }

    private void loadProductImage(ImageView imageView, String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            loadPlaceholderImage(imageView);
            return;
        }

        try {
            URL resourceUrl = getClass().getResource("/images/" + imageName);
            if (resourceUrl != null) {
                Image image = new Image(resourceUrl.toExternalForm());
                imageView.setImage(image);
                return;
            }
        } catch (Exception e) {
            // Ignore and try file path
        }

        try {
            File file = new File("src/main/resources/images/" + imageName);
            if (file.exists() && file.isFile()) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                return;
            }
        } catch (Exception e) {
            // Ignore and load placeholder
        }

        loadPlaceholderImage(imageView);
    }

    private void loadPlaceholderImage(ImageView imageView) {
        try {
            URL placeholderUrl = getClass().getResource("/images/product-placeholder.png");
            if (placeholderUrl != null) {
                Image placeholder = new Image(placeholderUrl.toExternalForm());
                imageView.setImage(placeholder);
            } else {
                imageView.setImage(null);
                StackPane parent = (StackPane) imageView.getParent();
                if (parent != null) {
                    parent.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 6;");
                }
            }
        } catch (Exception e) {
            imageView.setImage(null);
            StackPane parent = (StackPane) imageView.getParent();
            if (parent != null) {
                parent.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 6;");
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        try {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/styles/alert.css").toExternalForm());
        } catch (Exception e) {
            // Ignore stylesheet loading errors
        }

        alert.showAndWait();
    }

    private void navigateToProductDetails(Produit produit) {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/front/detailsProduit.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/detailsProduit.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            DetailsProduitController controller = loader.getController();
            controller.setProduit(produit);

            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to product details: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error opening product details", e.getMessage());
        }
    }

    // Navigation methods from DetailsProduitController
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
        navigateTo("/fxml/front/showProduit.fxml");
    }

    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to " + fxmlPath + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page", e.getMessage());
        }
    }

    // Update cart count
    private void updateCartCount() {
        if (cartItemServices == null) {
            cartItemServices = new CartItemServices();
        }

        try {
            SessionManager session = SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            int count = 0;

            if (currentUser != null) {
                // Get cart count logic would go here
                // This is a simplified version
                count = cartItemServices.showProduit().size();
            }

            if (cartCountLabel != null) {
                cartCountLabel.setText(String.valueOf(count));
            }

            if (cartCountLabel1 != null) {
                cartCountLabel1.setText(String.valueOf(count));
            }
        } catch (Exception e) {
            LOGGER.severe("Error updating cart count: " + e.getMessage());
        }
    }
}