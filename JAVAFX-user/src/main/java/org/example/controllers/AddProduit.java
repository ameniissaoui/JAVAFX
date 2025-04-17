package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.example.models.Produit;
import org.example.services.ProduitServices;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;

public class AddProduit {

    @FXML
    private DatePicker date;

    @FXML
    private TextField description;

    @FXML
    private TextField image;

    @FXML
    private TextField nom;

    @FXML
    private TextField prix;

    @FXML
    private TextField stock_quantite;

    private ProduitServices ps = new ProduitServices();
    @FXML
    void save(ActionEvent event) {
        try {
            // Convert necessary values
            float prixValue = Float.parseFloat(prix.getText());
            int stockQuantiteValue = Integer.parseInt(stock_quantite.getText());

            // Convert DatePicker's LocalDate to java.sql.Date
            LocalDate localDate = date.getValue();
            if (localDate == null) {
                System.out.println("Error: Date is not selected.");
                return;
            }
            Date sqlDate = Date.valueOf(localDate);

            // Create the product object with correct types
            Produit produit = new Produit(nom.getText(), description.getText(), prixValue, stockQuantiteValue, sqlDate, image.getText());

            // Add the product using ProduitServices
            ps.addProduit(produit);

        } catch (NumberFormatException e) {
            System.out.println("Error: Prix or Stock Quantité is not a valid number.");
        }
    }

//    @FXML
//    void afficher(ActionEvent event) {
//
//        try {
//            Parent root = FXMLLoader.load(getClass().getResource("showProduit.fxml"));
//            nom.getScene().setRoot(root);
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//
//    }

    @FXML
    private void afficher(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/front/showProduit.fxml");
            if (fxmlLocation == null) {
                System.out.println("FXML file still not found! Check resource path.");
                return;
            }
            Parent root = FXMLLoader.load(fxmlLocation);
            nom.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}


