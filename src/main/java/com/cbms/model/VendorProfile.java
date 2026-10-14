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
}
