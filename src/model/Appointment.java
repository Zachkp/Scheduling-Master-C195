package model;


import java.time.LocalDateTime;

/**
 * This class allows for the creation of a new appointment object in a Observable List which then can be added
 * to the database
 *
 * @author Zachariah Kordas-Potter
 */
public class Appointment {
    private int id;
    private String title;
    private String location;
    private String contact;
    private String type;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private int custId;
    private int userId;

    public Appointment(int id, String title, String location, String contact, String type, String description,
                       LocalDateTime start, LocalDateTime end, int custId, int userId) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.contact = contact;
        this.type = type;
        this.description = description;
        this.start = start;
        this.end = end;
        this.custId = custId;
        this.userId = userId;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to be set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to be set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to be set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact the contact info to be set
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * @return the type of appointment
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type of appointment to be set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to be set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the start date
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * @param start the start to be set
     */
    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    /**
     * @return the end date
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * @param end the end date to be set
     */
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    /**
     * @return the customer id
     */
    public int getCustId() {
        return custId;
    }

    /**
     * @param custId the cust id to be set
     */
    public void setCustId(int custId) {
        this.custId = custId;
    }

    /**
     * @return the user id
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the user ID to be set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

}
