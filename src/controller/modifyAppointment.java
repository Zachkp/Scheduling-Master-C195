package controller;

import helper.JDBC;
import helper.appointmentDB;
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

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class handles the modify appointment screen
 *
 * @author Zachariah Kordas-Potter
 */
public class modifyAppointment implements Initializable {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

    private static Appointment aptToBeModified = null;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button backButton;
    @FXML
    private TextArea description;
    @FXML
    private TextField titleField, aptLocationField, typeField, aptIdField, customerIdField, userIdField, startTimeField, endTimeField;
    @FXML
    private DatePicker startDatePicker, endDatePicker;
    @FXML
    private ComboBox contactBox;

    /**
     * Initializes the text fields with the data to be modified as well as initializing the back button
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

        description.setText(aptToBeModified.getDescription());
        titleField.setText(aptToBeModified.getTitle());
        aptLocationField.setText(aptToBeModified.getLocation());
        contactBox.setValue(aptToBeModified.getContact());
        typeField.setText(aptToBeModified.getType());
        startDatePicker.setValue(LocalDate.from(aptToBeModified.getStart()));
        endDatePicker.setValue(LocalDate.from(aptToBeModified.getEnd()));
        aptIdField.setText(Integer.toString(aptToBeModified.getId()));
        customerIdField.setText(Integer.toString(aptToBeModified.getCustId()));
        userIdField.setText(Integer.toString(aptToBeModified.getUserId()));

        LocalTime s = aptToBeModified.getStart().toLocalTime();
        String sf = s.format(formatter);
        startTimeField.setText(sf);

        LocalTime e = aptToBeModified.getEnd().toLocalTime();
        String ef = e.format(formatter);
        endTimeField.setText(ef);

        fillContactBox();
    }

    /**
     * Handles the save button being pressed by getting all the user inputted data and calling the errorChecker function
     * then adding them to the database
     *
     * @param event
     * @throws Exception
     */
    public void saveButtonPressed(ActionEvent event) throws Exception {
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm a");

        try {
            String title = titleField.getText();
            String location = aptLocationField.getText();
            String contact = (String) contactBox.getValue();
            String type = typeField.getText();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String desc = description.getText();
            LocalTime startTime = LocalTime.parse(startTimeField.getText(), formatterTime);
            LocalTime endTime = LocalTime.parse(endTimeField.getText(), formatterTime);
            String custId = customerIdField.getText();
            String userId = userIdField.getText();

            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            LocalDateTime end = LocalDateTime.of(endDate, endTime);

            if (errorChecker(title, location, contact, type, desc, start, end, custId, userId) == true) {
                appointmentDB.holdContact(contact);
                appointmentDB.modifyAppointment(aptToBeModified, aptToBeModified.getId(), title, location, type, desc,
                        start, end, custId, userId);
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/mainMenuScreen.fxml")));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
        } catch (NumberFormatException exception) {
            System.out.println("FAILED");
        }
    }

    /**
     * Checks for errors in user inputted data
     *
     * @param title    The title of the appointment
     * @param location The location of the appointment
     * @param contact  The contact for the appointment
     * @param type     The type of appointment
     * @param desc     The description of the appointment
     * @param start    The start date and time of the appointment
     * @param end      The end date and time of the appointment
     * @param custId   The customer ID for the appointment
     * @param userId   The user ID for the appointment
     * @return boolean true or false
     */
    public boolean errorChecker(String title, String location, String contact, String type, String desc,
                                LocalDateTime start, LocalDateTime end, String custId, String userId) {

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
        int close = 22;

        int sh = Integer.valueOf(fstHour);
        int eh = Integer.valueOf(fetHour);

        //CHECK BUSINESS HOURS
        if (formattedStartTime.indexOf("AM") > 0) {
            if (open > sh) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please enter a start time within business hours\nBUSINESS HOURS- 08:00 AM - 10:00 PM EST");
                alert.showAndWait();
                return false;
            }
        } else if (formattedStartTime.indexOf("PM") > 0) {
            if (sh > close) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please enter a start time within business hours\nBUSINESS HOURS- 08:00 AM - 10:00 PM EST");
                alert.showAndWait();
                return false;
            }
        }

        if (formattedEndTime.indexOf("AM") > 0) {
            if (open > eh) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please enter a end time within business hours\nBUSINESS HOURS- 08:00 AM - 10:00 PM EST");
                alert.showAndWait();
                return false;
            }
        } else if (formattedEndTime.indexOf("PM") > 0) {
            if (eh > close) {
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

    /**
     * This function queries the database in order to check that the userId that was entered is valid
     *
     * @param userId String userId entered by the user
     * @return int count
     */
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

    /**
     * Fills the contact box with the correct contact so the user may modify it
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
     * @param start LocalDateTime start the start time of the appointment
     * @param end   LocalDateTime end the endtime of the appointment
     * @return int count
     */
    public int getAptTimes(LocalDateTime start, LocalDateTime end) {
        int count = 0;
        int count2 = 0;

        try {
            String sql = "SELECT COUNT(Start) FROM client_schedule.appointments " +
                    "WHERE Start BETWEEN ? AND ? OR Start = ?" +
                    "OR End BETWEEN ? AND ? OR End = ?";

            String sql2 = "SELECT COUNT(Start) FROM client_schedule.appointments " +
                    "WHERE Start = ? AND End = ?";

            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ps.setTimestamp(5, Timestamp.valueOf(end));
            ps.setTimestamp(6, Timestamp.valueOf(end));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) count = rs.getInt(1);

            PreparedStatement ps2 = JDBC.getConnection().prepareStatement(sql2);
            ps2.setTimestamp(1, Timestamp.valueOf(start));
            ps2.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) count2 = rs2.getInt(1);

            if (count2 > 0) return 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void holdApt(Appointment selectedAppointment) {
        aptToBeModified = selectedAppointment;
    }
}
