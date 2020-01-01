package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


// Główna klasa uruchamiająca pierwsze okno aplikacji i kończąca wątki podczas wyłączenia aplikacji
public class Main extends Application {

public static boolean END = true;


    private static ServerConnection server = new ServerConnection();

    //Metoda rozpoczynająca działanie aplikacji
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../resources/sample.fxml"));
        primaryStage.setTitle("Client");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 536, 296));
        primaryStage.show();
    }

    //Główna metoda projektu, po zakończeniu programu wyłączenie wątków i zamknięcie połączenia
    public static void main(String[] args) throws IOException {
        launch(args);
        END = false;
        server.closeConnection();
    }
}
