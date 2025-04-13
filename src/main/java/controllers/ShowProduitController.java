package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Produit;
import services.ProduitServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ShowProduitController implements Initializable {

    @FXML
    private ListView<Produit> listview;

    @FXML
    private Button addButton;

    private ProduitServices ps;

    // Define the image directory path - same as in AddProduitController
    private final String IMAGE_DIR = "src/resources/images/";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize the product service
            ps = new ProduitServices();

            // Set up the list view first
            refreshProductsList();
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

            // Add a listener for window showing event to maximize the window immediately
            listview.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    // Get the Stage once the scene is available
                    Platform.runLater(() -> {
                        Stage stage = (Stage) newScene.getWindow();
                        if (stage != null) {
                            // Set stage to maximized state immediately
                            stage.setMaximized(true);

                            // Make sure the listview expands to fill the parent
                            VBox parent = (VBox) listview.getParent();
                            listview.prefHeightProperty().bind(parent.heightProperty().subtract(130));
                            listview.prefWidthProperty().bind(parent.widthProperty());
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to the database. Please ensure the database server is running.");
        }
    }

    private void setupListViewCellFactory() {
        listview.setCellFactory(lv -> new ListCell<Produit>() {
            private final HBox container = new HBox();
            private final Label idLabel = new Label();
            private final Label nameLabel = new Label();
            private final Label descriptionLabel = new Label();
            private final Label priceLabel = new Label();
            private final Label stockLabel = new Label();
            private final Label dateLabel = new Label();
            private final ImageView productImage = new ImageView();
            private final Label imageUrlLabel = new Label();
            private final VBox imageContainer = new VBox(5);

            private final Button viewButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox actionsContainer = new HBox(10);

            {
                // Configure layout and styling
                container.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                container.setPrefHeight(80);

                // Configure each column with proper alignment
                idLabel.setPrefWidth(50);
                idLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");

                nameLabel.setPrefWidth(120);
                nameLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");
                nameLabel.setMaxWidth(120);
                nameLabel.setWrapText(true);

                descriptionLabel.setPrefWidth(160);
                descriptionLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");
                descriptionLabel.setMaxWidth(160);
                descriptionLabel.setWrapText(true);

                priceLabel.setPrefWidth(80);
                priceLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");

                stockLabel.setPrefWidth(80);
                stockLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");

                dateLabel.setPrefWidth(100);
                dateLabel.setStyle("-fx-padding: 10; -fx-font-size: 13;");

                // Image container with image and URL
                productImage.setFitHeight(50);
                productImage.setFitWidth(50);
                productImage.setPreserveRatio(true);

                imageUrlLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6c757d;");
                imageUrlLabel.setWrapText(true);
                imageUrlLabel.setMaxWidth(100);

                imageContainer.getChildren().addAll(productImage, imageUrlLabel);
                imageContainer.setPrefWidth(150);
                imageContainer.setStyle("-fx-padding: 5; -fx-alignment: center;");

                // Action buttons with clear styling
                try {
                    // View button
                    ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                    viewIcon.setFitHeight(16);
                    viewIcon.setFitWidth(16);
                    viewButton.setGraphic(viewIcon);
                    viewButton.setStyle(
                            "-fx-background-color: #2196f3; " +  // Bright blue instead of #e3f2fd
                                    "-fx-background-radius: 4; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-text-fill: white;"  // White text for better contrast
                    );
                    viewButton.setTooltip(new Tooltip("View Details"));

                    // Edit button
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
                    editIcon.setFitHeight(16);
                    editIcon.setFitWidth(16);
                    editButton.setGraphic(editIcon);
                    editButton.setStyle(
                            "-fx-background-color: #ff9800; " +  // Bright orange instead of #fff8e1
                                    "-fx-background-radius: 4; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-text-fill: white;"  // White text for better contrast
                    );
                    editButton.setTooltip(new Tooltip("Edit Product"));

                    // Delete button
                    ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
                    deleteIcon.setFitHeight(16);
                    deleteIcon.setFitWidth(16);
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.setStyle(
                            "-fx-background-color: #f44336; " +  // Bright red instead of #ffebee
                                    "-fx-background-radius: 4; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5 10; " +
                                    "-fx-text-fill: white;"  // White text for better contrast
                    );
                    deleteButton.setTooltip(new Tooltip("Delete Product"));
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
                actionsContainer.setPrefWidth(140);
                actionsContainer.setStyle("-fx-padding: 10; -fx-alignment: center;");

                // Add all components to the main container
                container.getChildren().addAll(
                        idLabel,
                        nameLabel,
                        descriptionLabel,
                        priceLabel,
                        stockLabel,
                        dateLabel,
                        imageContainer,
                        actionsContainer
                );

                // Precisely match the table header widths with the cell widths
                idLabel.setPrefWidth(70);
                nameLabel.setPrefWidth(150);
                descriptionLabel.setPrefWidth(200);
                priceLabel.setPrefWidth(100);
                stockLabel.setPrefWidth(120);
                dateLabel.setPrefWidth(120);
                imageContainer.setPrefWidth(120);
                actionsContainer.setPrefWidth(120);

                // Add action handlers
                viewButton.setOnAction(e -> {
                    Produit product = getItem();
                    if (product != null) {
                        showProductDetails(product);
                    }
                });

                editButton.setOnAction(e -> {
                    Produit product = getItem();
                    if (product != null) {
                        navigateToEdit(product);
                    }
                });

                deleteButton.setOnAction(e -> {
                    Produit product = getItem();
                    if (product != null) {
                        confirmDelete(product);
                    }
                });
            }

            @Override
            protected void updateItem(Produit product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null) {
                    setGraphic(null);
                } else {
                    // Set values
                    idLabel.setText(String.valueOf(product.getId()));
                    nameLabel.setText(product.getNom());
                    descriptionLabel.setText(product.getDescription());
                    priceLabel.setText(String.format("$%.2f", product.getPrix()));

                    // Set stock status with color coding
                    int stockQuantity = product.getStock_quantite();
                    stockLabel.setText(String.valueOf(stockQuantity));

                    if (stockQuantity == 0) {
                        stockLabel.setStyle("-fx-padding: 10; -fx-font-size: 13; -fx-text-fill: #f44336; -fx-font-weight: bold;");
                    } else if (stockQuantity <= 5) {
                        stockLabel.setStyle("-fx-padding: 10; -fx-font-size: 13; -fx-text-fill: #ff9800; -fx-font-weight: bold;");
                    } else {
                        stockLabel.setStyle("-fx-padding: 10; -fx-font-size: 13; -fx-text-fill: #4caf50; -fx-font-weight: bold;");
                    }

                    dateLabel.setText(product.getDate().toString());

                    // Make sure action buttons are always visible and enabled
                    viewButton.setVisible(true);
                    editButton.setVisible(true);
                    deleteButton.setVisible(true);

                    viewButton.setDisable(false);
                    editButton.setDisable(false);
                    deleteButton.setDisable(false);

                    // Load image from file system
                    try {
                        if (product.getImage() != null && !product.getImage().isEmpty()) {
                            // Build the full path to the image file
                            File imageFile = new File(IMAGE_DIR + product.getImage());

                            // Check if file exists
                            if (imageFile.exists()) {
                                try (InputStream is = new FileInputStream(imageFile)) {
                                    Image img = new Image(is);
                                    productImage.setImage(img);

                                    // Display image name and dimensions
                                    String dimensions = String.format("%.0fx%.0f", img.getWidth(), img.getHeight());
                                    String imageName = product.getImage();

                                    // Shorten filename for display if too long
                                    if (imageName.length() > 15) {
                                        imageName = imageName.substring(0, 12) + "...";
                                    }

                                    imageUrlLabel.setText(imageName);
                                }
                            } else {
                                setPlaceholderImage();
                                imageUrlLabel.setText("Not found");
                            }
                        } else {
                            setPlaceholderImage();
                            imageUrlLabel.setText("No image");
                        }
                    } catch (Exception e) {
                        setPlaceholderImage();
                        imageUrlLabel.setText("Error");
                        e.printStackTrace();
                    }

                    // Alternating row colors
                    if (getIndex() % 2 == 0) {
                        container.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                    } else {
                        container.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                    }

                    setGraphic(container);
                }
            }

            private void setPlaceholderImage() {
                try {
                    productImage.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
                } catch (Exception ex) {
                    // If placeholder image not found, create a blank placeholder
                    productImage.setImage(null);
                    productImage.setStyle("-fx-background-color: #e9ecef;");
                }
            }
        });
    }

    private void showProductDetails(Produit product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Product Details");
        alert.setHeaderText(product.getNom());

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.getStyleClass().add("custom-alert");

        // Create image view for larger display
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);
        String imageInfo = "";

        try {
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                // Load image from file system
                File imageFile = new File(IMAGE_DIR + product.getImage());

                if (imageFile.exists()) {
                    try (InputStream is = new FileInputStream(imageFile)) {
                        Image img = new Image(is);
                        imageView.setImage(img);

                        // Add image attributes to details
                        imageInfo = String.format(
                                "Image File: %s\n" +
                                        "Dimensions: %.0fx%.0f pixels",
                                product.getImage(),
                                img.getWidth(),
                                img.getHeight()
                        );
                    }
                } else {
                    setPlaceholderImage(imageView);
                    imageInfo = "Image file not found: " + product.getImage();
                }
            } else {
                setPlaceholderImage(imageView);
                imageInfo = "No image available";
            }
        } catch (Exception e) {
            setPlaceholderImage(imageView);
            imageInfo = "Error loading image: " + e.getMessage();
            e.printStackTrace();
        }

        String content = String.format(
                "ID: %d\n" +
                        "Description: %s\n" +
                        "Price: $%.2f\n" +
                        "Stock Quantity: %d\n" +
                        "Date Added: %s\n\n" +
                        "%s",
                product.getId(),
                product.getDescription(),
                product.getPrix(),
                product.getStock_quantite(),
                product.getDate(),
                imageInfo
        );

        alert.setContentText(content);
        alert.setGraphic(imageView);

        // Ensure alert is centered on the screen
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.setOnShown(e -> {
            Stage parentStage = (Stage) listview.getScene().getWindow();
            alertStage.setX(parentStage.getX() + (parentStage.getWidth() - alertStage.getWidth()) / 2);
            alertStage.setY(parentStage.getY() + (parentStage.getHeight() - alertStage.getHeight()) / 2);
        });

        alert.showAndWait();
    }

    private void setPlaceholderImage(ImageView imageView) {
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
        } catch (Exception ex) {
            imageView.setImage(null);
            imageView.setStyle("-fx-background-color: #e9ecef;");
        }
    }

    private void refreshProductsList() {
        try {
            List<Produit> produits = ps.showProduit();
            ObservableList<Produit> observableList = FXCollections.observableArrayList(produits);
            listview.setItems(observableList);
        } catch (Exception e) {
            // Handle database connection errors
            e.printStackTrace();
            listview.setItems(FXCollections.observableArrayList(new ArrayList<>()));

            // Show the error only once during initialization, not on every refresh
            if (!listview.getItems().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        "Could not load products from database. Please check your connection.");
            }
        }
    }

    private void confirmDelete(Produit product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Product");
        confirmAlert.setContentText("Are you sure you want to delete the product: " + product.getNom() + "?");

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
            try {
                // Delete the associated image file if it exists
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    File imageFile = new File(IMAGE_DIR + product.getImage());
                    if (imageFile.exists()) {
                        boolean deleted = imageFile.delete();
                        if (!deleted) {
                            System.out.println("Could not delete image file: " + imageFile.getAbsolutePath());
                        }
                    }
                }

                // Remove product from database
                ps.removeProduit(product);
                refreshProductsList();

                // Show success message
                showAlert(Alert.AlertType.INFORMATION,
                        "Product Deleted",
                        "The product has been successfully deleted.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Delete Error",
                        "Could not delete the product. Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void navigateToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/addProduit.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) listview.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true); // Ensure the stage remains maximized when navigating
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load add product form: " + e.getMessage());
        }
    }

    private void navigateToEdit(Produit product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/editProduit.fxml"));
            Parent root = loader.load();

            EditProduitController controller = loader.getController();
            controller.initData(product);

            Stage stage = (Stage) listview.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true); // Ensure the stage remains maximized when navigating
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load edit product form: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) listview.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            // Don't maximize the login screen - usually login screens are smaller
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

    // Added a refresh method that can be called from UI
    @FXML
    public void handleRefresh() {
        refreshProductsList();
    }
}