Title: Scheduling Master
------------------------------------------------------------------------------------------------------------------------
The purpose of this application is to provide a way for the software company to easily add, modify, and delete both
customers and appointments from a SQL Database without directly having to write SQL code.
------------------------------------------------------------------------------------------------------------------------
Zachariah Kordas-Potter
zpotte2@wgu.edu
V1.0.1
3/22/2022
------------------------------------------------------------------------------------------------------------------------
The IDE used is IntelliJ IDEA Community Edition 2021.2.3. The JDK version used is 11.0.14
The JavaFX version used is 17.0.2

----------------------------------------DIRECTIONS TO RUN---------------------------------------------------------------
Once the program is launched the user must enter a Username and Password, both of which must be in the SQL Database in the
client_schedule.users table. Upon logging in the user will be notified if they have a appointment within 15 minutes.
After logging in the user will be presented with the Main Menu which has two tables, a Customer table and a Appointment table,
both of these tables have ADD, MODIFY, and DELETE buttons.

Customer Table:
ADD - All the user must do is press the add button, and it will take them to the addCustomerScreen and allow them to input
the necessary information.
MODIFY - In order to use the modify button the user must have a Customer selected from the table then the modifyCustomerScreen
will all them to change the data that it pulled from the SQL database.
DELETE - The Delete button functions similarly to the Modify button, the user must have a Customer selected and have all
that customers Appointments deleted then the customer will be removed from the SQL Database.

Appointments Table:
ADD - In order to add an Appointment the user must first have a Customer selected from the Customers Table. After pressing
the button the user is taken to the AddAppointmentScreen and may enter all the data that is required.
MODIFY - Similarly to the MODIFY button from the Customers' table the user must have an Appointment selected. Then the
modifyAppointmentsScreen will open and be filled with the selected Appointments' data from the database.
DELETE - Functions the same as the DELETE button from the Customers table. First select a appointment then press the button

In addition, the two tables and their subsequent buttons there is also the Reports button which will take the user to the
Reports screen and allow them to choose from three different Reports.
------------------------------------------------------------------------------------------------------------------------
The additional report that I added in part A3F was a total number of appointments created by a given user. The user
enters the username they would like to get the report for and then presses the appropriate button.
------------------------------------------------------------------------------------------------------------------------
The MySQL Connector driver version number used is --  mysql-connector-java-8.0.26