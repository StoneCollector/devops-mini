package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Vendor My Slots dashboard (/vendor/slots).
 */
public class VendorSlotsPage extends BasePage {

    private final By vendorSlotsTable = By.id("vendorSlotsTable");
    private final By slotCreatedAlert = By.id("slotCreatedAlert");
    private final By slotUpdatedAlert = By.id("slotUpdatedAlert");
    private final By slotDeletedAlert = By.id("slotDeletedAlert");
    private final By createSlotButton = By.id("vendorCreateSlotBtn");

    public VendorSlotsPage(WebDriver driver) {
        super(driver);
    }

    public VendorSlotsPage open(String baseUrl) {
        driver.get(baseUrl + "/vendor/slots");
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorSlotsTable));
        return this;
    }

    public boolean isSlotCreatedAlertDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(slotCreatedAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getSlotCreatedAlertText() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(slotCreatedAlert));
        return alert.getText();
    }

    public boolean hasSlot(String menuType) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorSlotsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vendorSlotsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                return true;
            }
        }
        return false;
    }

    public String getSlotDate(String menuType) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorSlotsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vendorSlotsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                return row.findElement(By.cssSelector("td:first-child")).getText().trim();
            }
        }
        throw new IllegalStateException("Slot with menu type '" + menuType + "' not found");
    }

    public int getSlotCapacity(String menuType) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(vendorSlotsTable));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vendorSlotsTable tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(menuType)) {
                String capText = row.findElement(By.cssSelector("td:nth-child(4)")).getText().trim();
                return Integer.parseInt(capText);
            }
        }
        throw new IllegalStateException("Slot with menu type '" + menuType + "' not found");
    }

    public VendorSlotFormPage clickCreateSlot() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(createSlotButton));
        btn.click();
        return new VendorSlotFormPage(driver);
    }
}
