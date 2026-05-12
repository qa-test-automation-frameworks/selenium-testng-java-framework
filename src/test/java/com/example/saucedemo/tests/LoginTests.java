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
import io.qameta.allure.Description;
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
  @Description(
      "Attempts invalid login scenarios and verifies the expected authentication error message for each credential set.")
  @Story("Invalid login handling")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyLoginShowsExpectedErrorMessage(InvalidLoginCase scenario) {
    if (scenario.requiresConfiguredPassword()) {
      ConfigFactory.requireLoginPassword(ConfigFactory.getConfig());
    }
    LoginPage loginPage = pages().login();
    loginPage.login(
        new LoginRequest(
            ConfigFactory.getConfig().appUrl(),
            scenario.credentials().username(),
            scenario.credentials().password()));

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
  @Description(
      "Logs in through the real UI form with configured standard-user credentials and verifies the inventory landing page.")
  @Story("Valid login")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyStandardUserCanLogin() {
    var config = ConfigFactory.getConfig();
    ConfigFactory.requireLoginPassword(config);

    pages()
        .login()
        .login(new LoginRequest(config.appUrl(), config.appUsername(), config.appPassword()));

    assertThat(pages().inventory().waitUntilLoaded().getHeaderText())
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
  @Description(
      "Attempts to open protected application routes without a session and verifies Sauce Demo redirects to login with the correct access error.")
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
  @Description(
      "Authenticates with the cookie shortcut, performs logout, and verifies the browser returns to the login page.")
  @Story("Authenticated user logout")
  @Severity(SeverityLevel.NORMAL)
  public void verifyUserCanLogout() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    log.info("Performing logout operation");
    LoginPage loginPage = header.logout();
    assertThat(loginPage.isLoginButtonVisible())
        .as("Logout should return the browser to the login page")
        .isTrue();
  }
}
