package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import sample.ServerConnection;
import sample.WindowOperation;

import static sample.Main.END;

import java.awt.*;
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

    private int room_number = 0;

    private ServerConnection server = new ServerConnection();

    public void initialize() {
        Thread thread = new Thread(() -> {
            String message;
            String f_long;
            String s_long;
            textTyping.setDisable(true);
            textDisplay.setWrapText(true);
            textDisplay.setText("Admin : Jesteś w poczekalni, wybierz pokój do rozmowy.");
            while (END) {
                try {
                    message = server.getMessage();
                    if (message.startsWith("0") && room_number != 0) {

                        message = message.substring(1, message.length());
                        f_long = message.substring(0, 4);
                        f_long = f_long.replaceAll("^0*", "");
                        s_long = message.substring(Integer.parseInt(f_long) + 4, Integer.parseInt(f_long) + 8);
                        s_long = s_long.replaceAll("^0*", "");

                        textDisplay.appendText(message.substring(4, 4 + Integer.parseInt(f_long)));

                        textDisplay.appendText("  :  " + message.substring(Integer.parseInt(f_long) + 8, Integer.parseInt(f_long) + 8 + Integer.parseInt(s_long)) + "\n");
                    } else if (message.startsWith("1") && room_number != 0) {

                        message = message.substring(1, message.length());
                        textDisplay.appendText("\t\t\t\t\t\tUżytkownik " + message.substring(4, message.length()) + " dołączył do pokoju.\n");
                    } else if (message.startsWith("2") && room_number != 0) {

                        message = message.substring(1, message.length());
                        textDisplay.appendText("\t\t\t\t\t\tUżytkownik " + message.substring(4, message.length()) + " opuścił pokój.\n");
                    }
                } catch (IOException e) {
                    END = false;
                } catch (NullPointerException nu) {

                    textDisplay.setText("BŁĄD serwera, aplikacja zostanie wyłączona, spróbój uruchomić ponownie. ");
                    textDisplay.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
                    textDisplay.setStyle("-fx-text-inner-color: red");

                    {
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            ;
                        }
                        System.exit(0);
                    }
                }
            }
        });
        thread.start();
    }

    public void onEnter(ActionEvent actionEvent) throws IOException {
        if(!textTyping.getText().isEmpty()) {
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

    private void sendRoomToServer(String number) {
        textDisplay.clear();
        textTyping.clear();
        room_number = Integer.parseInt(number);
        if (!number.equals("0"))
            textTyping.setDisable(false);
        Thread thread = new Thread(() -> {
            try {
                server.changeRoom("2" + number);
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
        if (room_number != 0) sendRoomToServer("0");
    }

    @FXML
    public void firstRoom(ActionEvent actionEvent) {

        if (room_number != 1) sendRoomToServer("1");
        first.setStyle("-fx-background-color: #ffcc66; -fx-background-radius: 8em;");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        second.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
        fifth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em;");
    }

    @FXML
    public void secondRoom(ActionEvent actionEvent) {
        if (room_number != 2) sendRoomToServer("2");
        second.setStyle("-fx-background-color: #ffcc66; -fx-background-radius: 8em; ");
        first.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fifth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
    }

    @FXML
    public void thridRoom(ActionEvent actionEvent) {
        if (room_number != 3) sendRoomToServer("3");
        third.setStyle("-fx-background-color: #ffcc66;-fx-background-radius: 8em;  ");
        first.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        second.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fifth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
    }

    @FXML
    public void fourthRoom(ActionEvent actionEvent) {
        if (room_number != 4) sendRoomToServer("4");
        fourth.setStyle("-fx-background-color: #ffcc66; -fx-background-radius: 8em; ");
        first.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        second.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fifth.setStyle("-fx-background-color: #00cc00;-fx-background-radius: 8em;  ");
    }

    @FXML
    public void fifthRoom(ActionEvent actionEvent) {
        if (room_number != 5) sendRoomToServer("5");
        fifth.setStyle("-fx-background-color: #ffcc66;-fx-background-radius: 8em;  ");
        first.setStyle("-fx-background-color: #00cc00;-fx-background-radius: 8em;  ");
        second.setStyle("-fx-background-color: #00cc00;-fx-background-radius: 8em;  ");
        third.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        fourth.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
        wait.setStyle("-fx-background-color: #00cc00; -fx-background-radius: 8em; ");
    }
}
