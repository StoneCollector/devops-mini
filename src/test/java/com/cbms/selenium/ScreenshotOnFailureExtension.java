package com.cbms.selenium;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JUnit 5 extension implementing AfterTestExecutionCallback.
 * Captures a screenshot immediately after test execution if an exception occurred,
 * before @AfterEach teardown runs and closes the WebDriver.
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotOnFailureExtension.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (context.getExecutionException().isPresent()) {
            Object testInstance = context.getRequiredTestInstance();
            if (testInstance instanceof BaseIT baseIT) {
                WebDriver driver = baseIT.getDriver();
                if (driver instanceof TakesScreenshot takesScreenshot) {
                    try {
                        String className = context.getRequiredTestClass().getSimpleName();
                        String methodName = context.getRequiredTestMethod().getName();
                        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
                        String fileName = String.format("%s_%s_%s.png", className, methodName, timestamp);

                        Path screenshotsDir = Paths.get("target", "screenshots");
                        Files.createDirectories(screenshotsDir);

                        Path targetPath = screenshotsDir.resolve(fileName);
                        byte[] pngBytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
                        Files.write(targetPath, pngBytes);

                        String absolutePath = targetPath.toAbsolutePath().toString();
                        log.error("[SCREENSHOT ON FAILURE] Saved failure screenshot to: {}", absolutePath);
                        System.err.println("[SCREENSHOT ON FAILURE] Saved: " + absolutePath);
                    } catch (Exception e) {
                        log.error("Failed to capture screenshot upon test failure", e);
                    }
                }
            }
        }
    }
}
