package pageObjects;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {

	private WebDriver _driver;

	public LoginPage(WebDriver driver) {
		_driver = driver;
    }

    public WebElement title() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Heirloom\")"));
    }

    public WebElement googleLink() {
        return _driver.findElement(MobileBy.AccessibilityId("Continue with Google"));
    }
    //Landing screen link that opens the email/password sign-in form
    public WebElement signInLink() {
        return _driver.findElement(MobileBy.AccessibilityId("Already have an account? Sign In"));
    }
    //Landing screen link that opens the email/password sign-in form
    public WebElement emailLink() {
        return _driver.findElement(MobileBy.AccessibilityId("Continue with Email"));
    }

    // Google's account-picker dialog (com.google.android.gms) has no accessibility id on this
    // row, only a plain "text" value equal to the account's email.
    public WebElement chooseAccount(String accountName) {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().text(\"" + accountName + "\")"));
    }
}
