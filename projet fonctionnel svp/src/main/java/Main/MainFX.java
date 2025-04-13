package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        // TRÈS IMPORTANT: Initialiser la base de données AVANT de charger la vue
        DatabaseConnection.getInstance();

        primaryStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/views/liste_historique.fxml"));
        Scene scene = new Scene(root);

        stage.setTitle("Gestion des Historiques Médicaux");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Vous pouvez aussi l'initialiser ici comme alternative
        DatabaseConnection.getInstance();
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}