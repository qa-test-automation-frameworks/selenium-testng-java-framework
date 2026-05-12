package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.saucedemo.app.auth.AuthService;
import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.data.AppConstants;
import com.example.saucedemo.framework.data.LoginRequest;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.LoginPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import com.example.saucedemo.tests.data.LoginScenario;
import com.example.saucedemo.tests.data.LoginScenario.InvalidLoginCase;
import com.example.saucedemo.tests.data.TestGroups;
import com.example.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.net.URI;
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
        "inventory.html",
        "Epic sadface: You can only access '/inventory.html' when you are logged in."
      },
      {"cart.html", "Epic sadface: You can only access '/cart.html' when you are logged in."},
      {
        "checkout-step-one.html",
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
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
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
        .contains(scenario.expectedErrorMessage());
  }

  @Test(
      testName = "Verify standard user can login",
      description =
          "Logs in through the real UI form with configured standard-user credentials and verifies the inventory landing page.",
      groups = {TestGroups.LOGIN},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
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
      testName = "Verify protected routes require authentication",
      description =
          "Attempts to open protected application routes without a session and verifies Sauce Demo redirects to login with the correct access error.",
      groups = {TestGroups.LOGIN, TestGroups.REGRESSION},
      dataProvider = "protectedRoutes",
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Protected route access")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyProtectedRoutesRequireAuthentication(
      String route, String expectedErrorMessage) {
    String protectedUrl = URI.create(ConfigFactory.getConfig().appUrl()).resolve(route).toString();

    getDriver().navigate().to(protectedUrl);
    LoginPage loginPage = pages().login().waitUntilLoaded();

    assertThat(loginPage.getErrorMessage())
        .as("Anonymous users should be blocked from %s", route)
        .isEqualTo(expectedErrorMessage);
  }

  @Test(
      testName = "Verify user can logout",
      description =
          "Authenticates with the cookie shortcut, performs logout, and verifies the browser returns to the login page.",
      groups = {TestGroups.SMOKE, TestGroups.LOGIN},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
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
