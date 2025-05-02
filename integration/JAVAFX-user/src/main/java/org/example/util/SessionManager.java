package org.example.util;

import org.example.models.User;
import org.example.models.Admin;
import org.example.models.Patient;
import org.example.models.Medecin;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String userType; // "admin", "patient", or "medecin"

    // Private constructor for singleton pattern
    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user, String userType) {
        this.currentUser = user;
        this.userType = userType;
    }

    // Method overload for when userType is not provided
    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Determine user type based on instance
        if (user instanceof Admin) {
            this.userType = "admin";
        } else if (user instanceof Patient) {
            this.userType = "patient";
        } else if (user instanceof Medecin) {
            this.userType = "medecin";
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void clearSession() {
        currentUser = null;
        userType = null;
    }

    // Helper methods for convenience
    public boolean isAdmin() {
        return "admin".equals(userType);
    }

    public boolean isPatient() {
        return "patient".equals(userType);
    }

    public boolean isMedecin() {
        return "medecin".equals(userType);
    }

    public Admin getCurrentAdmin() {
        if (isAdmin() && currentUser instanceof Admin) {
            return (Admin) currentUser;
        }
        return null;
    }

    public Patient getCurrentPatient() {
        if (isPatient() && currentUser instanceof Patient) {
            return (Patient) currentUser;
        }
        return null;
    }

    public Medecin getCurrentMedecin() {
        if (isMedecin() && currentUser instanceof Medecin) {
            return (Medecin) currentUser;
        }
        return null;
    }
}