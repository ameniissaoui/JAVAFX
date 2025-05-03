package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.models.Produit;
import org.example.services.ProduitServices;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ShowProduitController implements Initializable {
    @FXML
    private FlowPane flowPane;
    @FXML
    private Button addButton;
    @FXML private Button buttoncommande;
    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button tablesButton;
    @FXML private Button eventButton;
    @FXML private Button acceuil;
    @FXML private TextField searchField;

    @FXML
    private ProduitServices ps;
    private List<Produit> allProducts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttoncommande.setOnAction(event -> handleCommandeRedirect());
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        acceuil.setOnAction(event -> handleAcceuilRedirect());

        try {
            ps = new ProduitServices();
            refreshProductsList(); // Initial load

            // Load add button icon
            try {
                ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/plus.png")));
                addIcon.setFitHeight(16);
                addIcon.setFitWidth(16);
                addButton.setGraphic(addIcon);
            } catch (Exception e) {
                System.out.println("Could not load add button icon: " + e.getMessage());
            }

            // Maximize the window and adjust FlowPane size
            flowPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) newScene.getWindow();
                        if (stage != null) {
                            stage.setMaximized(true);
                            // Traverse up to find the VBox: FlowPane -> ScrollPane -> VBox
                            Node scrollPane = flowPane.getParent();
                            Node parent = scrollPane.getParent();
                            if (parent instanceof ScrollPane) {
                                Node vboxParent = parent.getParent();
                                if (vboxParent instanceof VBox) {
                                    VBox vbox = (VBox) vboxParent;
                                    flowPane.prefHeightProperty().bind(vbox.heightProperty().subtract(130));
                                    flowPane.prefWidthProperty().bind(vbox.widthProperty());
                                }
                            }
                        }
                    });
                }
            });

            // Add search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterProducts(newValue);
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to the database. Please ensure the database server is running.");
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAcceuilRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) acceuil.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshProductsList() {
        try {
            allProducts = ps.showProduit();
            updateProductDisplay(allProducts);
            searchField.clear(); // Clear search field on refresh
        } catch (Exception e) {
            e.printStackTrace();
            flowPane.getChildren().clear();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load products from database.");
        }
    }

    private void updateProductDisplay(List<Produit> products) {
        flowPane.getChildren().clear();
        for (Produit product : products) {
            flowPane.getChildren().add(createProductCard(product));
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateProductDisplay(allProducts);
        } else {
            List<Produit> filteredProducts = allProducts.stream()
                    .filter(product -> product.getNom().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
            updateProductDisplay(filteredProducts);
        }
    }

    private Node createProductCard(Produit product) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-radius: 5; -fx-padding: 15;");
        card.setPrefWidth(300);
        card.setPrefHeight(400);

        // Image
        ImageView productImage = new ImageView();
        productImage.setFitHeight(150);
        productImage.setFitWidth(150);
        productImage.setPreserveRatio(true);
        Label imageUrlLabel = new Label();
        imageUrlLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6c757d;");
        imageUrlLabel.setWrapText(true);
        loadImage(product, productImage, imageUrlLabel);

        // Product Details
        Label nameLabel = new Label(product.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Label descriptionLabel = new Label("Description: " + product.getDescription());
        descriptionLabel.setWrapText(true);

        Label priceLabel = new Label("Price: $" + String.format("%.2f", product.getPrix()));

        Label stockLabel = new Label("Stock: " + product.getStock_quantite());
        int stockQuantity = product.getStock_quantite();
        if (stockQuantity == 0) {
            stockLabel.setStyle("-fx-text-fill: #f44336;");
        } else if (stockQuantity <= 5) {
            stockLabel.setStyle("-fx-text-fill: #ff9800;");
        } else {
            stockLabel.setStyle("-fx-text-fill: #4caf50;");
        }

        Label dateLabel = new Label("Date: " + product.getDate().toString());

        // Spacer to push actions to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actionsContainer = new HBox(10);
        Button viewButton = new Button();
        Button editButton = new Button();
        Button deleteButton = new Button();

        try {
            ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
            viewIcon.setFitHeight(16);
            viewIcon.setFitWidth(16);
            viewButton.setGraphic(viewIcon);
            viewButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");

            ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
            editIcon.setFitHeight(16);
            editIcon.setFitWidth(16);
            editButton.setGraphic(editIcon);
            editButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");

            ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
            deleteIcon.setFitHeight(16);
            deleteIcon.setFitWidth(16);
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        } catch (Exception e) {
            viewButton.setText("👁");
            editButton.setText("✏");
            deleteButton.setText("🗑");
        }

        viewButton.setOnAction(e -> handleView(product));
        editButton.setOnAction(e -> handleEdit(product));
        deleteButton.setOnAction(e -> handleDelete(product));
        actionsContainer.getChildren().addAll(viewButton, editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(
                productImage,
                imageUrlLabel,
                nameLabel,
                descriptionLabel,
                priceLabel,
                stockLabel,
                dateLabel,
                spacer,
                actionsContainer
        );

        return card;
    }

    private void loadImage(Produit product, ImageView productImage, Label imageUrlLabel) {
        try {
            String imagePath = "/images/" + product.getImage();
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                Image img = new Image(imageStream);
                productImage.setImage(img);
                imageUrlLabel.setText(product.getImage());
            } else {
                setPlaceholderImage(productImage);
                imageUrlLabel.setText("Not found");
            }
        } catch (Exception e) {
            setPlaceholderImage(productImage);
            imageUrlLabel.setText("Error");
            e.printStackTrace();
        }
    }

    private void handleView(Produit product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Product Details");
        alert.setHeaderText(product.getNom());

        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        String imageInfo;
        try {
            String imagePath = "/images/" + product.getImage();
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                Image img = new Image(imageStream);
                imageView.setImage(img);
                imageInfo = String.format("Image File: %s\nDimensions: %.0fx%.0f pixels",
                        product.getImage(), img.getWidth(), img.getHeight());
            } else {
                setPlaceholderImage(imageView);
                imageInfo = "Image file not found: " + product.getImage();
            }
        } catch (Exception e) {
            setPlaceholderImage(imageView);
            imageInfo = "Error loading image: " + e.getMessage();
        }

        String content = String.format(
                "ID: %d\nDescription: %s\nPrice: $%.2f\nStock Quantity: %d\nDate Added: %s\n%s",
                product.getId(), product.getDescription(), product.getPrix(),
                product.getStock_quantite(), product.getDate(), imageInfo
        );
        alert.setContentText(content);
        alert.setGraphic(imageView);
        alert.showAndWait();
    }

    private void handleEdit(Produit product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/back/editProduit.fxml"));
            Parent root = loader.load();
            EditProduitController controller = loader.getController();
            controller.initData(product);

            Stage stage = (Stage) flowPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load edit product form.");
        }
    }

    private void handleDelete(Produit product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Product");
        confirmAlert.setContentText("Are you sure you want to delete the product: " + product.getNom() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ps.removeProduit(product);
                allProducts = ps.showProduit();
                filterProducts(searchField.getText());
                showAlert(Alert.AlertType.INFORMATION, "Product Deleted", "The product has been successfully deleted.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error", "Could not delete the product. Error: " + e.getMessage());
            }
        }
    }

    private void setPlaceholderImage(ImageView imageView) {
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
        } catch (Exception ex) {
            imageView.setImage(null);
            imageView.setStyle("-fx-background-color: #e9ecef;");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

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
            // Proceed without icon if it cannot be loaded
        }

        alert.showAndWait();
    }

    @FXML
    private void navigateToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/back/addProduit.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) flowPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load add product form.");
        }
    }
}