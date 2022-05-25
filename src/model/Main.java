package model;

import helper.JDBC;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

/**
 * This is the main class of the application, it starts up the initial screen
 */
public class Main extends Application {

    /**
     * Initiates the JavaFX GUI by loading the login screen
     *
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/loginScreen.fxml"));
            stage.setTitle("Login Screen");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Launches the program as well as closes the connection when on the
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
        JDBC.closeConnection();
    }
}
