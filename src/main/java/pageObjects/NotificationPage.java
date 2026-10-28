package pageObjects;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.*;

public class NotificationPage {

	private WebDriver _driver;

	public NotificationPage(WebDriver driver) {
		_driver = driver;
    }
    // The description includes the unread count ("Notifications, 48 unread")
    public WebElement notification() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().descriptionStartsWith(\"Notifications\")"));
    }
    // The minutes number changes ("34 min left"), so match only the fixed part
    public WebElement minLeft() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"min left\")"));
    }

    public WebElement upgrade() {
        return _driver.findElement(MobileBy.AccessibilityId("Open subscription to upgrade or top up"));
    }
}
