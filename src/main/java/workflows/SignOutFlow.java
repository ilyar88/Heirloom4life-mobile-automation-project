package workflows;

import extensions.*;
import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import pageObjects.*;
import utilities.WaitForElement;

public class SignOutFlow {

    private final NavigationPage navigation;
    private final AccountPage account;
    private final LoginPage login;

    public SignOutFlow(WebDriver driver) {
        navigation = new NavigationPage(driver);
        account = new AccountPage(driver);
        login = new LoginPage(driver);
    }

    public void SignOutFlow() {

		Allure.step("Sign out flow", () -> {

            WebElement accountTab = WaitForElement.waitFor(() -> navigation.navigationBar("Account"), "element displayed", 40);
            MobileActions.click(accountTab);
            MobileActions.scrollDown(() -> account.byName("Sign Out"));
            MobileActions.click(account.byName("Sign Out"));
            WebElement signOutConfirm = WaitForElement.waitFor(() -> account.byName("Sign Out"), "element displayed", 40);
            MobileActions.click(signOutConfirm);
            String title = MobileActions.getText(login.title());
            Verifications.verifyText(title, title.contains("Sam") ? "Sam" : "Heirloom");
		});
  }
}
