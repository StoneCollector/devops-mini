package com.cbms.selenium;

import com.cbms.selenium.pages.BookingsPage;
import com.cbms.selenium.pages.LoginPage;
import com.cbms.selenium.pages.RegisterPage;
import com.cbms.selenium.pages.SlotsPage;
import com.cbms.selenium.pages.VendorBookingsPage;
import com.cbms.selenium.pages.VendorSlotsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VendorConfirmBookingIT extends BaseIT {

    @Test
    @DisplayName("J5: Customer books slot, vendor confirms booking, and both views reflect CONFIRMED status")
    void vendorConfirmsCustomerBookingLifecycle() {
        String baseUrl = getBaseUrl();
        String customerEmail = TestData.generateUniqueCustomerEmail();
        String customerName = "Lifecycle Customer";
        String customerPassword = "Password123!";

        // 1. Register a dedicated customer for full isolation
        RegisterPage registerPage = new RegisterPage(driver).open(baseUrl);
        LoginPage loginPage = registerPage.registerUser(customerName, customerEmail, customerPassword, "CUSTOMER");

        // 2. Customer logs in and books a slot
        SlotsPage slotsPage = loginPage.loginAsCustomer(customerEmail, customerPassword);
        String targetMenu = slotsPage.isSlotVisible(TestData.SEEDED_SLOT_MENU)
                ? TestData.SEEDED_SLOT_MENU
                : slotsPage.getFirstAvailableSlotMenuType();

        BookingsPage customerBookings = slotsPage.bookSlotByMenu(targetMenu, 1);

        // Assert 1: Initial status in customer view is PENDING
        assertThat(customerBookings.getBookingStatusByMenu(targetMenu))
                .as("Initial booking status should be PENDING before vendor action")
                .isEqualTo("PENDING");

        // 3. Customer logs out
        LoginPage loginPageAfterCustomer = customerBookings.logout();

        // 4. Vendor logs in and opens bookings dashboard
        VendorSlotsPage vendorSlots = loginPageAfterCustomer.loginAsVendor(
                TestData.SEEDED_VENDOR_EMAIL, TestData.SEEDED_VENDOR_PASSWORD);
        VendorBookingsPage vendorBookings = new VendorBookingsPage(driver).open(baseUrl);

        // 5. Vendor confirms the customer's booking
        vendorBookings.confirmBookingByCustomerEmail(customerEmail);

        // Assert 2: Booking confirmed alert is displayed
        assertThat(vendorBookings.isBookingConfirmedAlertDisplayed())
                .as("Vendor confirmation banner should be displayed")
                .isTrue();

        // Assert 3: Status reflects CONFIRMED in vendor view
        assertThat(vendorBookings.getBookingStatusByCustomerEmail(customerEmail))
                .as("Vendor view must show booking as CONFIRMED")
                .isEqualTo("CONFIRMED");

        // 6. Vendor logs out
        loginPage = vendorBookings.logout();

        // 7. Customer logs in to verify updated status
        loginPage.open(baseUrl);
        loginPage.loginAsCustomer(customerEmail, customerPassword);
        customerBookings = new BookingsPage(driver).open(baseUrl);

        // Assert 4: Status reflects CONFIRMED in customer My Bookings view
        assertThat(customerBookings.getBookingStatusByMenu(targetMenu))
                .as("Customer view must now show booking as CONFIRMED")
                .isEqualTo("CONFIRMED");
    }
}
