package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.stage.Stage;
import org.example.models.Produit;
import org.example.services.ProduitServices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ShowProduitFrontController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TilePane productTilePane;

    @FXML
    private TextField productSearchField;

    @FXML
    private Slider priceRangeSlider;

    @FXML
    private Label maxPriceLabel;

    @FXML
    private Label cartCountLabel;

    @FXML
    private HBox paginationContainer;

    private ProduitServices ps = new ProduitServices();

    private int currentPage = 1;
    private int itemsPerPage = 36;
    private int totalPages = 1;
    private List<Produit> allProducts;
    private ObservableList<Produit> filteredProducts;

    private final Color PRIMARY_COLOR = Color.web("#30b4b4");
    private final Color SECONDARY_COLOR = Color.web("#2f3ff1");

    @FXML
    void initialize() {
        // Use Platform.runLater to ensure this happens after JavaFX initialization
        javafx.application.Platform.runLater(() -> {
            if (anchorPane.getScene() != null) {
                Stage stage = (Stage) anchorPane.getScene().getWindow();
                stage.setMaximized(true);
                setupResponsiveLayout();
            }
        });

        // Rest of your initialization code remains the same...
        updateCartCount();

        // Set up price slider listener
        priceRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxPriceLabel.setText(String.format("€%.2f", newVal.doubleValue()));
        });

        // Load all products
        loadAllProducts();

        // Render initial page of products
        renderProductsPage(currentPage);

        // Create pagination
        createPagination();
    }

    private void setupResponsiveLayout() {
        // Ensure anchorPane fills entire window
        AnchorPane.setTopAnchor(anchorPane, 0.0);
        AnchorPane.setBottomAnchor(anchorPane, 0.0);
        AnchorPane.setLeftAnchor(anchorPane, 0.0);
        AnchorPane.setRightAnchor(anchorPane, 0.0);

        // Make the productTilePane adjust its columns based on window width
        if (productTilePane != null) {
            productTilePane.prefWidthProperty().bind(anchorPane.widthProperty().subtract(300)); // 250px for sidebar + padding

            // Adjust the tile pane columns dynamically based on the window size
            productTilePane.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
                // Calculate optimal number of columns based on available width
                int availableWidth = newVal.intValue();
                int cardWidth = 300;
                int gap = 20;
                int columns = Math.max(1, availableWidth / (cardWidth + gap));

                productTilePane.setPrefColumns(columns);
            });
        }

        // Make sure the ScrollPane containing the TilePane also expands
        if (productTilePane.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) productTilePane.getParent();
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
        }
    }

    private void loadAllProducts() {
        allProducts = ps.showProduit();
        filteredProducts = FXCollections.observableArrayList(allProducts);
        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
    }

    @FXML
    void performSearch() {
        String searchTerm = productSearchField.getText().toLowerCase().trim();

        if (searchTerm.isEmpty()) {
            // Reset to show all products
            filteredProducts.setAll(allProducts);
        } else {
            // Filter products based on search term
            filteredProducts.setAll(allProducts.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(searchTerm) ||
                            p.getDescription().toLowerCase().contains(searchTerm))
                    .toList());
        }

        // Update pagination
        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
        currentPage = 1;

        // Refresh display
        renderProductsPage(currentPage);
        createPagination();
    }

    @FXML
    void applyPriceFilter() {
        double maxPrice = priceRangeSlider.getValue();

        filteredProducts.setAll(allProducts.stream()
                .filter(p -> p.getPrix() <= maxPrice)
                .toList());

        // Update pagination
        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
        currentPage = 1;

        // Refresh display
        renderProductsPage(currentPage);
        createPagination();
    }

    private void renderProductsPage(int page) {
        // Clear existing products
        productTilePane.getChildren().clear();

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredProducts.size());

        // Add products for current page
        for (int i = startIndex; i < endIndex; i++) {
            VBox productCard = createProductCard(filteredProducts.get(i));
            productTilePane.getChildren().add(productCard);
        }
    }

    private void createPagination() {
        paginationContainer.getChildren().clear();

        if (totalPages <= 1) {
            return; // No pagination needed
        }

        // Previous button
        Button prevButton = new Button("«");
        stylePageButton(prevButton, false);
        prevButton.setDisable(currentPage == 1);
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                navigateToPage(currentPage - 1);
            }
        });
        paginationContainer.getChildren().add(prevButton);

        // Page buttons
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        for (int i = startPage; i <= endPage; i++) {
            int pageNum = i;
            Button pageButton = new Button(String.valueOf(pageNum));
            stylePageButton(pageButton, pageNum == currentPage);
            pageButton.setOnAction(e -> navigateToPage(pageNum));
            paginationContainer.getChildren().add(pageButton);
        }

        // Next button
        Button nextButton = new Button("»");
        stylePageButton(nextButton, false);
        nextButton.setDisable(currentPage == totalPages);
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                navigateToPage(currentPage + 1);
            }
        });
        paginationContainer.getChildren().add(nextButton);
    }

    private void stylePageButton(Button button, boolean isActive) {
        if (isActive) {
            button.setStyle("-fx-background-color: #30b4b4; -fx-text-fill: white; -fx-min-width: 40px; -fx-min-height: 40px; -fx-background-radius: 4px;");
        } else {
            button.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-border-color: #dee2e6; -fx-min-width: 40px; -fx-min-height: 40px; -fx-background-radius: 4px;");
        }
    }

    private void navigateToPage(int page) {
        currentPage = page;
        renderProductsPage(page);
        createPagination();
    }

    private VBox createProductCard(Produit produit) {
        // Create main container
        VBox card = new VBox(8);
        card.setPrefWidth(300);
        card.setPrefHeight(420);
        card.setPadding(new Insets(0));
        card.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1; -fx-border-radius: 4;");

        // Brand label at top
        Label brandLabel = new Label("Brand");
        brandLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 14px; -fx-padding: 10 15 10 15;");

        // Product Image Container
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(250);
        imageContainer.setStyle("-fx-background-color: #f9f9f9;");

        // Product Image
        ImageView imageView = new ImageView();
        String imageName = produit.getImage();
        loadProductImage(imageView, imageName);

        imageView.setFitWidth(250);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageContainer.getChildren().add(imageView);

        // Product Info Container
        VBox infoContainer = new VBox(5);
        infoContainer.setPadding(new Insets(15));

        // Product Name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 16));
        nameLabel.setStyle("-fx-text-fill: #333333;");

        // Product SKU/ID - Fixed the primitive int issue
        String skuText = "ks" + String.format("%07d", produit.getId() > 0 ? produit.getId() : (int)(Math.random() * 10000000));
        Label skuLabel = new Label(skuText);
        skuLabel.setFont(Font.font("System", 12));
        skuLabel.setStyle("-fx-text-fill: #777777;");

        // Price Section with "from" text
        HBox priceBox = new HBox(5);
        priceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label fromLabel = new Label("from");
        fromLabel.setFont(Font.font("System", 12));
        fromLabel.setStyle("-fx-text-fill: #777777;");

        Label priceLabel = new Label(String.format("%.2f TND", produit.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setStyle("-fx-text-fill: #30b4b4;");

        Label taxLabel = new Label("ex. VAT*");
        taxLabel.setFont(Font.font("System", 12));
        taxLabel.setStyle("-fx-text-fill: #777777;");

        priceBox.getChildren().addAll(fromLabel, priceLabel, taxLabel);

        // Add To Cart Button
        Button addToCartBtn = new Button("commande");
        addToCartBtn.setPrefWidth(Double.MAX_VALUE);
        addToCartBtn.setPrefHeight(40);
        addToCartBtn.setStyle("-fx-background-color: #30b4b4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        addToCartBtn.setOnAction(e -> handleAddToCart(produit));

        // Add elements to info container
        infoContainer.getChildren().addAll(nameLabel, skuLabel, priceBox, addToCartBtn);

        // Add all components to card
        card.getChildren().addAll(brandLabel, imageContainer, infoContainer);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-border-color: #30b4b4; -fx-border-width: 1; -fx-border-radius: 4;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1; -fx-border-radius: 4;");
        });

        return card;
    }

    private void loadProductImage(ImageView imageView, String imageName) {
        // Debug output
        System.out.println("Attempting to load image: " + imageName);

        if (imageName == null || imageName.trim().isEmpty()) {
            System.out.println("Image name is null or empty, loading placeholder");
            loadPlaceholderImage(imageView);
            return;
        }

        boolean imageLoaded = false;

        // First try: Direct path from src/resources/images
        try {
            String imagePath = "src/resources/images/" + imageName;
            File imageFile = new File(imagePath);
            if (imageFile.exists() && imageFile.isFile()) {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
                System.out.println("Image loaded successfully from: " + imagePath);
                return;
            } else {
                System.out.println("File not found at: " + imagePath);
            }
        } catch (Exception e) {
            System.out.println("Error loading from direct path: " + e.getMessage());
        }

        // Second try: Load from classpath resource
        try {
            URL resourceUrl = getClass().getResource("/images/" + imageName);
            if (resourceUrl != null) {
                Image image = new Image(resourceUrl.toExternalForm());
                imageView.setImage(image);
                System.out.println("Image loaded successfully from resources: /images/" + imageName);
                return;
            } else {
                System.out.println("Resource not found: /images/" + imageName);
            }
        } catch (Exception e) {
            System.out.println("Error loading from resources: " + e.getMessage());
        }

        // Try other resource paths
        String[] resourcePaths = {
                "/resources/images/" + imageName,
                "/front/images/" + imageName,
                "/front/images/products/" + imageName
        };

        for (String path : resourcePaths) {
            try {
                URL resourceUrl = getClass().getResource(path);
                if (resourceUrl != null) {
                    Image image = new Image(resourceUrl.toExternalForm());
                    imageView.setImage(image);
                    System.out.println("Image loaded successfully from: " + path);
                    return;
                } else {
                    System.out.println("Resource not found: " + path);
                }
            } catch (Exception e) {
                System.out.println("Error loading from " + path + ": " + e.getMessage());
            }
        }

        // Try loading as external URL if it starts with http
        if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
            try {
                Image image = new Image(imageName);
                imageView.setImage(image);
                System.out.println("Image loaded successfully from URL: " + imageName);
                return;
            } catch (Exception e) {
                System.out.println("Error loading from URL: " + e.getMessage());
            }
        }

        // Last resort: Try absolute file path
        try {
            File file = new File(imageName);
            if (file.exists() && file.isFile()) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                System.out.println("Image loaded successfully from absolute path: " + imageName);
                return;
            } else {
                System.out.println("File not found at absolute path: " + imageName);
            }
        } catch (Exception e) {
            System.out.println("Error loading from absolute path: " + e.getMessage());
        }

        // If all attempts failed, load placeholder
        System.out.println("All image loading attempts failed, loading placeholder");
        loadPlaceholderImage(imageView);
    }

    private void loadPlaceholderImage(ImageView imageView) {
        try {
            // First try to load from resources
            URL placeholderUrl = getClass().getResource("/images/product-placeholder.png");
            if (placeholderUrl != null) {
                Image placeholder = new Image(placeholderUrl.toExternalForm());
                imageView.setImage(placeholder);
                System.out.println("Placeholder loaded from resources");
                return;
            }

            // Second try: direct file path
            File placeholderFile = new File("src/resources/images/product-placeholder.png");
            if (placeholderFile.exists() && placeholderFile.isFile()) {
                Image placeholder = new Image(placeholderFile.toURI().toString());
                imageView.setImage(placeholder);
                System.out.println("Placeholder loaded from file system");
                return;
            }

            // Last resort: create a default image
            System.out.println("No placeholder found, setting empty image");
            imageView.setImage(null);
            StackPane parent = (StackPane) imageView.getParent();
            if (parent != null) {
                parent.setStyle("-fx-background-color: #f5f5f5;");
            }
        } catch (Exception e) {
            System.out.println("Failed to load placeholder image: " + e.getMessage());
            imageView.setImage(null);
            StackPane parent = (StackPane) imageView.getParent();
            if (parent != null) {
                parent.setStyle("-fx-background-color: #f5f5f5;");
            }
        }
    }

    private void handleAddToCart(Produit produit) {
        if (produit.getStock_quantite() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock",
                    "This product is currently out of stock",
                    "Please check back later or browse our other products.");
            return;
        }

        // Increment cart count
        int currentCount = Integer.parseInt(cartCountLabel.getText());
        cartCountLabel.setText(String.valueOf(currentCount + 1));

        // Open the addCommande.fxml file
        try {
            URL fxmlLocation = getClass().getResource("/fxml/front/addCommande.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/addCommande.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Pass the selected product to the controller
            AddCommandeController controller = loader.getController();
            controller.setSelectedProduct(produit);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter une commande");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error opening commande form", e.getMessage());
        }
    }

    private void updateCartCount() {
        // Initialize with 0 if not already set
        if (cartCountLabel != null) {
            cartCountLabel.setText("0");
        }
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        try {
            // Style the alert dialog if CSS file exists
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/styles/alert.css").toExternalForm());
        } catch (Exception e) {
            // CSS file not found, use default styling
        }

        alert.showAndWait();
    }

    // Navigation methods defined in FXML
    @FXML
    void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to Home", e.getMessage());
        }
    }

    @FXML
    void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Profile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to Profile", e.getMessage());
        }
    }

    @FXML
    void navigateToShop() {
        // Since we're already in the shop view, we can just refresh
        loadAllProducts();
        renderProductsPage(1);
        createPagination();
    }

    @FXML
    void commande(ActionEvent event) {

        try {
            // Make sure to provide the correct path to your FXML file
            URL fxmlLocation = getClass().getResource("/fxml/front/showCommande.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found!");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter une commande");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }}