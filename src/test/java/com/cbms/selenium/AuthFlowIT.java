package com.cbms.selenium;

import com.cbms.selenium.pages.LoginPage;
import com.cbms.selenium.pages.RegisterPage;
import com.cbms.selenium.pages.SlotsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthFlowIT extends BaseIT {

    @Test
    @DisplayName("J1: Customer registration, login, welcome verification, logout, and protected slots redirect")
    void customerRegistrationAndLoginFlow() {
        String baseUrl = getBaseUrl();
        String customerName = "Automated Customer";
        String customerEmail = TestData.generateUniqueCustomerEmail();
        String customerPassword = "Password123!";

        // 1. Open registration page and register a new customer
        RegisterPage registerPage = new RegisterPage(driver).open(baseUrl);
        LoginPage loginPageAfterReg = registerPage.registerUser(customerName, customerEmail, customerPassword, "CUSTOMER");

        // Assert 1: Redirected to login with registration feedback (include error message if registration failed)
        String regError = registerPage.getRegistrationError();
        assertThat(loginPageAfterReg.getCurrentUrl())
                .as("Registration should redirect to login page" + (regError != null ? " (Registration failed with error: " + regError + ")" : ""))
                .contains("/login");
        assertThat(loginPageAfterReg.isSuccessMessageDisplayed())
                .as("Registration success message should be displayed")
                .isTrue();

        // 2. Log in with the newly registered customer credentials
        SlotsPage slotsPage = loginPageAfterReg.loginAsCustomer(customerEmail, customerPassword);

        // Assert 2: Redirected to /slots page
        assertThat(slotsPage.getCurrentUrl())
                .as("Logged-in customer should be redirected to slots page")
                .contains("/slots");

        // Assert 3: Welcome greeting contains registered user's name
        assertThat(slotsPage.getUserGreeting())
                .as("User greeting banner should display the registered customer's name")
                .contains(customerName);

        // 3. Log out
        LoginPage loginPageAfterLogout = slotsPage.logout();

        // Assert 4: Redirected to login page after logout
        assertThat(loginPageAfterLogout.getCurrentUrl())
                .as("Logout should redirect user to login page")
                .contains("/login");
        assertThat(loginPageAfterLogout.isLogoutMessageDisplayed())
                .as("Logout confirmation alert should be visible")
                .isTrue();

        // 4. Attempt unauthenticated access to /slots
        driver.get(baseUrl + "/slots");

        // Assert 5: Navigating to /slots unauthenticated redirects to /login
        assertThat(driver.getCurrentUrl())
                .as("Accessing /slots after logout must redirect to login page")
                .contains("/login");
    }
}
