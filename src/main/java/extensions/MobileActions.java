package extensions;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import io.qameta.allure.Allure;

public class MobileActions extends WebActions {

    private static final int MAX_SCROLL_SECONDS = 30;
    private static String _adbPath;
    private static String _udid;

    public static void init(String adbPath, String udid) {
        _adbPath = adbPath;
        _udid = udid;
    }

    // Scroll down until the target is visible (e.g. () -> navigation.navigationBar("Account")),
    // giving up after MAX_SCROLL_SECONDS so it can't hang forever if the target never appears.
    // Shells out to adb directly: this Appium server doesn't relay a drag/swipe gesture to the
    // device via either the W3C Actions API or the legacy TouchAction API (confirmed on-device:
    // a raw "adb shell input swipe" scrolls fine, but the identical gesture through Appium does nothing).
    public static void scrollDown(Supplier<WebElement> target) {
        Allure.step("Scroll down", () -> {
            int[] size = screenSize();
            int x = size[0] / 2;
            int startY = (int) (size[1] * 0.8);
            int endY = (int) (size[1] * 0.2);

            FluentWait<WebDriver> wait = new FluentWait<>(_driver)
                    .withTimeout(Duration.ofSeconds(MAX_SCROLL_SECONDS))
                    .pollingEvery(Duration.ofMillis(1))
                    .ignoring(NoSuchElementException.class);

            try {
                wait.until(d -> {
                    try {
                        // "Displayed" alone can mean hidden behind the bottom tab bar, where a click misses the target;
                        // require its top to be above the swipe start line instead (kept to one cheap call: Appium calls are slow here).
                        if (target.get().getLocation().getY() < startY) return true;
                    } catch (NoSuchElementException notYetVisible) {
                        // fall through and swipe
                    }

                    try {
                        new ProcessBuilder(_adbPath, "-s", _udid, "shell", "input", "swipe",
                                String.valueOf(x), String.valueOf(startY), String.valueOf(x), String.valueOf(endY), "300")
                                .start().waitFor();
                    } catch (Exception ignored) {
                        // this swipe attempt failed to launch; keep polling
                    }
                    return false;
                });
            } catch (Exception e) {
                Verifications.assertFailed("Scroll down: target not found within " + MAX_SCROLL_SECONDS + "s");
            }
        });
    }

    // Real device pixel dimensions via adb, since Appium's driver.manage().window().getSize()
    // isn't guaranteed to be in the same coordinate space "adb shell input swipe" expects.
    private static int[] screenSize() {
        try {
            Process process = new ProcessBuilder(_adbPath, "-s", _udid, "shell", "wm", "size")
                    .redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor();

            Matcher m = Pattern.compile("(\\d+)x(\\d+)").matcher(output);
            if (m.find()) {
                return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
            }
        } catch (Exception ignored) {
            // fall back below
        }
        org.openqa.selenium.Dimension size = _driver.manage().window().getSize();
        return new int[]{size.getWidth(), size.getHeight()};
    }
}
