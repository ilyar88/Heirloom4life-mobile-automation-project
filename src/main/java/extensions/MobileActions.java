package extensions;

import java.time.Duration;
import java.util.function.Supplier;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;
import io.appium.java_client.*;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import io.qameta.allure.Allure;

public class MobileActions extends WebActions {

    private static final int MAX_SCROLL_SECONDS = 30;
    private static final int SWIPE_MS = 300;
    private static final int ZOOM_MIN_GAP = 20;
    private static final int ZOOM_MAX_GAP = 200;
    // Scroll down until the target is visible (e.g. () -> navigation.navigationBar("Account")),
    // giving up after MAX_SCROLL_SECONDS so it can't hang forever if the target never appears.
    public static void scrollDown(Supplier<WebElement> target) {
        Allure.step("Scroll down", () -> {
            Dimension size = _driver.manage().window().getSize();
            int x = size.getWidth() / 2;
            int startY = (int) (size.getHeight() * 0.8);
            int endY = (int) (size.getHeight() * 0.2);

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
                        swipe(x, startY, x, endY, SWIPE_MS);
                    } catch (Exception ignored) {
                        // this swipe attempt failed; keep polling
                    }
                    return false;
                });
            } catch (Exception e) {
                Verifications.assertFailed("Scroll down: target not found within " + MAX_SCROLL_SECONDS + "s");
            }
        });
    }
    // Swipe "right" (finger moves left to right) or "left" along the vertical center of the screen,
    // using the same 80% / 20% distances as scrollDown.
    private static void swipe(String direction) {
        Allure.step("Swipe " + direction, () -> {
            Dimension size = _driver.manage().window().getSize();
            int y = size.getHeight() / 2;
            int fromX = (int) (size.getWidth() * 0.8);
            int toX = (int) (size.getWidth() * 0.2);

            switch (direction.toLowerCase()) {
                case "right":
                    swipe(toX, y, fromX, y, SWIPE_MS);
                    break;
                case "left":
                    swipe(fromX, y, toX, y, SWIPE_MS);
                    break;
                default:
                    throw new IllegalArgumentException("Swipe direction must be right or left: " + direction);
            }
        });
    }
    // Zoom in (true) or out (false) on the center of the element: two fingers, one above and one below the
    // center, move apart or toward each other.
    public static void zoom(WebElement element, boolean zoomIn) {
        Allure.step(zoomIn ? "Zoom in" : "Zoom out", () -> {
            int x = element.getLocation().getX() + element.getSize().getWidth() / 2;
            int y = element.getLocation().getY() + element.getSize().getHeight() / 2;
            int from = zoomIn ? ZOOM_MIN_GAP : ZOOM_MAX_GAP;
            int to = zoomIn ? ZOOM_MAX_GAP : ZOOM_MIN_GAP;

            new MultiTouchAction((PerformsTouchActions) _driver)
                    .add(finger(x, y - from, x, y - to, SWIPE_MS))
                    .add(finger(x, y + from, x, y + to, SWIPE_MS))
                    .perform();
        });
    }
    // Press at the start point, hold for durationMs, drag to the end point and release.
    public static void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        finger(startX, startY, endX, endY, durationMs).perform();
    }

    private static TouchAction<?> finger(int startX, int startY, int endX, int endY, int durationMs) {
        return new TouchAction<>((PerformsTouchActions) _driver)
                .press(PointOption.point(startX, startY))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(durationMs)))
                .moveTo(PointOption.point(endX, endY))
                .release();
    }
}
