package org.example.services;

import javafx.application.Platform;
import org.example.controllers.NotificationDisplayService;
import org.example.models.Patient;
import org.example.models.Reminder;
import org.example.util.SessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReminderNotificationChecker {
    private static final Logger logger = Logger.getLogger(ReminderNotificationChecker.class.getName());
    private static ReminderNotificationChecker instance;

    private final ReminderService reminderService;
    private final NotificationManager notificationManager;
    private Timer checkTimer;
    private final ConcurrentHashMap<Integer, Boolean> processedReminders;

    private ReminderNotificationChecker() {
        reminderService = new ReminderService();
        notificationManager = NotificationManager.getInstance();
        processedReminders = new ConcurrentHashMap<>();
        startPeriodicChecking();
        logger.info("ReminderNotificationChecker started");
    }

    public static synchronized ReminderNotificationChecker getInstance() {
        if (instance == null) {
            instance = new ReminderNotificationChecker();
        }
        return instance;
    }

    private void startPeriodicChecking() {
        checkTimer = new Timer(true); // Daemon timer
        checkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForDueReminders();
            }
        }, 0, 30000); // Check every 30 seconds
    }

    private void checkForDueReminders() {
        // Check if there's a logged-in patient
        if (!SessionManager.getInstance().isLoggedIn() ||
                !SessionManager.getInstance().isPatient()) {
            return;
        }

        Patient currentPatient = SessionManager.getInstance().getCurrentPatient();
        if (currentPatient == null) return;

        // Get active reminders
        List<Reminder> reminders = reminderService.getActiveReminders(currentPatient.getId());
        if (reminders.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (Reminder reminder : reminders) {
            // Skip if already processed
            if (processedReminders.containsKey(reminder.getId()) || reminder.isNotificationShown()) {
                continue;
            }

            // Calculate when notification should be shown
            LocalDateTime reminderDateTime = LocalDateTime.of(reminder.getDate(), reminder.getTime());
            LocalDateTime notificationTime = reminderDateTime.minusMinutes(reminder.getReminderMinutesBefore());

            System.out.println("now"+now);
            // Check if it's time to show the notification
            // We consider the timing correct if now is after the notification time
            // and before the actual reminder time
            if (now.isAfter(notificationTime) && now.isBefore(reminderDateTime)) {
                logger.info(String.format(
                        "Time to show reminder: ID=%d, Med=%s, Time=%s, Now=%s",
                        reminder.getId(), reminder.getMedicationName(),
                        reminderDateTime.toString(), now.toString()
                ));

                processedReminders.put(reminder.getId(), true);

                // Update this on the UI thread
                Platform.runLater(() -> {
                    // Mark notification as shown
                    reminder.setNotificationShown(true);
                    reminderService.update(reminder);

                    // Play notification sound
                    notificationManager.playNotificationSound();

                    // Update notification badge count
                    notificationManager.refreshUnreadCount();
                    NotificationDisplayService.getInstance().showReminderNotification(reminder);
                });

                // If repeating, create next occurrence
                if (reminder.isRepeated()) {
                    createNextRepeatReminder(reminder);
                }
            }
        }
    }

    private void createNextRepeatReminder(Reminder reminder) {
        if (reminder.getRepeatType() == null || reminder.getRepeatFrequency() <= 0) {
            return;
        }

        LocalDate nextDate = null;

        // Calculate next date based on repeat type
        switch (reminder.getRepeatType()) {
            case "daily":
                nextDate = reminder.getDate().plusDays(reminder.getRepeatFrequency());
                break;
            case "weekly":
                nextDate = reminder.getDate().plusWeeks(reminder.getRepeatFrequency());
                break;
            case "monthly":
                nextDate = reminder.getDate().plusMonths(reminder.getRepeatFrequency());
                break;
            default:
                return;
        }

        if (nextDate == null) return;

        // Create new reminder for next occurrence
        Reminder nextReminder = new Reminder();
        nextReminder.setPatientId(reminder.getPatientId());
        nextReminder.setMedicationName(reminder.getMedicationName());
        nextReminder.setDescription(reminder.getDescription());
        nextReminder.setDate(nextDate);
        nextReminder.setTime(reminder.getTime());
        nextReminder.setReminderMinutesBefore(reminder.getReminderMinutesBefore());
        nextReminder.setRepeated(true);
        nextReminder.setRepeatType(reminder.getRepeatType());
        nextReminder.setRepeatFrequency(reminder.getRepeatFrequency());
        nextReminder.setActive(true);
        nextReminder.setDone(false);
        nextReminder.setNotificationShown(false);

        // Save to database
        reminderService.add(nextReminder);

        logger.info(String.format(
                "Created next repeated reminder: Original ID=%d, New Date=%s",
                reminder.getId(), nextDate.toString()
        ));
    }

    public void shutdown() {
        if (checkTimer != null) {
            checkTimer.cancel();
            checkTimer = null;
        }
    }

    // For debugging - clear all processed reminders to allow re-notification
    public void resetProcessedReminders() {
        processedReminders.clear();
    }
}