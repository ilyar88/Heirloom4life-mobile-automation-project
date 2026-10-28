package pageObjects;

import java.util.*;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.*;
import utilities.WaitForElement;

public class MediaPage {

	private WebDriver _driver;

	public MediaPage(WebDriver driver) {
		_driver = driver;
    }
    // Media filter tabs: "All", "Photos", "Videos", "Docs"
    public WebElement mediaFilter(String value) {
        return _driver.findElement(MobileBy.AccessibilityId(value));
    }
    // All the tiles of the media grid that are loaded now (tiles further down only appear after scrolling).
    // The tiles are the clickable items of the second (nested) ScrollView; the first ScrollView is the whole
    // page. UiSelector returns only some matches of a list on this server, so look up the tiles one by one
    // by index until there is none.
    public List<WebElement> stories() {
        List<WebElement> tiles = new ArrayList<>();
        while (true) {
            int index = tiles.size();
            try {
                tiles.add(WaitForElement.waitFor(() -> _driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().className(\"android.widget.ScrollView\").instance(1).childSelector(new UiSelector().clickable(true).instance(" + index + "))")), "element exists", 1));
            } catch (TimeoutException noMoreTiles) {
                return tiles;
            }
        }
    }
    // The link icon (an SVG) sits two levels under the element described "Link to story".
    public WebElement linkToStory() {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().description(\"Link to story\").childSelector(new UiSelector().childSelector(new UiSelector().className(\"com.horcrux.svg.GroupView\")))"));
    }
    // Row of the "Link to Story" list (a ViewGroup) that has a child with the given title text.
    public WebElement linkToStoryItem(String title) {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().className(\"android.view.ViewGroup\").childSelector(new UiSelector().textContains(\"" + title + "\"))"));
    }
    // The "+" FAB (built from two SVG paths), picked out as the sibling of the bottom nav bar (which has "Media").
    public WebElement plusIcon() {
        return _driver.findElement(MobileBy.AndroidUIAutomator(
                "new UiSelector().className(\"android.view.ViewGroup\").childSelector(new UiSelector().childSelector(new UiSelector().className(\"com.horcrux.svg.PathView\"))).fromParent(new UiSelector().childSelector(new UiSelector().textContains(\"Media\")))"));
    }
}