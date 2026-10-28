package pageObjects;

import extensions.MobileActions;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.*;
import utilities.WaitForElement;

public class StoriesPage {

	private WebDriver _driver;

	public StoriesPage(WebDriver driver) {
		_driver = driver;
    }

    private static final int MAX_SWIPES = 5;

    public WebElement title() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Stories\")"));
    }

    public WebElement bookOfLife() {
        return _driver.findElement(MobileBy.AccessibilityId("Book of Life"));
    }

    public WebElement chat() {
        return _driver.findElement(MobileBy.AccessibilityId("Chat"));
    }
    // The text ends with an arrow character, so match only the fixed part
    public WebElement startTheirGift() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Start their gift\")"));
    }
    // Center button of the bottom bar (the "Record" label is a separate text node)
    public WebElement record() {
        return _driver.findElement(MobileBy.AccessibilityId("Record"));
    }

    public WebElement share() {
        return _driver.findElement(MobileBy.AccessibilityId("Share Memoir"));
    }
    // One "Filter by topic" chip by its name. The names change with the stories and the strip scrolls
    // sideways, so look for the chip by its description and, only when it isn't there, swipe right MAX_SWIPES
    // times (back to the start of the strip) and then left MAX_SWIPES times.
    public WebElement filter(String name) {
        int width = _driver.manage().window().getSize().getWidth();
        WebElement strip = _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().className(\"android.widget.HorizontalScrollView\")"));
        int y = strip.getLocation().getY() + strip.getSize().getHeight() / 2;
        for (int i = 0; i < 2 * MAX_SWIPES; i++) {
            try {
                return WaitForElement.waitFor(() -> _driver.findElement(MobileBy.AccessibilityId("Filter by " + name)), "element exists", 1);
            } catch (TimeoutException notVisibleYet) {
                boolean left = i >= MAX_SWIPES;
                MobileActions.swipe((int) (width * (left ? 0.7 : 0.3)), y, (int) (width * (left ? 0.3 : 0.7)), y, 300);
            }
        }
        throw new NoSuchElementException("Topic filter not found: " + name);
    }
}
