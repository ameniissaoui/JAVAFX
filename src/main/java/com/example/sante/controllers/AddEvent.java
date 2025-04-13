package com.example.sante.controllers;


import com.example.sante.Entity.Event;
import com.example.sante.service.EventService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;


public class AddEvent {


    @FXML
    private Button ajoutEvent;

    @FXML
    private DatePicker dateEventField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField imageField;

    @FXML
    private TextField lieuField;

    @FXML
    private TextField nbPlaceField;

    @FXML
    private TextField titreField;
    @FXML
    private ImageView imageView;
    // Dossier où les images seront copiées
    private static final String IMAGES_FOLDER = "src/main/resources/com/example/sante/images/";
    private final EventService eventService = new EventService();
    private Event selectedEvent;  // Événement sélectionné pour la mise à jour

    @FXML
    void ajoutevenement(ActionEvent event) throws IOException {
        // Validation des champs
        if (!validateFields()) {
            return;
        }
        String titre = titreField.getText();
        String dateevent = (dateEventField.getValue() != null) ? dateEventField.getValue().toString() : "";
        String lieu = lieuField.getText();
        String discription = descriptionField.getText();
        int nbplace = 0;

        // Vérifier que nbPlaceField contient bien un entier
        try {
            nbplace = Integer.parseInt(nbPlaceField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Erreur: Nombre de places invalide");
            return; // Sortie de la méthode en cas d'erreur
        }

        String image = imageField.getText();

        // Vérifier si l'image a bien été sélectionnée
        if (image.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Veuillez sélectionner une image.");
            alert.show();
            return;
        }
        // Création d'un nouvel événement
        Event newEvent = new Event(titre, dateevent, lieu, discription, nbplace, image);

        // Ajout de l'événement à la base de données (supposons une instance de service existante)
        EventService service = new EventService();
        service.ajouter(newEvent);

        System.out.println("Événement ajouté avec succès !");
        returnToDisplayScreen();
    }

    public void loadEvent(Event event) {
        this.selectedEvent = event;

        titreField.setText(event.getTitre());

        // Remplir le DatePicker avec la date de l'événement
        if (event.getDateevent() != null && !event.getDateevent().isEmpty()) {
            LocalDate date = LocalDate.parse(event.getDateevent());
            dateEventField.setValue(date);
        }

        lieuField.setText(event.getLieu());
        descriptionField.setText(event.getDiscription());
        nbPlaceField.setText(String.valueOf(event.getNbplace()));
        imageField.setText(event.getImage());
    }
    @FXML
    public void updateEvent() {
        // Validation des champs avant mise à jour
        if (!validateFields()) {
            return;
        }
        // Récupérer les données modifiées depuis les champs de texte
        String titre = titreField.getText();

        // Récupérer la date depuis le DatePicker
        LocalDate localDate = dateEventField.getValue();
        String dateevent = (localDate != null) ? localDate.toString() : "";  // Convertir LocalDate en String

        String lieu = lieuField.getText();
        String description = descriptionField.getText();
        int nbplace = Integer.parseInt(nbPlaceField.getText());
        String image = imageField.getText();

        // Créer un objet Event mis à jour
        Event updatedEvent = new Event(selectedEvent.getId(), titre, dateevent, lieu, description, nbplace, image);

        // Mettre à jour l'événement dans la base de données
        eventService.update(updatedEvent);

        // Afficher une alerte pour confirmer la mise à jour
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("L'événement a été mis à jour avec succès.");
        alert.show();

        // Retourner à l'écran d'affichage
        returnToDisplayScreen();
    }
    private boolean validateFields() {
        // Vérification que tous les champs sont remplis
        if (titreField.getText().isEmpty() || descriptionField.getText().isEmpty() || lieuField.getText().isEmpty() || nbPlaceField.getText().isEmpty() || imageField.getText().isEmpty()) {
            showAlert("Tous les champs doivent être remplis.");
            return false;
        }

        // Validation du nombre de places
        try {
            int nbPlace = Integer.parseInt(nbPlaceField.getText());
            if (nbPlace <= 0) {
                showAlert("Le nombre de places doit être un nombre positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Le nombre de places doit être un nombre valide.");
            return false;
        }

        // Validation de la date de l'événement (ne pas permettre une date dans le passé)
        LocalDate dateEvent = dateEventField.getValue();
        if (dateEvent != null && dateEvent.isBefore(LocalDate.now())) {
            showAlert("La date de l'événement ne peut pas être dans le passé.");
            return false;
        }

        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }

    private void returnToDisplayScreen() {
        // Charger la scène d'affichage avec les événements mis à jour
        try {
            // Charger le fichier FXML de l'écran d'affichage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sante/listevent.fxml"));
            Parent root = loader.load();  // Charger la scène avec le fichier FXML

            // Récupérer le contrôleur de la scène d'affichage
            ListEvent eventController = loader.getController();

            // Rafraîchir les événements dans la table de la scène d'affichage
            eventController.refreshEvents();

            // Obtenir le stage de la fenêtre actuelle
            Stage stage = (Stage) titreField.getScene().getWindow();  // Obtenir le stage actuel à partir du titreField
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);  // Mettre à jour la scène
            stage.show();  // Afficher la nouvelle scène

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors du retour à l'écran d'affichage.");
            alert.show();
        }
    }


    @FXML
    public void chooseImage(ActionEvent event) {
        // Ouvrir une boîte de dialogue pour sélectionner un fichier image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        // Récupérer la fenêtre actuelle pour ouvrir le file chooser
        Window stage = imageView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        // Si un fichier est sélectionné, traiter l'image
        if (selectedFile != null) {
            try {
                // Créer le dossier images s'il n'existe pas encore
                Files.createDirectories(Paths.get(IMAGES_FOLDER));

                // Créer un chemin de destination pour l'image dans le dossier local
                String imageName = selectedFile.getName();  // Utiliser le nom du fichier uniquement
                String destinationPath = IMAGES_FOLDER + imageName;  // Utiliser uniquement le nom pour la base de données

                // Copier l'image dans le dossier local
                File destinationFile = new File(destinationPath);
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Mettre à jour le TextField et l'ImageView avec le nom du fichier
                imageField.setText(imageName);  // Stocker uniquement le nom du fichier
                Image image = new Image("file:" + destinationFile.getAbsolutePath());
                imageView.setImage(image);  // Afficher l'aperçu dans l'ImageView

            } catch (IOException e) {
                // En cas d'erreur lors de la copie de l'image
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Erreur lors de la sauvegarde de l'image.");
                alert.show();
                e.printStackTrace();
            }
        } else {
            // Alerte si aucun fichier n'est sélectionné
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Aucune image sélectionnée.");
            alert.show();
        }
    }

}
