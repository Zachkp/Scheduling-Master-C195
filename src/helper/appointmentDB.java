package helper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;

import java.sql.*;
import java.time.*;

/**
 * This class handles getting the appointments from the database, deleting them, creating them, as well as modifying them
 *
 * @author Zachariah Kordas-Potter
 */
public class appointmentDB {
    private static Customer customer = null;
    private static String contact = null;
    private static String username = null;
    private static String password = null;

    /**
     * This function queries the database and retrieves all the elements of an appointment, then creates a new appointment
     * object and adds it to a list until the table is exhausted
     *
     * @return an observableList of the appointments from the database
     */
    public static ObservableList<Appointment> getAllAppointments() {
        ObservableList<Appointment> aList = FXCollections.observableArrayList();
        try {
            String sql = "SELECT * FROM client_schedule.appointments";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int aptId = rs.getInt("Appointment_ID");
                String aptTitle = rs.getString("Title");
                String aptDesc = rs.getString("Description");
                String aptLocation = rs.getString("Location");
                String aptType = rs.getString("Type");
                LocalDateTime aptStart = rs.getTimestamp("Start").toLocalDateTime();
                LocalDateTime aptEnd = rs.getTimestamp("End").toLocalDateTime();
                int aptCustId = rs.getInt("Customer_ID");
                int aptUserId = rs.getInt("User_ID");
                int aptContactId = rs.getInt("Contact_ID");

                Appointment a = new Appointment(aptId, aptTitle, aptLocation, getContactFromID(aptContactId), aptType, aptDesc,
                        aptStart, aptEnd, aptCustId, aptUserId);
                aList.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aList;
    }

    /**
     * This function takes in all the user inputted data passed to it and queries the database to INSERT the data as well as handling
     * the timezone conversions.
     *
     * @param start    The start date of the appointment
     * @param end      The end date of the appointment
     * @param title    The title of the appointment
     * @param location The location of the appointment
     * @param type     The type of appointment created
     * @param desc     The description of the appointment
     * @param contact  The contact person for the appointment
     * @param userId   The userID for the appointment
     */
    public static void createAppointment(String title, String location, String type, String desc, LocalDateTime start,
                                         LocalDateTime end, String contact, String custId, String userId) {

        try {
            String sqlCreate = "INSERT INTO client_schedule.appointments (Appointment_ID, Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) " +
                    "VALUES (NULL, ?, ?, ?, ?, ?, ?, NOW(), USER(), NOW(), USER(), ?, ?, (SELECT Contact_ID from client_schedule.contacts where Contact_Name = ?))";
            PreparedStatement psCreate = JDBC.getConnection().prepareStatement(sqlCreate);

            psCreate.setString(1, title);
            psCreate.setString(2, desc);
            psCreate.setString(3, location);
            psCreate.setString(4, type);
            psCreate.setTimestamp(5, Timestamp.valueOf(start));
            psCreate.setTimestamp(6, Timestamp.valueOf(end));
            psCreate.setString(7, custId);
            psCreate.setString(8, userId);
            psCreate.setString(9, contact);
            psCreate.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This functions takes in the appointment id then queries the database in order to delete the specified appointment
     *
     * @param appointmentId The appointment ID of the user selected appointment
     */
    public static void deleteAppointment(int appointmentId) {
        try {
            String sqlDelete = "DELETE FROM client_schedule.appointments WHERE Appointment_ID = ?";
            PreparedStatement psDelete = JDBC.getConnection().prepareStatement(sqlDelete);
            psDelete.setInt(1, appointmentId);
            psDelete.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function takes in the data from the user selected appointment so the user may change some of the data
     * then it queries the database in order to update the table with the new information. It also handles
     * timezone conversions.
     *
     * @param appointment   The selected appointment
     * @param appointmentId The appointment id of the selected appointment
     * @param title         The title of the appointment
     * @param location      The location of the appointment
     * @param type          The type of appointment created
     * @param description   The description of the appointment
     * @param start         The start date of the appointment
     * @param end           The end date of the appointment
     * @param custId        The customer ID for the appointment
     * @param userId        The user ID for the appointment
     */
    public static void modifyAppointment(Appointment appointment, int appointmentId, String title, String location, String type,
                                         String description, LocalDateTime start, LocalDateTime end, String custId, String userId) {

        try {
            String sqlMod = "UPDATE client_schedule.appointments " +
                    "SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, Last_Update = NOW(), Last_Updated_By = USER(), Customer_ID = ?, " +
                    "User_ID = ?, Contact_ID = (SELECT Contact_ID from client_schedule.contacts where Contact_Name = ?) " +
                    "WHERE Appointment_ID = ?";

            PreparedStatement psMod = JDBC.getConnection().prepareStatement(sqlMod);

            psMod.setString(1, title);
            psMod.setString(2, description);
            psMod.setString(3, location);
            psMod.setString(4, type);
            psMod.setTimestamp(5, Timestamp.valueOf(start));
            psMod.setTimestamp(6, Timestamp.valueOf(end));
            psMod.setString(7, custId);
            psMod.setString(8, userId);
            psMod.setString(9, contact);
            psMod.setInt(10, appointmentId);
            psMod.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function returns the Contact_Name from the database when given that contacts ID
     *
     * @param id The ID number of the contact
     * @return contact The name of the contact
     */
    public static String getContactFromID(int id) {
        String contact = null;
        try {
            String sql = "SELECT Contact_Name FROM client_schedule.contacts WHERE Contact_ID = ?";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                contact = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contact;
    }

    /**
     * This function takes in a selected customer object and gets its ID number in order to make the SQL queries easier
     *
     * @param selectedCustomer The customer whoms ID needs to be found
     * @return custId The selected customers ID number
     */
    public static int getCustID(Customer selectedCustomer) {
        int custId = 0;
        try {
            String sqlGetCustId = "SELECT Customer_ID FROM client_schedule.customers WHERE Customer_ID = ?";
            PreparedStatement psGetCustId = JDBC.getConnection().prepareStatement(sqlGetCustId);
            psGetCustId.setInt(1, selectedCustomer.getId());
            ResultSet rs = psGetCustId.executeQuery();

            while (rs.next()) {
                custId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return custId;
    }

    public static void holdCust(Customer selectedCustomer) {
        customer = selectedCustomer;
    }

    public static void holdContact(String selectedContact) {
        contact = selectedContact;
    }

    public static void holdUserAndPassword(String selectedUser, String selectedPassword) {
        username = selectedUser;
        password = selectedPassword;
    }
}