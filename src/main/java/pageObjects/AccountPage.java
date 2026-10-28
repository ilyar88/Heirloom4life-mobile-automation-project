package pageObjects;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.*;

public class AccountPage {

	private WebDriver _driver;

	public AccountPage(WebDriver driver) {
		_driver = driver;
    }

    // Any Account menu item or button, found by its exact visible text (e.g. "Notifications", "Sign Out").
    public WebElement byName(String name) {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"" + name + "\")"));
    }
}
