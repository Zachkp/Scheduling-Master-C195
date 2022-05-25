package controller;

import helper.JDBC;
import helper.customerDB;
import helper.fill;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;
import model.*;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class handles all controls for the modify customer screen
 */
public class modifyCustomer implements Initializable {


    private static Customer custToBeModified = null;
    private static String userSelection = null;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button backButton;
    @FXML
    private ComboBox countryComboBox, stateComboBox;
    @FXML
    private TextField customerName;
    @FXML
    private TextField customerAddress;
    @FXML
    private TextField customerPostalCode;
    @FXML
    private TextField customerPhoneNum;
    @FXML
    private TextField customerId;


    /**
     * This function initializes the text fields with the data from the user selected customer
     * <p>
     * Lambda -fillCountry fill- expression that uses SQL to fill the country combo box with the different countries from the
     * database
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //LAMBDA 1
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

        //LAMBDA 2
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
        getComboBoxData();

        stateComboBox.setDisable(false);
        customerName.setText(custToBeModified.getName());
        customerAddress.setText(custToBeModified.getAddress());
        customerPostalCode.setText(custToBeModified.getPostalCode());
        customerPhoneNum.setText(custToBeModified.getPhoneNum());
        customerId.setText(Integer.toString(custToBeModified.getId()));
        countryComboBox.setValue(custToBeModified.getCountry());
        stateComboBox.setValue(custToBeModified.getState());

    }

    /**
     * Fills the combo boxes based off the users country selection by Querying the database
     */
    public void getComboBoxData() {
        stateComboBox.getItems().clear();
        String userSelectionA = (String) countryComboBox.getValue();

        if (countryComboBox.getSelectionModel().isEmpty()) userSelection = custToBeModified.getCountry();
        else userSelection = userSelectionA;

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
     * This function handles the save button by getting all the user entered data and then sending it to the customerDB
     *
     * @param event
     * @throws Exception
     */
    public void saveButtonPressed(ActionEvent event) throws Exception {
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

                Customer newCustomer = new Customer(custToBeModified.getId(), name, phoneNum, customerCountry,
                        customerState, address, postalCode);
                customerDB.modifyCustomer(custToBeModified.getId(), name, phoneNum, address, postalCode);

                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/mainMenuScreen.fxml")));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }

        } catch (NumberFormatException exception) {
            exception.printStackTrace();
        }
    }

    public static void holdCustomer(Customer selectedCustomer) {
        custToBeModified = selectedCustomer;
    }

    private int getIndex(Customer custToBeFound) {
        ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
        allCustomers = customerDB.getAllCustomers();
        return allCustomers.indexOf(custToBeFound);
    }
}
