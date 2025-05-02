package org.example.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reminder {
    private int id;
    private int patientId;
    private String medicationName;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private int reminderMinutesBefore;
    private boolean isRepeated;
    private String repeatType; // daily, weekly, monthly
    private int repeatFrequency; // every X days/weeks/months
    private boolean isActive;
    private boolean isDone;
    private boolean notificationShown;

    public Reminder() {
        this.isActive = true;
        this.isDone = false;
    }

    public Reminder(int patientId, String medicationName, String description,
                    LocalDate date, LocalTime time, int reminderMinutesBefore,
                    boolean isRepeated, String repeatType, int repeatFrequency) {
        this.patientId = patientId;
        this.medicationName = medicationName;
        this.description = description;
        this.date = date;
        this.time = time;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.isRepeated = isRepeated;
        this.repeatType = repeatType;
        this.repeatFrequency = repeatFrequency;
        this.isActive = true;
        this.isDone = false;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public int getReminderMinutesBefore() {
        return reminderMinutesBefore;
    }

    public void setReminderMinutesBefore(int reminderMinutesBefore) {
        this.reminderMinutesBefore = reminderMinutesBefore;
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public void setRepeated(boolean repeated) {
        isRepeated = repeated;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatFrequency() {
        return repeatFrequency;
    }

    public void setRepeatFrequency(int repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
    // Add getter and setter
    public boolean isNotificationShown() {
        return notificationShown;
    }

    public void setNotificationShown(boolean notificationShown) {
        this.notificationShown = notificationShown;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", medicationName='" + medicationName + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", isRepeated=" + isRepeated +
                '}';
    }
}