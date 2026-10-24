package extensions;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import io.qameta.allure.Allure;

public class Verifications {

    public static void verifyEquals(String actual, String expected) {
        Allure.step("Verify text: " + actual + " is equal to " + expected, () -> {
            Assert.assertEquals(actual, expected,
                    "Verify equals failed: " + actual + " is not equal to: " + expected);
        });
    }

    public static void verifyText(String actual, String expected) {
        Allure.step("Verify text: " + actual + " is in " + expected, () -> {
            Assert.assertTrue(actual.contains(expected),
                "Verify text failed: '" + actual + "', does not contain: '" + expected + "'");
        });
    }

    public static void assertFailed(String message) {
        Allure.step("Assert failed", () -> {
            Assert.fail(message);
        });
    }

    public static void isDisplayed(WebElement elem, boolean expected) {
        Allure.step("Verify element display: " + WebActions.elementName(elem), () -> {
            boolean actual = elem != null && elem.isDisplayed();
            Assert.assertEquals(actual, expected,
                    "Verify display failed for element: " + WebActions.elementName(elem));
        });
    }

    public static void verifyTextSoft(String actual, String expected) {
    	SoftAssert softAssert = new SoftAssert();
        Allure.step("Verify soft assert text: " + actual + " is in " + expected, () -> {
        	softAssert.assertTrue(actual.contains(expected),
                    "Verify text failed: '" + actual + "', does not contain: '" + expected + "'");
        	softAssert.assertAll();
        });
    }
}
