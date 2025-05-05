package org.example.services;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.media.AudioClip;
import org.example.models.Patient;
import org.example.services.ReminderService;
import org.example.util.SessionManager;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationManager {
    private static final Logger logger = Logger.getLogger(NotificationManager.class.getName());
    private static NotificationManager instance;

    private final IntegerProperty unreadCount = new SimpleIntegerProperty(0);
    private AudioClip notificationSound;
    private ReminderService reminderService;

    private NotificationManager() {
        reminderService = new ReminderService();
        loadNotificationSound();
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    private void loadNotificationSound() {
        try {
            URL soundUrl = getClass().getResource("/sounds/notification.wav");
            if (soundUrl != null) {
                notificationSound = new AudioClip(soundUrl.toString());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load notification sound", e);
        }
    }

    public IntegerProperty unreadCountProperty() {
        return unreadCount;
    }

    public int getUnreadCount() {
        return unreadCount.get();
    }

    public void refreshUnreadCount() {
        Patient currentPatient = SessionManager.getInstance().getCurrentPatient();
        if (currentPatient != null) {
            int count = reminderService.getUnreadNotificationCount(currentPatient.getId());
            logger.info("Refreshing unread count for patient ID " + currentPatient.getId() + ": " + count + " notifications");
            unreadCount.set(count);
        } else {
            logger.warning("Cannot refresh notifications: No patient logged in");
            unreadCount.set(0);
        }
    }

    public void playNotificationSound() {
        if (notificationSound != null) {
            notificationSound.play();
        }
    }
}