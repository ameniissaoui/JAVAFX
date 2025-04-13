//package org.example;
//
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//
//public class MainFX extends Application {
//
//
//    @Override
//    public void start(Stage stage)   {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterHist.fxml"));
//
//
//        //1-element graphique
//        //AnchorPane pane = loader.load();
//
//        try {
//            Parent root = loader.load();
//            //2-scene
//            Scene scene = new Scene(root);
//
//            //3-stage
//            stage.setTitle("ChronoSerena");
//            stage.setScene(scene);
//            stage.show();
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//
//
//
//    }
//    public static void main(String[] args) {
//
//        launch(args);
//    }
//}
