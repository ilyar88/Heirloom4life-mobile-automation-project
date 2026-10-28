package workflows;

import extensions.*;
import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import pageObjects.*;
import utilities.WaitForElement;

public class LoginFlow {

    private final LoginPage login;
    private final StoriesPage stories;

    public LoginFlow(WebDriver driver) {
        login = new LoginPage(driver);
        stories = new StoriesPage(driver);
    }

    public void userLogin(String email) {

		Allure.step("User login flow", () -> {

			// Cold app launch on device can take longer than the default 20s wait
			WebElement googleLink = WaitForElement.waitFor(() -> login.googleLink(), "element displayed", 40);
			MobileActions.click(googleLink);
			WebElement account = WaitForElement.waitFor(() -> login.chooseAccount(email), "element displayed", 40);
			MobileActions.click(account);
			WebElement title = WaitForElement.waitFor(() -> stories.title(), "element displayed", 40);
			Verifications.verifyText(MobileActions.getText(title), "Stories");
		});
    }
}
