package extensions;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import io.qameta.allure.Allure;

public class UiActions {
	
	private static WebDriver _driver;
	
	public static void init(WebDriver driver) {
		_driver = driver;
    }
	
	// Returns a short, readable name for a WebElement's locator.
	public static String elementName(WebElement elem) {
    	final Pattern LOCATOR_PAT =
                Pattern.compile(".*->\\s*[^:]+:\\s*(.*?)]");
    	
        if (elem == null) return "<null>";
        String s = elem.toString();
        Matcher m = LOCATOR_PAT.matcher(s);
        return m.matches() ? m.group(1).trim() : s.trim();
    }

	public static void click(WebElement elem) { 
		Allure.step("Click on element (" + elementName(elem) + ")", () -> elem.click()); 
	}

    public static void enterText(WebElement elem, String text) {
    	//Hide text that equal to environment variable in the allure step
    	String shown = text;
    	for (String val : System.getenv().values()) {
            if (text.equals(val)) {
            	shown = text.replace(val, "*".repeat(val.length())); break;
            }
        }

        Allure.step("Enter text (" + elementName(elem) + "): " + shown, () -> {
        	try {
        	    elem.click();
        	} catch (Exception ignored) {
        	    // If element click failed continue to sendKeys
        	} finally {
        	    elem.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        	    elem.sendKeys(Keys.DELETE);
        	    elem.sendKeys(text);
        	}
        });
    }

    public static void selectOption(WebElement elem, String option, String value) {
            Select dropdown = new Select(elem);
            switch (option) {
                case "value":
                    dropdown.selectByValue(value);
                    break;
                case "index":
                    dropdown.selectByIndex(Integer.parseInt(value));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid select type: " + option);
            }
            Allure.step("Select option (" + elementName(elem) + "): "
            		+ dropdown.getFirstSelectedOption().getText(), () -> {
        });
    }
    
    //Move to the element that can't be seen in the page
    public static void moveToElement(WebElement elem, int num) {
        Allure.step("Move mouse to element", () -> {
            new Actions(_driver).moveToElement(elem).pause(Duration.ofMillis(num)).click().perform();
        });
    }


    public static String getText(WebElement elem) {
    	// Search for the first number in the text, if no number return all the text
        String text = elem.getText();
        Matcher num = Pattern.compile("-?\\d+").matcher(text);
        String r = num.find() ? num.group() : text;
        return Allure.step("Element text: " + r, () -> r);
    }
}
