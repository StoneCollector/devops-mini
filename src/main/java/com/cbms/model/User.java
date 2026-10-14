package com.cbms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "users") // 'user' is often a reserved keyword in DBs
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    
    // TODO: Hash password with BCrypt before any real deployment
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "city")
    private String city;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "gst_number")
    private String gstNumber;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = Role.CUSTOMER;
        this.active = true;
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = (role != null) ? role : Role.CUSTOMER;
        this.active = true;
    }

    public User(String name, String email, String password, Role role, boolean active) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = (role != null) ? role : Role.CUSTOMER;
        this.active = active;
    }

    public User(String name, String email, String password, Role role, String phoneNumber, String addressLine, String city, String pincode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = (role != null) ? role : Role.CUSTOMER;
        this.phoneNumber = phoneNumber;
        this.addressLine = addressLine;
        this.city = city;
        this.pincode = pincode;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
}
