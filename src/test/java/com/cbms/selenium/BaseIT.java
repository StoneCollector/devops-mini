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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base Integration Test class for Selenium WebDriver tests.
 * Initializes ChromeDriver deterministically matching Chrome major version,
 * supports headless toggle (-Dheadless=true), -Dchrome.binary override,
 * and dynamically resolves base URL using configured app.baseUrl, local port 8085,
 * or the embedded Spring Boot random port.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseIT {

    private static final String DEFAULT_CHROME_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static String cachedMajorVersion = null;
    private static boolean versionResolutionAttempted = false;

    @LocalServerPort
    protected int port;

    protected WebDriver driver;

    public WebDriver getDriver() {
        return driver;
    }

    @BeforeEach
    public void setUpDriver() {
        String chromeBinaryOverride = System.getProperty("chrome.binary");
        String chromePath = (chromeBinaryOverride != null && !chromeBinaryOverride.trim().isEmpty())
                ? chromeBinaryOverride.trim()
                : DEFAULT_CHROME_PATH;

        File chromeFile = new File(chromePath);
        if (chromeFile.exists() && chromeFile.isFile()) {
            if (!versionResolutionAttempted) {
                cachedMajorVersion = readChromeMajorVersionFromFile(chromeFile.getAbsolutePath());
                versionResolutionAttempted = true;
            }

            if (cachedMajorVersion != null && !cachedMajorVersion.isEmpty()) {
                WebDriverManager.chromedriver().browserVersion(cachedMajorVersion).setup();
            } else {
                WebDriverManager.chromedriver().setup();
            }
        } else {
            // Fall back to current behaviour if the file is not found
            WebDriverManager.chromedriver().setup();
        }

        ChromeOptions options = new ChromeOptions();

        // Set the binary on ChromeOptions only if the override property is given
        if (chromeBinaryOverride != null && !chromeBinaryOverride.trim().isEmpty()) {
            options.setBinary(chromeBinaryOverride.trim());
        }

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

    private static String readChromeMajorVersionFromFile(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-NoProfile", "-NonInteractive", "-Command",
                    "$v = (Get-Item -LiteralPath '" + filePath.replace("'", "''") + "').VersionInfo; if ($v.ProductVersion) { $v.ProductVersion } else { $v.FileVersion }"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        output = line;
                        break;
                    }
                }
            }
            process.waitFor(5, TimeUnit.SECONDS);
            if (output != null) {
                Matcher matcher = Pattern.compile("^(\\d+)\\.").matcher(output.trim());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
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
