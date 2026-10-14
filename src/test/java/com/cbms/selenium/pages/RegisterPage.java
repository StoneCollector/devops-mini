package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

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
    private final By nameError = By.id("nameError");
    private final By emailError = By.id("emailError");
    private final By passwordError = By.id("passwordError");
    private final By loginLink = By.id("loginLink");

    private String registrationError = null;

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

        registrationError = null;
        wait.until(d -> {
            if (d.getCurrentUrl().contains("/login")) {
                return true;
            }
            for (By errorLocator : List.of(errorMessage, nameError, emailError, passwordError)) {
                List<WebElement> elements = d.findElements(errorLocator);
                for (WebElement el : elements) {
                    try {
                        if (el.isDisplayed() && !el.getText().trim().isEmpty()) {
                            registrationError = el.getText().trim();
                            return true;
                        }
                    } catch (WebDriverException ignored) {
                    }
                }
            }
            return false;
        });

        if (driver.getCurrentUrl().contains("/login")) {
            wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        }

        return new LoginPage(driver);
    }

    public RegisterPage submitExpectingFailure() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return this;
    }

    public String getRegistrationError() {
        return registrationError;
    }

    public boolean hasRegistrationError() {
        return registrationError != null;
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
