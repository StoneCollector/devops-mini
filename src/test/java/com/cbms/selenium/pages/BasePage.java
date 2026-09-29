package com.cbms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base Page Object encapsulating common elements, WebDriver instance,
 * and 10-second explicit WebDriverWait.
 * No assertions are performed inside page objects.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getUserGreeting() {
        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userGreeting")));
        return greeting.getText();
    }

    public String getUserName() {
        WebElement name = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userName")));
        return name.getText();
    }

    public LoginPage logout() {
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("logoutBtn")));
        logoutBtn.click();
        return new LoginPage(driver);
    }
}
