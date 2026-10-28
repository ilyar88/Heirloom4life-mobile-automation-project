package com.heirloom4life;

import java.io.IOException;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;
import io.qameta.allure.*;
import workflows.*;
import base.BasePage;
import extensions.WebActions;
import utilities.TestData;

public class SanityTest extends BasePage {

	private WebDriver driver;
	private LoginFlow loginFlow;
	private SignOutFlow signOutFlow;

	public SanityTest() throws IOException {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void initDriver() {
		driver = getDriver();
		setup(driver);
	}

	@AfterMethod(alwaysRun = true)
	public void screenshotOnFailure(ITestResult result) throws IOException {
		if (result.getStatus() == ITestResult.FAILURE) takeSnapShot(driver, result.getName());
	}

	@AfterClass(alwaysRun = true)
	public void stopDriver() {
		teardown(driver);
	}

	@Feature("Login")
	@Severity(SeverityLevel.CRITICAL)
	@Description("Login to the app with email account.")
	@Test(priority = 1, dataProvider = "globalProvider", dataProviderClass = TestData.class)
	public void userLoginTest(String accountName) {
		WebActions.maskTestParameters();
		loginFlow = new LoginFlow(driver);
		loginFlow.userLogin(accountName);
	}
	@Feature("Sign Out")
	@Severity(SeverityLevel.CRITICAL)
	@Description("Sign out of the app.")
	@Test(priority = 2)
	public void userSignOutTest() {
		signOutFlow = new SignOutFlow(driver);
		signOutFlow.SignOutFlow();
	}
}
