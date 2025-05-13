package org.example.controllers;
import org.example.controllers.ProduitNavigationUtil;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Patient;
import org.example.models.Reminder;
import org.example.models.User;
import org.example.services.NotificationManager;
import org.example.services.PatientService;
import org.example.services.ReminderService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;



public class PatientProfileController extends BaseProfileController {
    private PatientService patientService;
    @FXML
    private AnchorPane sidebarContainer;
    private boolean sidebarOpen = false;
    private SessionManager sessionManager;
    @FXML
    private Button profileButton; // Button to navigate to profile

    @FXML
    private Button mesHistoriquesButton;
    @FXML
    private VBox emptyHistoryPlaceholder;
    private Popup notificationPopup;
    private ReminderService reminderService;
    @FXML
    private StackPane notificationIconContainer;

    @FXML
    private Button notificationButton;
    @FXML
    private Button evenementButton;
    @FXML
    private Label cartCountLabel;
    @FXML
    private StackPane notificationCountContainer;

    @FXML
    private Label notificationCountLabel;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button historique;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isPatient()) {
            showMessage("Erreur: Accès non autorisé", "danger");
            handleLogout();
            return;
        }
        reminderService = new ReminderService();
        setupNotificationBadge();
        Patient patient = SessionManager.getInstance().getCurrentPatient();
        if (patient != null) {
            currentUser = patient;
            loadUserData();
        }

        patientService = new PatientService();
        setUserService();
        super.initialize(url, resourceBundle);
        // Make sure sidebar is initially hidden
        if (sidebarContainer != null) {
            sidebarContainer.setVisible(false);
            sidebarContainer.setPrefWidth(0);
        }

        if (mesHistoriquesButton != null) {
            mesHistoriquesButton.setOnAction(this::redirectToHistoriques);
        }
        historique.setOnAction(event -> handleHistoRedirect());
        if (messageLabell != null) {
            messageLabell.setVisible(false);
        }

    }

    private void setupNotificationBadge() {
        if (notificationCountLabel != null && notificationCountContainer != null) {
            // Bind notification count to the label
            NotificationManager notificationManager = NotificationManager.getInstance();
            notificationCountLabel.textProperty().bind(notificationManager.unreadCountProperty().asString());

            // Show/hide notification badge based on count
            notificationCountContainer.visibleProperty().bind(
                    notificationManager.unreadCountProperty().greaterThan(0));

            // Refresh the notification count
            notificationManager.refreshUnreadCount();
        }
    }

    @FXML
    private void showNotifications() {
        System.out.println("Show notifications button clicked");

        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }

        // Get current patient
        Patient patient = SessionManager.getInstance().getCurrentPatient();
        if (patient == null) {
            System.out.println("No patient logged in, cannot show notifications");
            return;
        }

        System.out.println("Getting notifications for patient ID: " + patient.getId());

        // Initialize reminder service if needed
        if (reminderService == null) {
            reminderService = new ReminderService();
        }

        // Create popup
        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);
        notificationPopup.setHideOnEscape(true);

        // Create main container
        VBox container = new VBox(10);
        container.setPrefWidth(300);
        container.getStyleClass().add("notification-dropdown");

        // Create header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("notification-header");
        header.setPadding(new Insets(10));

        Label headerLabel = new Label("Rappels de médicaments");
        headerLabel.getStyleClass().add("notification-header-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markAllReadButton = new Button("Tout marquer comme lu");
        markAllReadButton.getStyleClass().add("mark-all-read-button");
        markAllReadButton.setOnAction(e -> {
            System.out.println("Marking all notifications as read");
            reminderService.markAllNotificationsAsRead(patient.getId());
            NotificationManager.getInstance().refreshUnreadCount();
            notificationPopup.hide();
        });

        header.getChildren().addAll(headerLabel, spacer, markAllReadButton);

        // Create content area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.getStyleClass().add("notification-list");

        VBox notificationsBox = new VBox(10);
        notificationsBox.setPadding(new Insets(10));

        // Get unread notifications
        List<Reminder> unreadReminders = reminderService.getUnreadNotifications(patient.getId());
        System.out.println("Found " + unreadReminders.size() + " unread reminders");

        if (unreadReminders.isEmpty()) {
            // Show empty state
            VBox emptyState = new VBox();
            emptyState.setAlignment(Pos.CENTER);
            emptyState.getStyleClass().add("notification-empty");

            Label emptyLabel = new Label("Aucun rappel à afficher");
            emptyLabel.getStyleClass().add("notification-empty-text");

            emptyState.getChildren().add(emptyLabel);
            notificationsBox.getChildren().add(emptyState);
        } else {
            // Add reminder notifications
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Reminder reminder : unreadReminders) {
                VBox notificationItem = new VBox(5);
                notificationItem.getStyleClass().add("notification-item");

                Label titleLabel = new Label(reminder.getMedicationName());
                titleLabel.getStyleClass().add("notification-item-title");

                Label timeLabel = new Label("Date: " + reminder.getDate().format(dateFormatter) +
                        " à " + reminder.getTime().format(timeFormatter));
                timeLabel.getStyleClass().add("notification-item-time");

                notificationItem.getChildren().addAll(titleLabel, timeLabel);

                if (reminder.getDescription() != null && !reminder.getDescription().isEmpty()) {
                    Label descLabel = new Label(reminder.getDescription());
                    descLabel.getStyleClass().add("notification-item-description");
                    descLabel.setWrapText(true);
                    notificationItem.getChildren().add(descLabel);
                }

                // Set click handler
                final int reminderId = reminder.getId();
                notificationItem.setOnMouseClicked(e -> {
                    System.out.println("Notification clicked: Reminder ID " + reminderId);
                    handleNotificationClick(reminderId);
                    notificationPopup.hide();
                });

                notificationsBox.getChildren().add(notificationItem);
            }
        }

        scrollPane.setContent(notificationsBox);

        // Add components to container
        container.getChildren().addAll(header, scrollPane);

        // Add CSS
        try {
            String cssPath = getClass().getResource("/css/notification-styles.css").toExternalForm();
            container.getStylesheets().add(cssPath);
            System.out.println("Added notification CSS: " + cssPath);
        } catch (Exception e) {
            System.err.println("Could not load notification CSS: " + e.getMessage());
        }

        // Add to popup and show
        notificationPopup.getContent().add(container);

        // Position below the notification button
        notificationPopup.show(notificationButton,
                notificationButton.localToScreen(0, 0).getX() - 270,
                notificationButton.localToScreen(0, 0).getY() + notificationButton.getHeight());

        System.out.println("Notification popup shown");
    }

    private void handleNotificationClick(int reminderId) {
        System.out.println("Handling notification click for reminder ID: " + reminderId);

        // Mark as read
        reminderService.markNotificationAsRead(reminderId);

        // Refresh count
        NotificationManager.getInstance().refreshUnreadCount();

        // Navigate to calendar page with the reminder selected
        navigateToCalendarWithReminder(reminderId);
    }

    private void navigateToCalendarWithReminder(int reminderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient_calendar.fxml"));
            Parent root = loader.load();

            // Get controller and pass the reminder ID
            PatientCalendarController controller = loader.getController();
            controller.selectReminder(reminderId);

            // Show calendar page
            Scene scene = new Scene(root);
            Stage stage = (Stage) notificationButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le calendrier",
                    "Une erreur est survenue: " + e.getMessage());
        }
    }

    @Override
    protected void setUserService() {
        this.userService = patientService;
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Patient) {
            super.setUser(user, "Patient");
            setUserService();
        } else {
            throw new IllegalArgumentException("User must be a Patient");
        }
    }

    public void setPatient(Patient patient) {
        this.currentUser = patient;
        this.userType = "Patient";
        loadUserData();
        setUserService();
        if (patient != null) {
            SessionManager.getInstance().setCurrentUser(patient, "Patient");
        }
    }

    @Override
    protected void saveUser() {
        if (currentUser instanceof Patient) {
            try {
                patientService.update((Patient) currentUser);
                SessionManager.getInstance().setCurrentUser(currentUser, "Patient");
                updateDisplayLabels();
                showMessage("Profil patient mis à jour avec succès", "success");
            } catch (Exception e) {
                showMessage("Erreur lors de la mise à jour du profil: " + e.getMessage(), "danger");
                e.printStackTrace();
            }
        }
    }


    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void toggleSidebar() {
        if (sidebarOpen) {
            // Close sidebar with animation
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(sidebarContainer.prefWidthProperty(), 0, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(250), kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(e -> sidebarContainer.setVisible(false)); // Hide after animation completes
            timeline.play();
            sidebarOpen = false;
        } else {
            // Make visible before opening
            sidebarContainer.setVisible(true);
            sidebarContainer.setPrefWidth(0);

            // Open sidebar with animation
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(sidebarContainer.prefWidthProperty(), 220, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(250), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
            sidebarOpen = true;
        }
    }


    ///////////////////////////////////NAVIGATION///////////////////////////////////////////////////////
    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_patient.fxml", event);
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        SceneManager.loadScene("/fxml/DemandeDashboard.fxml", event);

    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        SceneManager.loadScene("/fxml/rendez-vous-view.fxml", event);
    }

    @FXML
    public void viewDoctors(ActionEvent event) {
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Use SceneManager to load the DoctorList.fxml in full screen
            SceneManager.loadScene("/fxml/DoctorList.fxml", event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des médecins: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_calendar.fxml", event);

    }

    public void handleHistoRedirect() {
        try {
            // Enhanced logging
            System.out.println("Starting history redirect...");
            System.out.println("SessionManager.isLoggedIn(): " + SessionManager.getInstance().isLoggedIn());
            System.out.println("SessionManager.isPatient(): " + SessionManager.getInstance().isPatient());

            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showErrorDialog("Erreur", "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Check if the user is a patient
            if (!SessionManager.getInstance().isPatient()) {
                showErrorDialog("Erreur", "Seuls les patients peuvent accéder à cette fonctionnalité.");
                return;
            }

            // Get the patient from the session
            Patient patient = SessionManager.getInstance().getCurrentPatient();
            System.out.println("Patient from SessionManager: " + (patient != null ?
                    "ID=" + patient.getId() + ", Nom=" + patient.getNom() : "NULL"));

            if (patient == null) {
                System.out.println("Error: SessionManager.getCurrentPatient() returned null!");

                // Try to recover by using currentUser if it's a Patient
                if (currentUser instanceof Patient) {
                    patient = (Patient) currentUser;
                    System.out.println("Recovered patient from currentUser: ID=" + patient.getId());
                    // Update the session
                    SessionManager.getInstance().setCurrentUser(patient, "patient");
                } else {
                    showErrorDialog("Erreur", "Impossible de récupérer les informations du patient.");
                    return;
                }
            }

            // Load the historique page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_historique.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the patient
            AjouterHistController controller = loader.getController();
            controller.setPatient(patient);
            System.out.println("Patient passed to controller: ID=" + patient.getId() +
                    ", Nom=" + patient.getNom() + ", Prénom=" + patient.getPrenom());

            // Show the new scene with maximum size
            Stage stage = (Stage) historique.getScene().getWindow();

            // Get screen dimensions for maximum size
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error during navigation to historique: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur", "Impossible de charger la page d'ajout d'historique: " + e.getMessage());
        }
    }

    @FXML
    public void redirectToHistoriques(ActionEvent event) {
        try {
            if (currentUser instanceof Patient) {
                // Make sure the current user is set in the session
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");

                // Use SceneManager to load the view that displays historiques
                SceneManager.loadScene("/fxml/historiques_patient.fxml", event);

                System.out.println("Redirecting to historiques for patient ID: " + currentUser.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté en tant que patient pour accéder à cette page.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileButtonClick(ActionEvent event) {
        // Check if user is logged in before navigating to profile
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder à votre profil.");
            return;
        }

        // Route to appropriate profile page based on user type
        if (sessionManager.isPatient()) {
            SceneManager.loadScene("/fxml/patient_profile.fxml", event);
        } else if (sessionManager.isMedecin()) {
            SceneManager.loadScene("/fxml/doctor_profile.fxml", event);
        } else if (sessionManager.isAdmin()) {
            SceneManager.loadScene("/fxml/admin_profile.fxml", event);
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            System.out.println("Attempting to navigate to " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException(fxmlPath + " resource not found");
            }
            Parent root = loader.load();

            // Get the current stage
            Stage stage = null;


            if (stage == null) {
                throw new RuntimeException("Cannot get current stage");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.show();
            System.out.println("Successfully navigated to " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error navigating to page", e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during navigation: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred", e.getMessage());
        }
    }

    @FXML
    public void redirectToReservations(ActionEvent event) {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservationFront.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à vos réservations.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des réservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void redirectToEvents(ActionEvent event) {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().setCurrentUser(currentUser, "patient");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventFront.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder aux événements.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page des événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void redirectProduit(ActionEvent event) {
        try {
            System.out.println("Starting redirection to showProduit.fxml");

            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showErrorDialog("Erreur", "Vous devez être connecté pour accéder à cette page.");
                return;
            }

            // Create a simplified version of showProduit.fxml if the original has issues
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/simplified_product_view.fxml"));

            // If the simplified version doesn't exist, try the original
            if (loader.getLocation() == null) {
                System.out.println("Simplified version not found, trying original FXML");
                loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
            }

            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Get screen dimensions for maximum size
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            System.out.println("Screen dimensions: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());

            // Create a new scene with the screen dimensions
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Set the scene to the stage
            stage.setScene(scene);

            // Ensure the stage is maximized
            stage.setMaximized(true);
            System.out.println("Stage maximized: " + stage.isMaximized());

            // Show the stage
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading showProduit.fxml: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
        }
    }}