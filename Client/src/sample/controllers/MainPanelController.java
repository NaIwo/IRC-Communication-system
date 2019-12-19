package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import sample.ServerConnection;

import static sample.Main.END;

import java.io.IOException;

public class MainPanelController {

    @FXML
    private TextField textTyping;
    @FXML
    private TextArea textDisplay;
    @FXML
    private Button wait;
    @FXML
    private Button first;
    @FXML
    private Button second;
    @FXML
    private Button third;
    @FXML
    private Button fourth;
    @FXML
    private Button fifth;


    private ServerConnection server = new ServerConnection();
    public void initialize()
    {
        Thread thread = new Thread(() -> {
            String message;
            String f_long;
            String s_long;
            textTyping.setDisable(true);
            textDisplay.setText("Admin : Jesteś w poczekalni, wybierz pokój do rozmowy.");
            while(END) {
                try {
                    message = server.getMessage();
                    if(message.startsWith("0")) {
                        f_long = message.substring(0, 4);
                        f_long = f_long.replaceAll("^0*", "");
                        s_long = message.substring(Integer.parseInt(f_long) + 4, Integer.parseInt(f_long) + 8);
                        s_long = s_long.replaceAll("^0*", "");
                        textDisplay.setWrapText(true);
                        textDisplay.setFont(Font.font ("Brush Script MT", 10));
                        textDisplay.setStyle("-fx-text-fill: black ;") ;
                        textDisplay.appendText(message.substring(4, 4 + Integer.parseInt(f_long)));
                        textDisplay.setFont(Font.font (" Bold Italic", 15));
                        textDisplay.setStyle("-fx-text-fill:  #660066 ;") ;
                        textDisplay.appendText("  :  " + message.substring(Integer.parseInt(f_long) + 8, Integer.parseInt(f_long) + 8 + Integer.parseInt(s_long)) + "\n");
                    }
                    else
                    {
                        textDisplay.setWrapText(true);
                        textDisplay.setFont(Font.font ("Brush Script MT", 10));
                        textDisplay.setStyle("fx-text-inner-color: red;") ;
                        textDisplay.appendText("default : ");
                        textDisplay.setFont(Font.font ("Bold Italic", 15));
                        textDisplay.setStyle("-fx-text-fill:  #660066 ;") ;
                        textDisplay.appendText(message + "\n");
                    }
                } catch (IOException e) {
                    END = false;
                }
            }
        });
        thread.start();
    }

    public void onEnter(ActionEvent actionEvent) throws IOException {
        if(!textTyping.equals(""))
        {
            Thread thread = new Thread(() -> {
                try {
                    server.sendMessage(textTyping.getText());
                    textTyping.clear();
                } catch (IOException e) {
                    ;
                }
            });
            thread.start();
        }
    }

    private void sendRoomToServer(String number)
    {
        textDisplay.clear();
        textTyping.clear();
        if(!number.equals("0"))
            textTyping.setDisable(false);
        Thread thread = new Thread(() -> {
            try {
                server.changeRoom("2" + "000" + number);
            } catch (IOException e) {
                ;
            }
        });
        thread.start();
    }
    @FXML
    public void waitingRoom(ActionEvent actionEvent) {
        textDisplay.clear();
        textTyping.clear();
        textTyping.setDisable(true);
        wait.setStyle("-fx-background-color: #ffcc66; -fx-background-radius: 8em; ");
        first.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;  ");
        second.setStyle("-fx-background-color: #00cc00;  -fx-background-radius: 8em; ");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;  ");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fifth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        sendRoomToServer("0");
    }

    @FXML
    public void firstRoom(ActionEvent actionEvent) {

        sendRoomToServer("1");
        first.setStyle("-fx-background-color: #ffcc66; -fx-background-radius: 8em;");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        second.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        fifth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
    }
    @FXML
    public void secondRoom(ActionEvent actionEvent) {
        sendRoomToServer("2");
        second.setStyle("-fx-background-color: #ffcc66; -fx-background-radius: 8em; ");
        first.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fifth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
    }
    @FXML
    public void thridRoom(ActionEvent actionEvent) {
        sendRoomToServer("3");
        third.setStyle("-fx-background-color: #ffcc66;-fx-background-radius: 8em;  ");
        first.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        second.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fifth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
    }
    @FXML
    public void fourthRoom(ActionEvent actionEvent) {
        sendRoomToServer("4");
        fourth.setStyle("-fx-background-color: #ffcc66; -fx-background-radius: 8em; ");
        first.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        second.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fifth.setStyle("-fx-background-color: #00cc00;-fx-background-radius: 8em;  ");
    }
    @FXML
    public void fifthRoom(ActionEvent actionEvent) {
        sendRoomToServer("5");
        fifth.setStyle("-fx-background-color: #ffcc66;-fx-background-radius: 8em;  ");
        first.setStyle("-fx-background-color: #00cc00;-fx-background-radius: 8em;  ");
        second.setStyle("-fx-background-color: #00cc00;-fx-background-radius: 8em;  ");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
    }
}
