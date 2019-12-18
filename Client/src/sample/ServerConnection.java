package sample;

import javafx.scene.control.Alert;

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

        try{
            clientSocket.connect(socketAddress, timeOut);
        }
        catch (SocketTimeoutException ex)
        {
            window.warningWindow("Błąd", "Zbyt długi czas oczekiwania",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            clientSocket.close();
            return false;
        } catch (IOException e)
        {
            window.warningWindow("Błąd", "Nie udało się podłączyć do servera",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            clientSocket.close();
            return false;
        }
        return true;
    }

    public void closeConnection() throws IOException {
        clientSocket.close();
    }

    public boolean checkLogin(String login, WindowOperation window) throws IOException {

        if(login.replaceAll(" ", "").isEmpty())
        {
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

        if (serverMessage.equals("1"))
        {
            return true;
        }
        else
        {
            window.warningWindow("Błąd", "Podany login już istnieje",
                    "Wprowadź inny login.", Alert.AlertType.ERROR);
            return false;

        }
    }

    private String concatenateMessage(String login) {
        String length;
        if(login.length() < 10)
            length = "000" + Integer.toString(login.length());
        else if(login.length() < 100)
            length = "00" + Integer.toString(login.length());
        else if(login.length() < 1000)
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String serverMessage = reader.readLine();
        return serverMessage;
    }


}
