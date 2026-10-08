package com.cbms.selenium;

import com.cbms.selenium.pages.BookingsPage;
import com.cbms.selenium.pages.LoginPage;
import com.cbms.selenium.pages.SlotsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerBookingIT extends BaseIT {

    @Test
    @DisplayName("J3: Customer books a slot, verifies PENDING status, and checks capacity decrease")
    void customerBooksSlotAndVerifiesStatus() {
        String baseUrl = getBaseUrl();
        int guestCount = 2;

        // 1. Log in as seeded customer
        LoginPage loginPage = new LoginPage(driver).open(baseUrl);
        SlotsPage slotsPage = loginPage.loginAsCustomer(TestData.SEEDED_CUSTOMER_EMAIL, TestData.SEEDED_CUSTOMER_PASSWORD);

        // Assert 1: Authenticated customer reaches slots page
        assertThat(slotsPage.getCurrentUrl())
                .as("Customer should land on slots page after login")
                .contains("/slots");

        // 2. Identify target slot and note initial capacity
        String targetMenu = slotsPage.isSlotVisible(TestData.SEEDED_SLOT_MENU)
                ? TestData.SEEDED_SLOT_MENU
                : slotsPage.getFirstAvailableSlotMenuType();

        int initialCapacity = slotsPage.getAvailableCapacityByMenu(targetMenu);
        assertThat(initialCapacity)
                .as("Selected slot should have sufficient capacity for booking")
                .isGreaterThanOrEqualTo(guestCount);

        // 3. Book the slot
        BookingsPage bookingsPage = slotsPage.bookSlotByMenu(targetMenu, guestCount);

        // Assert 2: Redirected to My Bookings with success feedback
        assertThat(bookingsPage.getCurrentUrl())
                .as("Booking submission should redirect to user bookings page")
                .contains("/bookings/me");
        assertThat(bookingsPage.isSuccessAlertDisplayed())
                .as("Booking success message should be displayed")
                .isTrue();

        // Assert 3: Booking row shows the booked slot with PENDING status
        String status = bookingsPage.getBookingStatusByMenu(targetMenu);
        assertThat(status)
                .as("Newly created booking status should be PENDING")
                .isEqualTo("PENDING");

        // 4. Return to slots page and verify capacity reduction
        slotsPage.open(baseUrl);
        int updatedCapacity = slotsPage.getAvailableCapacityByMenu(targetMenu);

        // Assert 4: Available capacity decreased by the booked guest count
        assertThat(updatedCapacity)
                .as("Slot available capacity should decrease exactly by the number of booked guests")
                .isEqualTo(initialCapacity - guestCount);
    }
}
