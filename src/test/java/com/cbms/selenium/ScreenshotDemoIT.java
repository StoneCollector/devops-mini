package com.cbms.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deliberately failing test class tagged with @Tag("demo").
 * Excluded by default from normal builds via maven-failsafe-plugin.
 * Can be explicitly executed to prove the failure screenshot capture mechanism via:
 *   mvn verify -Dgroups=demo
 */
@Tag("demo")
public class ScreenshotDemoIT extends BaseIT {

    @Test
    @DisplayName("Demo: Deliberately failing test to trigger failure screenshot capture")
    void deliberateFailureForScreenshotVerification() {
        String baseUrl = getBaseUrl();
        driver.get(baseUrl + "/login");

        // Assert 1: Valid assertion (pre-check)
        assertThat(driver.getCurrentUrl())
                .as("Should reach login page")
                .contains("/login");

        // Assert 2: Deliberate failure assertion to trigger ScreenshotOnFailureExtension
        assertThat(driver.getTitle())
                .as("Deliberately failing assertion to generate a screenshot in target/screenshots/")
                .isEqualTo("NonExistentPageTitle_ExpectedToFailForScreenshotProof");
    }
}
