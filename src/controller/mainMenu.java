package controller;

import helper.appointmentDB;
import helper.customerDB;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Appointment;
import model.Customer;

import helper.JDBC;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static helper.appointmentDB.getContactFromID;

/**
 * This class is the controller for the Main Menu screen it handles switching between the various screens as well as displaying
 * a majority of the data
 *
 * @author Zachariah Kordas-Potter
 */
public class mainMenu implements Initializable {

    //Stuff needed for switching scenes
    private Stage stage;
    private Scene scene;
    private Parent root;

    //Buttons and Label
    @FXML
    private Button dataAddButton, dataModButton, dataDelButton, aptsAddButton, aptsModButton, aptsDelButton,
            logoutButton, reportButton;
    @FXML
    private Label mainMenuLabel;
    @FXML
    private RadioButton monthRadioButton, idRadioButton, weekRadioButton;

    //Customer table
    @FXML
    private TableView<Customer> custDataTable;
    @FXML
    private TableColumn<Customer, Integer> idColumn;
    @FXML
    private TableColumn<Customer, String> custNameColumn;
    @FXML
    private TableColumn<Customer, String> phoneNumColumn;
    @FXML
    private TableColumn<Customer, String> countryColumn;
    @FXML
    private TableColumn<Customer, String> stateColumn;
    @FXML
    private TableColumn<Customer, String> custAddressColumn;
    @FXML
    private TableColumn<Customer, Integer> custPostalCodeColumn;

    //Appointments Table
    @FXML
    private TableView<Appointment> aptsTable;
    @FXML
    private TableColumn<Appointment, Integer> aptIdColumn;
    @FXML
    private TableColumn<Appointment, String> titleColumn;
    @FXML
    private TableColumn<Appointment, String> descColumn;
    @FXML
    private TableColumn<Appointment, String> locationColumn;
    @FXML
    private TableColumn<Appointment, String> contactColumn;
    @FXML
    private TableColumn<Appointment, String> typeColumn;
    @FXML
    private TableColumn<Appointment, String> startDateColumn;
    @FXML
    private TableColumn<Appointment, String> endDateColumn;
    @FXML
    private TableColumn<Appointment, Integer> custIdColumn;
    @FXML
    private TableColumn<Appointment, Integer> userIdColumn;

