package io.github.prayag.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.prayag.saucedemo.app.auth.AuthService;
import io.github.prayag.saucedemo.app.data.AppConstants;
import io.github.prayag.saucedemo.app.data.AppRoute;
import io.github.prayag.saucedemo.app.data.LoginRequest;
import io.github.prayag.saucedemo.app.ui.component.HeaderComponent;
import io.github.prayag.saucedemo.app.ui.page.InventoryPage;
import io.github.prayag.saucedemo.app.ui.page.LoginPage;
import io.github.prayag.saucedemo.framework.config.ConfigFactory;
import io.github.prayag.saucedemo.framework.listener.Retryable;
import io.github.prayag.saucedemo.tests.data.LoginScenario;
import io.github.prayag.saucedemo.tests.data.LoginScenario.InvalidLoginCase;
import io.github.prayag.saucedemo.tests.data.ProductCatalog;
import io.github.prayag.saucedemo.tests.data.TestGroups;
import io.github.prayag.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Slf4j
@Epic("Sauce Demo")
@Feature("Login")
@Owner("QA Automation")
public class LoginTests extends BaseTestCase {

  @DataProvider(name = "invalidUsers")
  public Object[][] invalidUsers() {
    return new Object[][] {
      {
        new InvalidLoginCase(
            LoginScenario.emptyCredentials(), "Epic sadface: Username is required", false)
      },
      {
        new InvalidLoginCase(
            LoginScenario.missingUsername(), "Epic sadface: Username is required", false)
      },
      {
        new InvalidLoginCase(
            LoginScenario.missingPassword(), "Epic sadface: Password is required", false)
      },
      {
        new InvalidLoginCase(
            LoginScenario.lockedOutUser(),
            "Epic sadface: Sorry, this user has been locked out.",
            true)
      },
      {
        new InvalidLoginCase(
            LoginScenario.invalidUser(),
            "Epic sadface: Username and password do not match any user in this service",
            false)
      },
    };
  }

  @DataProvider(name = "protectedRoutes")
  public Object[][] protectedRoutes() {
    return new Object[][] {
      {
        AppRoute.INVENTORY,
        "Epic sadface: You can only access '/inventory.html' when you are logged in."
      },
      {AppRoute.CART, "Epic sadface: You can only access '/cart.html' when you are logged in."},
      {
        AppRoute.CHECKOUT_STEP_ONE,
        "Epic sadface: You can only access '/checkout-step-one.html' when you are logged in."
      },
    };
  }

  @Test(
      testName = "Verify login error messages",
      description =
          "Attempts invalid login scenarios and verifies the expected authentication error message for each credential set.",
      groups = {TestGroups.LOGIN},
      dataProvider = "invalidUsers",
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Invalid login handling")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyLoginShowsExpectedErrorMessage(InvalidLoginCase scenario) {
    if (scenario.requiresConfiguredPassword()) {
      assumePasswordConfigured();
    }
    LoginPage loginPage = pages().login();
    loginPage
        .open(ConfigFactory.getConfig().appUrl())
        .enterUsername(scenario.credentials().username())
        .enterPassword(scenario.credentials().password())
        .loginExpectingError();

    log.info("Verifying login error message");
    String error = loginPage.getErrorMessage();
    assertThat(error)
        .as("The login error message should match the expected scenario")
        .isEqualTo(scenario.expectedErrorMessage());
  }

  @Test(
      testName = "Verify standard user can login",
      description =
          "Logs in through the real UI form with configured standard-user credentials and verifies the inventory landing page.",
      groups = {TestGroups.LOGIN},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Valid login")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyStandardUserCanLogin() {
    var config = ConfigFactory.getConfig();
    assumePasswordConfigured();

    InventoryPage inventoryPage =
        pages()
            .login()
            .login(new LoginRequest(config.appUrl(), config.appUsername(), config.appPassword()));

    assertThat(inventoryPage.getAppLogoText())
        .as("A valid standard user login should land on the inventory page")
        .isEqualTo(AppConstants.HEADER_TITLE);
  }

