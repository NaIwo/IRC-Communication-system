package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import sample.ServerConnection;
import sample.WindowOperation;

import javafx.scene.control.TextField;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {

    @FXML
    private TextField login;
    @FXML
    private TextField port;
    @FXML
    private TextField host;
;

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

    @FXML
    public void logIn(ActionEvent actionEvent) throws IOException {


        if(!CONNECT)
        {
            try {
                Integer.parseInt(port.getText());
                CONNECT = server.connectServer(host.getText(), Integer.parseInt(port.getText()), window );
            }
            catch (Exception e)
            {
                window.warningWindow("Błąd", "Podano błędny port",
                        "Port powinien być wartością liczbową.", Alert.AlertType.ERROR);
            }

        }
        if(CONNECT)
        {
            if(server.checkLogin(login.getText(), window))
            {
                window.goToNextWindow(actionEvent, "/resources/mainPanel.fxml", 932, 636);
            }
        }

    }
}
