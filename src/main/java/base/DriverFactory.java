package base;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import extensions.WebActions;

//Creates the WebDriver (chrome/firefox/edge/android/ios) from the config properties.
public class DriverFactory {

    // Standard Android framework id for the window's content view (every screen has one).
    // Scoping an AndroidUIAutomator lookup under it skips the status/navigation bar nodes
    // that sit outside it, so UiAutomator2 walks a smaller tree on every element lookup.
    public static final String CONTENT_ROOT = "android:id/content";

    private final Properties prop;
    //Use temp browser profile for the GitHub Actions
    private Path tempProfileDir;

    public DriverFactory(Properties prop) {
        this.prop = prop;
    }

    public WebDriver create() {
        WebDriver driver;
        boolean headless = true;

        if (tempProfileDir == null) {
            try {
                tempProfileDir = Files.createTempDirectory("browser-profile-");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            // Create the driver by the "Denv" value: it comes from -Denv=... via the pom (surefire)
            // and config.properties. Allowed: chrome (default), firefox, edge = a local browser;
            // android, ios = a real device through the Appium server. Browsers run headless.
            switch (prop.getProperty("Denv")) {
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
                WebActions.describeElement(callerLabel(using));
                return super.findElement(by, scopeToContentRoot(by, using));
            }

            @Override
            public List<WebElement> findElements(String by, String using) {
                WebActions.describeElement(callerLabel(using));
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
    // Allure label for the element this lookup resolves: the name of the page-object method that
    // triggered it, plus "using" (the locator's value) when that method takes a parameter, e.g.
    // "chooseAccount or "record()" for a no-arg one. Walks the call stack
    // to the first pageObjects frame (unwrapping a lambda's synthetic name back to its enclosing
    // method) and checks that method's real parameter count via reflection.
    private static String callerLabel(String using) {
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String className = frame.getClassName();
            if (!className.startsWith("pageObjects.")) continue;

            String methodName = frame.getMethodName();
            if (methodName.startsWith("lambda$")) {
                methodName = methodName.substring(7, methodName.indexOf('$', 7));
            }

            boolean hasParam = false;
            try {
                for (Method m : Class.forName(className).getDeclaredMethods()) {
                    if (m.getName().equals(methodName)) {
                        hasParam = m.getParameterCount() > 0;
                        break;
                    }
                }
            } catch (ClassNotFoundException ignored) {
                // Keep hasParam false; still report the method name.
            }
            return methodName + (hasParam ? "(" + paramValue(using) + ")" : "()");
        }
        return using;
    }
    // "using" is either the plain locator value (accessibility id) or UiSelector source (AndroidUIAutomator);
    // for the latter, pull out just the parameter's literal (the last quoted string) instead of the source.
    // Masked in case it's a secret (OS env var or .env file value), e.g. the login email.
    private static String paramValue(String using) {
        Matcher m = Pattern.compile("\"([^\"]*)\"(?!.*\")").matcher(using);
        return WebActions.mask(m.find() ? m.group(1) : using);
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
}
