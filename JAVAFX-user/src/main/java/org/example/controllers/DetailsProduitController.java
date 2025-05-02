package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.CartItem;
import org.example.models.Produit;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.CartItemServices;
import org.example.services.CommandeServices;
import org.example.services.ProduitServices;
import org.example.util.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Cursor;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Pane;
import java.util.logging.Logger;

public class DetailsProduitController {
    private static final Logger LOGGER = Logger.getLogger(DetailsProduitController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Hyperlink backLink;

    @FXML
    private Label categoryLabel;

    @FXML
    private StackPane mainImageContainer;

    @FXML
    private ImageView mainImageView;

    @FXML
    private HBox thumbnailContainer;

    @FXML
    private Label nameLabel;

    @FXML
    private Label ratingLabel;

    @FXML
    private Label referenceLabel;

    @FXML
    private Label eanLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label grossPriceLabel;

    @FXML
    private Label savingsLabel;

    @FXML
    private Text descriptionText;

    @FXML
    private VBox featuresList;

    @FXML
    private Label stockLabel;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Button addToCartButton;

    @FXML
    private Hyperlink wishlistLink;

    @FXML
    private Label cartCountLabel;

    private Produit produit;
    private ProduitServices produitServices;
    private CartItemServices cartItemServices;

    // Zoom-related fields
    private Pane zoomLens;
    private ImageView zoomImageView;
    private final double ZOOM_FACTOR = 2.5; // Increased for more noticeable zoom
    private final double LENS_RADIUS = 60.0; // Slightly larger lens for better visibility

    public void setProduit(Produit produit) {
        this.produit = produit;
        initializeProductDetails();
    }

    @FXML
    void initialize() {
        produitServices = new ProduitServices();
        cartItemServices = new CartItemServices();

        updateCartCount();

        // Initialize zoom functionality
        setupZoomFunctionality();

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

    private void initializeProductDetails() {
        if (produit == null) return;

        categoryLabel.setText(produit.getNom());
        nameLabel.setText(produit.getNom());

        ratingLabel.setText(String.format("5 sur 13 évaluations"));
        referenceLabel.setText("135363W");
        eanLabel.setText("4260306777716");

        float price = produit.getPrix();
        priceLabel.setText(String.format("€%.2f", price));
        grossPriceLabel.setText(String.format("€%.2f brut", price * 1.2));
        savingsLabel.setText(String.format("€%.2f net", price * 0.05));

        if (produit.getImage() != null && !produit.getImage().isEmpty()) {
            String[] images = produit.getImage().split(",");
            loadMainImage(images[0]);
            loadThumbnails(images);
        }

        descriptionText.setText(produit.getDescription() != null ? produit.getDescription() : "");
        addProductFeatures();

        LocalDate deliveryDate = LocalDate.now().plusDays(3);
        stockLabel.setText(String.format("En stock. Date de livraison prévue: %s à 18h", deliveryDate.toString()));

        if (produit.getStock_quantite() <= 0) {
            addToCartButton.setText("Rupture de stock");
            addToCartButton.setDisable(true);
            addToCartButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-opacity: 0.65; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 5;");
        } else {
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, produit.getStock_quantite(), 1);
            quantitySpinner.setValueFactory(valueFactory);
        }
    }

    private void loadMainImage(String imageName) {
        try {
            String imagePath = "/images/" + imageName;
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            mainImageView.setImage(image);
            // Update zoom lens image
            if (zoomImageView != null) {
                zoomImageView.setImage(image);
            }
        } catch (Exception e) {
            mainImageView.setImage(null);
            if (zoomImageView != null) {
                zoomImageView.setImage(null);
            }
            mainImageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #eee; -fx-border-radius: 5;");
        }
    }

    private void loadThumbnails(String[] images) {
        thumbnailContainer.getChildren().clear();
        for (String imageName : images) {
            ImageView thumbnail = new ImageView();
            thumbnail.setFitWidth(60);
            thumbnail.setFitHeight(60);
            thumbnail.setPreserveRatio(true);
            thumbnail.setStyle("-fx-border-color: #eee; -fx-border-radius: 5;");

            try {
                String imagePath = "/images/" + imageName;
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                thumbnail.setImage(image);
            } catch (Exception e) {
                thumbnail.setImage(null);
            }

            thumbnail.setOnMouseClicked(event -> loadMainImage(imageName));
            thumbnailContainer.getChildren().add(thumbnail);
        }
    }

    private void addProductFeatures() {
        String[] features = {
                "Dossier pivote à 360°",
                "Idéal pour différentes positions assises",
                "Hauteur réglable de 48 - 58 cm",
                "Siège Ø: 38 cm",
                "Avec anneau de déverrouillage"
        };

        for (String feature : features) {
            Label featureLabel = new Label("• " + feature);
            featureLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            featuresList.getChildren().add(featureLabel);
        }
    }

    private void setupZoomFunctionality() {
        // Create zoom lens
        zoomLens = new Pane();
        zoomLens.setPrefSize(LENS_RADIUS * 2, LENS_RADIUS * 2);
        zoomLens.setStyle("-fx-border-color: #333; -fx-border-width: 2; -fx-background-color: rgba(255, 255, 255, 0.3);");

        // Create circular clip for zoom lens
        Circle clip = new Circle(LENS_RADIUS, LENS_RADIUS, LENS_RADIUS);
        zoomLens.setClip(clip);

        // Create ImageView for zoomed image
        zoomImageView = new ImageView();
        zoomImageView.setFitWidth(400 * ZOOM_FACTOR);
        zoomImageView.setFitHeight(300 * ZOOM_FACTOR);
        zoomImageView.setPreserveRatio(true);

        // Add zoomImageView to zoomLens
        zoomLens.getChildren().add(zoomImageView);

        // Initially hide zoom lens
        zoomLens.setVisible(false);
        mainImageContainer.getChildren().add(zoomLens);

        mainImageView.setOnMouseEntered(event -> {
            if (mainImageView.getImage() != null) {
                mainImageView.setCursor(Cursor.CROSSHAIR);
                zoomLens.setVisible(true);
            }
        });

        mainImageView.setOnMouseExited(event -> {
            mainImageView.setCursor(Cursor.DEFAULT);
            zoomLens.setVisible(false);
        });

        mainImageView.setOnMouseMoved(event -> {
            if (mainImageView.getImage() == null) {
                zoomLens.setVisible(false);
                return;
            }

            double mouseX = event.getX();
            double mouseY = event.getY();

            // ImageView dimensions
            double viewWidth = mainImageView.getFitWidth();
            double viewHeight = mainImageView.getFitHeight();

            // Calculate image's actual displayed size (considering preserveRatio)
            Image image = mainImageView.getImage();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
            double displayWidth = imageWidth * scale;
            double displayHeight = imageHeight * scale;

            // Calculate offsets if image is centered
            double offsetX = (viewWidth - displayWidth) / 2;
            double offsetY = (viewHeight - displayHeight) / 2;

            // Check if mouse is within the actual image bounds
            if (mouseX >= offsetX && mouseX <= offsetX + displayWidth &&
                    mouseY >= offsetY && mouseY <= offsetY + displayHeight) {
                // Position the zoom lens centered on the cursor
                double lensX = mouseX - LENS_RADIUS;
                double lensY = mouseY - LENS_RADIUS;

                // Keep lens within ImageView bounds
                lensX = Math.max(0, Math.min(lensX, viewWidth - LENS_RADIUS * 2));
                lensY = Math.max(0, Math.min(lensY, viewHeight - LENS_RADIUS * 2));

                zoomLens.setTranslateX(lensX);
                zoomLens.setTranslateY(lensY);
                zoomLens.setVisible(true);

                // Calculate the zoomed image's offset
                // Convert mouse position to original image coordinates
                double imageX = (mouseX - offsetX) / scale; // Pixel in original image
                double imageY = (mouseY - offsetY) / scale;

                // Center the zoomed image on the mouse position
                double zoomedImageX = imageX * ZOOM_FACTOR - LENS_RADIUS;
                double zoomedImageY = imageY * ZOOM_FACTOR - LENS_RADIUS;

                // Ensure the zoomed image stays within bounds
                double zoomViewWidth = LENS_RADIUS * 2;
                double zoomViewHeight = LENS_RADIUS * 2;

                zoomedImageX = Math.max(0, Math.min(zoomedImageX, imageWidth * ZOOM_FACTOR - zoomViewWidth));
                zoomedImageY = Math.max(0, Math.min(zoomedImageY, imageHeight * ZOOM_FACTOR - zoomViewHeight));

                zoomImageView.setTranslateX(-zoomedImageX);
                zoomImageView.setTranslateY(-zoomedImageY);
            } else {
                zoomLens.setVisible(false);
            }
        });
    }

    @FXML
    void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating back", e.getMessage());
        }
    }

    @FXML
    void addToCart() {
        if (produit.getStock_quantite() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock", "This product is currently out of stock", "Please check back later.");
            return;
        }

        try {
            int quantity = quantitySpinner.getValue();
            if (quantity > produit.getStock_quantite()) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Stock", "Requested quantity exceeds available stock", "Please reduce the quantity.");
                return;
            }

            // Retrieve the current user from SessionManager
            SessionManager session = SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            int utilisateurId;
            String userType = session.getUserType();

            if (currentUser instanceof Medecin) {
                utilisateurId = ((Medecin) currentUser).getId();
            } else if (currentUser instanceof Patient) {
                utilisateurId = ((Patient) currentUser).getId();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non authentifié ou type non valide", "");
                return;
            }

            // Only check pending cart items for this user
            List<CartItem> cartItems = getPendingCartItems();
            boolean productExists = cartItems.stream().anyMatch(item -> item.getProduitId() == produit.getId());

            if (productExists) {
                showAlert(Alert.AlertType.INFORMATION, "Déjà ajouté", "Ce produit a déjà été ajouté au panier", "");
                return;
            }

            // Update stock and add to cart
            produit.setStock_quantite(produit.getStock_quantite() - quantity);
            produitServices.editProduit(produit);

            CartItem cartItem = new CartItem(produit.getId(), 0, quantity, utilisateurId);
            cartItem.setProduit(produit);
            cartItemServices.addProduit(cartItem);

            // Update cart count based on pending items for this user
            updateCartCount();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Added to Cart",
                    String.format("%d %s added to your cart!", quantity, produit.getNom()));
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error adding to cart", e.getMessage());
        }
    }

    @FXML
    void navigateToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showCartItem.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to cart", e.getMessage());
        }
    }

    // Navigation methods from the first file
    @FXML
    void navigateToHome() {
        navigateTo("/fxml/front/home.fxml");
    }

    @FXML
    void navigateToHistoriques() {
        navigateTo("/fxml/front/historiques.fxml");
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
        navigateTo("/fxml/front/traitement.fxml");
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
    void commande(ActionEvent event) {
        try {
            // Use SceneManager to load the new scene in full screen
            SceneManager.loadScene("/fxml/front/showCartItem.fxml", event);

        } catch (Exception e) {
            LOGGER.severe("Error navigating to commande: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to commande form", e.getMessage());
        }
    }

    @FXML
    void navigateToCommandes() {
        navigateTo("/fxml/front/ShowCommande.fxml");
    }

    @FXML
    void navigateToShop() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    // Helper method for navigation
    private void navigateTo(String fxmlPath) {
        try {
            // Get the current stage
            Stage stage = (Stage) anchorPane.getScene().getWindow();

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Create the new scene with screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Apply stylesheets from the previous scene if needed
            if (stage.getScene() != null && !stage.getScene().getStylesheets().isEmpty()) {
                scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            }

            // Set the new scene
            stage.setScene(scene);

            // Make sure it's maximized for full screen display
            stage.setMaximized(true);

            // Show the stage
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page", e.getMessage());
        }
    }
    // Only count pending cart items for the current user
    private void updateCartCount() {
        List<CartItem> pendingItems = getPendingCartItems();
        cartCountLabel.setText(String.valueOf(pendingItems.size()));
    }

    // Helper method to get only pending cart items for the current user
    private List<CartItem> getPendingCartItems() {
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int utilisateurId = 0;

        if (currentUser instanceof Medecin) {
            utilisateurId = ((Medecin) currentUser).getId();
        } else if (currentUser instanceof Patient) {
            utilisateurId = ((Patient) currentUser).getId();
        }

        int finalUtilisateurId = utilisateurId;
        List<CartItem> allCartItems = cartItemServices.showProduit();
        return allCartItems.stream()
                .filter(item -> item.getUtilisateurId() == finalUtilisateurId)
                .filter(item -> item.getCommandeId() == 0 || "pending".equals(getCommandePaymentStatus(item)))
                .collect(Collectors.toList());
    }

    // Helper method to get payment status of a commande
    private String getCommandePaymentStatus(CartItem item) {
        if (item.getCommandeId() == 0) {
            return "pending";
        }
        CommandeServices commandeServices = new CommandeServices();
        var commande = commandeServices.getoneProduit(item.getCommandeId());
        return commande != null ? commande.getPaymentStatus() : "pending";
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}