package org.example.util;

import org.example.models.User;

/**
 * Singleton class to manage user sessions
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    // Private constructor to prevent instantiation
    private SessionManager() {
    }

    /**
     * Gets the singleton instance of SessionManager
     * @return the singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Sets the current logged-in user
     * @param user the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current logged-in user
     * @return the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the current session
     */
    public void clearSession() {
        this.currentUser = null;
    }

    /**
     * Checks if a user is logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}