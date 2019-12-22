package sample;

import javafx.scene.control.Alert;
import javafx.scene.text.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class ServerConnection {

    private static Socket clientSocket = null;

    public boolean connectServer(String host, int port, WindowOperation window) throws IOException {

        SocketAddress socketAddress = new InetSocketAddress(host, port);

        clientSocket = new Socket();
        int timeOut = 1000;

        try {
            clientSocket.connect(socketAddress, timeOut);
        } catch (SocketTimeoutException ex) {
            window.warningWindow("Błąd", "Zbyt długi czas oczekiwania",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            clientSocket.close();
            return false;
        } catch (IOException e) {
            window.warningWindow("Błąd", "Nie udało się podłączyć do servera",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            clientSocket.close();
            return false;
        }
        return true;
    }

    public void closeConnection() throws IOException {
        try
        {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println("3");
            clientSocket.close();
        } catch (Exception e)
        {
            ;
        }
    }

    public boolean checkLogin(String login, WindowOperation window) throws IOException {

        if (login.replaceAll(" ", "").isEmpty()) {
            window.warningWindow("Błąd", "Nie podano loginu",
                    "Wprowadź login.", Alert.AlertType.ERROR);
            return false;
        }
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

        String length = "";
        length = concatenateMessage(login);

        writer.println("0" + length + login);

        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String serverMessage = reader.readLine();

        if (serverMessage.equals("1")) {
            return true;
        } else {
            window.warningWindow("Błąd", "Podany login już istnieje",
                    "Wprowadź inny login.", Alert.AlertType.ERROR);
            return false;

        }
    }

    private String concatenateMessage(String login) {
        String length;
        if (login.length() < 10)
            length = "000" + Integer.toString(login.length());
        else if (login.length() < 100)
            length = "00" + Integer.toString(login.length());
        else if (login.length() < 1000)
            length = "0" + Integer.toString(login.length());
        else
            length = Integer.toString(login.length());
        return length;
    }

    public void sendMessage(String message) throws IOException {

        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        String length = concatenateMessage(message);

        writer.println("1" + length + message);
    }

    public String getMessage() throws IOException {

        String f_long;
        String s_long;
        String serverMessage = "00005ERROR0005ERROR";
        String message;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverMessage = reader.readLine();
            if (serverMessage.startsWith("0")) {
                serverMessage = serverMessage.substring(1, serverMessage.length());
                f_long = serverMessage.substring(0, 4);
                f_long = f_long.replaceAll("^0*", "");
                s_long = serverMessage.substring(Integer.parseInt(f_long) + 4, Integer.parseInt(f_long) + 8);
                s_long = s_long.replaceAll("^0*", "");
                message = serverMessage.substring(Integer.parseInt(f_long) + 8, serverMessage.length());
                while (message.length() < Integer.parseInt(s_long)) {
                    message = message + reader.readLine();
                }
                serverMessage = "0" + serverMessage.substring(0, 8 + Integer.parseInt(f_long)) + message;

            }
            else if (serverMessage.startsWith("1"))
            {
                serverMessage = serverMessage.substring(1,0);
                f_long = serverMessage.substring(0, 4);
                f_long = f_long.replaceAll("^0*", "");
                message = serverMessage.substring(4, serverMessage.length());
                while (message.length() < Integer.parseInt(f_long)) {
                    message = message + reader.readLine();
                }
                serverMessage = "1" + serverMessage.substring(0, 4) + message;
            }
            else if (serverMessage.startsWith("2"))
            {
                serverMessage = serverMessage.substring(1,0);
                f_long = serverMessage.substring(0, 4);
                f_long = f_long.replaceAll("^0*", "");
                message = serverMessage.substring(4, serverMessage.length());
                while (message.length() < Integer.parseInt(f_long)) {
                    message = message + reader.readLine();
                }
                serverMessage = "2" + serverMessage.substring(0, 4) + message;
            }
        } catch (Exception e)
        {
            ;
        }


        return serverMessage;
    }

    public void changeRoom(String number) throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        writer.println(number);
    }


}
