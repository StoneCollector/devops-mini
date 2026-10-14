package com.cbms.model;

public enum ServiceType {
    DELIVERY_ONLY("Delivery Only (Self-Serving)", "Food delivered hot/chilled in insulated containers. No vendor staff on-site."),
    DELIVERY_WITH_SERVING("Delivery with Serving Setup", "Food delivered with serving equipment, buffet stations, and professional waitstaff."),
    AT_VENUE("At Venue Catering", "Event hosted entirely at vendor's dedicated venue premises with full service."),
    TAKEAWAY("Takeaway / Self-Pickup", "Packaged meal trays or buffet boxes prepared for customer pickup from kitchen.");

    private final String displayName;
    private final String description;

    ServiceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresDeliveryAddress() {
        return this == DELIVERY_ONLY || this == DELIVERY_WITH_SERVING;
    }

    public boolean requiresPickupTime() {
        return this == TAKEAWAY;
    }

    public boolean consumesServingStaff() {
        return this == DELIVERY_WITH_SERVING || this == AT_VENUE;
    }

    public boolean consumesUtensilSets() {
        return this == DELIVERY_WITH_SERVING || this == AT_VENUE;
    }

    public boolean consumesDeliveryVehicle() {
        return this == DELIVERY_ONLY || this == DELIVERY_WITH_SERVING;
    }

    public boolean consumesVenueCovers() {
        return this == AT_VENUE;
    }
}
