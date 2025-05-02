package org.example.util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.IkonResolver;

public class MainFX extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RoleSelection.fxml"));


        Parent root= loader.load();
        //2-scene
        Scene scene = new Scene(root);
        

        //3-stage
        stage.setTitle("ChronoSerena");
        stage.setScene(scene);
        stage.show();

        // Initialize chat bubble on application start
        
    }
    public static void main(String[] args) {

        launch(args);
    }
}

