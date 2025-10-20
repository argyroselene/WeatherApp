package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WeatherUI.fxml"));

        Scene scene = new Scene(loader.load(),800,600);
        scene.getStylesheets().add(getClass().getResource("light.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Weather Application");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
