package controller;

import helper.JDBC;
import helper.customerDB;
import helper.fill;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Customer;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class handles all the controls for the addCustomer screen
 */
public class addCustomer implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private ComboBox countryComboBox, stateComboBox;
    @FXML
    private Button backButton, saveButton;
    @FXML
    private TextField customerName, customerAddress, customerPostalCode, customerPhoneNum, customerId;

    /**
     * Initializes the form with initial combo box data
     *
     * @param url            an absolute URL giving the base location
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stateComboBox.setDisable(true);

        fill fillCountry = () -> {
            try {
                String sql = "SELECT Country FROM client_schedule.countries";
                PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String country = rs.getString(1);
                    countryComboBox.getItems().addAll(country);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        fillCountry.fill();

        backButton.setOnAction(e -> {
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/mainMenuScreen.fxml")));
                stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * This function takes in all the user entered data and checks it for errors then displays the corresponding alert
     *
     * @param name       The name of the customer
     * @param phoneNum   The phone number of the customer
     * @param address    The address of the customer
     * @param postalCode The customers postal code
     * @return Boolean true or false
     */
    public boolean checkForErrors(String name, String phoneNum, String address, String postalCode) {
        if (phoneNum.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid phone number with area code");
            alert.showAndWait();
            return false;
        } else if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a name");
            alert.showAndWait();
            return false;
        } else if (countryComboBox.getSelectionModel().isEmpty() == true) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please select a country");
            alert.showAndWait();
            return false;
        } else if (stateComboBox.getSelectionModel().isEmpty() == true) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please select a state");
            alert.showAndWait();
            return false;
        } else if (address.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter an address");
            alert.showAndWait();
            return false;
        } else if (postalCode.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a postal code");
            alert.showAndWait();
            return false;
        } else return true;
    }

    /**
     * Fills the combo boxes based off the users country selection by Querying the database
     *
     * @param event handles initiating the function when the user activates the combo boxes
     */
    public void getComboBoxData(ActionEvent event) {
        String userSelection = (String) countryComboBox.getValue();
        stateComboBox.setDisable(false);
        try {
            String sql = "SELECT Division FROM client_schedule.first_level_divisions WHERE Country_ID = (SELECT Country_ID FROM client_schedule.countries WHERE Country = ?)";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, userSelection);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String state = rs.getString(1);
                stateComboBox.getItems().addAll(state);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action of the user pressing the save button by collecting user inputted data then calling
     * the checkForErrors() function
     *
     * @param event
     * @throws IOException
     */
    public void saveButtonPressed(ActionEvent event) throws IOException {
        System.out.println("---SAVE BUTTON PRESSED---");
        try {
            String name = customerName.getText();
            String address = customerAddress.getText();
            String postalCode = customerPostalCode.getText();
            String phoneNum = customerPhoneNum.getText();
            String customerCountry = (String) countryComboBox.getValue();
            String customerState = (String) stateComboBox.getValue();

            if (checkForErrors(name, phoneNum, address, postalCode) == true) {
                System.out.println("SAVE BUTTON WORKED");
                customerDB.createCustomer(name, address, postalCode, phoneNum, customerState);

                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/mainMenuScreen.fxml")));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
        } catch (NumberFormatException exception) {
            System.out.println("FAIL");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Blank Text Field Error");
            alert.setContentText("Failed");
            alert.showAndWait();
            exception.printStackTrace();
        }
    }
}
