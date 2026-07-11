package utilities;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import extensions.Verifications;
import io.qameta.allure.Allure;

public class WaitForElement {
	
	private static WebDriver _driver;
	private static int _seconds;
	
	public static void init(WebDriver driver, int seconds) {
		_driver = driver;
		_seconds = seconds;
    }
	
	public static void waitFor(WebElement elem, String forElement, int seconds) {
	    // Temporarily disable implicit wait to avoid it compounding with explicit wait
	    _driver.manage().timeouts().implicitlyWait(Duration.ZERO);

	    try {
	        WebDriverWait wait = new WebDriverWait(_driver, Duration.ofSeconds(seconds));
	        switch (For.parse(forElement)) {
	            case ELEMENT_EXISTS:
	                wait.until(d -> elem.isEnabled());
	                break;
	            case ELEMENT_DISPLAYED:
	                wait.until(ExpectedConditions.visibilityOf(elem));
	                break;
	            case ELEMENT_INVISIBLE:
	                wait.until(ExpectedConditions.invisibilityOf(elem));
	                break;
	            case ELEMENT_CLICKABLE:
	                wait.until(ExpectedConditions.elementToBeClickable(elem));
	                break;
	            default:
	                throw new IllegalArgumentException("Unsupported wait condition: " + forElement);
	        }
	    } finally { // Always restore the previously configured implicit wait
	        _driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(_seconds));
	    }
	}
	
	public static void waitForListSize(List<WebElement> elements, int size, int seconds) {
		try {
			WebDriverWait wait = new WebDriverWait(_driver, Duration.ofSeconds(seconds));
			wait.until(d -> elements.size() >= size);
		}
		catch (Exception e) {
	        Verifications.assertFailed("Expected list size >= " + size + " but found " + elements.size());
	        
		} finally {
			_driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(_seconds));
		}
	}
	
	public static void waitUntilUrlContains(String uri) {
		Allure.step("Wait for uri: " + uri, () -> {
			
			_driver.manage().timeouts().implicitlyWait(Duration.ZERO);
			try { // Wait up to 5 seconds until the browser URL contains the expected substring
			    new WebDriverWait(_driver, Duration.ofSeconds(5)).until(ExpectedConditions.urlContains(uri));
			} catch (Exception e) { // If timeout or failure: get the current actual URL
			    String actual = _driver.getCurrentUrl();
			    Verifications.verifyText(actual, uri);
			} finally {
				_driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(_seconds));
			}
		});
	}

	
	public static WebElement delayWait(WebElement element, int delaySeconds, int timeoutSeconds) {
	    try {
	    	// Temporarily disable implicit wait to avoid it compounding with explicit wait
	        _driver.manage().timeouts().implicitlyWait(Duration.ZERO);

	        // Build a FluentWait on the driver:
	        // - total timeout = delay + real timeout
	        // - poll every 1 second
	        FluentWait<WebDriver> wait = new FluentWait<>(_driver)
	            .withTimeout(Duration.ofSeconds(delaySeconds + timeoutSeconds))
	            .pollingEvery(Duration.ofSeconds(1));

	        long startTime = System.currentTimeMillis();

	        // Repeatedly evaluate this function until it returns a non-null WebElement
	        return wait.until(d -> {
	            // Calculate how many seconds have elapsed since we started waiting
	            long elapsed = (System.currentTimeMillis() - startTime) / 1000;

	            if (elapsed < delaySeconds) {
	                return null; // If still in "delay" phase return null
	            }

	            // After delay passes: return the element if it’s visible; otherwise keep polling
	            return element.isDisplayed() ? element : null;
	        });

	    } finally {
	        // Restore the original implicit wait for the driver
	        _driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(_seconds));
	    }
	}
	//Wait for specific time
	public static void waitForTime(String timeText) {
	    String[] parts = timeText.split(" ");
	    int value = Integer.parseInt(parts[0]);
	    String unit = parts[1];

	    long millis;
	    switch (unit) {
	        case "Seconds":
	            millis = value * 1000L;
	            break;
	        case "Minutes":
	            millis = value * 60_000L;
	            break;
	        case "Hours":
	            millis = value * 3_600_000L;
	            break;
	        default:
	            throw new IllegalArgumentException("Invalid unit: " + unit);
	    }

	    try {
	        Thread.sleep(millis);
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	    }
	}
	
	enum For {
	    ELEMENT_EXISTS, ELEMENT_DISPLAYED, ELEMENT_CLICKABLE, ELEMENT_INVISIBLE;

	    static For parse(String s) {
	        try {
	            return For.valueOf(
	                s.trim().replace(' ', '_').replace('-', '_').toUpperCase(java.util.Locale.ROOT)
	            );
	        } catch (Exception e) {
	            return ELEMENT_EXISTS; // default
	        }
	    }
	}
}