package com.heirloom4life;

import java.io.IOException;
import org.testng.annotations.Test;
import io.qameta.allure.*;
import workflows.LoginFlow;
import workflows.SignOutFlow;
import base.BasePage;
import utilities.TestData;

public class SanityTest extends BasePage {

	public SanityTest() throws IOException {
		super();
	}
	@Feature("Login")
	@Severity(SeverityLevel.CRITICAL)
	@Description("Login to the app with email account.")
	@Test(priority = 1, dataProvider = "globalProvider", dataProviderClass = TestData.class)
	public void userLoginTest(String accountName) {
		LoginFlow.userLogin(accountName);
	}
	@Feature("Sign Out")
	@Severity(SeverityLevel.CRITICAL)
	@Description("Sign out of the app.")
	@Test(priority = 2)
	public void userSignOutTest() {
		SignOutFlow.SignOutFlow();
	}
}
