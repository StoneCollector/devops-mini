package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * Page object for the Registration Page (/register).
 */
public class RegisterPage extends BasePage {

    private final By nameInput = By.id("name");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By roleSelect = By.id("role");
    private final By submitButton = By.id("registerSubmitBtn");
    private final By errorMessage = By.id("errorMessage");
    private final By loginLink = By.id("loginLink");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open(String baseUrl) {
        driver.get(baseUrl + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        return this;
    }

    public RegisterPage enterName(String name) {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        elem.clear();
        elem.sendKeys(name);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        elem.clear();
        elem.sendKeys(email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        elem.clear();
        elem.sendKeys(password);
        return this;
    }

    public RegisterPage selectRole(String role) {
        WebElement selectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(roleSelect));
        Select select = new Select(selectElem);
        select.selectByValue(role);
        return this;
    }

    public LoginPage registerUser(String name, String email, String password, String role) {
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        if (role != null) {
            selectRole(role);
        }
        return submitExpectingSuccess();
    }

    public LoginPage submitExpectingSuccess() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        btn.click();
        return new LoginPage(driver);
    }

    public RegisterPage submitExpectingFailure() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return this;
    }

    public String getErrorMessage() {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return elem.getText();
    }

    public LoginPage clickLoginLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(loginLink));
        link.click();
        return new LoginPage(driver);
    }
}
