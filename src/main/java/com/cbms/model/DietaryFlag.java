package com.cbms.model;

public enum DietaryFlag {
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    HALAL("Halal"),
    JAIN("Jain Friendly"),
    GLUTEN_FREE("Gluten Free"),
    NUT_FREE("Nut Free");

    private final String displayName;

    DietaryFlag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
