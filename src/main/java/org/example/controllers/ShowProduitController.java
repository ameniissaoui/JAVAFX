package org.example.controllers;

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
import org.example.models.Produit;
import org.example.services.ProduitServices;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize the product service
            ps = new ProduitServices();
            refreshProductsList();
            setupListViewCellFactory();

            // Load add button icon
            try {
                ImageView addIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/plus.png")));
                addIcon.setFitHeight(16);
                addIcon.setFitWidth(16);
                addButton.setGraphic(addIcon);
            } catch (Exception e) {
                System.out.println("Could not load add button icon: " + e.getMessage());
            }

            // Maximize the window when shown
            listview.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) newScene.getWindow();
                        if (stage != null) {
                            stage.setMaximized(true);
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
                configureLayout();
                addActionHandlers();
            }

            private void configureLayout() {
                container.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
                container.setPrefHeight(80);

                idLabel.setPrefWidth(70);
                nameLabel.setPrefWidth(150);
                descriptionLabel.setPrefWidth(200);
                priceLabel.setPrefWidth(100);
                stockLabel.setPrefWidth(120);
                dateLabel.setPrefWidth(120);

                productImage.setFitHeight(50);
                productImage.setFitWidth(50);
                productImage.setPreserveRatio(true);
                imageUrlLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6c757d;");
                imageUrlLabel.setWrapText(true);
                imageUrlLabel.setMaxWidth(100);
                imageContainer.getChildren().addAll(productImage, imageUrlLabel);
                imageContainer.setPrefWidth(120);

                configureActionButtons();
                actionsContainer.getChildren().addAll(viewButton, editButton, deleteButton);
                actionsContainer.setPrefWidth(120);

                container.getChildren().addAll(
                        idLabel, nameLabel, descriptionLabel, priceLabel,
                        stockLabel, dateLabel, imageContainer, actionsContainer
                );
            }

            private void configureActionButtons() {
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
            }

            private void addActionHandlers() {
                viewButton.setOnAction(e -> handleView(getItem()));
                editButton.setOnAction(e -> handleEdit(getItem()));
                deleteButton.setOnAction(e -> handleDelete(getItem()));
            }

            @Override
            protected void updateItem(Produit product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                } else {
                    idLabel.setText(String.valueOf(product.getId()));
                    nameLabel.setText(product.getNom());
                    descriptionLabel.setText(product.getDescription());
                    priceLabel.setText(String.format("$%.2f", product.getPrix()));

                    int stockQuantity = product.getStock_quantite();
                    stockLabel.setText(String.valueOf(stockQuantity));
                    if (stockQuantity == 0) {
                        stockLabel.setStyle("-fx-text-fill: #f44336;");
                    } else if (stockQuantity <= 5) {
                        stockLabel.setStyle("-fx-text-fill: #ff9800;");
                    } else {
                        stockLabel.setStyle("-fx-text-fill: #4caf50;");
                    }

                    dateLabel.setText(product.getDate().toString());
                    loadImage(product);

                    setGraphic(container);
                }
            }

            private void loadImage(Produit product) {
                try {
                    String imagePath = "/images/" + product.getImage();
                    InputStream imageStream = getClass().getResourceAsStream(imagePath);
                    if (imageStream != null) {
                        Image img = new Image(imageStream);
                        productImage.setImage(img);
                        imageUrlLabel.setText(product.getImage());
                    } else {
                        setPlaceholderImage();
                        imageUrlLabel.setText("Not found");
                    }
                } catch (Exception e) {
                    setPlaceholderImage();
                    imageUrlLabel.setText("Error");
                    e.printStackTrace();
                }
            }

            private void setPlaceholderImage() {
                try {
                    productImage.setImage(new Image(getClass().getResourceAsStream("/images/product-placeholder.png")));
                } catch (Exception ex) {
                    productImage.setImage(null);
                    productImage.setStyle("-fx-background-color: #e9ecef;");
                }
            }
        });
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

            Stage stage = (Stage) listview.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
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
                refreshProductsList();
                showAlert(Alert.AlertType.INFORMATION, "Product Deleted", "The product has been successfully deleted.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error", "Could not delete the product. Error: " + e.getMessage());
            }
        }
    }

    private void refreshProductsList() {
        try {
            List<Produit> produits = ps.showProduit();
            ObservableList<Produit> observableList = FXCollections.observableArrayList(produits);
            listview.setItems(observableList);
        } catch (Exception e) {
            e.printStackTrace();
            listview.setItems(FXCollections.observableArrayList(new ArrayList<>()));
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load products from database.");
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
            Stage stage = (Stage) listview.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load add product form.");
        }
    }
}