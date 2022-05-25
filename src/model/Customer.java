package model;

/**
 * This class allows for the creation of new Customer objects inside of a observable list. These objects can then be added
 * to the database.
 *
 * @author Zachariah Kordas-Potter
 */
public class Customer {
    private int id;
    private String name;
    private String phoneNum;
    private String address;
    private String postalCode;//
    private String country;
    private String state;

    public Customer(int id, String name, String phoneNum, String country, String state, String address, String postalCode) {
        this.id = id;
        this.name = name;
        this.phoneNum = phoneNum;
        this.country = country;
        this.state = state;
        this.address = address;
        this.postalCode = postalCode;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the phone number
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    /**
     * @param phoneNum the phone number to be set
     */
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to be set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to be set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to be set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the postal code
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @param postalCode the postal code to be set
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
