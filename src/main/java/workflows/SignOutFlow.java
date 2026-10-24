package workflows;

import base.BasePage;
import extensions.MobileActions;
import extensions.Verifications;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebElement;
import pageObjects.*;
import utilities.WaitForElement;

public class SignOutFlow {

    public static void SignOutFlow() {

		Allure.step("Sign out flow", () -> {

            NavigationPage navigation = new NavigationPage(BasePage.driver);
            AccountPage account = new AccountPage(BasePage.driver);
            LoginPage login = new LoginPage(BasePage.driver);
            WebElement accountTab = WaitForElement.waitFor(() -> navigation.navigationBar("Account"), "element displayed", 40);
            MobileActions.click(accountTab);
            MobileActions.scrollDown(() -> account.signOut());
            MobileActions.click(account.signOut());
            WebElement signOutConfirm = WaitForElement.waitFor(() -> account.signOutButton(), "element displayed", 40);
            MobileActions.click(signOutConfirm);
            String title = MobileActions.getText(login.title());
            Verifications.verifyText(title, title.contains("Sam") ? "Sam" : "Heirloom");
		});
  }
}
