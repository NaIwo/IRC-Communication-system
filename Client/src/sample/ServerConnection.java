package sample;

import javafx.scene.control.Alert;
import javafx.scene.text.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

//Klasa obsługująca akcje powiązane z udziałem serwera
public class ServerConnection {

    private static Socket clientSocket = null;
    private static String host;
    private static int port;

    //Metoda próbuje nawiązać połączenie z serwerem i w przypadku nieudanej próby, wyświetla odpowiedni komunikat
    public boolean connectServer(String host, int port, WindowOperation window) throws IOException {

        ServerConnection.host = host;
        ServerConnection.port = port;
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        clientSocket = new Socket();
        //zmienna określająca maksymalny czas, który można poświęcić na nawiązania połączenia, w przypadku przekroczenia, zwracany jest błąd
        int timeOut = 1000;

        //blok try catch wychwytujący błędy połączenia
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
        } catch (Exception e) {
            window.warningWindow("Błąd", "Nie udało się podłączyć do servera",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            clientSocket.close();
            return false;
        }
        return true;
    }

    //metoda zamykająca połączenie z serwerem
    public void closeConnection() throws IOException {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println("3");
            clientSocket.close();
        } catch (Exception e) {
            ;
        }
    }

    //Metoda wysyłająca login uzytkownika do serwera w celu sprawdzenia unikalności, jeśli login istnieje, serwer wyśle odpowiednią informacje
    public boolean checkLogin(String login, WindowOperation window) throws IOException {

        //jeśli login nie został podany, nie zostanie wysłana informacja
        if (login.replaceAll(" ", "").isEmpty()) {
            window.warningWindow("Błąd", "Nie podano loginu",
                    "Wprowadź login.", Alert.AlertType.ERROR);
            return false;
        }
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

        String length = "";
        length = concatenateMessage(login);

        writer.println("0" + length + login);
        //blok czeka na odpowiedź od serwera w celu potwierdzenia lub zaprzeczenia unikalności, jeśli nie jest unikalny to zstanei wyświetlony odpowiedni komunikat
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String serverMessage = reader.readLine();

            if (serverMessage.equals("1")) {
                return true;
            } else {
                window.warningWindow("Błąd", "Podany login już istnieje",
                        "Wprowadź inny login.", Alert.AlertType.ERROR);
                clientSocket.close();
                return false;

            }
        } catch (Exception in) {
            window.warningWindow("Błąd", "Nie udało się wysłać danych do serwera.",
                    "Możliwe, że serwer jest nieosiągalny, spróbuj ponownie.", Alert.AlertType.ERROR);
            clientSocket.close();
            return false;
        }
    }
// metoda służaca dopełneniu zerami długości wiadomości do 4 bitów
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

    //wysyłanie wiadomości od klienta do serwera
    public void sendMessage(String message) throws IOException {

        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        String length = concatenateMessage(message);

        writer.println("1" + length + message);
    }

    //odbierania wiadomości od innych użytkowników wraz z sprawdzaniem poprawności długości wiadomości
    public String getMessage() throws IOException {

        String f_long;
        String s_long;
        String serverMessage = "00005ERROR0005ERROR";
        String message;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverMessage = reader.readLine();
            // 0 oznacza nową wiadomość od innych użytkowników w pokoju
            if (serverMessage.startsWith("0")) {
                serverMessage = serverMessage.substring(1, serverMessage.length());
                f_long = serverMessage.substring(0, 4);
                f_long = f_long.replaceAll("^0*", "");
                s_long = serverMessage.substring(Integer.parseInt(f_long) + 4, Integer.parseInt(f_long) + 8);
                s_long = s_long.replaceAll("^0*", "");
                message = serverMessage.substring(Integer.parseInt(f_long) + 8, serverMessage.length());

                //jeśli wiadomość została sfragmentowana to pętla poniższa będzie czytać tak długo aż zapisze całą wiadomość
                while (message.length() < Integer.parseInt(s_long)) {
                    message = message + reader.readLine();
                }
                serverMessage = "0" + serverMessage.substring(0, 8 + Integer.parseInt(f_long)) + message;

                // 1 oznacza dołączenie nowego użytkownia do pokoju
            } else if (serverMessage.startsWith("1")) {
                serverMessage = serverMessage.substring(1, serverMessage.length());
                f_long = serverMessage.substring(0, 4);
                f_long = f_long.replaceAll("^0*", "");
                message = serverMessage.substring(4, serverMessage.length());
                //jeśli wiadomość została sfragmentowana to pętla poniższa będzie czytać tak długo aż zapisze całą wiadomość
                while (message.length() < Integer.parseInt(f_long)) {
                    message = message + reader.readLine();
                }
                serverMessage = "1" + serverMessage.substring(0, 4) + message;
                // 2 oznacza, że użytkownik opuścił pokój
            } else if (serverMessage.startsWith("2")) {
                serverMessage = serverMessage.substring(1, serverMessage.length());
                f_long = serverMessage.substring(0, 4);
                f_long = f_long.replaceAll("^0*", "");
                message = serverMessage.substring(4, serverMessage.length());
                //jeśli wiadomość została sfragmentowana to pętla poniższa będzie czytać tak długo aż zapisze całą wiadomość
                while (message.length() < Integer.parseInt(f_long)) {
                    message = message + reader.readLine();
                }
                serverMessage = "2" + serverMessage.substring(0, 4) + message;
            }
        } catch (Exception e) {
            ;
        }


        return serverMessage;
    }

    //Klient za pomocą tej metody wysyła informację o zmianie pokoju na inny
    public void changeRoom(String number) throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        writer.println(number);
    }


}
