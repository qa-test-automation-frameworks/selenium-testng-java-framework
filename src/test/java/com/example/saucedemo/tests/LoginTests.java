package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.data.AppConstants;
import com.example.saucedemo.framework.data.LoginRequest;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.LoginPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import com.example.saucedemo.framework.util.AuthService;
import com.example.saucedemo.tests.data.Credentials;
import com.example.saucedemo.tests.data.LoginScenario;
import com.example.saucedemo.tests.data.TestGroups;
import com.example.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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
      {LoginScenario.lockedOutUser(), "Epic sadface: Sorry, this user has been locked out."},
      {
        LoginScenario.invalidUser(),
        "Epic sadface: Username and password do not match any user in this service"
      }
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
  public void verifyLoginShowsExpectedErrorMessage(
      Credentials credentials, String expectedErrorMessage) {
    ConfigFactory.requireLoginPassword(ConfigFactory.getConfig());
    LoginPage loginPage = new LoginPage(getDriver());
    loginPage.login(
        new LoginRequest(
            ConfigFactory.getConfig().appUrl(), credentials.username(), credentials.password()));

    log.info("Verifying login error message");
    String error = loginPage.getErrorMessage();
    assertThat(error)
        .as("The login error message should match the expected scenario")
        .contains(expectedErrorMessage);
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
    InventoryPage inventoryPage = new InventoryPage(getDriver()).waitUntilLoaded();
    HeaderComponent header = new HeaderComponent(getDriver());
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    log.info("Performing logout operation");
    header.logout();
    LoginPage loginPage = new LoginPage(getDriver()).waitUntilLoaded();
    assertThat(loginPage.isLoginButtonVisible())
        .as("Logout should return the browser to the login page")
        .isTrue();
  }
}
