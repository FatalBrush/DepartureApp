package ch.fhnw.oop2.module10;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Seb on 12.05.2016.
 */
public class DepartureStarter extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception{
        DepartureModel departurePM = new DepartureModel();
        Parent rootPanel = new DepartureUI(departurePM);

        Scene scene = new Scene(rootPanel);

        String stylesheet = getClass().getResource("style.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);

        primaryStage.titleProperty().bind(departurePM.titleProperty());
        primaryStage.setMinWidth(1098);
        primaryStage.setMinHeight(542);
        primaryStage.centerOnScreen();
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args){ launch(args);  }
}
