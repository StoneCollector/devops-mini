package com.cbms.model;

public enum ServiceStyle {
    BUFFET("Buffet Style"),
    PLATED("Plated Service"),
    FAMILY_STYLE("Family Style"),
    LIVE_STATION("Live Food Stations");

    private final String displayName;

    ServiceStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
