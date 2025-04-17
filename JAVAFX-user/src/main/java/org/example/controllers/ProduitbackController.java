package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.models.Produit;
import org.example.services.ProduitServices;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProduitbackController {

    @FXML
    private ListView<Produit> listview;

    @FXML
    private TextField idField;

    @FXML
    private TextField nom;

    @FXML
    private TextField description;

    @FXML
    private TextField prix;

    @FXML
    private TextField stock_quantite;

    @FXML
    private DatePicker date;

    @FXML
    private TextField image;

    @FXML
    private Button browseImageBtn;

    @FXML
    private AnchorPane mainContent;

    private ProduitServices ps = new ProduitServices();

    // Define the image directory path - update this to your application's actual images folder
    private final String IMAGE_DIR = "src/resources/images/";

    @FXML
    void initialize() {
        refreshProductsList();
        setupListViewCellFactory();

        // Add selection listener to ListView
        listview.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Fill form fields with selected product data
                idField.setText(String.valueOf(newSelection.getId()));
                nom.setText(newSelection.getNom());
                description.setText(newSelection.getDescription());
                prix.setText(String.format("%.2f", newSelection.getPrix()));
                stock_quantite.setText(String.valueOf(newSelection.getStock_quantite()));

                if (newSelection.getDate() != null) {
                    date.setValue(newSelection.getDate().toLocalDate());
                } else {
                    date.setValue(null);
                }

                image.setText(newSelection.getImage());
            }
        });

        // Make sure the image directory exists
        File directory = new File(IMAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @FXML
    void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");

        // Set image extension filters
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show file chooser dialog
        Stage stage = (Stage) mainContent.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Generate a unique filename to avoid conflicts
                String uniqueFileName = UUID.randomUUID().toString() +
                        selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));

                // Copy the file to the application's image directory
                Path sourcePath = selectedFile.toPath();
                Path targetPath = Paths.get(IMAGE_DIR + uniqueFileName);

                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Update the image text field with the relative path
                image.setText(uniqueFileName);

                showAlert(Alert.AlertType.INFORMATION, "Image Import",
                        "Image Successfully Imported", "The image has been imported and linked to the product.");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Image Import Error",
                        "Failed to import image", "Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void setupListViewCellFactory() {
        listview.setCellFactory(lv -> new ListCell<Produit>() {
            private final HBox container = new HBox(5);
            private final Label idLabel = new Label();
            private final Label nameLabel = new Label();
            private final Label descLabel = new Label();
            private final Label priceLabel = new Label();
            private final Label stockLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label imageLabel = new Label();
            private final Button actionsBtn = new Button("•••");

            {
                // Configure layout
                container.setPadding(new Insets(5));

                // Set up widths to match column headers
                idLabel.setPrefWidth(50);
                nameLabel.setPrefWidth(120);
                descLabel.setPrefWidth(150);
                priceLabel.setPrefWidth(80);
                stockLabel.setPrefWidth(80);
                dateLabel.setPrefWidth(100);
                imageLabel.setPrefWidth(100);
                actionsBtn.setPrefWidth(80);

                // Styling for the actions button
                actionsBtn.setStyle("-fx-background-color: #1969B3; -fx-text-fill: white;");

                // Layout components
                container.getChildren().addAll(
                        idLabel, nameLabel, descLabel, priceLabel,
                        stockLabel, dateLabel, imageLabel, actionsBtn
                );

                // Add click handler for the actions button
                actionsBtn.setOnAction(e -> {
                    Produit product = getItem();
                    if (product != null) {
                        showContextMenu(product, actionsBtn);
                    }
                });
            }

            private void showContextMenu(Produit product, Button button) {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem editItem = new MenuItem("Edit");
                MenuItem deleteItem = new MenuItem("Delete");
                MenuItem viewImageItem = new MenuItem("View Image");

                editItem.setOnAction(e -> {
                    listview.getSelectionModel().select(product);
                });

                deleteItem.setOnAction(e -> {
                    confirmDelete(product);
                });

                viewImageItem.setOnAction(e -> {
                    if (product.getImage() != null && !product.getImage().isEmpty()) {
                        showProductImage(product);
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "No Image",
                                "No Image Available", "This product doesn't have an image.");
                    }
                });

                contextMenu.getItems().addAll(editItem, deleteItem, viewImageItem);
                contextMenu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
            }

            private void showProductImage(Produit product) {
                try {
                    // Create a new stage for the image
                    Stage imageStage = new Stage();
                    imageStage.setTitle("Product Image: " + product.getNom());

                    // Create image view
                    javafx.scene.image.Image img = new javafx.scene.image.Image(
                            new File(IMAGE_DIR + product.getImage()).toURI().toString());
                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(img);

                    // Set reasonable size constraints
                    imageView.setFitHeight(400);
                    imageView.setFitWidth(600);
                    imageView.setPreserveRatio(true);

                    // Create a scrollable pane in case the image is large
                    ScrollPane scrollPane = new ScrollPane(imageView);
                    scrollPane.setPannable(true);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(true);

                    // Show the stage
                    Scene scene = new Scene(scrollPane, 600, 400);
                    imageStage.setScene(scene);
                    imageStage.show();

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Image Error",
                            "Cannot Display Image", "Error loading image: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            protected void updateItem(Produit product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null) {
                    setGraphic(null);
                } else {
                    // Format the date
                    String dateStr = "";
                    if (product.getDate() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        dateStr = sdf.format(product.getDate());
                    }

                    // Set data values
                    idLabel.setText(String.valueOf(product.getId()));
                    nameLabel.setText(product.getNom());
                    descLabel.setText(product.getDescription());
                    priceLabel.setText(String.format("%.2f", product.getPrix()));
                    stockLabel.setText(String.valueOf(product.getStock_quantite()));
                    dateLabel.setText(dateStr);
                    imageLabel.setText(product.getImage() != null ? product.getImage().substring(0, Math.min(product.getImage().length(), 10)) + "..." : "");

                    // Alternating row colors for better readability
                    if (getIndex() % 2 == 0) {
                        container.setStyle("-fx-background-color: white;");
                    } else {
                        container.setStyle("-fx-background-color: #f8f8f8;");
                    }

                    setGraphic(container);
                }
            }
        });
    }

    // Refresh the ListView with the latest data from the database
    private void refreshProductsList() {
        List<Produit> produits = ps.showProduit();
        ObservableList<Produit> observableList = FXCollections.observableArrayList(produits);
        listview.setItems(observableList);
    }

    @FXML
    void clearFields() {
        idField.clear();
        nom.clear();
        description.clear();
        prix.clear();
        stock_quantite.clear();
        date.setValue(null);
        image.clear();
        listview.getSelectionModel().clearSelection();
    }

    @FXML
    void save() {
        try {
            // Validate input
            if (nom.getText().isEmpty() || description.getText().isEmpty() ||
                    prix.getText().isEmpty() || stock_quantite.getText().isEmpty() ||
                    date.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Information",
                        "Please fill all required fields", "All fields except image are required.");
                return;
            }

            // Convert values - FIX: Handle both comma and period as decimal separators
            float prixValue;
            try {
                // Normalize the input by replacing comma with period
                String normalizedPrice = prix.getText().replace(',', '.');
                prixValue = Float.parseFloat(normalizedPrice);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Invalid price format", "Please enter a valid number for price (e.g., 12.99 or 12,99).");
                return;
            }

            int stockValue;
            try {
                stockValue = Integer.parseInt(stock_quantite.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Invalid stock format", "Please enter a valid integer for stock quantity.");
                return;
            }

            Date sqlDate = Date.valueOf(date.getValue());

            // Create product object
            Produit produit = new Produit(nom.getText(), description.getText(),
                    prixValue, stockValue, sqlDate, image.getText());

            // Add to database
            ps.addProduit(produit);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Product Added", "The product has been successfully added.");

            // Refresh list and clear fields
            refreshProductsList();
            clearFields();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to add product", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void edit() {
        Produit selectedProduct = listview.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Product Selected", "Please select a product to edit.");
            return;
        }

        try {
            // Validate input
            if (nom.getText().isEmpty() || description.getText().isEmpty() ||
                    prix.getText().isEmpty() || stock_quantite.getText().isEmpty() ||
                    date.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Information",
                        "Please fill all required fields", "All fields except image are required.");
                return;
            }

            // Convert values - FIX: Handle both comma and period as decimal separators
            float prixValue;
            try {
                // Normalize the input by replacing comma with period
                String normalizedPrice = prix.getText().replace(',', '.');
                prixValue = Float.parseFloat(normalizedPrice);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Invalid price format", "Please enter a valid number for price (e.g., 12.99 or 12,99).");
                return;
            }

            int stockValue;
            try {
                stockValue = Integer.parseInt(stock_quantite.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Invalid stock format", "Please enter a valid integer for stock quantity.");
                return;
            }

            Date sqlDate = Date.valueOf(date.getValue());

            // Update product object
            selectedProduct.setNom(nom.getText());
            selectedProduct.setDescription(description.getText());
            selectedProduct.setPrix(prixValue);
            selectedProduct.setStock_quantite(stockValue);
            selectedProduct.setDate(sqlDate);
            selectedProduct.setImage(image.getText());

            // Update in database
            ps.editProduit(selectedProduct);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Product Updated", "The product has been successfully updated.");

            // Refresh list and clear fields
            refreshProductsList();
            clearFields();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to update product", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void remove() {
        Produit selectedProduct = listview.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Product Selected", "Please select a product to remove.");
            return;
        }

        confirmDelete(selectedProduct);
    }

    private void confirmDelete(Produit product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Product");
        confirmAlert.setContentText("Are you sure you want to delete the product: " +
                product.getNom() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the product
            ps.removeProduit(product);

            // Optionally delete the image file if it exists
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                try {
                    File imageFile = new File(IMAGE_DIR + product.getImage());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } catch (Exception e) {
                    // Just log the error, don't block the deletion
                    System.err.println("Failed to delete image file: " + e.getMessage());
                }
            }

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Product Deleted", "The product has been successfully deleted.");

            // Refresh list and clear fields
            refreshProductsList();
            clearFields();
        }
    }

    @FXML
    void showHome() {
        // Navigate to home screen
        // Replace with your actual home screen navigation code
        showAlert(Alert.AlertType.INFORMATION, "Navigation",
                "Home Screen", "Navigating to Home Screen (not implemented).");
    }

    @FXML
    void showProducts() {
        // Already on products screen, just refresh
        refreshProductsList();
    }

    @FXML
    void logout() {
        // Navigate to login screen
        // Replace with your actual logout code
        showAlert(Alert.AlertType.INFORMATION, "Logout",
                "Logging Out", "Logging out (not implemented).");
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}