  @Test(
      testName = "Verify problem user exposes known visual catalog defect",
      description =
          "Logs in as Sauce Demo's problem user and verifies the intentionally broken repeated product-image behavior is detectable.",
      groups = {TestGroups.PERSONA, TestGroups.REGRESSION},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Distinctive user personas")
  @Severity(SeverityLevel.NORMAL)
  public void verifyProblemUserVisualCatalogDefectIsDetectable() {
    var config = ConfigFactory.getConfig();
    assumePasswordConfigured();

    InventoryPage inventoryPage =
        pages()
            .login()
            .login(
                new LoginRequest(
                    config.appUrl(),
                    LoginScenario.problemUser().username(),
                    LoginScenario.problemUser().password()));

    assertThat(new HashSet<>(inventoryPage.getDisplayedProductImageSources()))
        .as("Problem user should expose Sauce Demo's repeated product-image defect")
        .hasSizeLessThan(ProductCatalog.EXPECTED_PRODUCT_COUNT);
  }

  @Test(
      testName = "Verify error user can still reach inventory",
      description =
          "Logs in as Sauce Demo's error user and verifies the framework can still establish the inventory landing page before deeper scenario coverage.",
      groups = {TestGroups.PERSONA, TestGroups.REGRESSION},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Distinctive user personas")
  @Severity(SeverityLevel.NORMAL)
  public void verifyErrorUserCanReachInventory() {
    var config = ConfigFactory.getConfig();
    assumePasswordConfigured();

    InventoryPage inventoryPage =
        pages()
            .login()
            .login(
                new LoginRequest(
                    config.appUrl(),
                    LoginScenario.errorUser().username(),
                    LoginScenario.errorUser().password()));

    assertThat(inventoryPage.getAppLogoText())
        .as("Error user should still be able to reach the inventory page")
        .isEqualTo(AppConstants.HEADER_TITLE);
  }

  @Test(
      testName = "Verify performance glitch user eventually reaches inventory",
      description =
          "Logs in as Sauce Demo's performance glitch user and verifies the framework waits long enough for the delayed inventory page.",
      groups = {TestGroups.PERSONA, TestGroups.REGRESSION},
      timeOut = TestTimeouts.SLOW_PERSONA_TIMEOUT_MS)
  @Story("Distinctive user personas")
  @Severity(SeverityLevel.NORMAL)
  @Retryable(
      reason = "Demonstrates the portfolio retry flow for an intentionally slow persona login")
  public void verifyPerformanceGlitchUserReachesInventory() {
    var config = ConfigFactory.getConfig();
    assumePasswordConfigured();

    InventoryPage inventoryPage =
        pages()
            .login()
            .login(
                new LoginRequest(
                    config.appUrl(),
                    LoginScenario.performanceGlitchUser().username(),
                    LoginScenario.performanceGlitchUser().password()));

    assertThat(inventoryPage.getAppLogoText())
        .as("Performance glitch user should eventually reach the inventory page")
        .isEqualTo(AppConstants.HEADER_TITLE);
  }

  @Test(
      testName = "Verify protected routes require authentication",
      description =
          "Attempts to open protected application routes without a session and verifies Sauce Demo redirects to login with the correct access error.",
      groups = {TestGroups.LOGIN, TestGroups.REGRESSION},
      dataProvider = "protectedRoutes",
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Protected route access")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyProtectedRoutesRequireAuthentication(
      AppRoute route, String expectedErrorMessage) {
    String protectedUrl = route.absoluteUrl(ConfigFactory.getConfig().appUrl());

    getDriver().navigate().to(protectedUrl);
    LoginPage loginPage = pages().login().waitUntilLoaded();

    assertThat(loginPage.getErrorMessage())
        .as("Anonymous users should be blocked from %s", route.path())
        .isEqualTo(expectedErrorMessage);
  }

  @Test(
      testName = "Verify user can logout",
      description =
          "Authenticates with the cookie shortcut, performs logout, and verifies the browser returns to the login page.",
      groups = {TestGroups.SMOKE, TestGroups.LOGIN},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Authenticated user logout")
  @Severity(SeverityLevel.NORMAL)
  public void verifyUserCanLogout() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();
    assertThat(inventoryPage.getAppLogoText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    log.info("Performing logout operation");
    LoginPage loginPage = header.logout();
    assertThat(loginPage.isLoginButtonVisible())
        .as("Logout should return the browser to the login page")
        .isTrue();
  }
}
