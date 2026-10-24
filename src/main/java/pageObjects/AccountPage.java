package pageObjects;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AccountPage {

	private WebDriver _driver;

	public AccountPage(WebDriver driver) {
		_driver = driver;
    }

    public WebElement notifications() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Notifications\")"));
    }
    
    public WebElement trustedPersonPrivacy() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Trusted Person & Privacy\")"));
    }

    public WebElement blockedStorytellers() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Blocked Storytellers\")"));
    }

    public WebElement helpAndSupport() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Help & Support\")"));
    }

    public WebElement signOut() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Sign Out\")"));
    }

    public WebElement signOutButton() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Sign Out\")"));
    }

    public WebElement deleteAccount() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Delete Account\")"));
    }
}