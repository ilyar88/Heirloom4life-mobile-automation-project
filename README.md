# MyFinancialPlan - E2E test automation

End-to-end test automation suite for the MyFinancialPlan web application, built with **Selenium**, **TestNG**, **Allure** reporting, run via **GitHub Actions**.

## About

MyFinancePlan is a SaaS website for creating and tracking personal financial plans. It allows users to build and manage their own plans by tracking income and expenses, view other users’ plans (if sharing access is enabled), access median financial data based on their status for comparison, and receive actionable steps alerts to help implement and monitor plans according to their path to the goals and dreams.

---

## Project structure

```
MyFinancialPlan-project/
├── src/main/java/
│   ├── base/
│   │   └── BasePage.java            # Browser lifecycle: loads config, creates WebDriver (chrome/firefox/edge), navigation & teardown
│   ├── co/il/testing/myfinancialplan/
│   │   ├── SanityTest.java          # Main sanity E2E suite
│   │   ├── ValidationTest.java      # Data-driven validation suite
│   │   └── LoadTest.java            # Load test entry point (delegates to LoadTestFlow)
│   ├── configuration/
│   │   └── config.properties        # URL, browser, credentials, wait time (env-var driven)
│   ├── extensions/                  # Reusable test utilities
│   │   ├── UiActions.java           # UI interactions (click, type, select, upload, etc.)
│   │   └── Verifications.java       # Assertion helpers with Allure steps
│   ├── pageObjects/                 # Page Object Model (POM), one class per page (Login, Register, MyPlan, etc.)
│   ├── workflows/                   # Multi-step user flows composed from page objects
│   ├── scripts/
│   │   └── summarize-test.sh        # Parses the Surefire XML report and prints pass/fail/skip counts in CI
│   └── utilities/                   # Wait conditions, data providers, page registry & failure listener
│       ├── WaitForElement.java      # Wait conditions & synchronization
│       ├── ManagePages.java         # Initializes and exposes all page objects
│       ├── Listeners.java           # Screenshot on failure & test lifecycle logging
│       ├── DataUtil.java            # Excel parser utility
│       ├── TestData.java            # TestNG @DataProvider backed by TestData.xlsx
│       └── K6ReportHelper.java      # Runs the k6 container and parses its results
├── src/test/resources/
│   ├── TestData.xlsx                # Data-driven test data
│   └── load-test.js                 # k6 load test script: virtual users, duration & status-code check
├── Suites/                          # TestNG suite XML files
│   ├── Sanity.xml
│   ├── Validation.xml
│   └── Load.xml
├── .github/
│   ├── actions/
│   │   └── action.yml               # Composite action: runs summarize-test.sh and prints the suite's test summary
│   └── workflows/
│       └── E2E test.yml             # GitHub Actions CI: Sanity → Validation → Load, per-browser dispatch
├── pom.xml                          # Maven config: dependencies, Surefire suite selection, Allure report plugin
└── architecture.svg                 # Project architecture diagram
```

---

## Architecture

![Architecture Diagram](architecture.svg)

| Layer | Purpose |
|-------|---------|
| **Test classes** (`co.il.testing.myfinancialplan`) | TestNG test specs that define test cases (Sanity, Validation, Load) |
| **Workflows** | Multi-step user flows (login, register, add incomes/expenses, choose path, etc.) |
| **Page Objects** | Encapsulate UI element locators per page |
| **Extensions** | Reusable utilities: UI actions and Allure-backed verifications |
| **Utilities** | Wait conditions, Excel-driven data provider, page registry, failure listener, k6 report helper |
| **Base** | `BasePage` loads config, launches/tears down the WebDriver (Chrome/Firefox/Edge) and takes failure screenshots |

## Project Overview

| Features                      | Description                    |
|-------------------------------------|--------------------------|
| Login page                          | Includes the following:<br>1. Link to create an account<br>2. Email address<br>3. Password <br>4. Login button<br>5. Quick connect button<br>6. Forgot password link |
| Registration page                   | Includes the following:<br>1. Login link<br>2. Email address<br>3. Password<br>4. Password verification<br>5. Account creation button<br>6. Quick registration button |
| OTP verification                    | Include email Verification |
| Profile page                        | Includes the following:<br>1. Email Address<br>2. Submit Request Button<br>3. Return to Login Page Link                       |
| Layout                              | Includes the following:<br>1. Financial plan<br>2. Monthly goal<br>3.My financial plan<br>4. Summary of actions<br>5. Home page<br>6. Other people's plans<br>7. Profile page |
| The financial planning process      | Includes the following:<br>1. Expenses and incomes<br>2. Financial goals<br>3. Multi-year recurring expenses<br>4. Emergency fund                           |
| Purchase offer page                 | Includes the following:<br>1. Basic service path<br>2. Standard service path<br>3. Prime service path                          |
| Clearing page                       | Includes the following:<br>1. Choosing a provider<br>2. Payment details<br>3. Purchase button redirects to the home page                                 |
| View/Edit Financial Plan Page       | Includes the following:<br>1. expenses and incomes<br>2. Financial goals<br>3. Emergency  Fund                                  |
| Profile page                        | Includes the following:<br>1. First name<br>2. Last name<br>3. Email<br>4. Cell phone<br>5. Age<br>6. Marital status<br>7. Number of children                 |

