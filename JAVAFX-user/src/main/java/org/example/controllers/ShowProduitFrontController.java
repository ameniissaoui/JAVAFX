package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.models.Produit;
import org.example.models.CartItem;
import org.example.models.Favoris;
import org.example.models.User;
import org.example.services.CommandeServices;
import org.example.services.ProduitServices;
import org.example.services.CartItemServices;
import org.example.services.FavorisServices;
import org.example.util.SessionManager;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import static org.example.controllers.NavigationController.navigateTo;

public class ShowProduitFrontController {

    private static final Logger LOGGER = Logger.getLogger(ShowProduitFrontController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TilePane productTilePane;

    @FXML
    private TextField productSearchField;

    @FXML
    private Slider priceRangeSlider;

    @FXML
    private Label minPriceLabel;

    @FXML
    private Label maxPriceLabel;

    @FXML
    private Label currentPriceLabel;

    @FXML
    private Label cartCountLabel;

    @FXML
    private HBox paginationContainer;

    @FXML
    private ComboBox<String> itemsPerPageComboBox;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private HBox navButtonsHBox;
    private SessionManager sessionManager;

    private final ProduitServices ps = new ProduitServices();
    private CartItemServices cartItemServices = new CartItemServices();
    private FavorisServices favorisServices = new FavorisServices();

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;
    private List<Produit> allProducts;
    private ObservableList<Produit> filteredProducts;
    private boolean isPriceFilterApplied = false;

    private final Color PRIMARY_COLOR = Color.web("#30b4b4");
    private final Color SECONDARY_COLOR = Color.web("#2f3ff1");

    private final double DEFAULT_WINDOW_WIDTH = 1280.0;
    private final double DEFAULT_WINDOW_HEIGHT = 720.0;

    @FXML
    void initialize() {
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            setStageSize(stage);
            setupResponsiveLayout();
            anchorPane.setUserData(this); // Set controller as userData for DynamicHeaderSetup
            DynamicHeaderSetup.setupHeader(navButtonsHBox); // Initialize header buttons
        });

        updateCartCount();

        if (itemsPerPageComboBox != null) {
            itemsPerPageComboBox.getItems().addAll("10", "20", "30");
            itemsPerPageComboBox.setValue("10");
            itemsPerPageComboBox.setOnAction(event -> {
                itemsPerPage = Integer.parseInt(itemsPerPageComboBox.getValue());
                totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
                currentPage = 1;
                renderProductsPage(currentPage);
                createPagination();
            });
        }

        if (sortComboBox != null) {
            sortComboBox.getItems().addAll("Recommandation", "Prix: Bas à Haut", "Prix: Haut à Bas", "Nouveautés");
            sortComboBox.setValue("Nouveautés");
            sortComboBox.setOnAction(event -> applySorting());
        }

