package pageObjects;

import java.util.*;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.*;

public class MemoirPage {

	private WebDriver _driver;

	public MemoirPage(WebDriver driver) {
		_driver = driver;
    }
    // Row of the "Add to Memoir" sheet (a ViewGroup) that has a child described by the given option
    // (e.g. "Take Photo", "Choose from Library", "Import from URL", "Pick a file").
    public WebElement addToMemior(String option) {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().className(\"android.view.ViewGroup\").childSelector(new UiSelector().description(\"" + option + "\"))"));
    }
    // Selection check icon on every image tile of the system photo picker.
    public List<WebElement> selectImage() {
        return _driver.findElements(MobileBy.AndroidUIAutomator(
                "new UiSelector().resourceId(\"com.google.android.providers.media.module:id/icon_check\")"));
    }
    // A file row in the "Pick a file" picker, found by its exact file name.
    public WebElement uploadFile(String fileName) {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().text(\"" + fileName + "\")"));
    }

    public WebElement AddImage() {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().resourceId(\"com.google.android.providers.media.module:id/button_add\")"));
    }

    public WebElement search() {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().resourceId(\"com.google.android.documentsui:id/search_src_text\")"));
    }
    // Upload progress text; the percentage after "Uploading" changes, so match only the fixed prefix.
    public WebElement uploading() {
        return _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Uploading\")"));
    }
}