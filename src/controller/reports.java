package controller;

import helper.JDBC;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class is the controller for the reports screen which shows 3 seperate reports.
 * - Schedule report
 * - Total number of appointments based off User
 * - Total number of appointments by type and month
 *
 * @author Zachariah Kordas-Potter
 */
public class reports implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button totalButton, scheduleButton, backButton, userAptsButton;
    @FXML
    private TextField contactTextField, userNameField, typeField;
    @FXML
    private ComboBox monthComboBox;
    @FXML
    private Label totalUserAptsLabel, totalsLabel;
    @FXML
    private TableView<Appointment> reportsTable;
    @FXML
    private TableColumn<Appointment, String> contactColumn, titleColumn, typeColumn, descColumn, startColumn, endColumn;
    @FXML
    private TableColumn<Appointment, Integer> aptIdColumn, custIdColumn;

    /**
     * Initializes the tables so they are ready to have the data displayed in them
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        //reportsTable
        aptIdColumn.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("title"));
        descColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("description"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("contact"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("type"));
        startColumn.setCellValueFactory(formatted -> new SimpleStringProperty(formatted.getValue().getStart().format(formatter)));
        endColumn.setCellValueFactory(formatted -> new SimpleStringProperty(formatted.getValue().getEnd().format(formatter)));
        custIdColumn.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("custId"));
        //months box
        monthComboBox.getItems().addAll(
                "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
        );

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
     * Handles the total button which creates a report of total number of appointments by type and month
     *
     * @param event
     */
    public void totalButtonPressed(ActionEvent event) {
        reportsTable.setVisible(false);
        totalUserAptsLabel.setVisible(false);
        totalsLabel.setVisible(true);
        getTotalByTypeAndMonth();
    }

    /**
     * Hides the reportsTable and makes the totals label visible also calls getTotal() function in order to set the label
     *
     * @param event
     */
    public void userAptsButtonPressed(ActionEvent event) {
        reportsTable.setVisible(false);
        totalUserAptsLabel.setVisible(true);
        totalsLabel.setVisible(false);
        getTotalUserApts();
    }

    /**
     * Sets the reportsTable with the data using getAllApts() function. Also hides the totals label and sets the reportsTable to visible
     *
     * @param event
     */
    public void scheduleButtonPressed(ActionEvent event) {
        totalUserAptsLabel.setVisible(false);
        reportsTable.setVisible(true);
        totalsLabel.setVisible(false);
        reportsTable.setItems(getAllApts());
    }

    /**
     * This function queries the database in order to get the total number of appointments by the type and month
     */
    public void getTotalByTypeAndMonth() {
        String type = typeField.getText();
        String month = (String) monthComboBox.getValue();
        try {
            String sql = "SELECT COUNT(*) FROM client_schedule.appointments WHERE Type = ? AND date_format(Start, '%m') = ?";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, type);
            ps.setString(2, month);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            totalsLabel.setText("There are #" + count + " appointments for " + type + " type of appointments in month: " + month);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns the count of appointments for a specified User
     */
    public void getTotalUserApts() {
        String userName = userNameField.getText();
        try {
            String sql = "SELECT COUNT(*) FROM client_schedule.appointments WHERE User_ID = (SELECT User_ID FROM client_schedule.users WHERE User_Name = ?)";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            totalUserAptsLabel.setText("User: " + userName + "  Total Apts is " + count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects all appointments for a specific Contact from the database then sets them to a observable list and returns that list
     *
     * @return ObservableList list
     */
    public ObservableList<Appointment> getAllApts() {
        String contact = contactTextField.getText();
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        try {
            String sql = "SELECT * FROM client_schedule.appointments WHERE Contact_ID = (SELECT Contact_ID FROM client_schedule.contacts WHERE Contact_Name = ?)";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, contact);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int aptId = rs.getInt("Appointment_ID");
                String aptTitle = rs.getString("Title");
                String aptDesc = rs.getString("Description");
                String aptLocation = rs.getString("Location");
                String aptType = rs.getString("Type");
                LocalDateTime aptStartDate = rs.getTimestamp("Start").toLocalDateTime();
                LocalDateTime aptEndDate = rs.getTimestamp("End").toLocalDateTime();
                int aptCustId = rs.getInt("Customer_ID");
                int aptUserId = rs.getInt("User_ID");

                Appointment a = new Appointment(aptId, aptTitle, aptLocation, contact, aptType, aptDesc,
                        aptStartDate, aptEndDate, aptCustId, aptUserId);
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}