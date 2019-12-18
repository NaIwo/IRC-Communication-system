package sample.controllers;

import javafx.event.ActionEvent;

import java.io.*;
import java.net.Socket;

public class Controller {

    public void check(ActionEvent actionEvent) throws IOException {

        Socket clientSocket = new Socket("127.0.0.2", 1234);
        //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //String serverMessage = reader.readLine();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        byte[] buffer = new byte[100];
        String serverMessage = reader.readLine();
        System.out.println(serverMessage);

        String clientMessage = "hello";
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        writer.println(clientMessage);

        clientSocket.close();

    }
}
