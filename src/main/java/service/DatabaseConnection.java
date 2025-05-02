package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Paramètres de connexion à la base de données
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/sante";
    private static final String USER = "root"; // Remplacez par votre nom d'utilisateur si différent
    private static final String PASSWORD = ""; // Remplacez par votre mot de passe si vous en avez un

    private static Connection connection;

    /**
     * Obtient une connexion à la base de données
     * @return Une connexion active à la base de données
     * @throws SQLException Si une erreur de connexion se produit
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Chargement du pilote JDBC (recommandé bien que facultatif depuis Java 6)
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Établissement de la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données établie avec succès !");
            } catch (ClassNotFoundException e) {
                System.err.println("Pilote MySQL introuvable : " + e.getMessage());
                throw new SQLException("Pilote MySQL introuvable", e);
            }
        }
        return connection;
    }

    /**
     * Ferme la connexion à la base de données
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion à la base de données fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}