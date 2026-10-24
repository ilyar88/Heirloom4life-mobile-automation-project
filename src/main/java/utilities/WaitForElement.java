package utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.openqa.selenium.By;
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
	    _driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);

	    try {
	        WebDriverWait wait = new WebDriverWait(_driver, seconds);
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
	        _driver.manage().timeouts().implicitlyWait(_seconds, TimeUnit.SECONDS);
	    }
	}

	// Same as waitFor(WebElement, ...) but re-locates by the locator on every poll, so it also
	// works for an element that doesn't exist in the DOM yet (not just one that exists but isn't
	// visible/clickable yet).
	public static void waitFor(By locator, String forElement, int seconds) {
	    _driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);

	    try {
	        WebDriverWait wait = new WebDriverWait(_driver, seconds);
	        switch (For.parse(forElement)) {
	            case ELEMENT_EXISTS:
	                wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	                break;
	            case ELEMENT_DISPLAYED:
	                wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	                break;
	            case ELEMENT_INVISIBLE:
	                wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
	                break;
	            case ELEMENT_CLICKABLE:
	                wait.until(ExpectedConditions.elementToBeClickable(locator));
	                break;
	            default:
	                throw new IllegalArgumentException("Unsupported wait condition: " + forElement);
	        }
	    } finally {
	        _driver.manage().timeouts().implicitlyWait(_seconds, TimeUnit.SECONDS);
	    }
	}

	// Same as waitFor(WebElement, ...) but takes a Supplier (e.g. () -> login.googleLink()) so the
	// element lookup itself is deferred and retried on every poll, instead of being resolved once
	// eagerly before the wait even starts. Returns the resolved element so callers don't have to
	// look it up a second time (e.g. for the click that follows).
	public static WebElement waitFor(Supplier<WebElement> elementSupplier, String forElement, int seconds) {
	    _driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);

	    try {
	        WebDriverWait wait = new WebDriverWait(_driver, seconds);
	        switch (For.parse(forElement)) {
	            case ELEMENT_EXISTS:
	            case ELEMENT_CLICKABLE:
	                return wait.until(d -> {
	                    WebElement e = elementSupplier.get();
	                    return e.isEnabled() ? e : null;
	                });
	            case ELEMENT_DISPLAYED:
	                return wait.until(d -> {
	                    WebElement e = elementSupplier.get();
	                    return e.isDisplayed() ? e : null;
	                });
	            case ELEMENT_INVISIBLE:
	                wait.until(d -> {
	                    try {
	                        return !elementSupplier.get().isDisplayed();
	                    } catch (org.openqa.selenium.NoSuchElementException ex) {
	                        return true;
	                    }
	                });
	                return null;
	            default:
	                throw new IllegalArgumentException("Unsupported wait condition: " + forElement);
	        }
	    } finally {
	        _driver.manage().timeouts().implicitlyWait(_seconds, TimeUnit.SECONDS);
	    }
	}

	public static void waitForListSize(List<WebElement> elements, int size, int seconds) {
		try {
			WebDriverWait wait = new WebDriverWait(_driver, seconds);
			wait.until(d -> elements.size() >= size);
		}
		catch (Exception e) {
	        Verifications.assertFailed("Expected list size >= " + size + " but found " + elements.size());

		} finally {
			_driver.manage().timeouts().implicitlyWait(_seconds, TimeUnit.SECONDS);
		}
	}

	public static void waitUntilUrlContains(String uri) {
		Allure.step("Wait for uri: " + uri, () -> {

			_driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
			try { // Wait up to 5 seconds until the browser URL contains the expected substring
			    new WebDriverWait(_driver, 5).until(ExpectedConditions.urlContains(uri));
			} catch (Exception e) { // If timeout or failure: get the current actual URL
			    String actual = _driver.getCurrentUrl();
			    Verifications.verifyText(actual, uri);
			} finally {
				_driver.manage().timeouts().implicitlyWait(_seconds, TimeUnit.SECONDS);
			}
		});
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