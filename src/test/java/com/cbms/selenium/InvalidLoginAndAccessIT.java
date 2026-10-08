package com.cbms.selenium;

import com.cbms.selenium.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InvalidLoginAndAccessIT extends BaseIT {

    @Test
    @DisplayName("J2: Invalid login shows error on login page and unauthenticated access redirects to login")
    void invalidLoginAndUnauthenticatedAccess() {
        String baseUrl = getBaseUrl();

        // 1. Attempt login with an invalid password
        LoginPage loginPage = new LoginPage(driver).open(baseUrl);
        loginPage.loginExpectingFailure(TestData.SEEDED_CUSTOMER_EMAIL, "DefinitelyWrongPassword123!");

        // Assert 1: Remains on login page
        assertThat(loginPage.getCurrentUrl())
                .as("URL should stay on /login upon authentication failure")
                .contains("/login");

        // Assert 2: Meaningful error message is presented
        assertThat(loginPage.getErrorMessage())
                .as("Error alert should indicate invalid credentials")
                .containsIgnoringCase("Invalid email or password");

        // 2. Attempt unauthenticated GET access to /slots
        driver.get(baseUrl + "/slots");

        // Assert 3: Unauthenticated GET /slots redirects to /login
        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated request to /slots must redirect to login page")
                .contains("/login");

        // 3. Attempt unauthenticated GET access to /bookings and /my-bookings
        driver.get(baseUrl + "/bookings");

        // Assert 4: Unauthenticated GET /bookings redirects to /login
        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated request to /bookings must redirect to login page")
                .contains("/login");

        driver.get(baseUrl + "/bookings/me");

        // Assert 5: Unauthenticated GET /bookings/me redirects to /login
        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated request to /bookings/me must redirect to login page")
                .contains("/login");
    }
}
