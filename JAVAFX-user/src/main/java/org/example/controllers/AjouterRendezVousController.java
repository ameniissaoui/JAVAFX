package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Planning;
import org.example.models.RendezVous;
import org.example.services.PlanningDAO;
import org.example.services.RendezVousDAO;
import org.example.services.GoogleCalendarService;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class AjouterRendezVousController implements Initializable {
    @FXML private Label headerLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> cmbHeure;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<Planning> cmbPlanning;
    @FXML private Button btnAnnuler;
    @FXML private Button btnEnregistrer;
    @FXML private CheckBox chkAddToGoogleCalendar;
    @FXML private Button btnViewInGoogleCalendar;

    private GoogleCalendarService googleCalendarService;
    private RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private PlanningDAO planningDAO = new PlanningDAO();
    private RendezVousViewController parentController;
    private RendezVous rendezVousAModifier;
    private boolean modeEdition = false;
    private List<Planning> tousLesPlannings = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupDatePicker();
        setupButtons();

        // Initialiser Google Calendar
        try {
            googleCalendarService = new GoogleCalendarService();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de Google Calendar: " + e.getMessage());
            if (chkAddToGoogleCalendar != null) {
                chkAddToGoogleCalendar.setDisable(true);
                chkAddToGoogleCalendar.setSelected(false);
            }
        }

        // Configurer le bouton pour voir dans Google Calendar
        if (btnViewInGoogleCalendar != null) {
            btnViewInGoogleCalendar.setVisible(false);
            btnViewInGoogleCalendar.setOnAction(e -> {
                if (rendezVousAModifier != null && rendezVousAModifier.getGoogleEventId() != null) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://calendar.google.com/calendar/event?eid=" + rendezVousAModifier.getGoogleEventId()));
                    } catch (Exception ex) {
                        showAlert("Erreur", "Impossible d'ouvrir Google Calendar: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }

    private void setupComboBoxes() {
        // Configuration des heures disponibles
        for (int i = 8; i <= 18; i++) {
            cmbHeure.getItems().add(String.format("%02d:00", i));
            cmbHeure.getItems().add(String.format("%02d:30", i));
        }
        cmbHeure.getSelectionModel().selectFirst();

        // Configuration des statuts possibles
        cmbStatut.getItems().addAll("En attente", "Confirmé", "Annulé");
        cmbStatut.getSelectionModel().selectFirst();

        // Configuration de l'affichage des plannings dans la combobox
        cmbPlanning.setCellFactory(param -> new ListCell<Planning>() {
            @Override
            protected void updateItem(Planning planning, boolean empty) {
                super.updateItem(planning, empty);
                if (empty || planning == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    setText(planning.getJour() + " (" +
                            planning.getHeuredebut().format(formatter) + " - " +
                            planning.getHeurefin().format(formatter) + ")");
                }
            }
        });

        // Même formattage pour l'élément sélectionné
        cmbPlanning.setButtonCell(new ListCell<Planning>() {
            @Override
            protected void updateItem(Planning planning, boolean empty) {
                super.updateItem(planning, empty);
                if (empty || planning == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    setText(planning.getJour() + " (" +
                            planning.getHeuredebut().format(formatter) + " - " +
                            planning.getHeurefin().format(formatter) + ")");
                }
            }
        });

        // Chargement des plannings disponibles
        try {
            tousLesPlannings = planningDAO.getAllPlannings();
            // Initialement, aucun planning n'est chargé - ils seront filtrés par jour
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les plannings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupDatePicker() {
        datePicker.setValue(LocalDate.now());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrerPlanningsParJour(newValue);
        });
        filtrerPlanningsParJour(datePicker.getValue());
    }

    private void filtrerPlanningsParJour(LocalDate date) {
        if (date == null || tousLesPlannings == null) return;

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String jourSemaine = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH);
        jourSemaine = jourSemaine.substring(0, 1).toUpperCase() + jourSemaine.substring(1);

        final String jour = jourSemaine;

        List<Planning> planningsFiltres = tousLesPlannings.stream()
                .filter(p -> p.getJour().equals(jour))
                .collect(Collectors.toList());

        cmbPlanning.getItems().clear();
        cmbPlanning.getItems().addAll(planningsFiltres);

        if (!planningsFiltres.isEmpty()) {
            cmbPlanning.getSelectionModel().selectFirst();
        }
    }

    private void setupButtons() {
        btnAnnuler.setOnAction(e -> closeDialog());
        btnEnregistrer.setOnAction(e -> saveRendezVous());
    }

    public void setParentController(RendezVousViewController controller) {
        this.parentController = controller;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVousAModifier = rendezVous;
        this.modeEdition = true;

        headerLabel.setText("Modifier le rendez-vous");

        LocalDate date = rendezVous.getDateheure().toLocalDate();
        datePicker.setValue(date);

        LocalTime time = rendezVous.getDateheure().toLocalTime();
        String formattedTime = String.format("%02d:%02d", time.getHour(), time.getMinute() - (time.getMinute() % 30));
        cmbHeure.setValue(formattedTime);

        cmbStatut.setValue(rendezVous.getStatut());
        txtDescription.setText(rendezVous.getDescription());

        if (rendezVous.getGoogleEventId() != null && !rendezVous.getGoogleEventId().isEmpty()) {
            btnViewInGoogleCalendar.setVisible(true);
        }

        if (rendezVous.getPlanning() != null) {
            for (Planning planning : cmbPlanning.getItems()) {
                if (planning.getId() == rendezVous.getPlanning_id()) {
                    cmbPlanning.setValue(planning);
                    break;
                }
            }
        }
    }

    private void saveRendezVous() {
        if (!validateInputs()) return;

        try {
            LocalDate date = datePicker.getValue();
            String[] timeparts = cmbHeure.getValue().split(":");
            LocalTime time = LocalTime.of(Integer.parseInt(timeparts[0]), Integer.parseInt(timeparts[1]));
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            Planning selectedPlanning = cmbPlanning.getValue();

            int rendezVousIdToExclude = modeEdition ? rendezVousAModifier.getId() : -1;
            boolean rendezVousExiste = rendezVousDAO.rendezVousExisteDejaADateHeure(dateTime, selectedPlanning.getId(), rendezVousIdToExclude);

            if (rendezVousExiste) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Créneau déjà occupé");
                alert.setHeaderText("Conflit d'horaire détecté");
                alert.setContentText("Un rendez-vous existe déjà à cette date et heure (" +
                        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " à " +
                        time.format(DateTimeFormatter.ofPattern("HH:mm")) + ").\n\n" +
                        "Veuillez choisir un autre créneau horaire.");
                alert.showAndWait();
                return;
            }

            String googleEventId = null;

            if (modeEdition) {
                rendezVousAModifier.setDateheure(dateTime);
                rendezVousAModifier.setStatut(cmbStatut.getValue());
                rendezVousAModifier.setDescription(txtDescription.getText());
                rendezVousAModifier.setPlanning_id(selectedPlanning.getId());
                rendezVousAModifier.setPlanning(selectedPlanning);

                if (chkAddToGoogleCalendar != null && chkAddToGoogleCalendar.isSelected() && googleCalendarService != null) {
                    try {
                        if (rendezVousAModifier.getGoogleEventId() != null) {
                            googleCalendarService.updateEventInCalendar(rendezVousAModifier.getGoogleEventId(), rendezVousAModifier);
                        } else {
                            String eventUrl = googleCalendarService.addEventToCalendar(rendezVousAModifier);
                            googleEventId = extractEventIdFromUrl(eventUrl);
                            rendezVousAModifier.setGoogleEventId(googleEventId);
                        }
                    } catch (Exception e) {
                        showAlert("Avertissement",
                                "Le rendez-vous sera enregistré, mais l'ajout à Google Calendar a échoué: " + e.getMessage(),
                                Alert.AlertType.WARNING);
                    }
                }

                rendezVousDAO.updateRendezVous(rendezVousAModifier);
                sendEmailNotification(rendezVousAModifier);
                if (parentController != null) {
                    parentController.refreshData();
                }
            } else {
                RendezVous newRendezVous = new RendezVous();
                newRendezVous.setDateheure(dateTime);
                newRendezVous.setStatut(cmbStatut.getValue());
                newRendezVous.setDescription(txtDescription.getText());
                newRendezVous.setPlanning_id(selectedPlanning.getId());
                newRendezVous.setPlanning(selectedPlanning);

                if (chkAddToGoogleCalendar != null && chkAddToGoogleCalendar.isSelected() && googleCalendarService != null) {
                    try {
                        String eventUrl = googleCalendarService.addEventToCalendar(newRendezVous);
                        googleEventId = extractEventIdFromUrl(eventUrl);
                        newRendezVous.setGoogleEventId(googleEventId);
                    } catch (Exception e) {
                        showAlert("Avertissement",
                                "Le rendez-vous sera enregistré, mais l'ajout à Google Calendar a échoué: " + e.getMessage(),
                                Alert.AlertType.WARNING);
                    }
                }

                int newId = rendezVousDAO.saveRendezVous(newRendezVous);
                newRendezVous.setId(newId);
                sendEmailNotification(newRendezVous);
                if (parentController != null) {
                    parentController.refreshData();
                }
            }

            closeDialog();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String extractEventIdFromUrl(String url) {
        if (url != null && url.contains("eid=")) {
            return url.substring(url.indexOf("eid=") + 4);
        }
        return null;
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (datePicker.getValue() == null) {
            errors.append("- Veuillez sélectionner une date\n");
        }

        if (cmbHeure.getValue() == null) {
            errors.append("- Veuillez sélectionner une heure\n");
        }

        if (cmbStatut.getValue() == null) {
            errors.append("- Veuillez sélectionner un statut\n");
        }

        if (txtDescription.getText().trim().isEmpty()) {
            errors.append("- Veuillez saisir une description\n");
        }

        if (cmbPlanning.getValue() == null) {
            errors.append("- Veuillez sélectionner un planning\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n" + errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void sendEmailNotification(RendezVous rendezVous) {
        final String expediteur = "mohamedjaffel08@gmail.com";
        final String motDePasse = "hsgctjhiakfzfdgw";
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

    public void setSelectedDate(LocalDate date) {
        if (datePicker != null) {
            datePicker.setValue(date);
        }
    }

    public void setSelectedPlanning(Planning planning) {
        if (cmbPlanning != null) {
            for (Planning p : cmbPlanning.getItems()) {
                if (p.getId() == planning.getId()) {
                    cmbPlanning.setValue(p);
                    break;
                }
            }
        }
    }
}
