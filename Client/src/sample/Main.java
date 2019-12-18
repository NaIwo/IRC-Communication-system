package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

public static boolean END = true;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../resources/sample.fxml"));
        primaryStage.setTitle("Client");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 536, 296));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
        END = false;
    }
}
