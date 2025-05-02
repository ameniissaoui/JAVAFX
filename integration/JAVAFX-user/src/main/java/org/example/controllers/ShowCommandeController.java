package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import org.example.models.Commande;
import org.example.services.CommandeServices;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ShowCommandeController implements Initializable {

    @FXML
    private VBox cardsContainer;

    @FXML
    private Button addButton;
    @FXML private Button buttoncommande;
    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button tablesButton;
    @FXML private Button eventButton;
    @FXML private Button acceuil;
    private CommandeServices cs = new CommandeServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttoncommande.setOnAction(event -> handleCommandeRedirect());
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        acceuil.setOnAction(event -> handleAcceuilRedirect());

        // Set up the add button
        try {
            ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/plus.png")));
            addIcon.setFitHeight(16);
            addIcon.setFitWidth(16);
            addButton.setGraphic(addIcon);
        } catch (Exception e) {
            // Fallback to text-only button if icon can't be loaded
            System.out.println("Could not load add button icon: " + e.getMessage());
        }

        // Wrap cardsContainer in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true); // Make content fit the width of the ScrollPane
        scrollPane.setFitToHeight(false); // Allow vertical scrolling
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Show vertical scrollbar when needed
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable horizontal scrollbar
        scrollPane.setStyle("-fx-background-color: transparent;"); // Make ScrollPane background transparent

        // Ensure the ScrollPane fills the parent container
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // If cardsContainer is inside another parent (e.g., a BorderPane or another VBox),
        // replace cardsContainer with scrollPane in the layout
        if (cardsContainer.getParent() instanceof Pane) {
            Pane parent = (Pane) cardsContainer.getParent();
            int index = parent.getChildren().indexOf(cardsContainer);
            parent.getChildren().remove(cardsContainer);
            parent.getChildren().add(index, scrollPane);
        }

        // Load the orders
        refreshCommandesList();
    }

    private void refreshCommandesList() {
        // Clear existing cards
        cardsContainer.getChildren().clear();

        // Get all orders
        List<Commande> commandes = cs.showProduit();

        // Create a FlowPane to hold all cards with automatic wrapping
        FlowPane cardsFlow = new FlowPane();
        cardsFlow.setPrefWidth(cardsContainer.getPrefWidth());
        cardsFlow.setHgap(20);
        cardsFlow.setVgap(20);
        cardsFlow.setPadding(new Insets(10));

        // Add each order as a card
        for (Commande commande : commandes) {
            VBox card = createCommandeCard(commande);
            cardsFlow.getChildren().add(card);
        }

        // Add the FlowPane to the VBox
        cardsContainer.getChildren().add(cardsFlow);

        // Ensure the cardsContainer has enough height to trigger scrolling if needed
        cardsContainer.setMinHeight(Region.USE_COMPUTED_SIZE);
        cardsContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        cardsContainer.setMaxHeight(Double.MAX_VALUE);
    }

    private VBox createCommandeCard(Commande commande) {
        // Card container
        VBox cardContainer = new VBox();
        cardContainer.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        cardContainer.setPadding(new Insets(15));
        cardContainer.setSpacing(15);
        cardContainer.setPrefWidth(350);
        cardContainer.setMaxWidth(350);

        // Card header with order ID and status
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(15);

        Label orderIdLabel = new Label("Commande #" + commande.getId());
        orderIdLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        orderIdLabel.setStyle("-fx-text-fill: #3b82f6;");

        Label statusLabel = new Label();
        statusLabel.setPadding(new Insets(3, 10, 3, 10));

        // Set status based on payment status
        String paymentStatus = commande.getPaymentStatus() != null ? commande.getPaymentStatus() : "paid";
        if (paymentStatus.equals("paid")) {
            statusLabel.setText("Payée");
            statusLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; " +
                    "-fx-background-radius: 20; -fx-font-size: 12; -fx-font-weight: bold;");
        } else if (paymentStatus.equals("pending")) {
            statusLabel.setText("En attente");
            statusLabel.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                    "-fx-background-radius: 20; -fx-font-size: 12; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("Non payée");
            statusLabel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; " +
                    "-fx-background-radius: 20; -fx-font-size: 12; -fx-font-weight: bold;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(orderIdLabel, statusLabel, spacer);

        // Customer info section
        HBox customerInfoBox = new HBox(20);
        customerInfoBox.setAlignment(Pos.CENTER_LEFT);

        // Customer icon
        Region userIcon = new Region();
        userIcon.setPrefSize(40, 40);
        userIcon.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 20; " +
                "-fx-shape: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z';");

        // Customer details
        VBox customerDetailsBox = new VBox(5);
        Label nameLabel = new Label(commande.getNom() + " " + commande.getPrenom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label emailLabel = new Label(commande.getEmail());
        emailLabel.setStyle("-fx-text-fill: #64748b;");

        Label phoneLabel = new Label("Tél: " + commande.getPhone_number());
        phoneLabel.setStyle("-fx-text-fill: #64748b;");

        customerDetailsBox.getChildren().addAll(nameLabel, emailLabel, phoneLabel);
        customerInfoBox.getChildren().addAll(userIcon, customerDetailsBox);

        // Address section
        VBox addressBox = new VBox(5);
        Label addressTitleLabel = new Label("Adresse de livraison");
        addressTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        addressTitleLabel.setStyle("-fx-text-fill: #475569;");

        Label addressLabel = new Label(commande.getAdresse());
        addressLabel.setStyle("-fx-text-fill: #64748b; -fx-wrap-text: true;");
        addressLabel.setMaxWidth(300);

        addressBox.getChildren().addAll(addressTitleLabel, addressLabel);

        // Add a separator
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e2e8f0;");

        // Content box with customer info and address
        VBox contentBox = new VBox(15);
        contentBox.getChildren().addAll(customerInfoBox, new Separator(), addressBox);

        // Footer with action buttons
        HBox footerBox = new HBox(10);
        footerBox.setAlignment(Pos.CENTER_RIGHT);



        // Add button icons
        try {

        } catch (Exception e) {
            System.out.println("Could not load button icons: " + e.getMessage());
        }





        // Add all sections to the card
        cardContainer.getChildren().addAll(headerBox, contentBox, footerBox);

        return cardContainer;
    }

    private void editCommande(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/back/EditCommande.fxml"));
            Parent root = loader.load();

            EditCommandeController controller = loader.getController();
            controller.initData(commande);

            Stage stage = new Stage();
            stage.setTitle("Modifier la commande");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the list after editing
            refreshCommandesList();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteCommande(Commande commande) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette commande ?");
        confirmDialog.setContentText("Cette action ne peut pas être annulée.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cs.removeProduit(commande);
                refreshCommandesList();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande supprimée avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleCommandeRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/back/showCommande.fxml"));
            Stage stage = (Stage) buttoncommande.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSuiviRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_suivi_back.fxml"));
            Stage stage = (Stage) suivi.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleHistoriqueRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_historique_back.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleeventRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/listevent.fxml"));
            Stage stage = (Stage) eventButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTablesRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/back/showProduit.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAcceuilRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Stage stage = (Stage) acceuil.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCommandeDetails(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Commande");
        alert.setHeaderText("Commande #" + commande.getId());

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.getStyleClass().add("custom-alert");

        // Create content
        String content = String.format(
                "Informations Client:\n\n" +
                        "Nom: %s %s\n" +
                        "Email: %s\n" +
                        "Adresse: %s\n" +
                        "Téléphone: %d",
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
            Stage parentStage = (Stage) cardsContainer.getScene().getWindow();
            alertStage.setX(parentStage.getX() + (parentStage.getWidth() - alertStage.getWidth()) / 2);
            alertStage.setY(parentStage.getY() + (parentStage.getHeight() - alertStage.getHeight()) / 2);
        });

        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        // Set icon based on alert type
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
    void show(ActionEvent event) {
        try {
            // Load the FXML file
            URL fxmlLocation = getClass().getResource("/front/showProduit.fxml");
            if (fxmlLocation == null) {
                // Try alternative paths if the primary one fails
                fxmlLocation = getClass().getResource("/front/showProduit.fxml");
                if (fxmlLocation == null) {
                    fxmlLocation = getClass().getResource("showProduit.fxml");
                    if (fxmlLocation == null) {
                        throw new IllegalStateException("FXML file not found!");
                    }
                }
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Get the current stage (window)
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Create new scene and set it to the current stage
            Scene scene = new Scene(root);
            currentStage.setScene(scene);

            // Multiple approaches to ensure maximization
            currentStage.setMaximized(true);
            currentStage.setFullScreen(false); // Reset any fullscreen state first

            // Set explicit size to screen size before maximizing
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            currentStage.setX(screenBounds.getMinX());
            currentStage.setY(screenBounds.getMinY());
            currentStage.setWidth(screenBounds.getWidth());
            currentStage.setHeight(screenBounds.getHeight());

            // Ensure the stage is visible
            currentStage.show();

            // Force maximization multiple times using Platform.runLater
            Platform.runLater(() -> {
                currentStage.setMaximized(true);

                // Second attempt after a short delay
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                delay.setOnFinished(e -> {
                    currentStage.setMaximized(true);

                    // Third attempt after layout is complete
                    Platform.runLater(() -> {
                        currentStage.setMaximized(true);
                    });
                });
                delay.play();
            });

            // If the controller implements Initializable, get access to it
            ShowCommandeFrontController controller = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
            // Create and show an error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to display orders");
            alert.setContentText("Error: " + e.getMessage() + "\n\nStack trace has been printed to console.");
            alert.showAndWait();
        }
    }
}