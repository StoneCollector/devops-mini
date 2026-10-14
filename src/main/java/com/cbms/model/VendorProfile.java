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
@Table(name = "vendor_profiles")
public class VendorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", unique = true, nullable = false)
    private User vendor;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_phone")
    private String businessPhone;

    @Column(name = "business_address", columnDefinition = "TEXT")
    private String businessAddress;

    @Column(name = "service_radius")
    private Integer serviceRadius = 25; // km

    @Column(name = "gstin")
    private String gstin;

    @Column(name = "fssai_license_no")
    private String fssaiLicenseNo;

    @Column(name = "operational_days")
    private String operationalDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY";

    @Column(name = "advance_booking_days")
    private Integer advanceBookingDays = 2;

    @Column(name = "opening_time")
    private java.time.LocalTime openingTime = java.time.LocalTime.of(8, 0);

    @Column(name = "closing_time")
    private java.time.LocalTime closingTime = java.time.LocalTime.of(23, 59);

    @Column(name = "takeaway_enabled")
    private Boolean takeawayEnabled = true;

    @Column(name = "ready_to_dine_enabled")
    private Boolean readyToDineEnabled = true;

    @Column(name = "ready_to_dine_start_time")
    private java.time.LocalTime readyToDineStartTime = java.time.LocalTime.of(11, 0);

    @Column(name = "ready_to_dine_end_time")
    private java.time.LocalTime readyToDineEndTime = java.time.LocalTime.of(23, 0);

    public VendorProfile() {}

    public VendorProfile(User vendor, String businessName, String businessPhone, String businessAddress) {
        this.vendor = vendor;
        this.businessName = businessName;
        this.businessPhone = businessPhone;
        this.businessAddress = businessAddress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public Integer getServiceRadius() { return serviceRadius; }
    public void setServiceRadius(Integer serviceRadius) { this.serviceRadius = serviceRadius; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getFssaiLicenseNo() { return fssaiLicenseNo; }
    public void setFssaiLicenseNo(String fssaiLicenseNo) { this.fssaiLicenseNo = fssaiLicenseNo; }

    public String getOperationalDays() { return operationalDays; }
    public void setOperationalDays(String operationalDays) { this.operationalDays = operationalDays; }

    public Integer getAdvanceBookingDays() { return advanceBookingDays; }
    public void setAdvanceBookingDays(Integer advanceBookingDays) { this.advanceBookingDays = advanceBookingDays; }

    public java.time.LocalTime getOpeningTime() { return openingTime; }
    public void setOpeningTime(java.time.LocalTime openingTime) { this.openingTime = openingTime; }

    public java.time.LocalTime getClosingTime() { return closingTime; }
    public void setClosingTime(java.time.LocalTime closingTime) { this.closingTime = closingTime; }

    public boolean isTakeawayEnabled() { return takeawayEnabled != null && takeawayEnabled; }
    public void setTakeawayEnabled(Boolean takeawayEnabled) { this.takeawayEnabled = (takeawayEnabled != null && takeawayEnabled); }

    public boolean isReadyToDineEnabled() { return readyToDineEnabled != null && readyToDineEnabled; }
    public void setReadyToDineEnabled(Boolean readyToDineEnabled) { this.readyToDineEnabled = (readyToDineEnabled != null && readyToDineEnabled); }

    public java.time.LocalTime getReadyToDineStartTime() { return readyToDineStartTime; }
    public void setReadyToDineStartTime(java.time.LocalTime readyToDineStartTime) { this.readyToDineStartTime = readyToDineStartTime; }

    public java.time.LocalTime getReadyToDineEndTime() { return readyToDineEndTime; }
    public void setReadyToDineEndTime(java.time.LocalTime readyToDineEndTime) { this.readyToDineEndTime = readyToDineEndTime; }

    /**
     * Checks if the vendor is currently operating on the given date and time.
     * Vendors whose closing time has already passed are NOT operating.
     */
    public boolean isOperatingAt(java.time.LocalDate date, java.time.LocalTime time) {
        if (operationalDays != null && !operationalDays.trim().isEmpty() && date != null) {
            String dayName = date.getDayOfWeek().name();
            if (!operationalDays.toUpperCase().contains(dayName)) {
                return false;
            }
        }
        if (time == null) {
            return true;
        }
        // If closing time has already passed, the vendor was open before current time and is now closed
        if (closingTime != null && closingTime.isBefore(time)) {
            return false;
        }
        // Must be open at or after opening time
        if (openingTime != null && time.isBefore(openingTime)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if Takeaway is active at the given time.
     */
    public boolean isTakeawayActiveAt(java.time.LocalTime time) {
        if (!takeawayEnabled) {
            return false;
        }
        if (time == null) {
            return true;
        }
        if (closingTime != null && closingTime.isBefore(time)) {
            return false;
        }
        if (openingTime != null && time.isBefore(openingTime)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if Ready to Dine is active at the given time.
     */
    public boolean isReadyToDineActiveAt(java.time.LocalTime time) {
        if (!readyToDineEnabled) {
            return false;
        }
        if (time == null) {
            return true;
        }
        if (readyToDineStartTime != null && time.isBefore(readyToDineStartTime)) {
            return false;
        }
        if (readyToDineEndTime != null && time.isAfter(readyToDineEndTime)) {
            return false;
        }
        return true;
    }
}
