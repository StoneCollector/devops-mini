package com.cbms.selenium;

import java.time.LocalDate;

/**
 * Centralized test data constants and unique generators for Selenium integration tests.
 */
public final class TestData {

    private TestData() {
        // Prevent instantiation
    }

    // Seeded Customer Credentials (from DataInitializer)
    public static final String SEEDED_CUSTOMER_NAME = "John Doe";
    public static final String SEEDED_CUSTOMER_EMAIL = "customer@example.com";
    public static final String SEEDED_CUSTOMER_PASSWORD = "customer123";

    // Seeded Vendor Credentials (from DataInitializer)
    public static final String SEEDED_VENDOR_NAME = "Chef Mario Catering";
    public static final String SEEDED_VENDOR_EMAIL = "chef.mario@smartcatering.com";
    public static final String SEEDED_VENDOR_PASSWORD = "vendor123";

    // Seeded Admin Credentials (from DataInitializer)
    public static final String SEEDED_ADMIN_NAME = "System Admin";
    public static final String SEEDED_ADMIN_EMAIL = "admin@smartcatering.com";
    public static final String SEEDED_ADMIN_PASSWORD = "admin123";

    // Seeded Slot Details
    public static final String SEEDED_SLOT_MENU = "Standard Buffet";
    public static final String SEEDED_SLOT_TIME = "Lunch (12:00 PM - 2:00 PM)";

    // Default test slot creation data
    public static final String NEW_SLOT_DATE = LocalDate.now().plusDays(3).toString();
    public static final String NEW_SLOT_TIME = "Dinner (7:00 PM - 9:00 PM)";
    public static final String NEW_SLOT_MENU_PREFIX = "Gourmet Banquet ";
    public static final int NEW_SLOT_CAPACITY = 25;

    public static String generateUniqueCustomerEmail() {
        return "cust_" + System.currentTimeMillis() + "@smartcatering-test.com";
    }

    public static String generateUniqueVendorEmail() {
        return "vendor_" + System.currentTimeMillis() + "@smartcatering-test.com";
    }

    public static String generateUniqueMenuType(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
