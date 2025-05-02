package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
import org.example.services.IServices;
import org.example.util.SessionManager;

import java.io.IOException;
import java.util.List;

public class ShowCartItemController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ListView<CartItem> cartListView;

    @FXML
    private Label totalLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label taxLabel;

    @FXML
    private Label cartCountLabel;

    @FXML
    private Label itemCountLabel;

    @FXML
    private Button clearCartButton;

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private CartItemServices cartItemServices;
    private IServices<Produit> produitServices;
    private CommandeServices commandeServices;

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateCartCount() {
        int count = cartItems.size();
        cartCountLabel.setText(String.valueOf(count));
    }

    private void updateTotal() {
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            if (item.getProduit() != null) {
                subtotal += item.getProduit().getPrix() * item.getQuantite();
            }
        }
        double taxRate = 0.1; // 10% tax
        double tax = subtotal * taxRate;
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("%.2f TND", subtotal));
        taxLabel.setText(String.format("%.2f TND", tax));
        totalLabel.setText(String.format("%.2f TND", total));
    }

    private void updateItemCount() {
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantite).sum();
        itemCountLabel.setText(String.valueOf(totalItems));
    }

    private void updateClearCartButtonVisibility() {
        clearCartButton.setVisible(!cartItems.isEmpty());
    }

    private void showEmptyCartMessage() {
        if (cartItems.isEmpty()) {
            cartListView.setPlaceholder(new Label("Votre panier est vide."));
        }
    }

    private void removeFromCart(CartItem item) {
        cartItems.remove(item);
        cartItemServices.removeProduit(item);
        updateCartCount();
        updateTotal();
        updateItemCount();
        updateClearCartButtonVisibility();
        if (cartItems.isEmpty()) {
            showEmptyCartMessage();
        }
    }

    public String getCommandePaymentStatus(CartItem item) {
        if (item.getCommandeId() == 0) {
            return "pending";
        }
        var commande = commandeServices.getoneProduit(item.getCommandeId());
        return commande != null ? commande.getPaymentStatus() : "pending";
    }

    @FXML
    void initialize() {
        cartItemServices = new CartItemServices();
        produitServices = new ProduitServices();
        commandeServices = new CommandeServices();

        cartListView.setCellFactory(param -> new ListCell<CartItem>() {
            private final HBox hbox = new HBox();
            private final Label productLabel = new Label();
            private final ImageView imageView = new ImageView();
            private final Label unitPriceLabel = new Label();
            private final HBox quantityBox = new HBox();
            private final Label totalPriceLabel = new Label();
            private final Button deleteButton = new Button();

            {
                hbox.setSpacing(20);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPadding(new Insets(15));
                hbox.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-border-color: transparent; -fx-border-width: 2;");
                hbox.setPrefHeight(80);

                hbox.setOnMouseEntered(e -> hbox.setStyle("-fx-background-color: #f9f9f9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-border-color: #30b4b4; -fx-border-width: 2;"));
                hbox.setOnMouseExited(e -> hbox.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-border-color: transparent; -fx-border-width: 2;"));

                productLabel.setPrefWidth(150);
                productLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                productLabel.setWrapText(true);

                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-border-radius: 5; -fx-background-radius: 5;");

                unitPriceLabel.setPrefWidth(120);
                unitPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

                quantityBox.setPrefWidth(120);
                quantityBox.setAlignment(Pos.CENTER);
                Button minusButton = new Button("−");
                Label quantityLabel = new Label();
                Button plusButton = new Button("+");
                minusButton.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 50%; -fx-pref-width: 30px; -fx-pref-height: 30px; -fx-cursor: hand;");
                plusButton.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 50%; -fx-pref-width: 30px; -fx-pref-height: 30px; -fx-cursor: hand;");
                quantityLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-padding: 0 15;");
                quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton);

                totalPriceLabel.setPrefWidth(120);
                totalPriceLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-cursor: hand;");
                deleteButton.setGraphic(new Text("\uD83D\uDDD1️"));
                deleteButton.setTooltip(new Tooltip("Supprimer cet article"));

                hbox.getChildren().addAll(productLabel, imageView, unitPriceLabel, quantityBox, totalPriceLabel, deleteButton);

                minusButton.setOnAction(event -> {
                    CartItem item = getItem();
                    if (item != null && item.getQuantite() > 1) {
                        item.setQuantite(item.getQuantite() - 1);
                        quantityLabel.setText(String.valueOf(item.getQuantite()));
                        totalPriceLabel.setText(String.format("%.2f TND", item.getProduit().getPrix() * item.getQuantite()));
                        cartItemServices.editProduit(item);
                        updateTotal();
                    }
                });

                plusButton.setOnAction(event -> {
                    CartItem item = getItem();
                    if (item != null) {
                        Produit produit = ((ProduitServices) produitServices).getoneProduit(item.getProduitId());
                        if (produit != null && item.getQuantite() < produit.getStock_quantite()) {
                            item.setQuantite(item.getQuantite() + 1);
                            quantityLabel.setText(String.valueOf(item.getQuantite()));
                            totalPriceLabel.setText(String.format("%.2f TND", item.getProduit().getPrix() * item.getQuantite()));
                            cartItemServices.editProduit(item);
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Stock Limit", "Cannot add more", "The requested quantity exceeds available stock.");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    productLabel.setText(item.getProduit().getNom());
                    unitPriceLabel.setText(String.format("%.2f TND", item.getProduit().getPrix()));
                    quantityBox.getChildren().get(1).setUserData(item.getQuantite());
                    ((Label) quantityBox.getChildren().get(1)).setText(String.valueOf(item.getQuantite()));
                    totalPriceLabel.setText(String.format("%.2f TND", item.getProduit().getPrix() * item.getQuantite()));

                    try {
                        String imagePath = "/images/" + item.getProduit().getImage().split(",")[0];
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        imageView.setImage(image);
                    } catch (Exception e) {
                        imageView.setImage(new Image("/images/product-placeholder.png"));
                    }

                    deleteButton.setOnAction(event -> removeFromCart(item));
                    setGraphic(hbox);
                }
            }
        });

        loadCartItems();
        updateCartCount();
        updateTotal();
        updateItemCount();
        updateClearCartButtonVisibility();
    }

    public void setCartItems(List<CartItem> items) {
        cartItems.clear();
        // Filter items by current user's ID
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int utilisateurId = 0;

        if (currentUser instanceof Medecin) {
            utilisateurId = ((Medecin) currentUser).getId();
        } else if (currentUser instanceof Patient) {
            utilisateurId = ((Patient) currentUser).getId();
        }

        if (items != null) {
            int finalUtilisateurId = utilisateurId;
            items.stream()
                    .filter(item -> item.getUtilisateurId() == finalUtilisateurId)
                    .forEach(cartItems::add);
        }
        cartListView.setItems(cartItems);
        updateCartCount();
        updateTotal();
        updateItemCount();
        updateClearCartButtonVisibility();
        if (cartItems.isEmpty()) {
            showEmptyCartMessage();
        }
    }

    public void addToCart(CartItem cartItem) {
        // Check if the product is already in the cart for the current user
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int utilisateurId = 0;

        if (currentUser instanceof Medecin) {
            utilisateurId = ((Medecin) currentUser).getId();
        } else if (currentUser instanceof Patient) {
            utilisateurId = ((Patient) currentUser).getId();
        }

        int finalUtilisateurId = utilisateurId;
        boolean itemExists = cartItems.stream()
                .anyMatch(item -> item.getProduitId() == cartItem.getProduitId() &&
                        item.getUtilisateurId() == finalUtilisateurId &&
                        (item.getCommandeId() == 0 || "pending".equals(getCommandePaymentStatus(item))));

        if (itemExists) {
            showAlert(Alert.AlertType.WARNING, "Item Already in Cart",
                    "This product is already in your cart",
                    "You can adjust the quantity in the cart page.");
            return;
        }

        cartItem.setUtilisateurId(utilisateurId);
        cartItemServices.addProduit(cartItem);
        cartItems.add(cartItem);
        updateCartCount();
        updateTotal();
        updateItemCount();
        updateClearCartButtonVisibility();
    }

    private void loadCartItems() {
        cartItems.clear();
        List<CartItem> allCartItems = cartItemServices.showProduit();
        // Filter by current user's ID
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int utilisateurId = 0;

        if (currentUser instanceof Medecin) {
            utilisateurId = ((Medecin) currentUser).getId();
        } else if (currentUser instanceof Patient) {
            utilisateurId = ((Patient) currentUser).getId();
        }

        int finalUtilisateurId = utilisateurId;
        for (CartItem item : allCartItems) {
            if (item.getUtilisateurId() == finalUtilisateurId &&
                    (item.getCommandeId() == 0 || "pending".equals(getCommandePaymentStatus(item)))) {
                cartItems.add(item);
                System.out.println("Added cart item to UI: produitId=" + item.getProduitId() + ", commandeId=" + item.getCommandeId() + ", utilisateurId=" + item.getUtilisateurId());
            } else {
                System.out.println("Excluded cart item from UI: produitId=" + item.getProduitId() + ", commandeId=" + item.getCommandeId() + ", utilisateurId=" + item.getUtilisateurId());
            }
        }
        cartListView.setItems(cartItems);
        if (cartItems.isEmpty()) {
            showEmptyCartMessage();
        }
    }

    @FXML
    void clearCart() {
        cartItems.forEach(cartItemServices::removeProduit);
        cartItems.clear();
        updateCartCount();
        updateTotal();
        updateItemCount();
        updateClearCartButtonVisibility();
        showEmptyCartMessage();
    }

    @FXML
    void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Your cart is empty", "Please add items to your cart before proceeding to checkout.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/addCommande.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("addCommande.fxml resource not found at /fxml/front/addCommande.fxml");
            }
            Parent root = loader.load();

            AddCommandeController controller = loader.getController();
            controller.setCartItems(cartItems);

            Scene scene = new Scene(root);
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Checkout");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to checkout", e.getMessage());
        }
    }

    @FXML
    void continueShopping() {
        navigateTo("/fxml/front/showProduit.fxml");
    }

    // Navigation methods from the first file
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
        try {
            // Get the current stage
            Stage stage = (Stage) anchorPane.getScene().getWindow();

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showCartItem.fxml"));
            Parent root = loader.load();

            // Create the new scene with screen dimensions
            javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to cart page", e.getMessage());
        }
    }
    @FXML
    void navigateToCommandes() {
        navigateTo("/fxml/front/ShowCommande.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            // Get the current stage
            Stage stage = (Stage) anchorPane.getScene().getWindow();

            // Use SceneManager to load the scene with proper screen dimensions
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Create the new scene with screen dimensions
            javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
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
}