package org.example.controllers;

import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Screen;
import org.example.models.Admin;
import org.example.models.Commande;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.CommandeServices;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
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
                showAlert(Alert.AlertType.WARNING, "User Error", "Unknown user type", "");
                Platform.runLater(this::navigateToLogin);
                return;
            }
        } else {
            // Handle the case when no user is logged in
            showAlert(Alert.AlertType.WARNING, "Authentication Required", "Please log in to view your orders", "");
            Platform.runLater(this::navigateToLogin);
            return;
        }

        // Set up the card container with proper styling
        cardContainer = new FlowPane();
        cardContainer.setVgap(20);
        cardContainer.setHgap(20);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setPrefWrapLength(1200); // Adjust based on your window size
        scrollPane.setContent(cardContainer);

        // Configure filter options
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

        // Set up the card container and load data
        refreshCommandesList();

        // Configure ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Add button image if not already set in FXML
        try {
            ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/plus.png")));
            addIcon.setFitHeight(16);
            addIcon.setFitWidth(16);
            addButton.setGraphic(addIcon);
        } catch (Exception e) {
            System.out.println("Could not load add button icon: " + e.getMessage());
            addButton.setText("+");
        }

        // Add a listener to handle stage setup once the scene is available
        cardContainer.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    Stage stage = (Stage) cardContainer.getScene().getWindow();
                    maximizeStage(stage);

                    // Bind scrollPane size to anchorpane
                    AnchorPane parent = (AnchorPane) scrollPane.getParent();
                    scrollPane.prefHeightProperty().bind(parent.heightProperty().subtract(130));
                    scrollPane.prefWidthProperty().bind(parent.widthProperty().subtract(60));
                });
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

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        Button viewButton = createActionButton("View", "#2196f3");
        Button editButton = createActionButton("Edit", "#ff9800");
        Button deleteButton = createActionButton("Delete", "#f44336");

        // Button actions
        viewButton.setOnAction(e -> showCommandeDetails(commande));
        editButton.setOnAction(e -> navigateToEdit(commande));
        deleteButton.setOnAction(e -> confirmDelete(commande));

        actions.getChildren().addAll(viewButton, editButton, deleteButton);

        // Add all elements to the card
        card.getChildren().addAll(header, nameLabel, contactInfo, separator, actions);

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

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 15;"
        );

        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: derive(" + color + ", 10%); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 4; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 15;"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 4; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 15;"
            );
        });

        return button;
    }

    private void showCommandeDetails(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + commande.getId());

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.getStyleClass().add("custom-alert");

        // Create content
        String content = String.format(
                "Customer Information:\n\n" +
                        "Name: %s %s\n" +
                        "Email: %s\n" +
                        "Address: %s\n" +
                        "Phone Number: %d",
                commande.getNom(),
                commande.getPrenom(),
                commande.getEmail(),
                commande.getAdresse(),
                commande.getPhone_number()
        );

        alert.setContentText(content);

        // Add an icon
        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/shopping-cart.png")));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            alert.setGraphic(icon);
        } catch (Exception e) {
            // If icon can't be loaded, proceed without it
        }

        // Ensure alert is centered on the screen
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.setOnShown(e -> {
            Stage parentStage = (Stage) scrollPane.getScene().getWindow();
            alertStage.setX(parentStage.getX() + (parentStage.getWidth() - alertStage.getWidth()) / 2);
            alertStage.setY(parentStage.getY() + (parentStage.getHeight() - alertStage.getHeight()) / 2);
        });

        alert.showAndWait();
    }

    private void confirmDelete(Commande commande) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Order");
        confirmAlert.setContentText("Are you sure you want to delete the order from: " + commande.getNom() + " " + commande.getPrenom() + "?");

        // Style the confirmation dialog
        DialogPane dialogPane = confirmAlert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        // Create custom buttons
        ButtonType yesButton = new ButtonType("Yes, Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(yesButton, cancelButton);

        // Style the buttons
        Button yesBtn = (Button) dialogPane.lookupButton(yesButton);
        yesBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        Button cancelBtn = (Button) dialogPane.lookupButton(cancelButton);
        cancelBtn.setStyle("-fx-background-color: #e0e0e0;");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            // Remove commande from database
            cs.removeProduit(commande);
            refreshCommandesList();

            // Show success message
            showAlert(Alert.AlertType.INFORMATION,
                    "Order Deleted",
                    "The order has been successfully deleted.",
                    "");
        }
    }

    @FXML
    private void navigateToAdd() {
        navigateTo("/fxml/front/addCommande.fxml");
    }

    private void navigateToEdit(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/editCommande.fxml"));
            Parent root = loader.load();

            EditCommandeController controller = loader.getController();

            Stage currentStage = (Stage) scrollPane.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            maximizeStage(currentStage);

            controller.initData(commande);

            root.applyCss();
            root.layout();

            currentStage.show();

            Platform.runLater(() -> {
                maximizeStage(currentStage);
            });
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load edit order form", e.getMessage());
        }
    }

    private void navigateToLogin() {
        navigateTo("/fxml/login.fxml");
    }

    @FXML
    private void logout() {
        try {
            // Clear session
            SessionManager.getInstance().clearSession();

            navigateTo("/fxml/login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not log out", "");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

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
    void navigateToHome() {
        navigateTo("/fxml/front/home.fxml");
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
    void navigateToCommandes() {
        // We're already on this page, so do nothing or refresh
        refreshCommandesList();
    }

    @FXML
    void commande() {
        navigateTo("/fxml/front/showCartItem.fxml");
    }

    @FXML
    void navigateToShop() {
        navigateTo("/fxml/front/showProduit.fxml");
    }

    @FXML
    void show() {
        navigateToShop();
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
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page", e.getMessage());
        }
    }
}