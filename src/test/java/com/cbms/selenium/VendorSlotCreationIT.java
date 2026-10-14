package com.cbms.selenium;

import com.cbms.selenium.pages.LoginPage;
import com.cbms.selenium.pages.SlotsPage;
import com.cbms.selenium.pages.VendorSlotFormPage;
import com.cbms.selenium.pages.VendorSlotsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VendorSlotCreationIT extends BaseIT {

    @Test
    @DisplayName("J4: Vendor creates slot, verifies details in vendor list, and confirms customer visibility")
    void vendorCreatesSlotAndVerifiesCustomerVisibility() {
        String baseUrl = getBaseUrl();
        String uniqueMenu = TestData.generateUniqueMenuType(TestData.NEW_SLOT_MENU_PREFIX);
        String slotDate = TestData.NEW_SLOT_DATE;
        String slotTime = TestData.NEW_SLOT_TIME;
        int slotCapacity = TestData.NEW_SLOT_CAPACITY;

        // 1. Log in as vendor
        LoginPage loginPage = new LoginPage(driver).open(baseUrl);
        VendorSlotsPage vendorSlotsPage = loginPage.loginAsVendor(TestData.SEEDED_VENDOR_EMAIL, TestData.SEEDED_VENDOR_PASSWORD);

        assertThat(vendorSlotsPage.getCurrentUrl())
                .as("Vendor should be redirected to vendor slots dashboard after login")
                .contains("/vendor/slots");

        // 2. Navigate to slot creation form and create a new slot
        VendorSlotFormPage formPage = vendorSlotsPage.clickCreateSlot();
        vendorSlotsPage = formPage.createSlot(slotDate, slotTime, uniqueMenu, slotCapacity);

        // Assert 1: Slot creation success alert is displayed
        assertThat(vendorSlotsPage.isSlotCreatedAlertDisplayed())
                .as("Success alert should confirm slot was created")
                .isTrue();

        // Assert 2: Slot appears in vendor's dashboard with correct menu, date, and capacity
        assertThat(vendorSlotsPage.hasSlot(uniqueMenu))
                .as("Newly published slot should appear in vendor's slots list")
                .isTrue();
        assertThat(vendorSlotsPage.getSlotDate(uniqueMenu))
                .as("Vendor slot date should match input")
                .isEqualTo(slotDate);
        assertThat(vendorSlotsPage.getSlotCapacity(uniqueMenu))
                .as("Vendor slot capacity should match input")
                .isEqualTo(slotCapacity);

        // 3. Log out vendor
        LoginPage loginPageAfterVendorLogout = vendorSlotsPage.logout();

        // 4. Log in as customer
        SlotsPage customerSlotsPage = loginPageAfterVendorLogout.loginAsCustomer(
                TestData.SEEDED_CUSTOMER_EMAIL, TestData.SEEDED_CUSTOMER_PASSWORD);

        // Assert 3: Customer slots page displays the vendor's newly created slot
        assertThat(customerSlotsPage.isSlotVisible(uniqueMenu))
                .as("Customer should be able to view the newly created vendor slot on /slots")
                .isTrue();
    }
}
