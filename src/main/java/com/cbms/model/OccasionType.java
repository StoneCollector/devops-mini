package com.cbms.model;

public enum OccasionType {
    WEDDING("Wedding / Reception"),
    CORPORATE("Corporate Event / Seminar"),
    BIRTHDAY("Birthday Celebration"),
    SOCIAL("Social Gathering / Party"),
    CONFERENCE("Conference / Summit"),
    RELIGIOUS("Religious Ceremony / Feast"),
    OTHER("Other Occasion");

    private final String displayName;

    OccasionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
