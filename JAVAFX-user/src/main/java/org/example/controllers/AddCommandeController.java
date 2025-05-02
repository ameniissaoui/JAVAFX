package org.example.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
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
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.models.Commande;
import org.example.models.Produit;
import org.example.models.CartItem;
import org.example.models.User;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.CommandeServices;
import org.example.services.CartItemServices;
import org.example.services.ProduitServices;
import org.example.services.SmsGenerator;
import org.example.util.ConfigLoader;
import org.example.util.SessionManager;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

public class AddCommandeController {

    @FXML
    private TextField nomField;

    @FXML
    private Label nomError;

    @FXML
    private TextField prenomField;

    @FXML
    private Label prenomError;

    @FXML
    private TextField emailField;

    @FXML
    private Label emailError;

    @FXML
    private TextField phoneField;

    @FXML
    private Label phoneError;

    @FXML
    private TextField adresseField;

    @FXML
    private Label adresseError;

    @FXML
    private Button submitButton;

    @FXML
    private Button backButton;

    @FXML
    private ListView<CartItem> cartItemsListView1;

    @FXML
    private Label cartCountLabel1;

    @FXML
    private Label cartCountLabel;

    private CommandeServices commandeServices;
    private CartItemServices cartItemServices;
    private ProduitServices produitServices;
    private SmsGenerator smsGenerator;
    private Produit selectedProduct;
    private List<CartItem> cartItems;
    private final ObservableList<CartItem> observableCartItems = FXCollections.observableArrayList();
    private HttpServer server;
    private static final String STRIPE_API_KEY = "sk_test_51Q8guRRqOXMp4o1HGPTVEZpvcogn1v8SxvtYbNT2ugbc5vYyG4d8wVGULXizAQe6xQnhlgeBQRg4KFRMi9kAvXLF00OyawA2RF";
    private static final int SERVER_PORT = 8090 ;
    private Commande currentCommande;

