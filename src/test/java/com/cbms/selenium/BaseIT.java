package com.cbms.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Base Integration Test class for Selenium WebDriver tests.
 * Initializes ChromeDriver, supports headless toggle (-Dheadless=true),
 * and dynamically resolves base URL using configured app.baseUrl, local port 8085,
 * or the embedded Spring Boot random port.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseIT {

    @LocalServerPort
    protected int port;

    protected WebDriver driver;

    public WebDriver getDriver() {
        return driver;
    }

    @BeforeEach
    public void setUpDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        String headlessProperty = System.getProperty("headless");
        boolean headless = "true".equalsIgnoreCase(headlessProperty) || Boolean.getBoolean("headless");

        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        // Note: Implicit wait is intentionally omitted in favor of explicit WebDriverWait (10s) in page objects.
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    /**
     * Resolves the base URL for the test execution.
     * Priority:
     * 1. System property app.baseUrl (e.g. -Dapp.baseUrl=http://localhost:8085)
     * 2. Live local app on port 8085 if reachable
     * 3. Embedded Spring Boot random port from @LocalServerPort
     */
    public String getBaseUrl() {
        String configuredUrl = System.getProperty("app.baseUrl");
        if (configuredUrl != null && !configuredUrl.trim().isEmpty()) {
            return configuredUrl.trim().replaceAll("/+$", "");
        }

        if (isReachable("http://localhost:8085/login")) {
            return "http://localhost:8085";
        }

        return "http://localhost:" + port;
    }

    private boolean isReachable(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
            conn.setConnectTimeout(300);
            conn.setReadTimeout(300);
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
