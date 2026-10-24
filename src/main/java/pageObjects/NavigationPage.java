package pageObjects;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class NavigationPage {

	private WebDriver _driver;

	public NavigationPage(WebDriver driver) {
		_driver = driver;
    }

    // Bottom navigation bar options: "Stories" "Media", "Shared", "Account"
    public WebElement navigationBar(String value) {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"" + value + "\")"));
    }
}