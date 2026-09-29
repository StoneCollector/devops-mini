package com.cbms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class CateringSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String timeSlot; // e.g. "Lunch" / "Dinner"
    private String menuType;
    private int capacity;
    private int slotsBooked;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private User vendor;

    public CateringSlot() {}

    public CateringSlot(LocalDate date, String timeSlot, String menuType, int capacity) {
        this.date = date;
        this.timeSlot = timeSlot;
        this.menuType = menuType;
        this.capacity = capacity;
        this.slotsBooked = 0;
    }

    public CateringSlot(LocalDate date, String timeSlot, String menuType, int capacity, User vendor) {
        this.date = date;
        this.timeSlot = timeSlot;
        this.menuType = menuType;
        this.capacity = capacity;
        this.slotsBooked = 0;
        this.vendor = vendor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public int getSlotsBooked() { return slotsBooked; }
    public void setSlotsBooked(int slotsBooked) { this.slotsBooked = slotsBooked; }

    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }

    public int getAvailableCapacity() {
        return capacity - slotsBooked;
    }
}

