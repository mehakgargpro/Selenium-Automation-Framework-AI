package com.mehak.framework.utilities;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotUtils.class);

    private ScreenshotUtils() {
    }

    public static String capture(WebDriver driver, String testName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        Path screenshotsDir = Paths.get("test-output", "screenshots");
        try {
            Files.createDirectories(screenshotsDir);
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path screenshotPath = screenshotsDir.resolve(safeTestName + "_" + timestamp + ".png");
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(screenshotPath, screenshotBytes);
            LOGGER.info("Screenshot saved to {}", screenshotPath.toAbsolutePath());
            return screenshotPath.toString();
        } catch (IOException e) {
            LOGGER.error("Unable to capture screenshot for test {}", testName, e);
            return "";
        }
    }
}
