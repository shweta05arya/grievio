package com.grievio.model;

public class User {
    private int id;
    private String name, email, role, society, sector, department, phone;

    public User() {}
    public User(int id, String name, String email, String role, String society) {
        this.id = id; this.name = name; this.email = email;
        this.role = role; this.society = society;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSociety() { return society; }
    public void setSociety(String society) { this.society = society; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
