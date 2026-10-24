package pageObjects;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class StoriesPage {

	private WebDriver _driver;

	public StoriesPage(WebDriver driver) {
		_driver = driver;
    }

    public WebElement title() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Stories\")"));
    }

    // The minutes number changes ("34 min left"), so match only the fixed part
    public WebElement minLeft() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"min left\")"));
    }

    public WebElement upgrade() {
        return _driver.findElement(MobileBy.AccessibilityId("Open subscription to upgrade or top up"));
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

    // The description includes the unread count ("Notifications, 48 unread")
    public WebElement notifications() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().descriptionStartsWith(\"Notifications\")"));
    }

    public WebElement share() {
        return _driver.findElement(MobileBy.AccessibilityId("Share Memoir"));
    }
}
