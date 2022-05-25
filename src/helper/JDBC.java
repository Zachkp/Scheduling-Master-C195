package helper;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * This class handles opening and closing the connection to the database
 */
public abstract class JDBC {
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER"; // LOCAL
    private static final String driver = "com.mysql.cj.jdbc.Driver"; // Driver reference
    private static final String userName = "sqlUser"; // Username
    private static final String password = "Passw0rd!"; // Password
    public static Connection connection = null;  // Connection Interface

    /**
     * Opens the connection to the database using JDBC
     */
    public static void openConnection() {
        try {
            Class.forName(driver); // Locate Driver
            connection = DriverManager.getConnection(jdbcUrl, userName, password); // Reference Connection object
            System.out.println("Connection to database successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the connection to the database using JDBC
     */
    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Connection to database closed!");
        } catch (Exception e) {
            //dont care
        }
    }

    /**
     * Gets the connection by returning the connection variable
     *
     * @return Connection
     */
    public static Connection getConnection() {
        return connection;
    }
}