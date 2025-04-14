package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    final String URL = "jdbc:mysql://localhost:3306/chrono";
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


    public Connection getCnx() {
        return cnx;
    }

    public static MaConnexion getInstance() {
        if (instance == null)
        {
            instance = new MaConnexion();
        }

        return instance ;
    }

    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed();
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return false;
        }
    }
}
