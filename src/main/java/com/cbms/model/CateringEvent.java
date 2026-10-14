package com.cbms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "catering_events")
public class CateringEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuisine_id")
    private VendorCuisine cuisine;

    @Enumerated(EnumType.STRING)
    @Column(name = "occasion_type", nullable = false, length = 30)
    private OccasionType occasionType = OccasionType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 40)
    private ServiceType serviceType = ServiceType.DELIVERY_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_style", length = 30)
    private ServiceStyle serviceStyle = ServiceStyle.BUFFET;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time_from")
    private String eventTimeFrom;

    @Column(name = "event_time_to")
    private String eventTimeTo;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "pickup_time")
    private String pickupTime;

    // Resource consumption audit snapshots
    @Column(name = "staff_consumed")
    private int staffConsumed = 0;

    @Column(name = "utensils_consumed")
    private int utensilsConsumed = 0;

    @Column(name = "vehicle_consumed")
    private int vehicleConsumed = 0;

    @Column(name = "venue_covers_consumed")
    private int venueCoversConsumed = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CateringEvent() {}

    public CateringEvent(User customer, User vendor, VendorCuisine cuisine, OccasionType occasionType,
                         ServiceType serviceType, ServiceStyle serviceStyle, LocalDate eventDate,
                         String eventTimeFrom, String eventTimeTo, int guestCount,
                         String specialNotes, String deliveryAddress, String pickupTime) {
        this.customer = customer;
        this.vendor = vendor;
        this.cuisine = cuisine;
        this.occasionType = (occasionType != null) ? occasionType : OccasionType.OTHER;
        this.serviceType = (serviceType != null) ? serviceType : ServiceType.DELIVERY_ONLY;
        this.serviceStyle = (serviceStyle != null) ? serviceStyle : ServiceStyle.BUFFET;
        this.eventDate = eventDate;
        this.eventTimeFrom = eventTimeFrom;
        this.eventTimeTo = eventTimeTo;
        this.guestCount = guestCount;
        this.specialNotes = specialNotes;
        this.deliveryAddress = deliveryAddress;
        this.pickupTime = pickupTime;
        this.status = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }

    public VendorCuisine getCuisine() { return cuisine; }
    public void setCuisine(VendorCuisine cuisine) { this.cuisine = cuisine; }

    public OccasionType getOccasionType() { return occasionType; }
    public void setOccasionType(OccasionType occasionType) { this.occasionType = occasionType; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public ServiceStyle getServiceStyle() { return serviceStyle; }
    public void setServiceStyle(ServiceStyle serviceStyle) { this.serviceStyle = serviceStyle; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getEventTimeFrom() { return eventTimeFrom; }
    public void setEventTimeFrom(String eventTimeFrom) { this.eventTimeFrom = eventTimeFrom; }

    public String getEventTimeTo() { return eventTimeTo; }
    public void setEventTimeTo(String eventTimeTo) { this.eventTimeTo = eventTimeTo; }

    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }

    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }

    public int getStaffConsumed() { return staffConsumed; }
    public void setStaffConsumed(int staffConsumed) { this.staffConsumed = staffConsumed; }

    public int getUtensilsConsumed() { return utensilsConsumed; }
    public void setUtensilsConsumed(int utensilsConsumed) { this.utensilsConsumed = utensilsConsumed; }

    public int getVehicleConsumed() { return vehicleConsumed; }
    public void setVehicleConsumed(int vehicleConsumed) { this.vehicleConsumed = vehicleConsumed; }

    public int getVenueCoversConsumed() { return venueCoversConsumed; }
    public void setVenueCoversConsumed(int venueCoversConsumed) { this.venueCoversConsumed = venueCoversConsumed; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
