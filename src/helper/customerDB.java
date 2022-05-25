package helper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class handles the majority of database functions for the Customers table
 */
public class customerDB {
    public static ObservableList<Customer> getAllCustomers() {

        ObservableList<Customer> cList = FXCollections.observableArrayList();
        try {
            String sql = "SELECT * FROM client_schedule.customers";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int customerId = rs.getInt("Customer_ID");
                String customerName = rs.getString("Customer_Name");
                String customerAddress = rs.getString("Address");
                String customerPostalCode = rs.getString("Postal_Code");
                String customerPhoneNum = rs.getString("Phone");
                String division = rs.getString("Division_ID");
                String state = getState(division);
                String country = getCountry(division);

                Customer c = new Customer(customerId, customerName, customerPhoneNum, country,
                        state, customerAddress, customerPostalCode);
                cList.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cList;
    }

    /**
     * This function takes in the division ID from the database and retrieves the String name of that division
     *
     * @param division String Division_ID that was passed to it
     * @return String state - The state that was selected from the database
     */
    public static String getState(String division) {
        String state = null;
        try {
            String sql = "SELECT Division FROM client_schedule.first_level_divisions WHERE Division_ID = ?";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, division);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                state = rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return state;
    }

    /**
     * This function takes in the division ID from the database and retrieves the String name of the country for that division
     *
     * @param division String Division_ID that was passed to it
     * @return String country - The country that was selected from the database
     */
    public static String getCountry(String division) {
        String country = null;
        try {
            String sql = "SELECT Country FROM client_schedule.countries WHERE Country_ID = " +
                    "(SELECT Country_ID FROM client_schedule.first_level_divisions WHERE Division_ID = ?)";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(sql);
            ps.setString(1, division);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                country = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return country;
    }

    /**
     * This function takes all the user passed data and queries the table in order to insert the data
     *
     * @param customerName       The user entered customer name
     * @param customerAddress    The user entered address
     * @param customerPostalCode The user entered Postal Code
     * @param customerPhoneNum   The user entered phone number
     * @param state              The user entered state
     */
    public static void createCustomer(String customerName, String customerAddress, String customerPostalCode, String customerPhoneNum, String state) {
        try {
            String sqlCreateCust = "INSERT INTO client_schedule.customers VALUES(NULL, ?, ?, ?, ?, NOW(), USER(), NOW(), USER(), ?)";

            PreparedStatement psCreateCust = JDBC.getConnection().prepareStatement(sqlCreateCust);

            psCreateCust.setString(1, customerName);
            psCreateCust.setString(2, customerAddress);
            psCreateCust.setString(3, customerPostalCode);
            psCreateCust.setString(4, customerPhoneNum);
            String div = getDivision(state);
            psCreateCust.setString(5, div);

            psCreateCust.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function handles deleting the customer by querying the database using the given customer ID
     *
     * @param customerId The customer ID of the selected customer that is to be deleted
     */
    public static void deleteCustomer(int customerId) {
        try {
            String sqlDelete = "DELETE FROM client_schedule.customers WHERE Customer_ID = ?";
            PreparedStatement psDelete = JDBC.getConnection().prepareStatement(sqlDelete);

            psDelete.setInt(1, customerId);
            psDelete.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function handles the modification of a Customer object by taking in the user entered data and querying the table
     * to update the specific customer
     *
     * @param customerId         The customer ID of the selected customer
     * @param customerName       The user entered customer name
     * @param customerPhoneNum   The user entered customer phone number
     * @param customerAddress    The user entered customer address
     * @param customerPostalCode The user entered postal code
     */
    public static void modifyCustomer(int customerId, String customerName, String customerPhoneNum, String customerAddress,
                                      String customerPostalCode) {
        try {
            String sqlMod = "UPDATE client_schedule.customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Last_Update = NOW(), Last_Updated_By = USER() WHERE Customer_ID = ?";
            PreparedStatement psMod = JDBC.getConnection().prepareStatement(sqlMod);

            psMod.setString(1, customerName);
            psMod.setString(2, customerAddress);
            psMod.setString(3, customerPostalCode);
            psMod.setString(4, customerPhoneNum);
            psMod.setInt(5, customerId);

            psMod.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function gets the division ID of a given state
     *
     * @param state The selected state
     * @return the division ID
     */
    public static String getDivision(String state) {
        String div = "";
        try {
            String sqlGetDiv = "SELECT Division_ID FROM client_schedule.first_level_divisions WHERE Division = ?";
            PreparedStatement psGetDiv = JDBC.getConnection().prepareStatement(sqlGetDiv);
            psGetDiv.setString(1, state);
            ResultSet rs = psGetDiv.executeQuery();

            while (rs.next()) {
                div = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return div;
    }
}
