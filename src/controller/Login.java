package controller;

import helper.JDBC;

import helper.appointmentDB;
import helper.defaultZoneId;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Appointment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class handles all functions of the Login Screen including getting the users location, default language, and opens
 * the connection to the database
 *
 * @author Zachariah Kordas-Potter
 */
public class Login implements Initializable {
    private static int num = 1;
    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Label languageLabel, mainLabel, locationLabel;
    @FXML
    private Button loginButton;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField usernameField;

    /**
     * Initializes the screen by getting the users location, preferred language, and opening the connection to the database
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ZoneId zone = ZoneId.systemDefault();
        String location = zone.toString();

        System.out.println("Initialized-- LOGIN SCREEN");

        defaultZoneId userZone = () -> locationLabel.setText(location);
        userZone.setDefaultZoneId();
        JDBC.openConnection();
        languageUpdaterV2();
    }

    /**
     * This function handles the login button. It writes to login_activity.txt in order to keep track of all login attempts
     * It also collects the entered username and password then checks them against the database.
     *
     * @param event
     * @throws IOException
     */
    public void loginButtonPressed(ActionEvent event) throws IOException {
        File f = new File("src/login_activity.txt");
        FileWriter fw = new FileWriter(f.getName(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateTime = dtf.format(now).toString();

        String username = usernameField.getText();
        String password = passwordField.getText();
        String language = System.getProperty("user.language");

        if (loginCheckUser(username) == null || loginCheckPass(password) == null) {
            bw.write("USER- " + username + " PASSWORD- " + password + " TIMESTAMP " + dateTime + " FAILED LOGIN\n\n");
            bw.close();

            if (Locale.getDefault().getLanguage().equals("fr")) {
                errorCheckFr(password, username);
            } else if (Locale.getDefault().getLanguage().equals("en")) {
                errorCheckEn(password, username);
            }
        } else {
            System.out.println("--LOGGING IN--");
            bw.write("USER- " + username + " PASSWORD- " + password + " TIMESTAMP " + dateTime + " SUCCESSFUL LOGIN\n\n");
            bw.close();
            appointmentDB.holdUserAndPassword(username, password);
            aptWithin15();
            JDBC.openConnection();

            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/mainMenuScreen.fxml")));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * This function collects the users computers default language and changes the labels accordingly using the
     * resourcebundle nat_fr.properties
     */
    public void languageUpdaterV2() {
        ResourceBundle rb = ResourceBundle.getBundle("resourceBundles/nat", Locale.getDefault());
        if (Locale.getDefault().getLanguage().equals("fr")) {
            mainLabel.setText(rb.getString("loginLabel"));
            usernameField.setPromptText(rb.getString("username"));
            passwordField.setPromptText(rb.getString("password"));
            loginButton.setText(rb.getString("login"));
            languageLabel.setText(Locale.getDefault().getLanguage().toString());
        }
    }

    /**
     * This function takes in the user entered password and username if the language is french then using the
     * resourcebundle nat_fr.properties translates the errors into french
     *
     * @param password The user entered password
     * @param username The user entered username
     */
    public void errorCheckFr(String password, String username) {
        ResourceBundle rb = ResourceBundle.getBundle("resourceBundles/nat", Locale.getDefault());
        if (username.isEmpty() && password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(rb.getString("error") + " " + rb.getString("error") + " " + rb.getString("error"));
            alert.setContentText(rb.getString("userPassError"));
            alert.showAndWait();
        } else if (username.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(rb.getString("error") + " " + rb.getString("error") + " " + rb.getString("error"));
            alert.setContentText(rb.getString("usernameError"));
            alert.showAndWait();
        } else if (password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(rb.getString("error") + " " + rb.getString("error") + " " + rb.getString("error"));
            alert.setContentText(rb.getString("passwordError"));
            alert.showAndWait();
        } else if (loginCheckUser(username) == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText(rb.getString("usernameError"));
            alert.showAndWait();
        } else if (loginCheckPass(password) == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(rb.getString("error") + " " + rb.getString("error") + " " + rb.getString("error"));
            alert.setContentText(rb.getString("passwordError"));
            alert.showAndWait();
        }
    }

    /**
     * This function takes in the user entered password and username then checks them against the database and displays
     * error messages in english
     *
     * @param password The user entered password
     * @param username The user entered username
     */
    public void errorCheckEn(String password, String username) {
        if (username.isEmpty() && password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid username and password");
            alert.showAndWait();
        } else if (username.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid username");
            alert.showAndWait();
        } else if (password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid password");
            alert.showAndWait();
        } else if (loginCheckUser(username) == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Username not in Database");
            alert.showAndWait();
        } else if (loginCheckPass(password) == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Password not found in database");
            alert.showAndWait();
        }
    }

    /**
     * This function checks the user entered username against the database to validate it.
     *
     * @param username String username of the user
     * @return un The username that was collected. If null is returned then the user entered username is invalid
     */
    public static String loginCheckUser(String username) {
        String un = null;
        try {
            String sql = "SELECT User_Name FROM client_schedule.users WHERE User_Name = " + "\"" + username + "\"";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                un = rs.getString("User_Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return un;
    }

    /**
     * This function checks the user entered password against the database to validate it.
     *
     * @param password password the user entered password
     * @return pw The password that was collected. If null is returned then the user entered passowrd is invalid
     */
    public static String loginCheckPass(String password) {
        String pw = null;
        try {
            String sql = "SELECT Password FROM client_schedule.users WHERE Password = " + "\"" + password + "\"";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                pw = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pw;
    }

    /**
     * This function checks the database in order to see if there is a appointment within 15 minutes
     */
    public void aptWithin15() {
        LocalDateTime time = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        try {
            String sql = "SELECT Start FROM client_schedule.appointments WHERE Start >= NOW() AND Start <= NOW() + INTERVAL 15 MINUTE";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                time = rs.getTimestamp("Start").toLocalDateTime();
            }

            if (time == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("You do not have an appointment within 15 minutes!");
                alert.showAndWait();
            } else {
                String formattedTime = time.format(formatter);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("You have an appointment within 15 minutes at " + formattedTime + "!");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
