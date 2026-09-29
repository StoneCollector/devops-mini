package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Vendor Bookings on My Slots dashboard (/vendor/bookings).
 */
public class VendorBookingsPage extends BasePage {

    private final By vendorBookingsTable = By.id("vendorBookingsTable");
    private final By confirmedAlert = By.id("bookingConfirmedAlert");
    private final By rejectedAlert = By.id("bookingRejectedAlert");

    public VendorBookingsPage(WebDriver driver) {
        super(driver);
    }

    public VendorBookingsPage open(String baseUrl) {
        driver.get(baseUrl + "/vendor/bookings");
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorBookingsTable));
        return this;
    }

    public boolean isBookingConfirmedAlertDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(confirmedAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getBookingConfirmedAlertText() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmedAlert));
        return alert.getText();
    }

    public String getFirstBookingStatus() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorBookingsTable));
        WebElement status = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#vendorBookingsTable tbody tr:first-child .status-badge")));
        return status.getText().trim();
    }

    public String getBookingStatusByCustomerEmail(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorBookingsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vendorBookingsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(email)) {
                return row.findElement(By.cssSelector(".status-badge")).getText().trim();
            }
        }
        throw new IllegalStateException("Booking for customer email '" + email + "' not found");
    }

    public VendorBookingsPage confirmFirstPendingBooking() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorBookingsTable));
        List<WebElement> confirmButtons = driver.findElements(By.cssSelector("#vendorBookingsTable tbody tr .confirm-btn"));
        if (confirmButtons.isEmpty()) {
            throw new IllegalStateException("No pending bookings available to confirm");
        }
        WebElement form = confirmButtons.get(0).findElement(By.xpath("./ancestor::form"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmedAlert));
        return this;
    }

    public VendorBookingsPage confirmBookingByCustomerEmail(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorBookingsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vendorBookingsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(email)) {
                WebElement confirmBtn = row.findElement(By.cssSelector(".confirm-btn"));
                WebElement form = confirmBtn.findElement(By.xpath("./ancestor::form"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
                wait.until(ExpectedConditions.visibilityOfElementLocated(confirmedAlert));
                return this;
            }
        }
        throw new IllegalStateException("Booking for customer email '" + email + "' not found to confirm");
    }

    public boolean hasBookingForCustomer(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorBookingsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vendorBookingsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(email)) {
                return true;
            }
        }
        return false;
    }
}
