package com.cbms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendor_resource_pools")
public class VendorResourcePool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", unique = true, nullable = false)
    private User vendor;

    @Column(name = "staff_count", nullable = false)
    private int staffCount = 10;

    @Column(name = "utensil_sets", nullable = false)
    private int utensilSets = 100;

    @Column(name = "delivery_vehicles", nullable = false)
    private int deliveryVehicles = 2;

    @Column(name = "venue_cover_capacity", nullable = false)
    private int venueCoverCapacity = 0; // 0 if vendor has no venue

    public VendorResourcePool() {}

    public VendorResourcePool(User vendor, int staffCount, int utensilSets, int deliveryVehicles, int venueCoverCapacity) {
        this.vendor = vendor;
        this.staffCount = Math.max(0, staffCount);
        this.utensilSets = Math.max(0, utensilSets);
        this.deliveryVehicles = Math.max(0, deliveryVehicles);
        this.venueCoverCapacity = Math.max(0, venueCoverCapacity);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }

    public int getStaffCount() { return staffCount; }
    public void setStaffCount(int staffCount) { this.staffCount = Math.max(0, staffCount); }

    public int getUtensilSets() { return utensilSets; }
    public void setUtensilSets(int utensilSets) { this.utensilSets = Math.max(0, utensilSets); }

    public int getDeliveryVehicles() { return deliveryVehicles; }
    public void setDeliveryVehicles(int deliveryVehicles) { this.deliveryVehicles = Math.max(0, deliveryVehicles); }

    public int getVenueCoverCapacity() { return venueCoverCapacity; }
    public void setVenueCoverCapacity(int venueCoverCapacity) { this.venueCoverCapacity = Math.max(0, venueCoverCapacity); }
}
