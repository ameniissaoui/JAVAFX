package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
    private ListView<Commande> listview;

    @FXML
    private Button addButton;
    @FXML private Button buttoncommande;
    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button tablesButton; // Add this field
    @FXML private Button eventButton; // Add this field
    @FXML private Button acceuil; // Add this field
    private CommandeServices cs = new CommandeServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttoncommande.setOnAction(event -> handleCommandeRedirect());
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        buttoncommande.setOnAction(event -> handleCommandeRedirect());
        acceuil.setOnAction(event -> handleAcceuilRedirect());
        // Set up the list view first
        refreshCommandesList();
        setupListViewCellFactory();

        // Add button image if not already set in FXML
        try {
            ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/plus.png")));
            addIcon.setFitHeight(16);
            addIcon.setFitWidth(16);
            addButton.setGraphic(addIcon);
        } catch (Exception e) {
            // Fallback to text-only button if icon can't be loaded
            System.out.println("Could not load add button icon: " + e.getMessage());
        }

        // Add a listener to handle stage setup once the scene is available
        listview.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    Stage stage = (Stage) listview.getScene().getWindow();
                    stage.setMaximized(true);

                    // Bind listview size to anchorpane
                    AnchorPane parent = (AnchorPane) listview.getParent();
                    listview.prefHeightProperty().bind(parent.heightProperty().subtract(180)); // Adjust for header height
                    listview.prefWidthProperty().bind(parent.widthProperty().subtract(60));
                });
            }
        });
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
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
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
            showErrorDialog("Erreur", "Impossible de charger la page d'acceuil': " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupListViewCellFactory() {
        // Your existing cell factory code - unchanged
        listview.setCellFactory(lv -> new ListCell<Commande>() {
            private final HBox container = new HBox();
            private final Label idLabel = new Label();
            private final Label nomLabel = new Label();
            private final Label prenomLabel = new Label();
            private final Label emailLabel = new Label();
            private final Label adresseLabel = new Label();
            private final Label phoneLabel = new Label();

            private final Button viewButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox actionsContainer = new HBox(10);

            {
                // Configure layout and styling
                container.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                container.setPrefHeight(60);

                // Configure each column with proper alignment
                idLabel.setPrefWidth(70);
                idLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");

                nomLabel.setPrefWidth(120);
                nomLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");
                nomLabel.setMaxWidth(120);
                nomLabel.setWrapText(true);

                prenomLabel.setPrefWidth(120);
                prenomLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");
                prenomLabel.setMaxWidth(120);
                prenomLabel.setWrapText(true);

                emailLabel.setPrefWidth(180);
                emailLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");
                emailLabel.setMaxWidth(180);
                emailLabel.setWrapText(true);

                adresseLabel.setPrefWidth(200);
                adresseLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");
                adresseLabel.setMaxWidth(200);
                adresseLabel.setWrapText(true);

                phoneLabel.setPrefWidth(150);
                phoneLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");



                // Add buttons to container with proper spacing
                actionsContainer.setPrefWidth(150);
                actionsContainer.setStyle("-fx-padding: 10; -fx-alignment: center;");

                // Add all components to the main container
                container.getChildren().addAll(
                        idLabel,
                        nomLabel,
                        prenomLabel,
                        emailLabel,
                        adresseLabel,
                        phoneLabel
                );


            }

            @Override
            protected void updateItem(Commande commande, boolean empty) {
                super.updateItem(commande, empty);

                if (empty || commande == null) {
                    setGraphic(null);
                } else {
                    // Set values
                    idLabel.setText(String.valueOf(commande.getId()));
                    nomLabel.setText(commande.getNom());
                    prenomLabel.setText(commande.getPrenom());
                    emailLabel.setText(commande.getEmail());
                    adresseLabel.setText(commande.getAdresse());
                    phoneLabel.setText(String.valueOf(commande.getPhone_number()));

                    // Alternating row colors
                    if (getIndex() % 2 == 0) {
                        container.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                    } else {
                        container.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                    }

                    setGraphic(container);
                }
            }
        });
    }

    private void showCommandeDetails(Commande commande) {
        // Your existing showCommandeDetails method - unchanged
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
            Stage parentStage = (Stage) listview.getScene().getWindow();
            alertStage.setX(parentStage.getX() + (parentStage.getWidth() - alertStage.getWidth()) / 2);
            alertStage.setY(parentStage.getY() + (parentStage.getHeight() - alertStage.getHeight()) / 2);
        });

        alert.showAndWait();
    }

    private void refreshCommandesList() {
        List<Commande> commandes = cs.showProduit();
        ObservableList<Commande> observableList = FXCollections.observableArrayList(commandes);
        listview.setItems(observableList);
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