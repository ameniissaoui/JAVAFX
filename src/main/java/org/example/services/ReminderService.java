package org.example.services;

import org.example.models.Reminder;
import org.example.util.MaConnexion;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReminderService {
    private static final Logger logger = Logger.getLogger(ReminderService.class.getName());
    private final Connection connection;

    public ReminderService() {
        // Get the connection once when service is instantiated
        this.connection = MaConnexion.getInstance().getCnx();
        // Ensure notification_shown column exists when service is created
        addNotificationShownColumn();
    }

    /**
     * Add notification_shown column to the reminders table if it doesn't exist
     */
    public void addNotificationShownColumn() {
        String checkColumnSQL = "SHOW COLUMNS FROM reminders LIKE 'notification_shown'";
        String alterTableSQL = "ALTER TABLE reminders ADD COLUMN notification_shown BOOLEAN DEFAULT 0";

        try (Statement stmt = connection.createStatement()) {
            // Check if column exists
            try (ResultSet rs = stmt.executeQuery(checkColumnSQL)) {
                if (!rs.next()) {
                    // Column doesn't exist, add it
                    stmt.executeUpdate(alterTableSQL);
                    logger.info("Added notification_shown column to reminders table");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking/adding notification_shown column", e);
        }
    }

    public boolean add(Reminder reminder) {
        String sql = "INSERT INTO reminders (patient_id, medication_name, description, date, time, " +
                "reminder_minutes_before, is_repeated, repeat_type, repeat_frequency, is_active, is_done, notification_shown) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, reminder.getPatientId());
            pstmt.setString(2, reminder.getMedicationName());
            pstmt.setString(3, reminder.getDescription());
            pstmt.setString(4, reminder.getDate().toString());
            pstmt.setString(5, reminder.getTime().toString());
            pstmt.setInt(6, reminder.getReminderMinutesBefore());
            pstmt.setBoolean(7, reminder.isRepeated());
            pstmt.setString(8, reminder.getRepeatType());
            pstmt.setInt(9, reminder.getRepeatFrequency());
            pstmt.setBoolean(10, reminder.isActive());
            pstmt.setBoolean(11, reminder.isDone());
            pstmt.setBoolean(12, reminder.isNotificationShown());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reminder.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding reminder", e);
        }
        return false;
    }

    public boolean update(Reminder reminder) {
        String sql = "UPDATE reminders SET medication_name = ?, description = ?, date = ?, time = ?, " +
                "reminder_minutes_before = ?, is_repeated = ?, repeat_type = ?, " +
                "repeat_frequency = ?, is_active = ?, is_done = ?, notification_shown = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reminder.getMedicationName());
            pstmt.setString(2, reminder.getDescription());
            pstmt.setString(3, reminder.getDate().toString());
            pstmt.setString(4, reminder.getTime().toString());
            pstmt.setInt(5, reminder.getReminderMinutesBefore());
            pstmt.setBoolean(6, reminder.isRepeated());
            pstmt.setString(7, reminder.getRepeatType());
            pstmt.setInt(8, reminder.getRepeatFrequency());
            pstmt.setBoolean(9, reminder.isActive());
            pstmt.setBoolean(10, reminder.isDone());
            pstmt.setBoolean(11, reminder.isNotificationShown());
            pstmt.setInt(12, reminder.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating reminder", e);
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reminders WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting reminder", e);
            return false;
        }
    }

    public Reminder getById(int id) {
        String sql = "SELECT * FROM reminders WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReminder(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting reminder by ID", e);
        }
        return null;
    }

    public List<Reminder> getByPatientId(int patientId) {
        String sql = "SELECT * FROM reminders WHERE patient_id = ?";
        List<Reminder> reminders = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reminders.add(mapResultSetToReminder(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting reminders by patient ID", e);
        }
        return reminders;
    }

    public List<Reminder> getByDate(int patientId, LocalDate date) {
        String sql = "SELECT * FROM reminders WHERE patient_id = ? AND date = ?";
        List<Reminder> reminders = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, date.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reminders.add(mapResultSetToReminder(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting reminders by date", e);
        }
        return reminders;
    }

    public List<Reminder> getActiveReminders(int patientId) {
        String sql = "SELECT * FROM reminders WHERE patient_id = ? AND is_active = 1 AND is_done = 0";
        List<Reminder> reminders = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reminders.add(mapResultSetToReminder(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting active reminders", e);
        }
        return reminders;
    }

    public List<LocalDate> getDatesWithReminders(int patientId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT DISTINCT date FROM reminders WHERE patient_id = ? AND date BETWEEN ? AND ?";
        List<LocalDate> dates = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                dates.add(LocalDate.parse(rs.getString("date")));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting dates with reminders", e);
        }
        return dates;
    }

    public List<Reminder> getUnreadNotifications(int patientId) {
        String sql = "SELECT * FROM reminders WHERE patient_id = ? AND notification_shown = 1 " +
                "AND is_active = 1 AND is_done = 0 ORDER BY date ASC, time ASC";
        List<Reminder> reminders = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reminders.add(mapResultSetToReminder(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting unread notifications", e);
        }
        return reminders;
    }

    public boolean markNotificationAsRead(int reminderId) {
        String sql = "UPDATE reminders SET notification_shown = 1 WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reminderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error marking notification as read", e);
            return false;
        }
    }

    public boolean markAllNotificationsAsRead(int patientId) {
        String sql = "UPDATE reminders SET notification_shown = 1 WHERE patient_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error marking all notifications as read", e);
            return false;
        }
    }

    public int getUnreadNotificationCount(int patientId) {
        String sql = "SELECT COUNT(*) as count FROM reminders WHERE patient_id = ? AND notification_shown = 1 " +
                "AND is_active = 1 AND is_done = 0";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting unread notification count", e);
        }
        return 0;
    }

    private Reminder mapResultSetToReminder(ResultSet rs) throws SQLException {
        Reminder reminder = new Reminder();
        reminder.setId(rs.getInt("id"));
        reminder.setPatientId(rs.getInt("patient_id"));
        reminder.setMedicationName(rs.getString("medication_name"));
        reminder.setDescription(rs.getString("description"));
        reminder.setDate(LocalDate.parse(rs.getString("date")));
        reminder.setTime(LocalTime.parse(rs.getString("time")));
        reminder.setReminderMinutesBefore(rs.getInt("reminder_minutes_before"));
        reminder.setRepeated(rs.getBoolean("is_repeated"));
        reminder.setRepeatType(rs.getString("repeat_type"));
        reminder.setRepeatFrequency(rs.getInt("repeat_frequency"));
        reminder.setActive(rs.getBoolean("is_active"));
        reminder.setDone(rs.getBoolean("is_done"));

        // Handle the notification_shown column, it might not exist in older databases
        try {
            reminder.setNotificationShown(rs.getBoolean("notification_shown"));
        } catch (SQLException e) {
            reminder.setNotificationShown(false); // Default to false if column doesn't exist
        }

        return reminder;
    }

    // Very important to close the connection when service is done
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }
}