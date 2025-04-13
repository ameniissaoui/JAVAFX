package Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    Connection cnx;
    public static DatabaseConnection instance;

    public Connection getCnx() {
        return cnx;
    }

    public DatabaseConnection() {
        String Url = "jdbc:mysql://localhost/santee";
        String Username = "root";
        String Password = "";

        try {
            cnx = DriverManager.getConnection(Url, Username, Password);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}