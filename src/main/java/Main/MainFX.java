package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));


        //1-element graphique
        //AnchorPane pane = loader.load();
           Parent root= loader.load();
        //2-scene
        Scene scene = new Scene(root);

        //3-stage
        stage.setTitle("ChronoSerena");
        stage.setScene(scene);
        stage.show();


    }
    public static void main(String[] args) {

        launch(args);
    }
}