    /**
     * Initializes the mainMenu forms tables with data from the SQL database
     * <p>
     * Lambda logoutButton -- This lambda handles the logout button function as well as closing the connection to the
     * database which saved me from needing to have two extra functions.
     * <p>
     * Lambda formatted -- This lambda handles formatting the LocalDateTime object into a String so it may be displayed
     * in the format "yyyy-MM-dd hh:mm a" instead of the default format pulled from the MySQL database
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

        logoutButton.setOnAction(e -> {
            try {
                JDBC.closeConnection();
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/loginScreen.fxml")));
                stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        //Customer Table
        idColumn.setCellValueFactory(new PropertyValueFactory<Customer, Integer>("id"));
        custNameColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("name"));
        phoneNumColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("phoneNum"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("country"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("state"));
        custAddressColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("address"));
        custPostalCodeColumn.setCellValueFactory(new PropertyValueFactory<Customer, Integer>("postalCode"));
        //Appointments Table
        aptIdColumn.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("title"));
        descColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("location"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("contact"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<Appointment, String>("type"));
        startDateColumn.setCellValueFactory(formatted -> new SimpleStringProperty(formatted.getValue().getStart().format(formatter)));
        endDateColumn.setCellValueFactory(formatted -> new SimpleStringProperty(formatted.getValue().getEnd().format(formatter)));
        custIdColumn.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("custId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<Appointment, Integer>("userId"));

        custDataTable.setItems(customerDB.getAllCustomers());
        aptsTable.setItems(appointmentDB.getAllAppointments());

        System.out.println("INITIALIZED\n\n");
    }

    /**
     * Handles the sorting of the tables using radio buttons
     *
     * @param event
     */
    public void radioButtons(ActionEvent event) {
        ObservableList<Appointment> aList = FXCollections.observableArrayList();

        if (monthRadioButton.isSelected()) {
            aptsTable.getSortOrder().clear();
            aptsTable.getItems().clear();
            try {
                String sql = "SELECT * FROM client_schedule.appointments WHERE month(Start) = month(NOW())";
                PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
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
                    int aptContactId = rs.getInt("Contact_ID");

                    Appointment a = new Appointment(aptId, aptTitle, aptLocation, getContactFromID(aptContactId), aptType, aptDesc,
                            aptStartDate, aptEndDate, aptCustId, aptUserId);
                    aList.add(a);
                }
                aptsTable.setItems(aList);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (weekRadioButton.isSelected()) {
            aptsTable.getSortOrder().clear();
            aptsTable.getItems().clear();
            try {
                String sql = "SELECT * FROM client_schedule.appointments WHERE week(Start) = week(NOW())";
                PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
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
                    int aptContactId = rs.getInt("Contact_ID");

                    Appointment a = new Appointment(aptId, aptTitle, aptLocation, getContactFromID(aptContactId), aptType, aptDesc,
                            aptStartDate, aptEndDate, aptCustId, aptUserId);
                    aList.add(a);
                }
                aptsTable.setItems(aList);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (idRadioButton.isSelected()) {
            aptsTable.getSortOrder().clear();
            aptsTable.getItems().clear();
            aptsTable.setItems(appointmentDB.getAllAppointments());
        }
    }

    /**
     * Handles the add data button by switching to the addCustomerScreen
     *
     * @param event
     * @throws IOException
     */
    public void dataAddButtonPressed(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/addCustomerScreen.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Handles the data modify button by switching to the modifyCustomerScreen
     *
     * @param event
     * @throws IOException
     */
    public void dataModButtonPressed(ActionEvent event) throws IOException {
        Customer selectedCustomer = (Customer) custDataTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            System.out.println("selected customer = null");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please select a valid CUSTOMER from the table");
            alert.showAndWait();
            return;
        } else {
            modifyCustomer.holdCustomer(selectedCustomer);
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/modifyCustomerScreen.fxml")));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Handles the data delete button by getting the selected item then removing it from the table and database
     *
     * @param event
     * @throws IOException
     */
    public void dataDelButtonPressed(ActionEvent event) throws IOException {
        System.out.println("--- Data Del Button Pressed ---");
        Customer selectedCustomer = (Customer) custDataTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            if (selectedCustomer == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR ERROR ERROR");
                alert.setContentText("Please select a valid CUSTOMER from the table");
                alert.showAndWait();
            }
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Are you sure you want to delete the Customer?");
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println("Customer deleted");
                customerDB.deleteCustomer(selectedCustomer.getId());
                custDataTable.setItems(customerDB.getAllCustomers());
                aptsTable.setItems(appointmentDB.getAllAppointments());
            }
        }
    }

    /**
     * Handles the add appointments button by switching to the addAppointmentScreen
     *
     * @param event
     * @throws IOException
     */
    public void aptsAddButtonPressed(ActionEvent event) throws IOException {
        Customer selectedCustomer = (Customer) custDataTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please select a valid CUSTOMER from the table");
            alert.showAndWait();
            return;
        } else {
            appointmentDB.holdCust(selectedCustomer);
            addAppointment.holdApt(selectedCustomer);
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/addAppointmentScreen.fxml")));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Handles the modify appointments button by switchign to the modifyAppointmentScreen
     *
     * @param event
     * @throws IOException
     */
    public void aptsModButtonPressed(ActionEvent event) throws IOException {
        Appointment selectedAppointment = (Appointment) aptsTable.getSelectionModel().getSelectedItem();

        if (selectedAppointment == null) {
            System.out.println("selected appointment = null");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please select a valid APPOINTMENT from the table");
            alert.showAndWait();
            return;
        } else {
            modifyAppointment.holdApt(selectedAppointment);
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/modifyAppointmentScreen.fxml")));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Handles the delete appointments button by getting the selected item then removing it from the table and database
     *
     * @param event
     * @throws IOException
     */
    public void aptsDelButtonPressed(ActionEvent event) throws IOException {
        System.out.println("--- Apts Del Button Pressed ---");
        Appointment selectedAppointment = (Appointment) aptsTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment == null) {
            System.out.println("selected appointment = null");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR ERROR ERROR");
            alert.setContentText("Please select a valid APPOINTMENT from the table");
            alert.showAndWait();
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Are you sure you want to delete the Appointment " + selectedAppointment.getId() + " of type: " + selectedAppointment.getType());
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println("Appointment deleted");
                appointmentDB.deleteAppointment(selectedAppointment.getId());
                aptsTable.setItems(appointmentDB.getAllAppointments());
            }
        }
    }

    /**
     * Handles the report button action. Switches to the reports screen
     *
     * @param event
     * @throws IOException
     */
    public void reportButtonPressed(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/reportsScreen.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