---

## Setup

### Prerequisites
- Java 17
- Maven
- Chrome/Firefox/Edge browser

### Install
```bash
mvn clean install -DskipTests
```

---

## Running tests

```bash
# Sanity suite on Chrome
mvn clean -Dbrowser=chrome -DsuiteXmlFiles=Suites/Sanity.xml test allure:report

# Validation suite
mvn clean -Dbrowser=chrome -DsuiteXmlFiles=Suites/Validation.xml test allure:report

# Load suite
mvn clean -Dbrowser=chrome -DsuiteXmlFiles=Suites/Load.xml test allure:report
```

`browser` accepts `chrome`, `firefox` or `edge`. The Allure report is generated under `target/site/allure-maven-plugin`.

## Sanity test

| Test cases                           | Description              |
|--------------------------------------|--------------------------|
| New User register to the website     | Register to the website with username and password. |
| Login to Homepage                    | Login to the website with username and password. |
| Create a profile                  | Create profile with the following details:<br>1. FirstName<br>2. LastName<br>3. Age<br>4. Marital status<br>5. Gender<br>6. Single breadwinner<br>7. Residential area, etc. |
| Introduction page                    | Verify expanding of 3 goals in the page. |
| Add several incomes                  | Add incomes with amonut like:<br>1. First job<br>2. Second job<br>3. Allowance |
| Add several expenses                 | Add expenses with amount like:<br>1. Apartment<br>2. Internet and media<br>3. Car |
| Congratulation page                  | Choose financial path Prime service |
| Order summary page                   | Fill in the following details and redirect to  the Payment page:<br>1. Full name<br>2. Email address                          |
| Credit checkout page                 | Fill in the payment information  |
| Join success page                    | Enable "I want to watch other people's financial plans." checkbox and redirect to the Home page                                   |
| Home page                            | Navigate to the following links:<br>1. Home page<br>2. Profile<br>3. Path to goals and dreams<br>4. My plan<br>5. Other people’s plans<br>6. Report a problem<br>7. Suggestions for improvement<br>8. Contact us |
| Notification icon                    | Click on the notifications icon and verify that each update you made appears in the notifications list.                    |
| Actions (Deposit and withdrawal)     | Verify deposit and withdrawal functionality via "Path to goals and dreams" link. |                           

## Validation test

Test data is driven from `src/test/resources/TestData.xlsx`, parsed by `DataUtil` and served via the `globalProvider` `@DataProvider` in `TestData.java`.

| Test cases                           | Description              |
|--------------------------------------|--------------------------|
| Reset password page                  | Execute reset password with different data |
| Login page                           | Execute login page with different data  |
| OTP page                             | Execute OTP with different data |
| My plan page                         | Update incomes or expenses in my plan page with different data |

## Load test

Load testing runs a Grafana k6 container via `K6ReportHelper` against `src/test/resources/load-test.js`.

| Test cases                           | Description              |
|--------------------------------------|--------------------------|
| Load test                            | Execute a load test with 10 virtual users and verify that the requests return a 200 status code                                   |

---

## Reporting

### Allure report
- Every UI action is logged as an **Allure step**
- Tests are tagged with **Allure severity** and **description**
- **Screenshots** are automatically captured and attached on failure (via `Listeners`)

---

## CI/CD - GitHub Actions

The workflow (`.github/workflows/E2E test.yml`) runs via **manual dispatch** with browser selection:

**Trigger:** GitHub Actions > Run workflow > Select browser (chrome/edge/firefox)

**Pipeline:**
1. **Sanity** — checkout, setup Java 17, run `Suites/Sanity.xml`, generate Allure report, print test summary, upload report artifact
2. **Validation** — same steps, runs `Suites/Validation.xml` after Sanity passes
3. **Load** — sets up Docker, runs `Suites/Load.xml` (k6 load test) after Validation passes, uploads the k6 HTML report

Each job's pass/fail/skip counts are printed via `.github/actions/action.yml`, which runs `src/main/java/scripts/summarize-test.sh` against the Surefire XML report.

---

## Infrastructure Highlights

- **Page Object Design Pattern**
- **Project Layers** (Extensions/Work Flows/Test Cases/TDD...)
- **Support of different clients/browsers**
- **Failure Mechanism**
- **Common Functionality**
- **External Files Support**
- **Reporting System** (include screenshots)
- **CI Support**

## Tech stack

| Tool | Purpose |
|------|---------|
| [Selenium](https://www.selenium.dev/) | Browser automation |
| [TestNG](https://testng.org/) | Test runner & data providers |
| [Allure](https://allurereport.org/) | Test reporting |
| [Apache POI](https://poi.apache.org/) | Excel data parsing |
| [WebDriverManager](https://bonigarcia.dev/webdrivermanager/) | Automatic browser driver management |
| [Testcontainers k6 module](https://testcontainers.com/) | Runs Grafana k6 load tests in a container |
| [Angus Mail](https://eclipse-ee4j.github.io/angus-mail/) | Reads OTP emails via IMAP |
| [Maven](https://maven.apache.org/) | Build tool |
| [GitHub Actions](https://github.com/features/actions) | CI/CD pipeline |
