package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Customer My Bookings page (/bookings/me).
 */
public class BookingsPage extends BasePage {

    private final By successAlert = By.id("bookingSuccessAlert");
    private final By cancelledAlert = By.id("bookingCancelledAlert");
    private final By bookingsTable = By.id("myBookingsTable");

    public BookingsPage(WebDriver driver) {
        super(driver);
    }

    public BookingsPage open(String baseUrl) {
        driver.get(baseUrl + "/bookings/me");
        wait.until(ExpectedConditions.visibilityOfElementLocated(bookingsTable));
        return this;
    }

    public boolean isSuccessAlertDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getSuccessAlertText() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(successAlert));
        return alert.getText();
    }

    public String getFirstBookingStatus() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(bookingsTable));
        WebElement statusSpan = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#myBookingsTable tbody tr:first-child .status-badge")));
        return statusSpan.getText().trim();
    }

    public String getFirstBookingMenuType() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(bookingsTable));
        WebElement menuCell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#myBookingsTable tbody tr:first-child td:nth-child(3)")));
        return menuCell.getText().trim();
    }

    public String getBookingStatusByMenu(String menuType) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(bookingsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#myBookingsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                return row.findElement(By.cssSelector(".status-badge")).getText().trim();
            }
        }
        throw new IllegalStateException("Booking with menu type '" + menuType + "' not found");
    }

    public boolean hasBookingForMenu(String menuType) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(bookingsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#myBookingsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                return true;
            }
        }
        return false;
    }
}
