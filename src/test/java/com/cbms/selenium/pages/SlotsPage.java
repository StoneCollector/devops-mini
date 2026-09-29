package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Customer Slots browsing and booking page (/slots).
 */
public class SlotsPage extends BasePage {

    private final By slotsTable = By.id("slotsTable");
    private final By pageHeading = By.cssSelector(".page-header h1");
    private final By vendorCreateSlotBtn = By.id("vendorCreateSlotBtn");
    private final By dateFilter = By.id("dateFilter");
    private final By menuTypeFilter = By.id("menuTypeFilter");
    private final By applyFiltersBtn = By.id("applyFiltersBtn");

    public SlotsPage(WebDriver driver) {
        super(driver);
    }

    public SlotsPage open(String baseUrl) {
        driver.get(baseUrl + "/slots");
        wait.until(ExpectedConditions.visibilityOfElementLocated(slotsTable));
        return this;
    }

    public String getPageHeading() {
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        return heading.getText();
    }

    public boolean isSlotsTableDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(slotsTable)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSlotVisible(String menuType) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(slotsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#slotsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                return true;
            }
        }
        return false;
    }

    public int getAvailableCapacityByMenu(String menuType) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(slotsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#slotsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                WebElement capElem = row.findElement(By.cssSelector(".capacity-indicator .avail-val, .capacity-indicator"));
                String text = capElem.getText().trim();
                // If it contains "X / Y available", extract X
                String[] parts = text.split("[/\\s]+");
                return Integer.parseInt(parts[0]);
            }
        }
        throw new IllegalStateException("Slot with menu type '" + menuType + "' not found");
    }

    public int getAvailableCapacityForSlot(Long slotId) {
        WebElement capElem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("slot-capacity-" + slotId)));
        String text = capElem.getText().trim();
        String[] parts = text.split("[/\\s]+");
        return Integer.parseInt(parts[0]);
    }

    public BookingsPage bookSlot(Long slotId, int guests) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("guests-input-" + slotId)));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true})); " +
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                input, String.valueOf(guests));

        WebElement bookBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("book-btn-" + slotId)));
        WebElement form = bookBtn.findElement(By.xpath("./ancestor::form"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
        wait.until(ExpectedConditions.urlContains("/bookings/me"));
        return new BookingsPage(driver);
    }

    public BookingsPage bookSlotByMenu(String menuType, int guests) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(slotsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#slotsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                WebElement input = row.findElement(By.cssSelector("input[name='numberOfGuests']"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].value = arguments[1]; " +
                        "arguments[0].dispatchEvent(new Event('input', {bubbles: true})); " +
                        "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                        input, String.valueOf(guests));

                WebElement form = row.findElement(By.tagName("form"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
                wait.until(ExpectedConditions.urlContains("/bookings/me"));
                return new BookingsPage(driver);
            }
        }
        throw new IllegalStateException("Slot with menu type '" + menuType + "' not found to book");
    }

    public BookingsPage bookFirstAvailableSlot(int guests) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(slotsTable));
        List<WebElement> bookButtons = driver.findElements(By.cssSelector("#slotsTable tbody tr .book-btn"));
        if (bookButtons.isEmpty()) {
            throw new IllegalStateException("No available slots to book");
        }
        WebElement bookBtn = bookButtons.get(0);
        WebElement form = bookBtn.findElement(By.xpath("./ancestor::form"));
        WebElement input = form.findElement(By.cssSelector("input[name='numberOfGuests']"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true})); " +
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                input, String.valueOf(guests));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
        wait.until(ExpectedConditions.urlContains("/bookings/me"));
        return new BookingsPage(driver);
    }

    public String getFirstAvailableSlotMenuType() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(slotsTable));
        List<WebElement> bookButtons = driver.findElements(By.cssSelector("#slotsTable tbody tr .book-btn"));
        if (bookButtons.isEmpty()) {
            throw new IllegalStateException("No available slots found");
        }
        WebElement row = bookButtons.get(0).findElement(By.xpath("./ancestor::tr"));
        return row.findElement(By.cssSelector("td:nth-child(3)")).getText().trim();
    }

    public VendorSlotFormPage clickCreateSlot() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(vendorCreateSlotBtn));
        btn.click();
        return new VendorSlotFormPage(driver);
    }
}
