package com.example.sante;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
      // FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
       // FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("addevent.fxml"));
      // FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("test.fxml"));
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("listevent.fxml"));
//FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("eventFront.fxml"));
    //  FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reservationFront.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}