        loadAllProducts();
        setupPriceSlider();
        renderProductsPage(currentPage);
        createPagination();

    }

    private void setStageSize(Stage stage) {
        stage.setWidth(DEFAULT_WINDOW_WIDTH);
        stage.setHeight(DEFAULT_WINDOW_HEIGHT);
        stage.centerOnScreen();
    }

    private void setupResponsiveLayout() {
        AnchorPane.setTopAnchor(anchorPane, 0.0);
        AnchorPane.setBottomAnchor(anchorPane, 0.0);
        AnchorPane.setLeftAnchor(anchorPane, 0.0);
        AnchorPane.setRightAnchor(anchorPane, 0.0);

        if (productTilePane != null) {
            productTilePane.prefWidthProperty().bind(anchorPane.widthProperty().subtract(270));
            productTilePane.prefHeightProperty().bind(anchorPane.heightProperty().subtract(300));

            productTilePane.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
                int availableWidth = newVal.intValue();
                int cardWidth = 300;
                int gap = 20;
                int columns = Math.max(1, Math.min(3, availableWidth / (cardWidth + gap)));
                productTilePane.setPrefColumns(columns);
            });
        }

        if (productTilePane.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) productTilePane.getParent();
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.prefHeightProperty().bind(anchorPane.heightProperty().subtract(300));
            scrollPane.prefWidthProperty().bind(anchorPane.widthProperty().subtract(270));
        }
    }

    private void loadAllProducts() {
        try {
            allProducts = ps.showProduit();
            allProducts.sort(Comparator.comparingDouble(Produit::getPrix));
            filteredProducts = FXCollections.observableArrayList(allProducts);
            totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
            LOGGER.info("Loaded " + allProducts.size() + " products, total pages: " + totalPages);
        } catch (Exception e) {
            LOGGER.severe("Error loading products: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products", e.getMessage());
        }
    }

    private void setupPriceSlider() {
        if (allProducts == null || allProducts.isEmpty()) {
            priceRangeSlider.setDisable(true);
            minPriceLabel.setText("€0.00");
            maxPriceLabel.setText("€0.00");
            currentPriceLabel.setText("€0.00");
            return;
        }

        double minPrice = allProducts.stream()
                .mapToDouble(Produit::getPrix)
                .min()
                .orElse(0.0);
        double maxPrice = allProducts.stream()
                .mapToDouble(Produit::getPrix)
                .max()
                .orElse(1000.0);

        priceRangeSlider.setMin(minPrice);
        priceRangeSlider.setMax(maxPrice);
        priceRangeSlider.setValue(maxPrice);
        priceRangeSlider.setMajorTickUnit((maxPrice - minPrice) / 10);
        priceRangeSlider.setMinorTickCount(5);
        priceRangeSlider.setShowTickMarks(true);
        priceRangeSlider.setShowTickLabels(false);

        minPriceLabel.setText(String.format("€%.2f", minPrice));
        maxPriceLabel.setText(String.format("€%.2f", maxPrice));
        currentPriceLabel.setText("All Prices");

        priceRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isPriceFilterApplied) {
                isPriceFilterApplied = true;
            }
            currentPriceLabel.setText(String.format("€%.2f", newVal.doubleValue()));
            filterProductsByPrice(newVal.doubleValue());
        });
    }

    @FXML
    void performSearch() {
        String searchTerm = productSearchField.getText().toLowerCase().trim();

        if (searchTerm.isEmpty()) {
            filteredProducts.setAll(allProducts);
        } else {
            List<Produit> filteredList = allProducts.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(searchTerm) ||
                            p.getDescription().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
            filteredProducts.setAll(filteredList);
        }

        if (isPriceFilterApplied) {
            filterProductsByPrice(priceRangeSlider.getValue());
        } else {
            applySorting();
        }

        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
        currentPage = 1;
        LOGGER.info("Search performed, total pages: " + totalPages);
        renderProductsPage(currentPage);
        createPagination();
    }

    private void filterProductsByPrice(double maxPrice) {
        List<Produit> filteredList = allProducts.stream()
                .filter(p -> p.getPrix() <= maxPrice)
                .filter(p -> {
                    String searchTerm = productSearchField.getText().toLowerCase().trim();
                    return searchTerm.isEmpty() ||
                            p.getNom().toLowerCase().contains(searchTerm) ||
                            p.getDescription().toLowerCase().contains(searchTerm);
                })
                .sorted(Comparator.comparingDouble(Produit::getPrix))
                .collect(Collectors.toList());
        filteredProducts.setAll(filteredList);

        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
        currentPage = 1;
        LOGGER.info("Price filter applied, total pages: " + totalPages);
        renderProductsPage(currentPage);
        createPagination();
    }

    private void applySorting() {
        String sortOption = sortComboBox.getValue();
        List<Produit> sortedList = filteredProducts.stream()
                .filter(p -> !isPriceFilterApplied || p.getPrix() <= priceRangeSlider.getValue())
                .filter(p -> {
                    String searchTerm = productSearchField.getText().toLowerCase().trim();
                    return searchTerm.isEmpty() ||
                            p.getNom().toLowerCase().contains(searchTerm) ||
                            p.getDescription().toLowerCase().contains(searchTerm);
                })
                .collect(Collectors.toList());

        switch (sortOption) {
            case "Prix: Bas à Haut":
                sortedList.sort(Comparator.comparingDouble(Produit::getPrix));
                break;
            case "Prix: Haut à Bas":
                sortedList.sort(Comparator.comparingDouble(Produit::getPrix).reversed());
                break;
            case "Nouveautés":
                sortedList.sort(Comparator.comparing(Produit::getDate, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "Recommandation":
            default:
                break;
        }

        filteredProducts.setAll(sortedList);
        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
        currentPage = 1;
        renderProductsPage(currentPage);
        createPagination();
    }

    private void renderProductsPage(int page) {
        productTilePane.getChildren().clear();

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredProducts.size());

        LOGGER.info("Rendering page " + page + ": products " + startIndex + " to " + endIndex);
        for (int i = startIndex; i < endIndex; i++) {
            VBox productCard = createProductCard(filteredProducts.get(i));
            productTilePane.getChildren().add(productCard);
        }
    }

    private void createPagination() {
        paginationContainer.getChildren().clear();

        if (totalPages <= 1) {
            return;
        }

        Button prevButton = new Button("«");
        stylePageButton(prevButton, false);
        prevButton.setDisable(currentPage == 1);
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                navigateToPage(currentPage - 1);
            }
        });
        paginationContainer.getChildren().add(prevButton);

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        for (int i = startPage; i <= endPage; i++) {
            int pageNum = i;
            Button pageButton = new Button(String.valueOf(pageNum));
            stylePageButton(pageButton, pageNum == currentPage);
            pageButton.setOnAction(e -> navigateToPage(pageNum));
            paginationContainer.getChildren().add(pageButton);
        }

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
            button.setStyle("-fx-background-color: #30b4b4; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 40px; -fx-min-height: 40px; -fx-background-radius: 4px;");
        } else {
            button.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-min-width: 40px; -fx-min-height: 40px; -fx-background-radius: 4px;");
        }
    }

    private void navigateToPage(int page) {
        currentPage = page;
        renderProductsPage(page);
        createPagination();
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setPrefHeight(450);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        boolean isOutOfStock = produit.getStock_quantite() <= 0;

        StackPane badgeContainer = new StackPane();
        badgeContainer.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        badgeContainer.setPadding(new Insets(10));

        boolean isNew = false;
        java.sql.Date sqlDate = produit.getDate();
        if (sqlDate != null) {
            LocalDate productDate = sqlDate.toLocalDate();
            isNew = ChronoUnit.DAYS.between(productDate, LocalDate.now()) <= 7;
        }

        if (isNew || isOutOfStock) {
            Label badgeLabel = new Label(isNew ? "New" : "Rupture de stock");
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
        if (isOutOfStock) {
            ColorAdjust grayscale = new ColorAdjust();
            grayscale.setSaturation(-1);
            imageView.setEffect(grayscale);
        }
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

        Button favoriteButton = new Button();
        boolean isFavorited = favorisServices.isFavorited(produit);
        favoriteButton.setText("♡");
        favoriteButton.getStyleClass().add("heart-button");
        favoriteButton.getStyleClass().add(isFavorited ? "heart-active" : "heart-inactive");
        favoriteButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-padding: 5;");
        favoriteButton.setOnAction(e -> toggleFavorite(produit, favoriteButton));
        if (isOutOfStock) {
            favoriteButton.setDisable(true);
        }

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(favoriteButton);

        infoContainer.getChildren().addAll(nameLabel, skuLabel, priceBox, buttonBox);

        card.getChildren().addAll(badgeContainer, imageContainer, infoContainer);

        if (!isOutOfStock) {
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
        } else {
            card.setMouseTransparent(true);
            card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-opacity: 0.7;");
        }

        try {
            card.getStylesheets().add(getClass().getResource("/fxml/front/heart-animation.css").toExternalForm());
        } catch (Exception e) {
            LOGGER.warning("Failed to load heart-animation.css: " + e.getMessage());
        }

        return card;
    }

    private void toggleFavorite(Produit produit, Button favoriteButton) {
        try {
            SessionManager session = SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            if (!(currentUser instanceof User)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non authentifié", "Veuillez vous connecter pour ajouter aux favoris.");
                return;
            }

            if (favorisServices.isFavorited(produit)) {
                favorisServices.removeByProduit(produit);
                favoriteButton.getStyleClass().remove("heart-active");
                favoriteButton.getStyleClass().add("heart-inactive");
                showAlert(Alert.AlertType.INFORMATION, "Removed from Favorites", "Product Removed", produit.getNom() + " has been removed from your favorites.");
            } else {
                int utilisateurId = ((User) currentUser).getId();
                Favoris favoris = new Favoris(produit, utilisateurId);
                favorisServices.addProduit(favoris);
                favoriteButton.getStyleClass().remove("heart-inactive");
                favoriteButton.getStyleClass().add("heart-active");
                showAlert(Alert.AlertType.INFORMATION, "Added to Favorites", "Product Added", produit.getNom() + " has been added to your favorites.");
            }
        } catch (RuntimeException e) {
            LOGGER.severe("Error toggling favorite for product " + produit.getNom() + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Favorite Error", "Failed to update favorites", "An error occurred while updating favorites: " + e.getMessage());
            boolean isFavorited = favorisServices.isFavorited(produit);
            favoriteButton.getStyleClass().removeAll("heart-active", "heart-inactive");
            favoriteButton.getStyleClass().add(isFavorited ? "heart-active" : "heart-inactive");
        }
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
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to product details: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error opening product details", e.getMessage());
        }
    }

    private void handleAddToCart(Produit produit) {
        if (produit == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Product", "The selected product is null.");
            return;
        }

        if (produit.getStock_quantite() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock",
                    "This product is currently out of stock",
                    "Please check back later or browse our other products.");
            return;
        }

        try {
            SessionManager session = SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            int utilisateurId;

            if (currentUser instanceof User) {
                utilisateurId = ((User) currentUser).getId();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non authentifié", "Veuillez vous connecter pour ajouter au panier.");
                return;
            }

            URL fxmlLocation = getClass().getResource("/fxml/front/showCartItem.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/showCartItem.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            ShowCartItemController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("ShowCartItemController is null after loading FXML");
            }

            List<CartItem> currentCartItems = cartItemServices.showProduit();
            int finalUtilisateurId = utilisateurId;
            boolean itemExists = currentCartItems.stream()
                    .anyMatch(item -> item.getProduitId() == produit.getId() &&
                            item.getUtilisateurId() == finalUtilisateurId &&
                            (item.getCommandeId() == 0 || "pending".equals(controller.getCommandePaymentStatus(item))));

            if (itemExists) {
                showAlert(Alert.AlertType.WARNING, "Item Already in Cart",
                        "This product is already in your cart",
                        "You can adjust the quantity in the cart page.");
                return;
            }

            CartItem cartItem = new CartItem(produit.getId(), 0, 1, utilisateurId);
            cartItem.setProduit(produit);
            controller.addToCart(cartItem);

            updateCartCount();

            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Panier");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to cart: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error opening cart", e.getMessage());
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

    private void updateCartCount() {
        if (cartCountLabel != null) {
            SessionManager session = SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            int utilisateurId = 0;

            if (currentUser instanceof User) {
                utilisateurId = ((User) currentUser).getId();
            }

            int finalUtilisateurId = utilisateurId;
            List<CartItem> currentCartItems = cartItemServices.showProduit();
            int count = (int) currentCartItems.stream()
                    .filter(item -> item.getUtilisateurId() == finalUtilisateurId)
                    .filter(item -> item.getCommandeId() == 0 || "pending".equals(getCommandePaymentStatus(item)))
                    .count();
            cartCountLabel.setText(String.valueOf(count));
        }
    }

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

        try {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/styles/alert.css").toExternalForm());
            dialogPane.getStylesheets().add(getClass().getResource("/fxml/front/heart-animation.css").toExternalForm());
        } catch (Exception e) {
            // Ignore stylesheet loading errors
        }

        alert.showAndWait();
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        try {
            if (SessionManager.getInstance().getCurrentUser() != null) {
                SessionManager.getInstance().setCurrentUser(SessionManager.getInstance().getCurrentUser(), "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                setStageSize(stage);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de demande: " + e.getMessage());
            LOGGER.severe("Error navigating to DemandeDashboard: " + e.getMessage());
        }
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        try {
            if (SessionManager.getInstance().getCurrentUser() != null) {
                SessionManager.getInstance().setCurrentUser(SessionManager.getInstance().getCurrentUser(), "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rendez-vous-view.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                setStageSize(stage);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de rendez-vous: " + e.getMessage());
            LOGGER.severe("Error navigating to rendez-vous-view: " + e.getMessage());
        }
    }

    @FXML
    public void redirectProduit(ActionEvent event) {
        try {
            if (SessionManager.getInstance().getCurrentUser() != null) {
                SessionManager.getInstance().setCurrentUser(SessionManager.getInstance().getCurrentUser(), "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                setStageSize(stage);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des produits: " + e.getMessage());
            LOGGER.severe("Error navigating to showProduit: " + e.getMessage());
        }
    }

    @FXML
    public void viewDoctors(ActionEvent event) {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DoctorList.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des médecins: " + e.getMessage());
            LOGGER.severe("Error navigating to DoctorList: " + e.getMessage());
        }
    }

    @FXML
    void navigateToHome() {
        try {
            SessionManager session = SessionManager.getInstance();
            String fxmlPath = session.isMedecin() ? "/fxml/main_view_medecin.fxml" : "/fxml/main_view_patient.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Accueil");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to home: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to Home", e.getMessage());
        }
    }

    @FXML
    void navigateToHistoriques() {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }
            URL fxmlLocation = getClass().getResource("/fxml/front/historiques.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/historiques.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Historiques");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to historiques: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to historiques", e.getMessage());
        }
    }

    @FXML
    void navigateToEvent() {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }
            URL fxmlLocation = getClass().getResource("/fxml/eventFront.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/eventFront.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("evenements");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to evenement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to evenements", e.getMessage());
        }
    }

    @FXML
    void navigateToReservation() {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }
            URL fxmlLocation = getClass().getResource("/fxml/reservationFront.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/reservationFront.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("reservation");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to reservation: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to reservation", e.getMessage());
        }
    }

    @FXML
    void navigateToTraitement() {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }
            URL fxmlLocation = getClass().getResource("/fxml/front/traitement.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/traitement.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Traitement");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to traitement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to traitement", e.getMessage());
        }
    }

    @FXML
    void navigateToContact() {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/front/contact.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/contact.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Contact");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to contact: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to contact", e.getMessage());
        }
    }

    @FXML
    void navigateToProfile() {
        try {
            SessionManager session = SessionManager.getInstance();
            String userType = session.getUserType();
            String fxmlPath;

            switch (userType) {
                case "admin":
                    fxmlPath = "/fxml/AdminDashboard.fxml";
                    break;
                case "medecin":
                    fxmlPath = "/fxml/medecin_profile.fxml";
                    break;
                case "patient":
                    fxmlPath = "/fxml/patient_profile.fxml";
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Error", "User type not recognized",
                            "Cannot navigate to profile page for unknown user type.");
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to profile: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to Profile", e.getMessage());
        }
    }

    @FXML
    void navigateToShop() {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/front/showCartItem.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/showProduit.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Shop");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to shop: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to shop", e.getMessage());
        }
    }

    @FXML
    void commande(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/front/showCartItem.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/showCartItem.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Ajouter une commande");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to commande: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to commande form", e.getMessage());
        }
    }

    @FXML
    void navigateToFavorites() {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/front/favoris.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/favoris.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mes Favoris");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to favorites: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to favorites", e.getMessage());
        }
    }

    @FXML
    void navigateToCommandes() {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/front/showCommande.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("FXML file not found: /fxml/front/showCommande.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mes Commandes");
            setStageSize(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.severe("Error navigating to commandes: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to commandes", e.getMessage());
        }
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

    /**
     * Helper method to check login status before navigation
     * @return true if logged in, false otherwise
     */
    private boolean checkLoginForNavigation() {
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder à cette fonctionnalité.");
            return false;
        }
        return true;
    }
}