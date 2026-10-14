package com.cbms.model;

import java.util.Arrays;
import java.util.List;

public enum OccasionType {
    // Large Event Types
    WEDDING("Wedding / Reception"),
    CONFERENCE("Conference / Summit"),

    // General & Corporate Gathering Types
    CORPORATE("Corporate Event / Seminar"),
    BIRTHDAY("Birthday Celebration"),
    SOCIAL("Social Gathering / Party"),
    RELIGIOUS("Religious Ceremony / Feast"),

    // Takeaway & Dine-In Tailored Types
    FAMILY_MEAL("Family Dinner / Quick Meal"),
    OFFICE_LUNCH("Office / Team Lunch"),
    CASUAL_DINING("Casual Dining / Table Meal"),
    ANNIVERSARY("Anniversary / Date Night"),

    OTHER("Other Occasion");

    private final String displayName;

    OccasionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns contextual occasions tailored to the selected service type.
     * Prevents mismatched pairings like weddings for small takeaways.
     */
    public static List<OccasionType> getOccasionsFor(ServiceType serviceType) {
        if (serviceType == null) {
            return Arrays.asList(values());
        }
        return switch (serviceType) {
            case TAKEAWAY -> List.of(
                    FAMILY_MEAL,
                    OFFICE_LUNCH,
                    BIRTHDAY,
                    SOCIAL,
                    RELIGIOUS,
                    OTHER
            );
            case AT_VENUE -> List.of(
                    CASUAL_DINING,
                    BIRTHDAY,
                    ANNIVERSARY,
                    CORPORATE,
                    SOCIAL,
                    WEDDING,
                    OTHER
            );
            case DELIVERY_ONLY -> List.of(
                    CORPORATE,
                    OFFICE_LUNCH,
                    FAMILY_MEAL,
                    BIRTHDAY,
                    SOCIAL,
                    RELIGIOUS,
                    CONFERENCE,
                    OTHER
            );
            case DELIVERY_WITH_SERVING -> List.of(
                    WEDDING,
                    CORPORATE,
                    CONFERENCE,
                    BIRTHDAY,
                    SOCIAL,
                    RELIGIOUS,
                    OTHER
            );
        };
    }
}
