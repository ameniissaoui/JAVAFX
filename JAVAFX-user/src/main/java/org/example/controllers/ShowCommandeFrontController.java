package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.models.Commande;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.CommandeServices;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ShowCommandeFrontController implements Initializable {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private FlowPane cardContainer;

    @FXML
    private Button addButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Label cartCountLabel1;

    @FXML
    private AnchorPane anchorPane;

    private CommandeServices cs = new CommandeServices();
    private int currentUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user ID from session
        if (SessionManager.getInstance().getCurrentUser() != null) {
            // Extract user ID from the current user object based on user type
            Object currentUser = SessionManager.getInstance().getCurrentUser();
            String userType = SessionManager.getInstance().getUserType();

            if (userType.equals("medecin")) {
                currentUserId = ((Medecin) currentUser).getId();
            } else if (userType.equals("patient")) {
                currentUserId = ((Patient) currentUser).getId();
            } else {
                // Handle unknown user type
                showAlert(Alert.AlertType.WARNING, "User Error", "Unknown user type");
                Platform.runLater(this::navigateToLogin);
                return;
            }
        } else {
            // Handle the case when no user is logged in
            showAlert(Alert.AlertType.WARNING, "Authentication Required", "Please log in to view your orders");
            Platform.runLater(this::navigateToLogin);
            return;
        }

        // Set up the filter options
        if (filterComboBox != null) {
            filterComboBox.getItems().addAll("All Orders", "Recent Orders", "Oldest Orders");
            filterComboBox.setValue("All Orders");
            filterComboBox.setOnAction(event -> refreshCommandesList());
        }

        // Configure search field
        if (searchField != null) {
            searchField.setPromptText("Search by name or email...");
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                refreshCommandesList();
            });
        }

        // Initialize cart count
        if (cartCountLabel1 != null) {
            cartCountLabel1.setText("0");
        }

        // Set up the card container and load data
        refreshCommandesList();

        // Configure ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Add button image if not already set in FXML
        if (addButton != null) {
            try {
                ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/plus.png")));
                addIcon.setFitHeight(16);
                addIcon.setFitWidth(16);
                addButton.setGraphic(addIcon);
            } catch (Exception e) {
                System.out.println("Could not load add button icon: " + e.getMessage());
                addButton.setText("+");
            }
        }

        // Add a listener to handle stage setup once the scene is available
        if (cardContainer != null) {
            cardContainer.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) cardContainer.getScene().getWindow();
                        stage.setMaximized(true);

                        // Bind scrollPane size to anchorpane
                        AnchorPane parent = (AnchorPane) scrollPane.getParent();
                        scrollPane.prefHeightProperty().bind(parent.heightProperty().subtract(160));
                        scrollPane.prefWidthProperty().bind(parent.widthProperty().subtract(60));
                    });
                }
            });
        }
    }

    private void refreshCommandesList() {
        // Get all commandes from database
        List<Commande> allCommandes = cs.showProduit();

        // Filter by current user ID
        List<Commande> userCommandes = allCommandes.stream()
                .filter(commande -> commande.getUtilisateurId() == currentUserId)
                .collect(Collectors.toList());

        cardContainer.getChildren().clear();

        // Filter by search text if available
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";

        // Apply sorting based on filter selection
        if (filterComboBox != null && !"All Orders".equals(filterComboBox.getValue())) {
            if ("Recent Orders".equals(filterComboBox.getValue())) {
                // Sort by ID descending (assuming higher ID means more recent)
                userCommandes.sort((c1, c2) -> Integer.compare(c2.getId(), c1.getId()));
            } else if ("Oldest Orders".equals(filterComboBox.getValue())) {
                // Sort by ID ascending
                userCommandes.sort((c1, c2) -> Integer.compare(c1.getId(), c2.getId()));
            }
        }

        // Apply filtering and create cards
        for (Commande commande : userCommandes) {
            // Skip if doesn't match search
            if (!searchText.isEmpty() &&
                    !commande.getNom().toLowerCase().contains(searchText) &&
                    !commande.getPrenom().toLowerCase().contains(searchText) &&
                    !commande.getEmail().toLowerCase().contains(searchText)) {
                continue;
            }

            cardContainer.getChildren().add(createCommandeCard(commande));
        }

        // Show a message if no results
        if (cardContainer.getChildren().isEmpty()) {
            Label noResults = new Label("No orders found matching your criteria");
            noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            cardContainer.getChildren().add(noResults);
        }
    }

    private VBox createCommandeCard(Commande commande) {
        // Card container
        VBox card = new VBox();
        card.setPrefWidth(350);
        card.setPrefHeight(220);
        card.setMaxWidth(350);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e9ecef; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        // Header - ID and Status
        HBox header = new HBox();
        header.setSpacing(10);

        // ID Label
        Label idLabel = new Label("Order #" + commande.getId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #30b4b4;");

        // Status indicator
        Region statusIndicator = new Region();
        statusIndicator.setPrefSize(12, 12);
        statusIndicator.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 6;");

        Label statusLabel = new Label("Completed");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");

        HBox statusBox = new HBox(5, statusIndicator, statusLabel);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Add spacer to push status to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(idLabel, spacer, statusBox);

        // Customer name
        Label nameLabel = new Label(commande.getNom() + " " + commande.getPrenom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 5 0 0 0;");

        // Contact info
        VBox contactInfo = new VBox(4);

        Label emailLabel = new Label(commande.getEmail());
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label phoneLabel = new Label("📞 " + commande.getPhone_number());
        phoneLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label addressLabel = new Label("📍 " + commande.getAdresse());
        addressLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        addressLabel.setWrapText(true);

        contactInfo.getChildren().addAll(emailLabel, phoneLabel, addressLabel);

        // Add all elements to the card
        card.getChildren().addAll(header, nameLabel, contactInfo);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #30b4b4; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 15; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #e9ecef; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 15; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
            );
        });

        return card;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        try {
            String iconPath = "";
            if (alertType == Alert.AlertType.INFORMATION) {
                iconPath = "/images/info.png";
            } else if (alertType == Alert.AlertType.WARNING) {
                iconPath = "/images/warning.png";
            } else if (alertType == Alert.AlertType.ERROR) {
                iconPath = "/images/error.png";
            }

            if (!iconPath.isEmpty()) {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                ImageView iconView = new ImageView(icon);
                iconView.setFitWidth(48);
                iconView.setFitHeight(48);
                alert.setGraphic(iconView);
            }
        } catch (Exception e) {
            // If icon can't be loaded, proceed without it
        }

        alert.showAndWait();
    }

    @FXML
    private void navigateToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/addCommande.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load add order form: " + e.getMessage());
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not navigate to login page: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            // Clear session
            SessionManager.getInstance().clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not log out");
        }
    }

    @FXML
    void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to home page");
        }
    }

    @FXML
    void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/profile.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to profile page");
        }
    }

    @FXML
    void navigateToFavorites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/favorites.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to favorites page");
        }
    }

    @FXML
    void navigateToCommandes() {
        // We're already on this page, so do nothing or refresh
        refreshCommandesList();
    }

    @FXML
    void commande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/cart.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to cart page");
        }
    }

    @FXML
    void navigateToShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to shop page");
        }
    }

    @FXML
    void show(ActionEvent event) {
        navigateToShop();
    }

    @FXML
    void navigateToHistoriques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/historiques.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to historiques page");
        }
    }

    @FXML
    void redirectToDemande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/demande.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to demande page");
        }
    }

    @FXML
    void redirectToRendezVous() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/rendezVous.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to rendez-vous page");
        }
    }

    @FXML
    void redirectProduit() {
        navigateToShop();
    }

    @FXML
    void navigateToTraitement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/traitement.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to traitement page");
        }
    }

    @FXML
    void viewDoctors() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/doctors.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to doctors page");
        }
    }

    @FXML
    void navigateToContact() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/contact.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to contact page");
        }
    }
}