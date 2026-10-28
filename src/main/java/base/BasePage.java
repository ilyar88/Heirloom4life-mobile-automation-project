package base;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.testng.annotations.Listeners;
import extensions.WebActions;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import utilities.*;

@Listeners({AllureTestNg.class})
public class BasePage {
    //Base test class include: loads config, creates the WebDriver and handles teardown.
    //The test class owns the driver and passes it to the workflows and page objects.
    protected Properties prop;

    public BasePage() throws IOException {
        prop = new Properties();
        Path cfg = Paths.get(System.getProperty("user.dir"),
                "src", "main", "java", "configuration", "config.properties");
        try (InputStream input = Files.newInputStream(cfg)) {
            prop.load(input);
        }
        // Any property written as ${NAME} is a placeholder; DataUtil resolves it from the local
        // .env file or a JVM system property (e.g. CI secrets), or leaves it as-is otherwise.
        for (String key : prop.stringPropertyNames()) {
            prop.setProperty(key, DataUtil.resolveEnv(prop.getProperty(key)));
        }
    }

    public WebDriver getDriver() {
        return new DriverFactory(prop).create();
    }

    //Prepare the driver before tests: set waits, open URL, init helpers.
    public void setup(WebDriver driver) {
        String build = isMobile() ? buildNumber() : null;
        writeEnvironmentInfo(build);

        int waitSeconds = Integer.parseInt(prop.getProperty("waitTime"));

        driver.manage().timeouts().implicitlyWait(waitSeconds, TimeUnit.SECONDS);

        if (!isMobile()) {
            driver.manage().window().maximize();
            driver.get(prop.getProperty("URL"));
        }

        WebActions.init(driver);
        WaitForElement.init(driver, waitSeconds);
    }

    public void teardown(WebDriver driver) {
        // Swallow quit() failures so a flaky driver shutdown doesn't mask the real test result.
        try { if (driver != null) driver.quit(); }
        catch (Exception ignore) {}
    }

    // Writes allure-results/environment.properties so the report's Environment widget shows the
    // installed app's build number. Unlike executor.json, the Allure Maven plugin never
    // regenerates this file, so whatever we write here survives into the report untouched.
    private void writeEnvironmentInfo(String build) {
        if (build == null) return;
        Path envFile = Paths.get(System.getProperty("user.dir"), "allure-results", "environment.properties");
        try {
            Files.createDirectories(envFile.getParent());
            Files.writeString(envFile, "Build=" + build);
        } catch (IOException ignored) {
            // Allure report just won't show the build name; not worth failing the suite for.
        }
    }

    // Returns the installed app's version (e.g. "v2.6.11 (309)"), or null if it can't be read.
    // Shells out to adb directly rather than Appium's "mobile: shell", since that requires the
    // server to be started with the adb_shell insecure feature enabled, which isn't guaranteed.
    private String buildNumber() {
        try {
            Process process = new ProcessBuilder(
                    prop.getProperty("Adb_path"), "-s", prop.getProperty("Udid"),
                    "shell", "dumpsys", "package", prop.getProperty("App_package")
            ).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor();

            Matcher versionName = Pattern.compile("versionName=(\\S+)").matcher(output);
            Matcher versionCode = Pattern.compile("versionCode=(\\d+)").matcher(output);
            if (versionName.find() && versionCode.find()) {
                return "v" + versionName.group(1) + " (" + versionCode.group(1) + ")";
            }
        } catch (Exception ignored) {
            // adb not available; skip the build number
        }
        return null;
    }

    private boolean isMobile() {
        String device = prop.getProperty("Denv");
        return "android".equals(device) || "ios".equals(device);
    }

    //Take a screenshot, save it and attach to Allure.
    public void takeSnapShot(WebDriver driver, String name) throws IOException {
        Allure.step("Take snapshot", () -> {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            File destFile = Paths.get(
                    System.getProperty("user.dir"), "target", "screenshots",name, timestamp() + ".png").toFile();

            // Ensure folder exists
            destFile.getParentFile().mkdirs();

            FileUtils.copyFile(srcFile, destFile);

            try (java.io.FileInputStream content = new java.io.FileInputStream(destFile)) {
                Allure.addAttachment(timestamp(), "image/png", content, "png");
            }
        });
    }

    String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
    }
}
