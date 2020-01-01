package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import sample.ServerConnection;
import sample.WindowOperation;

import javafx.scene.control.TextField;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;


//Klasa pierwszego okna, okna logowania
public class Controller {

    @FXML
    private TextField login;
    @FXML
    private TextField port;
    @FXML
    private TextField host;


    private ServerConnection server;
    private boolean CONNECT = false;
    private WindowOperation window = new WindowOperation();

    public Controller() throws IOException {
        server = new ServerConnection();
    }

    public void initialize()
    {
        host.setText("127.0.0.1");
        port.setText("1234");
    }

    //metoda logowania
    @FXML
    public void logIn(ActionEvent actionEvent) throws IOException {


        //jeśli połączenie nie zostało jeszcze nawiązane
        if(!CONNECT)
        {
            //jeśli login nie został wpisany, nie zostanie podjęta próba połączenia w celu ułatwienia pracy serwera
            try {
                Integer.parseInt(port.getText());
                if(!login.getText().isEmpty()) CONNECT = server.connectServer(host.getText(), Integer.parseInt(port.getText()), window );
            }
            catch (Exception e)
            {
                window.warningWindow("Błąd", "Podano błędny port",
                        "Port powinien być wartością liczbową.", Alert.AlertType.ERROR);
            }

        }
        //jeśli połączenie już zostało nawiązane, a login jest unikalny, zostanie przełączone okno na kolejne
        if(CONNECT)
        {
            if(server.checkLogin(login.getText(), window))
            {
                window.goToNextWindow(actionEvent, "/resources/mainPanel.fxml", 932, 636);
            }
            else
                CONNECT = false;
        }

    }
}
