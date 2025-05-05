package org.example.controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.Patient;
import org.example.models.Reminder;
import org.example.services.NotificationManager;
import org.example.services.ReminderNotificationChecker;
import org.example.services.ReminderService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PatientCalendarController {
    @FXML
    private BorderPane rootPane;

    @FXML
    private Label monthYearLabel;
    @FXML
    private VBox emptyHistoryPlaceholder;
    private Popup notificationPopup;
    private ReminderService reminderService;
    @FXML
    private StackPane notificationIconContainer;
    @FXML
    private Button profileButton;
    @FXML
    private Button notificationButton;

    @FXML
    private StackPane notificationCountContainer;

    @FXML
    private Label notificationCountLabel;
    private SessionManager sessionManager;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private VBox reminderPane;

    @FXML
    private Label selectedDateLabel;

    @FXML
    private TextField medicationNameField;
    @FXML
    private AnchorPane sidebarContainer;
    private boolean sidebarOpen = false;
    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> hourComboBox;

    @FXML
    private ComboBox<String> minuteComboBox;

    @FXML
    private ComboBox<Integer> reminderComboBox;

    @FXML
    private ToggleButton repeatToggle;

    @FXML
    private HBox repeatOptionsBox;

    @FXML
    private ComboBox<String> repeatTypeComboBox;

    @FXML
    private Spinner<Integer> repeatFrequencySpinner;

    @FXML
    private ScrollPane remindersForDatePane;

    @FXML
    private VBox remindersForDateBox;

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private Patient currentPatient;
    private List<LocalDate> datesWithReminders;

    @FXML
    public void initialize() {
        // Initialize services first
        reminderService = new ReminderService();

        // Check if user is logged in and is a patient
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isPatient()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous n'êtes pas autorisé à accéder à cette page",
                    "Veuillez vous connecter en tant que patient.");
            return;
        }

        currentPatient = SessionManager.getInstance().getCurrentPatient();

        // Initialize current month and selected date
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();

        // Initialize UI components
        initializeTimeControls();
        initializeReminderControls();
        initializeRepeatControls();

        // Update the calendar for the current month
        updateCalendar();

        // Update the selected date label
        updateSelectedDateLabel(selectedDate);

        // Load reminders for the selected date
        loadRemindersForDate(selectedDate);
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

    private void initializeTimeControls() {
        // Hour ComboBox (0-23)
        ObservableList<String> hours = IntStream.range(0, 24)
                .mapToObj(i -> String.format("%02d", i))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        hourComboBox.setItems(hours);
        hourComboBox.setValue("08"); // Default to 8 AM

        // Minute ComboBox (0-59)
        ObservableList<String> minutes = IntStream.range(0, 60)
                .mapToObj(i -> String.format("%02d", i))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        minuteComboBox.setItems(minutes);
        minuteComboBox.setValue("00"); // Default to 00 minutes
    }

    public void selectReminder(int reminderId) {
        // Get the reminder from the database
        Reminder reminder = reminderService.getById(reminderId);

        if (reminder != null) {
            // Set selected date to reminder date
            selectedDate = reminder.getDate();
            currentYearMonth = YearMonth.from(selectedDate);

            // Update the calendar UI
            updateCalendar();
            updateSelectedDateLabel(selectedDate);
            loadRemindersForDate(selectedDate);

            // Highlight the specific reminder in the list
            highlightReminder(reminder);
        }
    }

    private void highlightReminder(Reminder reminder) {
        // Find the reminder in the list and highlight it
        for (Node node : remindersForDateBox.getChildren()) {
            if (node instanceof VBox) {
                VBox reminderItem = (VBox) node;

                // Find a way to identify the right reminder
                // This is a simplified approach - you may need to adapt based on your UI structure
                if (reminderItem.getUserData() != null &&
                        reminderItem.getUserData().equals(reminder.getId())) {

                    // Apply highlight style
                    reminderItem.getStyleClass().add("highlighted-reminder");

                    // Ensure it's visible by scrolling to it
                    remindersForDatePane.setVvalue(
                            remindersForDateBox.getChildren().indexOf(reminderItem) /
                                    (double) remindersForDateBox.getChildren().size()
                    );

                    break;
                }
            }
        }
    }

    private void initializeReminderControls() {
        // Reminder time options (in minutes)
        ObservableList<Integer> reminderTimes = FXCollections.observableArrayList(
                0, 5, 10, 15, 30, 60, 120, 1440); // 0 = no reminder, 1440 = 1 day
        reminderComboBox.setItems(reminderTimes);
        reminderComboBox.setValue(15); // Default to 15 minutes

        // Add a cell factory to display user-friendly descriptions
        reminderComboBox.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item == 0) {
                        setText("Aucun rappel");
                    } else if (item < 60) {
                        setText(item + " minutes avant");
                    } else if (item == 60) {
                        setText("1 heure avant");
                    } else if (item == 120) {
                        setText("2 heures avant");
                    } else if (item == 1440) {
                        setText("1 jour avant");
                    } else {
                        setText(item + " minutes avant");
                    }
                }
            }
        });

        // Use the same cell factory for the button cell
        reminderComboBox.setButtonCell(reminderComboBox.getCellFactory().call(null));
    }

    private void initializeRepeatControls() {
        // Initially hide repeat options
        repeatOptionsBox.setVisible(false);
        repeatOptionsBox.setManaged(false);

        // Repeat types
        ObservableList<String> repeatTypes = FXCollections.observableArrayList(
                "Quotidien", "Hebdomadaire", "Mensuel");
        repeatTypeComboBox.setItems(repeatTypes);
        repeatTypeComboBox.setValue("Quotidien");

        // Repeat frequency spinner (1-30)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1);
        repeatFrequencySpinner.setValueFactory(valueFactory);
    }

    private void updateCalendar() {
        // Update month/year label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE);
        monthYearLabel.setText(currentYearMonth.format(formatter));

        // Clear existing calendar
        calendarGrid.getChildren().clear();

        // Get dates with reminders for this month
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        LocalDate lastOfMonth = currentYearMonth.atEndOfMonth();
        if (currentPatient != null) {
            datesWithReminders = reminderService.getDatesWithReminders(
                    currentPatient.getId(), firstOfMonth, lastOfMonth);
        } else {
            datesWithReminders = new ArrayList<>();
        }

        // Determine the day of week for the first of the month (0 = Monday, ... 6 = Sunday in ISO)
        DayOfWeek firstDayOfMonth = firstOfMonth.getDayOfWeek();
        int dayOfWeekValue = firstDayOfMonth.getValue() - 1; // Adjust to 0-based

        // Previous month days to display
        int daysInPreviousMonth = currentYearMonth.minusMonths(1).lengthOfMonth();
        LocalDate previousMonth = currentYearMonth.minusMonths(1).atEndOfMonth();

        // Add calendar days
        int day = 1;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Create 6 rows of 7 days each
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                VBox dayCell = new VBox();
                dayCell.getStyleClass().add("calendar-day-cell");
                dayCell.setPadding(new Insets(5));

                Label dayLabel = new Label();
                dayLabel.getStyleClass().add("calendar-day-label");

                // Previous month days
                if (i == 0 && j < dayOfWeekValue) {
                    int prevDay = daysInPreviousMonth - (dayOfWeekValue - j) + 1;
                    dayLabel.setText(String.valueOf(prevDay));
                    dayLabel.getStyleClass().add("calendar-day-label-other-month");
                    dayCell.getStyleClass().add("calendar-day-cell-other-month");

                    LocalDate date = previousMonth.minusDays(dayOfWeekValue - j - 1);
                    setupDayCell(dayCell, dayLabel, date, true);
                }
                // Current month days
                else if (day <= daysInMonth) {
                    dayLabel.setText(String.valueOf(day));

                    LocalDate date = currentYearMonth.atDay(day);

                    // Check if this is today
                    if (date.equals(LocalDate.now())) {
                        dayCell.getStyleClass().add("calendar-day-cell-today");
                        dayLabel.getStyleClass().add("calendar-day-label-today");
                    }

                    // Check if this date has reminders
                    if (datesWithReminders.contains(date)) {
                        dayCell.getStyleClass().add("calendar-day-with-reminders");

                        // Add a small red circle indicator
                        Circle reminderIndicator = new Circle(4);
                        reminderIndicator.getStyleClass().add("reminder-indicator");
                        dayCell.getChildren().add(reminderIndicator);
                    }

                    // Check if this is the selected date
                    if (date.equals(selectedDate)) {
                        dayCell.getStyleClass().add("calendar-day-cell-selected");
                    }

                    setupDayCell(dayCell, dayLabel, date, false);

                    day++;
                }
                // Next month days
                else {
                    int nextDay = day - daysInMonth;
                    dayLabel.setText(String.valueOf(nextDay));
                    dayLabel.getStyleClass().add("calendar-day-label-other-month");
                    dayCell.getStyleClass().add("calendar-day-cell-other-month");

                    LocalDate date = currentYearMonth.plusMonths(1).atDay(nextDay);
                    setupDayCell(dayCell, dayLabel, date, true);

                    day++;
                }

                dayCell.getChildren().add(0, dayLabel);
                dayCell.setAlignment(Pos.TOP_RIGHT);

                calendarGrid.add(dayCell, j, i);
            }
        }
    }

    private void setupDayCell(VBox dayCell, Label dayLabel, LocalDate date, boolean isOtherMonth) {
        // Set up day cell click event
        dayCell.setOnMouseClicked(e -> {
            // Update selected date
            selectedDate = date;

            // Update calendar to show the correct month if clicking on prev/next month days
            if (isOtherMonth) {
                currentYearMonth = YearMonth.from(date);
                updateCalendar();
            } else {
                // Just update the selected cell styling
                calendarGrid.getChildren().forEach(node -> {
                    if (node instanceof VBox) {
                        node.getStyleClass().remove("calendar-day-cell-selected");
                    }
                });
                dayCell.getStyleClass().add("calendar-day-cell-selected");
            }

            // Update the selected date label
            updateSelectedDateLabel(date);

            // Load reminders for the selected date
            loadRemindersForDate(date);
        });
    }

    private void updateSelectedDateLabel(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE);
        selectedDateLabel.setText(date.format(formatter));
    }

    private void loadRemindersForDate(LocalDate date) {
        if (currentPatient == null) return;

        // Get reminders for this date
        List<Reminder> reminders = reminderService.getByDate(currentPatient.getId(), date);

        // Clear the existing reminders display
        remindersForDateBox.getChildren().clear();

        // Show the reminders pane if there are reminders
        remindersForDatePane.setVisible(!reminders.isEmpty());
        remindersForDatePane.setManaged(!reminders.isEmpty());

        // Add reminders to the display
        if (!reminders.isEmpty()) {
            Label remindersHeaderLabel = new Label("Rappels pour cette date:");
            remindersHeaderLabel.getStyleClass().add("section-header");
            remindersForDateBox.getChildren().add(remindersHeaderLabel);

            for (Reminder reminder : reminders) {
                remindersForDateBox.getChildren().add(createReminderItemView(reminder));
            }
        }
    }

    private VBox createReminderItemView(Reminder reminder) {
        VBox reminderItem = new VBox(5);
        reminderItem.getStyleClass().add("reminder-item");
        reminderItem.setUserData(reminder.getId());


        // Medication name as title
        Label titleLabel = new Label(reminder.getMedicationName());
        titleLabel.getStyleClass().add("reminder-item-title");

        // Time display
        Label timeLabel = new Label("Heure: " + reminder.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.getStyleClass().add("reminder-item-time");

        // Description if available
        if (reminder.getDescription() != null && !reminder.getDescription().isEmpty()) {
            Label descLabel = new Label(reminder.getDescription());
            descLabel.getStyleClass().add("reminder-item-description");
            descLabel.setWrapText(true);

            reminderItem.getChildren().addAll(titleLabel, timeLabel, descLabel);
        } else {
            reminderItem.getChildren().addAll(titleLabel, timeLabel);
        }

        // Add buttons for edit and delete
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(e -> editReminder(reminder));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("secondary-button");
        deleteButton.setOnAction(e -> deleteReminder(reminder));

        buttonsBox.getChildren().addAll(editButton, deleteButton);
        reminderItem.getChildren().add(buttonsBox);

        return reminderItem;
    }

    private void editReminder(Reminder reminder) {
        // Populate form fields with reminder data
        medicationNameField.setText(reminder.getMedicationName());
        descriptionField.setText(reminder.getDescription());

        // Set time
        hourComboBox.setValue(String.format("%02d", reminder.getTime().getHour()));
        minuteComboBox.setValue(String.format("%02d", reminder.getTime().getMinute()));

        // Set reminder minutes
        reminderComboBox.setValue(reminder.getReminderMinutesBefore());

        // Set repeat options
        repeatToggle.setSelected(reminder.isRepeated());
        handleRepeatToggle();

        if (reminder.isRepeated()) {
            switch (reminder.getRepeatType()) {
                case "daily":
                    repeatTypeComboBox.setValue("Quotidien");
                    break;
                case "weekly":
                    repeatTypeComboBox.setValue("Hebdomadaire");
                    break;
                case "monthly":
                    repeatTypeComboBox.setValue("Mensuel");
                    break;
            }

            repeatFrequencySpinner.getValueFactory().setValue(reminder.getRepeatFrequency());
        }

        // Set up the save button to update instead of create
        Button saveButton = (Button) rootPane.lookup("#saveButton");
        saveButton.setOnAction(e -> {
            updateReminder(reminder);
            saveButton.setOnAction(this::handleSaveReminder); // Reset to original handler
        });
        saveButton.setText("Mettre à jour");
    }

    private void updateReminder(Reminder reminder) {
        // Validate input
        if (!validateReminderForm()) return;

        // Update the reminder with form values
        reminder.setMedicationName(medicationNameField.getText());
        reminder.setDescription(descriptionField.getText());

        // Set time
        int hour = Integer.parseInt(hourComboBox.getValue());
        int minute = Integer.parseInt(minuteComboBox.getValue());
        reminder.setTime(LocalTime.of(hour, minute));

        // Set reminder minutes
        reminder.setReminderMinutesBefore(reminderComboBox.getValue());

        // Set repeat options
        reminder.setRepeated(repeatToggle.isSelected());

        if (repeatToggle.isSelected()) {
            String repeatTypeValue = repeatTypeComboBox.getValue();
            String repeatType = "daily"; // Default

            switch (repeatTypeValue) {
                case "Quotidien":
                    repeatType = "daily";
                    break;
                case "Hebdomadaire":
                    repeatType = "weekly";
                    break;
                case "Mensuel":
                    repeatType = "monthly";
                    break;
            }

            reminder.setRepeatType(repeatType);
            reminder.setRepeatFrequency(repeatFrequencySpinner.getValue());
        }

        // Save to database
        if (reminderService.update(reminder)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Rappel mis à jour",
                    "Le rappel a été mis à jour avec succès.");

            // Reset the form
            resetForm();

            // Update calendar and reminders display
            updateCalendar();
            loadRemindersForDate(selectedDate);

            // Reset the save button text
            Button saveButton = (Button) rootPane.lookup("#saveButton");
            saveButton.setText("Enregistrer le rappel");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur de mise à jour",
                    "Une erreur est survenue lors de la mise à jour du rappel.");
        }
    }

    private void deleteReminder(Reminder reminder) {
        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le rappel");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce rappel?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete from database
                if (reminderService.delete(reminder.getId())) {
                    // Update calendar and reminders display
                    updateCalendar();
                    loadRemindersForDate(selectedDate);

                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Rappel supprimé",
                            "Le rappel a été supprimé avec succès.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur de suppression",
                            "Une erreur est survenue lors de la suppression du rappel.");
                }
            }
        });
    }

    @FXML
    private void handlePreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleToday() {
        selectedDate = LocalDate.now();
        currentYearMonth = YearMonth.from(selectedDate);
        updateCalendar();
        updateSelectedDateLabel(selectedDate);
        loadRemindersForDate(selectedDate);
    }

    @FXML
    private void handleRepeatToggle() {
        boolean isRepeating = repeatToggle.isSelected();
        repeatOptionsBox.setVisible(isRepeating);
        repeatOptionsBox.setManaged(isRepeating);
    }

    @FXML
    private void handleSaveReminder(ActionEvent event) {
        if (currentPatient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Utilisateur non identifié",
                    "Vous devez être connecté en tant que patient pour créer un rappel.");
            return;
        }

        // Validate input
        if (!validateReminderForm()) return;

        // Create a new reminder
        Reminder reminder = new Reminder();
        reminder.setPatientId(currentPatient.getId());
        reminder.setMedicationName(medicationNameField.getText());
        reminder.setDescription(descriptionField.getText());
        reminder.setDate(selectedDate);

        // Set time
        int hour = Integer.parseInt(hourComboBox.getValue());
        int minute = Integer.parseInt(minuteComboBox.getValue());
        reminder.setTime(LocalTime.of(hour, minute));

        // Set reminder minutes
        reminder.setReminderMinutesBefore(reminderComboBox.getValue());

        // Set repeat options
        reminder.setRepeated(repeatToggle.isSelected());

        if (repeatToggle.isSelected()) {
            String repeatTypeValue = repeatTypeComboBox.getValue();
            String repeatType = "daily"; // Default

            switch (repeatTypeValue) {
                case "Quotidien":
                    repeatType = "daily";
                    break;
                case "Hebdomadaire":
                    repeatType = "weekly";
                    break;
                case "Mensuel":
                    repeatType = "monthly";
                    break;
            }

            reminder.setRepeatType(repeatType);
            reminder.setRepeatFrequency(repeatFrequencySpinner.getValue());
        }
        // Save to database
        if (reminderService.add(reminder)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Rappel créé",
                    "Le rappel a été créé avec succès.");

            // Reset the form
            resetForm();

            // Update calendar and reminders display
            updateCalendar();
            loadRemindersForDate(selectedDate);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur de création",
                    "Une erreur est survenue lors de la création du rappel.");
        }
    }

    @FXML
    private void handleCancel() {
        resetForm();
    }

    private void resetForm() {
        medicationNameField.clear();
        descriptionField.clear();
        hourComboBox.setValue("08");
        minuteComboBox.setValue("00");
        reminderComboBox.setValue(15);
        repeatToggle.setSelected(false);
        handleRepeatToggle();
    }

    private boolean validateReminderForm() {
        // Check medication name
        if (medicationNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Nom de médicament manquant",
                    "Veuillez entrer un nom de médicament.");
            return false;
        }

        // Check description (optional, but limit length if provided)
        if (descriptionField.getText().length() > 500) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Description trop longue",
                    "La description ne doit pas dépasser 500 caractères.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigation methods


    @FXML
    private void redirectProduit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front/showProduit.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur de navigation",
                    "Impossible de charger la page des produits: " + e.getMessage());
        }
    }

    @FXML
    private void redirectToCalendar(ActionEvent event) {
        // Since we're already on the calendar page, we might just refresh the page
        // or do nothing since we're already here
        updateCalendar();
        loadRemindersForDate(selectedDate);
    }

    @FXML
    private void showNotifications(ActionEvent event) {
        // Implement notification display logic here
        // This could be a popup with recent notifications
        Alert notificationsAlert = new Alert(Alert.AlertType.INFORMATION);
        notificationsAlert.setTitle("Notifications");
        notificationsAlert.setHeaderText("Vos notifications");

        // Here you would load actual notifications from a service
        // This is just a placeholder
        VBox notificationsContent = new VBox(10);
        notificationsContent.getChildren().addAll(
                new Label("Rappel: Prendre médicament à 14:00"),
                new Label("Rendez-vous demain à 10:30")
        );

        notificationsAlert.getDialogPane().setContent(notificationsContent);
        notificationsAlert.showAndWait();
    }

    public void redirectToHistorique(ActionEvent event) {
        SceneManager.loadScene("/fxml/ajouter_historique.fxml", event);

    }

    @FXML
    private void navigateToAcceuil(ActionEvent event) {
        SceneManager.loadScene("/fxml/main_view_patient.fxml", event);
    }

    @FXML
    public void redirectToDemande(ActionEvent event) {
        try {
            // Since we already have currentPatient, we can check that instead
            if (currentPatient != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeDashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de demande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void redirectToRendezVous(ActionEvent event) {
        try {
            // Since we already have currentPatient, we can check that instead
            if (currentPatient != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rendez-vous-view.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté",
                        "Vous devez être connecté pour accéder à cette page.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de Navigation",
                    "Impossible d'ouvrir la page de rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
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
    private void handleProfileButtonClick(ActionEvent event) {
        SceneManager.loadScene("/fxml/patient_profile.fxml", event);
    }


}

