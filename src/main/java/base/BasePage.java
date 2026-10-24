package base;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.firefox.*;
import org.testng.annotations.*;
import org.testng.annotations.Listeners;
import extensions.MobileActions;
import extensions.WebActions;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.openqa.selenium.remote.DesiredCapabilities;
import utilities.*;
import java.net.URL;

@Listeners({AllureTestNg.class, utilities.Listeners.class })
public class BasePage {
    //Base test class include: loads config, initializes WebDriver and handles teardown.
    //Public and static so workflow/page classes (e.g. LoginFlow) can reach the one active
    //driver for the suite without needing a BasePage instance passed around.
	public static WebDriver driver;
    protected Properties prop;
    // Standard Android framework id for the window's content view (every screen has one).
    // Scoping an AndroidUIAutomator lookup under it skips the status/navigation bar nodes
    // that sit outside it, so UiAutomator2 walks a smaller tree on every element lookup.
    public static final String CONTENT_ROOT = "android:id/content";

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
    //Prepare browser before tests: create driver, set waits, open URL, init page objects.
    @BeforeClass(alwaysRun = true)
    public void setup() {
        driver = getDriver();
        String build = isMobile() ? buildNumber() : null;
        writeEnvironmentInfo(build);

        int waitSeconds = Integer.parseInt(prop.getProperty("waitTime"));

        driver.manage().timeouts().implicitlyWait(waitSeconds, TimeUnit.SECONDS);

        if (!isMobile()) {
            driver.manage().window().maximize();
            driver.get(prop.getProperty("URL"));
        } else {
            MobileActions.init(prop.getProperty("Adb_path"), prop.getProperty("Udid"));
        }

        WebActions.init(driver);
        WaitForElement.init(driver, waitSeconds);
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
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
    //Use temp browser profile for the GitHub Actions
    private Path tempProfileDir;

    public WebDriver getDriver() {
        // One driver per suite: return the existing session instead of starting a second one.
        if (driver != null) return driver;

        boolean headless = true;

        if (tempProfileDir == null) {
            try {
                tempProfileDir = Files.createTempDirectory("browser-profile-");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            switch (prop.getProperty("browser")) {
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions fOpts = new FirefoxOptions();
                    fOpts.addArguments("-profile", tempProfileDir.toAbsolutePath().toString());
                    fOpts.addArguments("-width=1920", "-height=1080");
                    if (headless) fOpts.addArguments("-headless");
                    driver = new FirefoxDriver(fOpts);
                    break;

                case "edge":
                    WebDriverManager.edgedriver().setup();
                    driver = new EdgeDriver(new EdgeOptions());
                    break;

                case "android":
                    driver = getAndroidDriver();
                    break;

                case "ios":
                    driver = getIosDriver();
                    break;

                case "chrome":
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions cOpts = new ChromeOptions();
                    cOpts.addArguments("--user-data-dir=" + tempProfileDir.toAbsolutePath());
                    cOpts.addArguments("--window-size=1920,1080");
                    if (headless) cOpts.addArguments("--headless");
                    driver = new ChromeDriver(cOpts);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return driver;
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
        String browser = prop.getProperty("browser");
        return "android".equals(browser) || "ios".equals(browser);
    }

    private AndroidDriver<WebElement> getAndroidDriver() throws IOException {
        DesiredCapabilities dc = new DesiredCapabilities();
        String udid = prop.getProperty("Udid");
        if (udid != null) dc.setCapability("udid", udid);
        dc.setCapability("appPackage", prop.getProperty("App_package"));
        dc.setCapability("appActivity", prop.getProperty("App_activity"));
        dc.setCapability("platformName", "android");
        //dc.setCapability("automationName", "UiAutomator2");
        // Every UiAutomator locator is scoped under CONTENT_ROOT here, so page objects need no change.
        return new AndroidDriver<WebElement>(new URL(prop.getProperty("Appium_server")), dc) {
            @Override
            public WebElement findElement(String by, String using) {
                return super.findElement(by, scopeToContentRoot(by, using));
            }

            @Override
            public List<WebElement> findElements(String by, String using) {
                return super.findElements(by, scopeToContentRoot(by, using));
            }
        };
    }

    // Wraps a UiSelector so the search runs only inside CONTENT_ROOT (a smaller tree, so elements are found faster);
    // other locator types are returned unchanged.
    private static String scopeToContentRoot(String by, String using) {
        return "-android uiautomator".equals(by) && using.startsWith("new UiSelector()")
                ? "new UiSelector().resourceId(\"" + CONTENT_ROOT + "\").childSelector(" + using + ")"
                : using;
    }

    private IOSDriver<MobileElement> getIosDriver() throws IOException {
        DesiredCapabilities dc = new DesiredCapabilities();
        String udid = prop.getProperty("Udid");
        if (udid != null) dc.setCapability("udid", udid);
        dc.setCapability("bundleId", prop.getProperty("Bundle_id"));
        dc.setCapability("platformName", "ios");
        //dc.setCapability("automationName", "XCUITest");
        return new IOSDriver<>(new URL(prop.getProperty("Appium_server")), dc);
    }
    //Take a screenshot, save it and attach to Allure.
    public void takeSnapShot(String name) throws IOException {
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