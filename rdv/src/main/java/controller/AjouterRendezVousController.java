package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Planning;
import model.RendezVous;
import service.PlanningDAO;
import service.RendezVousDAO;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import java.time.format.TextStyle;
import java.util.Locale;

import jakarta.mail.*;
import jakarta.mail.internet.*;


import java.util.stream.Collectors;




import java.util.Properties;

public class AjouterRendezVousController implements Initializable {

    @FXML
    private Label headerLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> cmbHeure;

    @FXML
    private ComboBox<String> cmbStatut;

    @FXML
    private TextArea txtDescription;

    @FXML
    private ComboBox<Planning> cmbPlanning;

    @FXML
    private Button btnEnregistrer;

    @FXML
    private Button btnAnnuler;


    private List<Planning> allPlannings;

    private RendezVous rendezVous;
    private RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private PlanningDAO planningDAO = new PlanningDAO();
    private RendezVousViewController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupButtons();
        datePicker.valueProperty().addListener((obs, ancienneDate, nouvelleDate) -> {
            if (nouvelleDate != null) {
                String jourSemaine = nouvelleDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
                filtrerPlanningsParJour(jourSemaine);
            }
        });
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;

        if (rendezVous != null) {
            headerLabel.setText("Modifier le rendez-vous");

            // Remplir les champs avec les valeurs existantes
            if (rendezVous.getDateheure() != null) {
                datePicker.setValue(rendezVous.getDateheure().toLocalDate());
                cmbHeure.setValue(rendezVous.getDateheure().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            cmbStatut.setValue(rendezVous.getStatut());
            txtDescription.setText(rendezVous.getDescription());

            // Sélectionner le planning correspondant
            for (Planning planning : cmbPlanning.getItems()) {
                if (planning.getId() == rendezVous.getPlanning_id()) {
                    cmbPlanning.setValue(planning);
                    break;
                }
            }
        }
    }

    public void setParentController(RendezVousViewController controller) {
        this.parentController = controller;
    }

    private void setupComboBoxes() {
        // Configuration des heures
        for (int h = 8; h <= 20; h++) {
            for (int m = 0; m < 60; m += 15) { // Intervalles de 15 minutes
                cmbHeure.getItems().add(String.format("%02d:%02d", h, m));
            }
        }

        // Configuration des statuts
        cmbStatut.getItems().addAll("Confirmé", "En attente", "Annulé");
        cmbStatut.setValue("Confirmé");

        // Chargement des plannings disponibles
        try {
            allPlannings = planningDAO.getAllPlannings();
            cmbPlanning.getItems().addAll(allPlannings);

            // Configuration de l'affichage des plannings dans le ComboBox
            cmbPlanning.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Planning item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getJour() + " (" +
                                item.getHeuredebut().format(DateTimeFormatter.ofPattern("HH:mm")) +
                                " - " +
                                item.getHeurefin().format(DateTimeFormatter.ofPattern("HH:mm")) +
                                ")");
                    }
                }
            });

            cmbPlanning.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Planning item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getJour() + " (" +
                                item.getHeuredebut().format(DateTimeFormatter.ofPattern("HH:mm")) +
                                " - " +
                                item.getHeurefin().format(DateTimeFormatter.ofPattern("HH:mm")) +
                                ")");
                    }
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les plannings: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Valeurs par défaut
        datePicker.setValue(LocalDate.now());
        cmbHeure.setValue("09:00");
    }

    private void setupButtons() {
        btnEnregistrer.setOnAction(e -> saveRendezVous());
        btnAnnuler.setOnAction(e -> closeWindow());
    }

    @FXML
    private void saveRendezVous() {
        try {
            // ✅ Collecter les infos du formulaire
            LocalDate date = datePicker.getValue();
            String heure = cmbHeure.getSelectionModel().getSelectedItem();
            String statut = cmbStatut.getSelectionModel().getSelectedItem();
            String description = txtDescription.getText();
            Planning planning = cmbPlanning.getValue();

            if (date == null || heure == null || statut == null || planning == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
                return;
            }

            // ✅ Fusionner date + heure
            LocalTime time = LocalTime.parse(heure);
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            // ✅ Créer le rendez-vous
            RendezVous rendezVous = new RendezVous();
            rendezVous.setDateheure(dateTime);
            rendezVous.setStatut(statut);
            rendezVous.setDescription(description);
            rendezVous.setPlanning_id(planning.getId());
            rendezVous.setPlanning(planning); // très utile pour getPlanningInfo()

            // ✅ Enregistrer en base
            RendezVousDAO rendezVousDAO = new RendezVousDAO();
            rendezVousDAO.saveRendezVous(rendezVous);

            // ✅ ENVOYER L’EMAIL ICI
            sendEmailNotification(rendezVous);

            // ✅ Confirmer à l’utilisateur
            showAlert("Succès", "Rendez-vous ajouté avec succès", Alert.AlertType.INFORMATION);

            // ✅ Fermer la fenêtre
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // 1. Validation des champs obligatoires
        if (datePicker.getValue() == null) {
            errors.append("- La date est obligatoire\n");
            isValid = false;
        }

        if (cmbHeure.getValue() == null || cmbHeure.getValue().trim().isEmpty()) {
            errors.append("- L'heure est obligatoire\n");
            isValid = false;
        }

        if (cmbStatut.getValue() == null || cmbStatut.getValue().trim().isEmpty()) {
            errors.append("- Le statut est obligatoire\n");
            isValid = false;
        }

        if (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty()) {
            errors.append("- La description est obligatoire\n");
            isValid = false;
        }

        if (cmbPlanning.getValue() == null) {
            errors.append("- Le planning est obligatoire\n");
            isValid = false;
        }

        // Si les champs obligatoires sont vides, inutile de continuer les validations
        if (!isValid) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n" + errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        // 2. Validation de la date - pas dans le passé
        LocalDate selectedDate = datePicker.getValue();
        LocalDate today = LocalDate.now();
        if (selectedDate.isBefore(today)) {
            errors.append("- Vous ne pouvez pas créer un rendez-vous dans le passé\n");
            isValid = false;
        }

        // 3. Validation de l'heure avec le planning sélectionné
        try {
            LocalTime selectedTime = LocalTime.parse(cmbHeure.getValue());
            Planning selectedPlanning = cmbPlanning.getValue();

            if (selectedTime.isBefore(selectedPlanning.getHeuredebut()) ||
                    selectedTime.isAfter(selectedPlanning.getHeurefin())) {
                errors.append("- L'heure du rendez-vous doit être comprise dans les heures du planning: " +
                        selectedPlanning.getHeuredebut().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        " à " +
                        selectedPlanning.getHeurefin().format(DateTimeFormatter.ofPattern("HH:mm")) + "\n");
                isValid = false;
            }

            // 4. Vérification des chevauchements avec d'autres rendez-vous
            LocalDateTime newDateTime = LocalDateTime.of(selectedDate, selectedTime);

            // Si modification, on ne compte pas le rendez-vous actuel
            int currentId = (rendezVous != null) ? rendezVous.getId() : 0;

            if (hasOverlappingAppointment(newDateTime, currentId)) {
                errors.append("- Il existe déjà un rendez-vous à cette date et heure\n");
                isValid = false;
            }
        } catch (Exception e) {
            errors.append("- Format d'heure invalide. Utilisez le format HH:MM\n");
            isValid = false;
        }

        // 5. Limitation de la longueur de la description
        String description = txtDescription.getText().trim();
        if (description.length() > 500) {
            errors.append("- La description est trop longue (maximum 500 caractères)\n");
            isValid = false;
        }

        if (!isValid) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n" + errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }
    private boolean hasOverlappingAppointment(LocalDateTime dateTime, int excludeId) {
        try {
            // Récupérer tous les rendez-vous
            List<RendezVous> allAppointments = rendezVousDAO.getAllRendezVous();

            // Vérifier s'il y a chevauchement
            for (RendezVous rdv : allAppointments) {
                // Ne pas comparer avec le rendez-vous actuel en cas de modification
                if (rdv.getId() == excludeId) continue;

                // Vérifier si le rendez-vous est pour la même date et heure
                if (rdv.getDateheure() != null &&
                        rdv.getDateheure().equals(dateTime)) {
                    return true;
                }
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // En cas d'erreur, on laisse passer pour ne pas bloquer l'utilisateur
        }
    }

    private void closeWindow() {
        ((Stage) btnAnnuler.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }
    private void sendEmailNotification(RendezVous rendezVous) {
        final String expediteur = "mohamedjaffel08@gmail.com";         // 💡 À remplacer par ton adresse Gmail
        final String motDePasse = "hsgctjhiakfzfdgw";      // ⚠️ Mot de passe d'application Gmail



        final String destinataire = "mohamedjaffel08@gmail.com";


        String sujet = "Nouveau rendez-vous enregistré";
        String message = String.format(
                "Nouveau rendez-vous confirmé\n\nDate : %s\nHeure : %s\nStatut : %s\nDescription : %s\nPlanning : %s",
                rendezVous.getFormattedDate(),
                rendezVous.getFormattedTime(),
                rendezVous.getStatut(),
                rendezVous.getDescription(),
                rendezVous.getPlanningInfo()
        );

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(expediteur, motDePasse);
            }
        });

        try {
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(expediteur));
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            email.setSubject(sujet);
            email.setText(message);

            Transport.send(email);

            System.out.println("📨 Email envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
            showAlert("Erreur Email", "L'email n'a pas pu être envoyé :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filtrerPlanningsParJour(String jourSemaine) {
        if (allPlannings == null) return;

        // Filtrer les plannings dont `planning.getJour().equalsIgnoreCase(jourSemaine)`
        List<Planning> filtres = allPlannings.stream()
                .filter(p -> p.getJour().equalsIgnoreCase(jourSemaine))
                .collect(Collectors.toList());

        // Mettre à jour le ComboBox
        cmbPlanning.getItems().clear();
        cmbPlanning.getItems().addAll(filtres);

        // Bonus : si aucun planning disponible ce jour-là
        if (filtres.isEmpty()) {
            showAlert("Aucun planning", "Il n’existe aucun planning pour le jour sélectionné : " + jourSemaine, Alert.AlertType.INFORMATION);
        }
    }
}