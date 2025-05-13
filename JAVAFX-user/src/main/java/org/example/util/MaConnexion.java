package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    final String URL = "jdbc:mysql://localhost:3306/finalee";
    final String USER = "root";
    final String PASS = "";
    private Connection cnx;

    private static MaConnexion instance;

    private MaConnexion() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connexion OK");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets an active connection to the database.
     * If the current connection is closed, creates a new one.
     * @return An active database connection
     */
    public Connection getCnx() {
        try {
            // Check if connection is closed or null
            if (cnx == null || cnx.isClosed()) {
                // Reconnect
                System.out.println("Connection closed, reconnecting...");
                cnx = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Reconnexion OK");
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
            // Try to reconnect one more time
            try {
                cnx = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Reconnexion successful after error");
            } catch (SQLException ex) {
                System.err.println("Failed to reconnect: " + ex.getMessage());
                throw new RuntimeException("Database connection failed", ex);
            }
        }
        return cnx;
    }

    public static MaConnexion getInstance() {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }

    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed() && cnx.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Forces reconnection to the database
     */
    public void reconnect() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
            }
            cnx = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Forced reconnection successful");
        } catch (SQLException e) {
            System.err.println("Failed to force reconnect: " + e.getMessage());
            throw new RuntimeException("Database reconnection failed", e);
        }
    }
}
