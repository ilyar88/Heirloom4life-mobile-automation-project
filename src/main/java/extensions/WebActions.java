package extensions;

import java.time.Duration;
import java.util.regex.*;
import java.util.stream.Stream;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import io.qameta.allure.Allure;
import utilities.DataUtil;

public class WebActions {
	
	protected static WebDriver _driver;

	// Set by the driver's findElement(By) hook to the page-object method (and argument, if it has
	// one) that looked the element up, e.g. "chooseAccount or "record()".
	private static final ThreadLocal<String> lastElementLabel = new ThreadLocal<>();

	// The label of the last "Click on ..." step shown, so a click that immediately repeats on the
	// same target (e.g. waiting for a confirmation dialog's button with the same name) doesn't show
	// as a second, identical-looking step.
	private static final ThreadLocal<String> lastClickLabel = new ThreadLocal<>();

	public static void init(WebDriver driver) {
		_driver = driver;
		lastClickLabel.remove();
    }
	// Records which page-object call last looked up an element, so functionName() can report the
	// call instead of the raw locator.
	public static void describeElement(String label) {
		lastElementLabel.set(label);
	}
	// Returns a short, readable name for a WebElement: the page-object call that found it when
	// known, otherwise its locator.
	public static String functionName(WebElement elem) {
    	final Pattern LOCATOR_PAT =
                Pattern.compile(".*->\\s*[^:]+:\\s*(.*?)]");

    	String label = lastElementLabel.get();
    	if (label != null) return label;

        if (elem == null) return "<null>";
        String s = elem.toString();
        Matcher m = LOCATOR_PAT.matcher(s);
        return mask(m.matches() ? m.group(1).trim() : s.trim());
    }

	// Masks value with asterisks if it equals a configured secret (OS env var or .env file value);
	// otherwise returns it unchanged.
	public static String mask(String value) {
		boolean isSecret = Stream.concat(System.getenv().values().stream(), DataUtil.dotEnvValues().stream())
				.anyMatch(val -> value.equals(String.valueOf(val)));
		return isSecret ? "*".repeat(value.length()) : value;
	}

	// Masks this test's already-recorded Allure parameters (e.g. "arg0" from a @Test/@DataProvider
	// argument) in place, for any value that's a secret. Call at the top of the test method, after
	// AllureTestNg has attached the parameters but before they're otherwise shown in the report.
	public static void maskTestParameters() {
		Allure.getLifecycle().updateTestCase(tr -> tr.getParameters().forEach(p -> p.setValue(mask(p.getValue()))));
	}

	public static void click(WebElement elem) {
		String label = "Click on " + functionName(elem);
		if (label.equals(lastClickLabel.get())) {
			elem.click();
		} else {
			lastClickLabel.set(label);
			Allure.step(label, () -> elem.click());
		}
	}

    public static void enterText(WebElement elem, String text) {
        Allure.step("Enter text " + functionName(elem) + ": " + mask(text), () -> {
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
            Allure.step("Select option " + functionName(elem) + ": "
            		+ dropdown.getFirstSelectedOption().getText(), () -> {
        });
    }   
    //Move to the element that can't be seen in the page
    public static void moveToElement(WebElement elem, int num) {
        Allure.step("Move mouse to " + functionName(elem), () -> {
            new Actions(_driver).moveToElement(elem).pause(Duration.ofMillis(num)).click().perform();
        });
    }

    public static String getText(WebElement elem) {
    	// Search for the first number in the text, if no number return all the text
        String text = elem.getText();
        Matcher num = Pattern.compile("-?\\d+").matcher(text);
        String r = num.find() ? num.group() : text;
        return Allure.step("Get text " + functionName(elem) + ": " + r, () -> r);
    }
}
