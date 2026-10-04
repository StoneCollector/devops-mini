package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the Vendor Slot Creation/Editing form (/vendor/slots/new).
 */
public class VendorSlotFormPage extends BasePage {

    private final By dateInput = By.id("date");
    private final By timeSlotInput = By.id("timeSlot");
    private final By menuTypeInput = By.id("menuType");
    private final By capacityInput = By.id("capacity");
    private final By submitButton = By.id("slotSubmitBtn");
    private final By errorAlert = By.id("slotErrorAlert");

    public VendorSlotFormPage(WebDriver driver) {
        super(driver);
    }

    public VendorSlotFormPage open(String baseUrl) {
        driver.get(baseUrl + "/vendor/slots/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(dateInput));
        return this;
    }

    public VendorSlotFormPage enterDate(String date) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(dateInput));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true})); " +
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                input, date);
        return this;
    }

    public VendorSlotFormPage enterTimeSlot(String timeSlot) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(timeSlotInput));
        input.clear();
        input.sendKeys(timeSlot);
        return this;
    }

    public VendorSlotFormPage enterMenuType(String menuType) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(menuTypeInput));
        input.clear();
        input.sendKeys(menuType);
        return this;
    }

    public VendorSlotFormPage enterCapacity(int capacity) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(capacityInput));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true})); " +
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                input, String.valueOf(capacity));
        return this;
    }

    public VendorSlotsPage submitSlot() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(submitButton));
        WebElement form = btn.findElement(By.xpath("./ancestor::form"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
        wait.until(ExpectedConditions.urlContains("/vendor/slots"));
        wait.until(d -> "complete".equals(((org.openqa.selenium.JavascriptExecutor) d).executeScript("return document.readyState")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("vendorSlotsTable")));
        return new VendorSlotsPage(driver);
    }

    public VendorSlotsPage createSlot(String date, String timeSlot, String menuType, int capacity) {
        enterDate(date);
        enterTimeSlot(timeSlot);
        enterMenuType(menuType);
        enterCapacity(capacity);
        return submitSlot();
    }

    public String getErrorMessage() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert));
        return alert.getText();
    }
}
