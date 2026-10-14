package com.cbms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "vendor_cuisines")
public class VendorCuisine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @Column(name = "cuisine_name", nullable = false)
    private String cuisineName;

    // Comma-separated enum names, e.g. "BUFFET,PLATED"
    @Column(name = "service_styles", nullable = false)
    private String serviceStyles = "BUFFET";

    @Column(name = "price_per_head")
    private Double pricePerHead;

    @Column(name = "min_guests")
    private int minGuests = 15;

    @Column(name = "max_guests")
    private int maxGuests = 500;

    // Comma-separated enum names, e.g. "VEGETARIAN,JAIN"
    @Column(name = "dietary_flags")
    private String dietaryFlags;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public VendorCuisine() {}

    public VendorCuisine(User vendor, String cuisineName, String serviceStyles, Double pricePerHead, int minGuests, int maxGuests, String dietaryFlags, String description) {
        this.vendor = vendor;
        this.cuisineName = cuisineName;
        this.serviceStyles = (serviceStyles != null && !serviceStyles.isBlank()) ? serviceStyles : "BUFFET";
        this.pricePerHead = pricePerHead;
        this.minGuests = Math.max(1, minGuests);
        this.maxGuests = Math.max(this.minGuests, maxGuests);
        this.dietaryFlags = dietaryFlags;
        this.description = description;
        this.active = true;
    }

    public List<ServiceStyle> getServiceStyleList() {
        if (serviceStyles == null || serviceStyles.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(serviceStyles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return ServiceStyle.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<DietaryFlag> getDietaryFlagList() {
        if (dietaryFlags == null || dietaryFlags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(dietaryFlags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return DietaryFlag.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }

    public String getCuisineName() { return cuisineName; }
    public void setCuisineName(String cuisineName) { this.cuisineName = cuisineName; }

    public String getServiceStyles() { return serviceStyles; }
    public void setServiceStyles(String serviceStyles) { this.serviceStyles = serviceStyles; }

    public Double getPricePerHead() { return pricePerHead; }
    public void setPricePerHead(Double pricePerHead) { this.pricePerHead = pricePerHead; }

    public int getMinGuests() { return minGuests; }
    public void setMinGuests(int minGuests) { this.minGuests = minGuests; }

    public int getMaxGuests() { return maxGuests; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }

    public String getDietaryFlags() { return dietaryFlags; }
    public void setDietaryFlags(String dietaryFlags) { this.dietaryFlags = dietaryFlags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
