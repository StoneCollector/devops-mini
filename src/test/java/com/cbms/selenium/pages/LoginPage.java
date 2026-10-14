package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the Login Page (/login).
 */
public class LoginPage extends BasePage {

    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.id("loginSubmitBtn");
    private final By errorMessage = By.id("errorMessage");
    private final By successMessage = By.id("successMessage");
    private final By logoutMessage = By.id("logoutMessage");
    private final By registerLink = By.id("registerLink");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.urlContains("/login"));
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        wait.until(ExpectedConditions.elementToBeClickable(emailInput));
        return this;
    }

    public LoginPage enterEmail(String email) {
        try {
            WebElement input = wait.until(ExpectedConditions.elementToBeClickable(emailInput));
            input.clear();
            input.sendKeys(email);
        } catch (WebDriverException e) {
            WebElement input = wait.until(ExpectedConditions.elementToBeClickable(emailInput));
            input.clear();
            input.sendKeys(email);
        }
        return this;
    }

    public LoginPage enterPassword(String password) {
        try {
            WebElement input = wait.until(ExpectedConditions.elementToBeClickable(passwordInput));
            input.clear();
            input.sendKeys(password);
        } catch (WebDriverException e) {
            WebElement input = wait.until(ExpectedConditions.elementToBeClickable(passwordInput));
            input.clear();
            input.sendKeys(password);
        }
        return this;
    }

    public void clickSignIn() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            btn.click();
        } catch (WebDriverException e) {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            btn.click();
        }
    }

    public SlotsPage loginAsCustomer(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
        wait.until(ExpectedConditions.urlContains("/slots"));
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        return new SlotsPage(driver);
    }

    public VendorSlotsPage loginAsVendor(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
        wait.until(ExpectedConditions.urlContains("/vendor/slots"));
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        return new VendorSlotsPage(driver);
    }

    public LoginPage loginExpectingFailure(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return this;
    }

    public String getErrorMessage() {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return elem.getText();
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getSuccessMessage() {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        return elem.getText();
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getLogoutMessage() {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(logoutMessage));
        return elem.getText();
    }

    public boolean isLogoutMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(logoutMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public RegisterPage clickRegisterLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(registerLink));
        link.click();
        return new RegisterPage(driver);
    }
}
