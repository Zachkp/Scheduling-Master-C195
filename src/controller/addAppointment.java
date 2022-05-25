package controller;

import com.sun.javafx.charts.Legend;
import helper.JDBC;
import helper.appointmentDB;
import helper.fill;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Appointment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Customer;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class handles all the functions of the add appointment screen
 *
 * @author Zachariah kordas-potter
 */
public class addAppointment implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    //FXML stuff
    @FXML
    private Button backButton;
    @FXML
    private TextField titleField, aptLocationField, typeField, aptIdField, userIdField, startTimeField, endTimeField, customerIdField;
    @FXML
    private TextArea description;
    @FXML
    private DatePicker startDatePicker, endDatePicker;
    @FXML
    private ComboBox contactBox;

    private static Customer cust = null;

    /**
     * Initializes the add Appointment form with the contact box data as well as initializing the back button
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        fillContactBox();

        customerIdField.setText(Integer.toString(cust.getId()));

    }

    /**
     * Handles when the save button is pressed. Checks for errors and gets all the user inputted data and finally creates
     * the new appointment
     *
     * @param event
     * @throws Exception
     */
    //TODO startTime to LocalTime
    public void saveButtonPressed(ActionEvent event) throws Exception {
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm a");


        try {
            String title = titleField.getText();
            String location = aptLocationField.getText();
            String contact = (String) contactBox.getValue();
            String type = typeField.getText();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            LocalTime startTime = LocalTime.parse(startTimeField.getText(), formatterTime);
            LocalTime endTime = LocalTime.parse(endTimeField.getText(), formatterTime);
            String desc = description.getText();
            String custId = customerIdField.getText();
            String userId = userIdField.getText();


            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            LocalDateTime end = LocalDateTime.of(endDate, endTime);


            if (errorChecker(title, location, contact, type, desc, start, end, custId, userId) == true) {
                System.out.println("---SAVE BUTTON WORKED---");
                appointmentDB.createAppointment(title, location, type, desc, start, end, contact, custId, userId);
                appointmentDB.holdContact(contact);

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

    /**
     * Checks for errors in user inputted data
     *
     * @param title    String title of the appointment
     * @param location String location of the appointment
     * @param contact  String contact of the appointment
     * @param type     String type of the appointment
     * @param desc     String description of the appointment
     * @param start    LocalDateTime start of the appointment
     * @param end      LocalDateTime end of the appointment
     * @param custId   String custId The customer ID that the user entered
     * @param userId   String userId the user Id that the user entered
     * @return boolean true or false
     */
    public boolean errorChecker(String title, String location, String contact, String type, String desc,
                                LocalDateTime start, LocalDateTime end, String custId, String userId) {
        //String start = startDate + " " + startTime;
        //String end = endDate + " " + endTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm a");

        int indexStart = start.format(formatter).indexOf("M");
        int indexEnd = end.format(formatter).indexOf("M");

        if (indexStart < 0 || indexEnd < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please specify AM or PM");
            alert.showAndWait();
            return false;
        }

        //CONVERT TO EST
        ZoneId est = ZoneId.of("US/Eastern");
        ZoneId userZone = ZoneId.systemDefault();
        LocalDateTime ns = start.atZone(userZone).withZoneSameInstant(est).toLocalDateTime();
        LocalDateTime ne = end.atZone(userZone).withZoneSameInstant(est).toLocalDateTime();
        String formattedStartTime = ns.format(formatterTime);
        String formattedEndTime = ne.format(formatterTime);
        String fstHour = formattedStartTime.substring(0, 2);
        String fetHour = formattedEndTime.substring(0, 2);

        int open = 8;
        int close = 10;

        //CHECK BUSINESS HOURS
        if (indexStart > 0) {
            if (Integer.valueOf(fstHour) < open) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please enter a start time within business hours\nBUSINESS HOURS- 08:00 AM - 10:00 PM EST");
                alert.showAndWait();
                return false;
            }
        } else if (indexEnd > 0) {
            if (Integer.valueOf(fstHour) > open) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please enter a start time within business hours\nBUSINESS HOURS- 08:00 AM - 10:00 PM EST");
                alert.showAndWait();
                return false;
            }
        }

        if (formattedEndTime.indexOf("AM") > 0) {
            if (open > Integer.valueOf(fetHour)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please enter a end time within business hours\nBUSINESS HOURS- 08:00 AM - 10:00 PM EST");
                alert.showAndWait();
                return false;
            }
        } else if (formattedEndTime.indexOf("PM") > 0) {
            if (Integer.valueOf(fetHour) < close) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please enter a end time within business hours\nBUSINESS HOURS- 08:00 AM - 10:00 PM EST");
                alert.showAndWait();
                return false;
            }
        }

        if (title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a title");
            alert.showAndWait();
            return false;
        } else if (location.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a location");
            alert.showAndWait();
            return false;
        } else if (contact == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter the contact information");
            alert.showAndWait();
            return false;
        } else if (type.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter the type of appointment you would like to create");
            alert.showAndWait();
            return false;
        } else if (desc.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a description of the appointment");
            alert.showAndWait();
            return false;
        } else if (start == null || start.isAfter(end)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid Start Date");
            alert.showAndWait();
            return false;
        } else if (end == null || end.isBefore(start)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid End Date");
            alert.showAndWait();
            return false;
        } else if (checkCustId(custId) == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid Customer Id");
            alert.showAndWait();
            return false;
        } else if (checkUserId(userId) == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a valid User Id");
            alert.showAndWait();
            return false;
        } else if (getAptTimes(start, end) > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please enter a start and end that don't overlap with other appointments");
            alert.showAndWait();
            return false;
        } else return true;
    }

    public static void holdApt(Customer selectedCustomer) {
        cust = selectedCustomer;
    }

    /**
     * Fills the contact combo box with all the contacts in the database
     */
    public void fillContactBox() {
        String contact;
        ArrayList<String> contacts = new ArrayList<String>();
        try {
            String sql = "SELECT Contact_Name FROM client_schedule.contacts";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                contact = rs.getString(1);
                contacts.add(contact);
            }
            contactBox.getItems().addAll(contacts);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function checks the database in order to make sure overlapping appointments arent happening
     *
     * @param start LocalDateTime start the start date-time of the appointment
     * @param end   LocalDateTime end the end date-time of the appointment
     * @return int count
     */
    public int getAptTimes(LocalDateTime start, LocalDateTime end) {
        int count = 0;

        try {
            String sql = "SELECT COUNT(Start) FROM client_schedule.appointments " +
                    "WHERE Start BETWEEN ? AND ? OR Start = ?" +
                    "OR End BETWEEN ? AND ? OR End = ?";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ps.setTimestamp(5, Timestamp.valueOf(end));
            ps.setTimestamp(6, Timestamp.valueOf(end));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * This function queries the database in order to check that the custId that was entered is valid
     *
     * @param custId String custId entered by the user
     * @return int count
     */
    public int checkCustId(String custId) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(Customer_ID) FROM client_schedule.customers WHERE Customer_ID = ?";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, custId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int checkUserId(String userId) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(User_ID) FROM client_schedule.users WHERE User_ID = ?";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
