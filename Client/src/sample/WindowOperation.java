package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class WindowOperation {

    public void goToNextWindow(ActionEvent actionEvent, String path, int width, int height) throws IOException {
        Parent log_root = FXMLLoader.load(getClass().getResource(path));
        Scene scene = new Scene(log_root, width, height);
        Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }


    @FXML
    public void warningWindow(String title, String header, String text, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);

        alert.showAndWait();
    }
}
