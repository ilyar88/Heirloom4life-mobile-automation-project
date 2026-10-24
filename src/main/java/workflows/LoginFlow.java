package workflows;

import base.BasePage;
import extensions.MobileActions;
import extensions.Verifications;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebElement;
import pageObjects.LoginPage;
import pageObjects.StoriesPage;
import utilities.WaitForElement;

public class LoginFlow {

    public static void userLogin(String email) {

		Allure.step("User login flow", () -> {

			LoginPage login = new LoginPage(BasePage.driver);
			StoriesPage stories = new StoriesPage(BasePage.driver);
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