    @FXML
    void initialize() {
        commandeServices = new CommandeServices();
        cartItemServices = new CartItemServices();
        produitServices = new ProduitServices();

        smsGenerator = new SmsGenerator(
                ConfigLoader.getProperty("twilio.accountSid"),
                ConfigLoader.getProperty("twilio.authToken"),
                ConfigLoader.getProperty("twilio.fromNumber")
        );

        Stripe.apiKey = STRIPE_API_KEY;
        System.out.println("AddCommandeController initialized");

        populateUserFields();

        submitButton.setOnAction(event -> handleSubmit());
        backButton.setOnAction(event -> navigateBack());

        setupCartItemsListView();

        updateCartCountLabel();

        startLocalServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
            System.out.println("Shutdown hook executed: Server stopped");
        }));

        Platform.runLater(() -> {
            Stage stage;
            if (nomField != null && nomField.getScene() != null && nomField.getScene().getWindow() != null) {
                stage = (Stage) nomField.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    stop();
                    System.out.println("Window close event: Server stopped");
                });

                maximizeStage(stage);
            } else {
                System.err.println("Could not set close request handler: nomField or its scene/window is null during initialize");
            }
        });
    }

    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }

    private void setupCartItemsListView() {
        cartItemsListView1.setItems(observableCartItems);
        cartItemsListView1.setCellFactory(param -> new ListCell<CartItem>() {
            private final HBox hbox = new HBox();
            private final Label productLabel = new Label();
            private final ImageView imageView = new ImageView();
            private final Label unitPriceLabel = new Label();
            private final Label quantityLabel = new Label();
            private final Label totalPriceLabel = new Label();

            {
                hbox.setSpacing(20);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPadding(new Insets(10));
                hbox.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

                productLabel.setPrefWidth(150);
                productLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                productLabel.setWrapText(true);

                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-border-radius: 5; -fx-background-radius: 5;");

                unitPriceLabel.setPrefWidth(100);
                unitPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

                quantityLabel.setPrefWidth(80);
                quantityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

                totalPriceLabel.setPrefWidth(100);
                totalPriceLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

                hbox.getChildren().addAll(productLabel, imageView, unitPriceLabel, quantityLabel, totalPriceLabel);
            }

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    productLabel.setText(item.getProduit().getNom());
                    unitPriceLabel.setText(String.format("%.2f TND", item.getProduit().getPrix()));
                    quantityLabel.setText("Qty: " + item.getQuantite());
                    totalPriceLabel.setText(String.format("%.2f TND", item.getProduit().getPrix() * item.getQuantite()));

                    try {
                        String imagePath = "/images/" + item.getProduit().getImage().split(",")[0];
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        imageView.setImage(image);
                    } catch (Exception e) {
                        imageView.setImage(new Image("/images/product-placeholder.png"));
                    }

                    setGraphic(hbox);
                }
            }
        });

        if (cartItems != null && !cartItems.isEmpty()) {
            observableCartItems.setAll(cartItems);
            updateCartCountLabel();
        }
    }

    private void updateCartCountLabel() {
        if (cartCountLabel1 != null) {
            int count = observableCartItems.size();
            cartCountLabel1.setText(String.valueOf(count));
        }

        if (cartCountLabel != null) {
            int count = observableCartItems.size();
            cartCountLabel.setText(String.valueOf(count));
        }
    }

    private void populateUserFields() {
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();

        if (currentUser instanceof User) {
            User user = (User) currentUser;
            nomField.setText(user.getNom() != null ? user.getNom() : "");
            prenomField.setText(user.getPrenom() != null ? user.getPrenom() : "");
            emailField.setText(user.getEmail() != null ? user.getEmail() : "");
            phoneField.setText(user.getTelephone() != null ? user.getTelephone() : "");
            System.out.println("Form fields populated with user info: " + user.toString());
        } else {
            System.err.println("No user logged in or invalid user type in SessionManager");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non authentifié", "Veuillez vous connecter pour continuer.");
        }
    }

    private void startLocalServer() {
        try {
            if (!isPortAvailable(SERVER_PORT)) {
                System.err.println("Port " + SERVER_PORT + " is already in use");
                showAlert(Alert.AlertType.ERROR, "Port Error", "Port in use",
                        "Port " + SERVER_PORT + " is already in use. Please free the port or choose another.");
                return;
            }

            server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
            server.createContext("/success", new SuccessHandler());
            server.createContext("/cancel", new CancelHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Local server started on port " + SERVER_PORT);
        } catch (IOException e) {
            System.err.println("Error starting local server: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Server Error", "Failed to start local server", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private class SuccessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Success handler called");

            String query = exchange.getRequestURI().getQuery();
            String sessionId = null;
            if (query != null && query.contains("session_id=")) {
                sessionId = query.split("session_id=")[1];
                System.out.println("Session ID from callback: " + sessionId);

                if (currentCommande != null && sessionId != null) {
                    try {
                        currentCommande.setPaymentStatus("paid");
                        commandeServices.editProduit(currentCommande);
                        System.out.println("Updated payment status to paid for commande: " + currentCommande.getId());

                        for (CartItem item : cartItems) {
                            Produit produit = produitServices.getoneProduit(item.getProduitId());
                            if (produit != null) {
                                int purchasedQuantity = item.getQuantite();
                                int newStock = produit.getStock_quantite() - purchasedQuantity;
                                produit.setStock_quantite(Math.max(0, newStock));
                                produitServices.editProduit(produit);
                                System.out.println("Updated stock for product ID " + produit.getId() + " to " + produit.getStock_quantite() + " (Purchased: " + purchasedQuantity + ")");
                            } else {
                                System.err.println("Produit not found for cart item: produitId=" + item.getProduitId());
                            }
                        }

                        smsGenerator.sendOrderConfirmation(currentCommande, cartItems);
                        System.out.println("SMS confirmation sent for commande: " + currentCommande.getId());

                        for (CartItem item : cartItems) {
                            cartItemServices.removeProduit(item);
                            System.out.println("Removed cart item from database: produitId=" + item.getProduitId());
                        }
                        cartItems.clear();
                        observableCartItems.clear();
                        updateCartCountLabel();

                        System.out.println("Payment successful. Stock updated, SMS sent, and cart items removed from database.");

                    } catch (Exception e) {
                        System.err.println("Error updating payment status, stock, sending SMS, or removing cart items: " + e.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update payment status, stock, send SMS, or remove cart items", e.getMessage());
                    }
                } else {
                    System.out.println("currentCommande or sessionId is null: currentCommande=" + currentCommande + ", sessionId=" + sessionId);
                }
            } else {
                System.out.println("Query or session_id missing in callback: " + query);
            }

            String response = "<html><body><h1>Payment successful!</h1><p>You can close this window and return to the application.</p>"
                    + "<script>window.close();</script></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

            navigateToSuccessPage();
        }

        private void navigateToSuccessPage() {
            Platform.runLater(() -> {
                try {
                    System.out.println("Attempting to navigate to success.fxml");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/success.fxml"));
                    if (loader.getLocation() == null) {
                        throw new IOException("success.fxml resource not found at /fxml/front/success.fxml");
                    }
                    Parent root = loader.load();
                    Scene scene = new Scene(root);

                    Stage stage;
                    if (nomField != null && nomField.getScene() != null && nomField.getScene().getWindow() != null) {
                        stage = (Stage) nomField.getScene().getWindow();
                        System.out.println("Retrieved existing stage from nomField for success.fxml");
                    } else {
                        stage = new Stage();
                        System.out.println("Created new stage for success.fxml as nomField was not accessible");
                    }

                    stage.setScene(scene);
                    stage.setTitle("Payment Success");
                    stage.show();
                    stage.toFront();
                    maximizeStage(stage);
                    System.out.println("Successfully navigated to success.fxml");

                } catch (IOException e) {
                    System.err.println("Error loading success.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load success.fxml", e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("Unexpected error during navigation to success.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred", e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    private class CancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Cancel handler called");

            String response = "<html><body><h1>Payment cancelled</h1><p>You can close this window and return to the application.</p>"
                    + "<script>window.close();</script></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

            Platform.runLater(() -> {
                try {
                    System.out.println("Attempting to navigate to cancel.fxml");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/cancel.fxml"));
                    if (loader.getLocation() == null) {
                        throw new IOException("cancel.fxml resource not found at /fxml/front/cancel.fxml");
                    }
                    Parent root = loader.load();
                    Scene scene = new Scene(root);

                    Stage stage;
                    if (nomField != null && nomField.getScene() != null && nomField.getScene().getWindow() != null) {
                        stage = (Stage) nomField.getScene().getWindow();
                        System.out.println("Retrieved existing stage from nomField for cancel.fxml");
                    } else {
                        stage = new Stage();
                        System.out.println("Created new stage for cancel.fxml as nomField was not accessible");
                    }

                    stage.setScene(scene);
                    stage.setTitle("Payment Cancelled");
                    stage.show();
                    stage.toFront();
                    maximizeStage(stage);
                    System.out.println("Successfully navigated to cancel.fxml");
                } catch (IOException e) {
                    System.err.println("Error loading cancel.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load cancel.fxml", e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("Unexpected error during navigation to cancel.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred", e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    public void setSelectedProduct(Produit produit) {
        this.selectedProduct = produit;
        System.out.println("setSelectedProduct called with product: " + (produit != null ? "ID=" + produit.getId() + ", Name=" + produit.getNom() : "null"));
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        observableCartItems.setAll(cartItems);
        updateCartCountLabel();
        System.out.println("setCartItems called with " + (cartItems != null ? cartItems.size() : 0) + " items");
    }

    @FXML
    void handleSubmit() {
        System.out.println("Handle submit called");
        System.out.println("Current selectedProduct: " + (selectedProduct != null ? "ID=" + selectedProduct.getId() + ", Name=" + selectedProduct.getNom() : "null"));
        System.out.println("Current cartItems: " + (cartItems != null ? cartItems.size() : 0) + " items");

        if (server == null) {
            showAlert(Alert.AlertType.ERROR, "Server Error", "Server not running",
                    "Cannot process payment: Local server is not running.");
            return;
        }

        if (!validateForm()) {
            System.out.println("Form validation failed");
            return;
        }

        try {
            SessionManager session = SessionManager.getInstance();
            Object currentUser = session.getCurrentUser();
            int utilisateurId;

            if (currentUser instanceof User) {
                utilisateurId = ((User) currentUser).getId();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non authentifié ou type non valide", "");
                return;
            }

            Commande commande = new Commande();
            commande.setNom(nomField.getText());
            commande.setPrenom(prenomField.getText());
            commande.setEmail(emailField.getText());
            commande.setPhone_number(Integer.parseInt(phoneField.getText()));
            commande.setAdresse(adresseField.getText());
            commande.setUtilisateurId(utilisateurId);

            double totalAmount = calculateTotalAmount();
            commande.setTotalAmount(totalAmount);
            commande.setPaymentStatus("pending");

            if (selectedProduct != null) {
                commande.setProduit(selectedProduct);
                System.out.println("Adding commande for product: " + selectedProduct.getNom());
            }

            commandeServices.addProduit(commande);
            System.out.println("Commande added to database with ID: " + commande.getId());

            this.currentCommande = commande;

            if (cartItems != null && !cartItems.isEmpty()) {
                for (CartItem item : cartItems) {
                    item.setCommandeId(commande.getId());
                    cartItemServices.editProduit(item);
                }
                System.out.println("Updated commande_id for cart items to: " + commande.getId());
            }

            String checkoutUrl = createStripeCheckoutSession(commande);

            String sessionId = "";
            if (checkoutUrl.contains("cs_")) {
                sessionId = "cs_" + checkoutUrl.split("cs_")[1].split("[/?]")[0];
                commande.setStripeSessionId(sessionId);
                commandeServices.editProduit(commande);
                System.out.println("Updated commande with Stripe session ID: " + sessionId);
            }

            clearForm();

            try {
                Desktop.getDesktop().browse(new URI(checkoutUrl));
                System.out.println("Opening Stripe checkout URL: " + checkoutUrl);
                showAlert(Alert.AlertType.INFORMATION, "Payment Processing",
                        "Redirecting to Stripe",
                        "Please complete your payment in the browser. The application will update automatically when payment is completed.");
            } catch (URISyntaxException e) {
                System.err.println("Invalid URL format: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "URL Error", "Error opening payment page", e.getMessage());
            }

        } catch (StripeException e) {
            System.err.println("Stripe error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Payment Error", "Error initiating payment", "Stripe error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Browser Error", "Error opening payment page", e.getMessage());
        } catch (Exception e) {
            System.err.println("Error adding commande: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error adding commande", e.getMessage());
        }
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        phoneField.clear();
        adresseField.clear();
        nomError.setText("");
        prenomError.setText("");
        emailError.setText("");
        phoneError.setText("");
        adresseError.setText("");
    }

    @FXML
    void show() {
        navigateTo("/fxml/front/showCommande.fxml");
    }

    @FXML
    void navigateBack() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    private double calculateTotalAmount() {
        double total = 0.0;
        if (selectedProduct != null) {
            total = selectedProduct.getPrix();
        } else if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem item : cartItems) {
                total += item.getProduit().getPrix() * item.getQuantite();
            }
        }
        return total;
    }

    private String createStripeCheckoutSession(Commande commande) throws StripeException {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(commande.getEmail())
                .setSuccessUrl("http://localhost:" + SERVER_PORT + "/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:" + SERVER_PORT + "/cancel");

        if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem item : cartItems) {
                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("eur")
                                        .setUnitAmount((long) (item.getProduit().getPrix() * 100))
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(item.getProduit().getNom())
                                                        .build()
                                        )
                                        .build()
                        )
                        .setQuantity((long) item.getQuantite())
                        .build();
                paramsBuilder.addLineItem(lineItem);
            }
        } else if (selectedProduct != null) {
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount((long) (selectedProduct.getPrix() * 100))
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(selectedProduct.getNom())
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity(1L)
                    .build();
            paramsBuilder.addLineItem(lineItem);
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }

    private boolean validateForm() {
        return validateNom() && validatePrenom() && validateEmail() && validatePhone() && validateAdresse();
    }

    private boolean validateNom() {
        String nom = nomField.getText();
        if (nom.isEmpty()) {
            nomError.setText("Nom is required");
            return false;
        }
        nomError.setText("");
        return true;
    }

    private boolean validatePrenom() {
        String prenom = prenomField.getText();
        if (prenom.isEmpty()) {
            prenomError.setText("Prénom is required");
            return false;
        }
        prenomError.setText("");
        return true;
    }

    private boolean validateEmail() {
        String email = emailField.getText();
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (email.isEmpty()) {
            emailError.setText("Email is required");
            return false;
        } else if (!Pattern.matches(emailRegex, email)) {
            emailError.setText("Invalid email format");
            return false;
        }
        emailError.setText("");
        return true;
    }

    private boolean validatePhone() {
        String phone = phoneField.getText();
        if (phone.isEmpty()) {
            phoneError.setText("Phone number is required");
            return false;
        } else if (!phone.matches("\\d+")) {
            phoneError.setText("Phone number must contain only digits");
            return false;
        }
        phoneError.setText("");
        return true;
    }

    private boolean validateAdresse() {
        String adresse = adresseField.getText();
        if (adresse.isEmpty()) {
            adresseError.setText("Adresse is required");
            return false;
        }
        adresseError.setText("");
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Local server stopped");
        }
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

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
            maximizeStage(stage);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page", e.getMessage());
        }
    }
}
