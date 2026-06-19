package com.partnersdashboard.model;

/** Society entity representing a residential complex. */
public class Society {
    private int id;
    private String name;
    private String address;
    private String city;
    private String pincode;

    public Society() {}
    public Society(int id, String name) { this.id = id; this.name = name; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    @Override
    public String toString() { return name; }  // for ComboBox display
}