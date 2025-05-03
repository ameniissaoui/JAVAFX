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

public class ShowCommandeFrontController implements Initializable {

    @FXML
    private ListView<Commande> listview;

    @FXML
    private Button addButton;

    private CommandeServices cs = new CommandeServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                    listview.prefHeightProperty().bind(parent.heightProperty().subtract(130));
                    listview.prefWidthProperty().bind(parent.widthProperty().subtract(60));
                });
            }
        });
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

                // Action buttons with clear styling
                try {
                    // View button
                    ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                    viewIcon.setFitHeight(16);
                    viewIcon.setFitWidth(16);
                    viewButton.setGraphic(viewIcon);
                    viewButton.setStyle(
                            "-fx-background-color: #2196f3; " +
                                    "-fx-background-radius: 4; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-text-fill: white;"
                    );
                    viewButton.setTooltip(new Tooltip("View Details"));

                    // Edit button
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
                    editIcon.setFitHeight(16);
                    editIcon.setFitWidth(16);
                    editButton.setGraphic(editIcon);
                    editButton.setStyle(
                            "-fx-background-color: #ff9800; " +
                                    "-fx-background-radius: 4; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-text-fill: white;"
                    );
                    editButton.setTooltip(new Tooltip("Edit Order"));

                    // Delete button
                    ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
                    deleteIcon.setFitHeight(16);
                    deleteIcon.setFitWidth(16);
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.setStyle(
                            "-fx-background-color: #f44336; " +
                                    "-fx-background-radius: 4; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-text-fill: white;"
                    );
                    deleteButton.setTooltip(new Tooltip("Delete Order"));
                } catch (Exception e) {
                    // Fallback if icons not found
                    viewButton.setText("👁");
                    viewButton.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 4; -fx-text-fill: #2196f3; -fx-cursor: hand; -fx-padding: 5 10;");

                    editButton.setText("✏");
                    editButton.setStyle("-fx-background-color: #fff8e1; -fx-background-radius: 4; -fx-text-fill: #ffc107; -fx-cursor: hand; -fx-padding: 5 10;");

                    deleteButton.setText("🗑");
                    deleteButton.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 4; -fx-text-fill: #f44336; -fx-cursor: hand; -fx-padding: 5 10;");
                }

                // Add buttons to container with proper spacing
                actionsContainer.getChildren().addAll(viewButton, editButton, deleteButton);
                actionsContainer.setPrefWidth(150);
                actionsContainer.setStyle("-fx-padding: 10; -fx-alignment: center;");

                // Add all components to the main container
                container.getChildren().addAll(
                        idLabel,
                        nomLabel,
                        prenomLabel,
                        emailLabel,
                        adresseLabel,
                        phoneLabel,
                        actionsContainer
                );

                // Add action handlers
                viewButton.setOnAction(e -> {
                    Commande commande = getItem();
                    if (commande != null) {
                        showCommandeDetails(commande);
                    }
                });

                editButton.setOnAction(e -> {
                    Commande commande = getItem();
                    if (commande != null) {
                        navigateToEdit(commande);
                    }

                });

                deleteButton.setOnAction(e -> {
                    Commande commande = getItem();
                    if (commande != null) {
                        confirmDelete(commande);
                    }
                });
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

    private void confirmDelete(Commande commande) {
        // Your existing confirmDelete method - unchanged
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
                    "The order has been successfully deleted.");
        }
    }

    @FXML
    private void navigateToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/addCommande.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) listview.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load add order form: " + e.getMessage());
        }
    }

    private void navigateToEdit(Commande commande) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/editCommande.fxml"));
            Parent root = loader.load();

            // Get the controller
            EditCommandeController controller = loader.getController();

            // Create a new Stage (closes the current one)
            Stage currentStage = (Stage) listview.getScene().getWindow();

            // Create new scene
            Scene scene = new Scene(root);

            // Apply the scene to the current stage
            currentStage.setScene(scene);

            // Set window state
            currentStage.setMaximized(true);

            // Initialize the controller with data
            controller.initData(commande);

            // Force layout recalculation
            root.applyCss();
            root.layout();

            // Show the stage after everything is set up
            currentStage.show();

            // Add an additional call to maximize the window after showing
            Platform.runLater(() -> {
                currentStage.setMaximized(true);
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load edit order form: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) listview.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not log out");
        }
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
            URL fxmlLocation = getClass().getResource("/fxml/front/showProduit.fxml");
            if (fxmlLocation == null) {
                // Try alternative paths if the primary one fails
                fxmlLocation = getClass().getResource("/fxml/front/showProduit.fxml");
                if (fxmlLocation == null) {
                    fxmlLocation = getClass().getResource("/fxml/front/showProduit.fxml");
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