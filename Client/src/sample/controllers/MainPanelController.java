package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    private ServerConnection server = new ServerConnection();
    public void initialize()
    {
        Thread thread = new Thread(() -> {
            String message;
            String f_long;
            String s_long;
            while(END) {
                try {
                    message = server.getMessage();
                    if(message.startsWith("0")) {
                        f_long = message.substring(0, 4);
                        f_long = f_long.replaceAll("^0*", "");
                        s_long = message.substring(Integer.parseInt(f_long) + 4, Integer.parseInt(f_long) + 8);
                        s_long = s_long.replaceAll("^0*", "");
                        textDisplay.setFont(Font.font ("Brush Script MT", 10));
                        textDisplay.setStyle("-fx-text-fill: black ;") ;
                        textDisplay.appendText(message.substring(4, 4 + Integer.parseInt(f_long)));
                        textDisplay.setFont(Font.font (" Bold Italic", 15));
                        textDisplay.setStyle("-fx-text-fill:  #660066 ;") ;
                        textDisplay.appendText("  :  " + message.substring(Integer.parseInt(f_long) + 8, Integer.parseInt(f_long) + 8 + Integer.parseInt(s_long)) + "\n");
                    }
                    else
                    {
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

}
