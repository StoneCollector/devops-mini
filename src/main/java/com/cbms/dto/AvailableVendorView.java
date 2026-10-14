package com.cbms.dto;

import com.cbms.model.User;
import com.cbms.model.VendorCuisine;
import com.cbms.model.VendorProfile;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * View presentation model for currently available vendors on the main discovery page.
 */
public class AvailableVendorView {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final User vendor;
    private final VendorProfile profile;
    private final List<VendorCuisine> cuisines;
    private final boolean openNow;
    private final boolean takeawayActive;
    private final boolean readyToDineActive;

    public AvailableVendorView(User vendor,
                               VendorProfile profile,
                               List<VendorCuisine> cuisines,
                               boolean openNow,
                               boolean takeawayActive,
                               boolean readyToDineActive) {
        this.vendor = vendor;
        this.profile = profile;
        this.cuisines = cuisines != null ? cuisines : Collections.emptyList();
        this.openNow = openNow;
        this.takeawayActive = takeawayActive;
        this.readyToDineActive = readyToDineActive;
    }

    public User getVendor() {
        return vendor;
    }

    public VendorProfile getProfile() {
        return profile;
    }

    public List<VendorCuisine> getCuisines() {
        return cuisines;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public boolean isTakeawayActive() {
        return takeawayActive;
    }

    public boolean isReadyToDineActive() {
        return readyToDineActive;
    }

    public String getFormattedOperatingHours() {
        if (profile == null || profile.getOpeningTime() == null || profile.getClosingTime() == null) {
            return "Operating Today";
        }
        return profile.getOpeningTime().format(TIME_FORMATTER) + " – " + profile.getClosingTime().format(TIME_FORMATTER);
    }

    public String getFormattedDineInHours() {
        if (profile == null || !profile.isReadyToDineEnabled()) {
            return "Dine-in not offered";
        }
        if (profile.getReadyToDineStartTime() == null || profile.getReadyToDineEndTime() == null) {
            return "Dine-in: Open throughout operational hours";
        }
        return profile.getReadyToDineStartTime().format(TIME_FORMATTER) + " – " + profile.getReadyToDineEndTime().format(TIME_FORMATTER);
    }

    public String getLowestCuisinePrice() {
        if (cuisines.isEmpty()) return null;
        double min = cuisines.stream()
                .mapToDouble(c -> c.getPricePerHead() != null ? c.getPricePerHead() : 0.0)
                .min()
                .orElse(0.0);
        return String.format("%.0f", min);
    }
}
