package tests;

import static org.assertj.core.api.Assertions.assertThat;

import common.config.ConfigFactory;
import common.data.AppConstants;
import common.data.Credentials;
import common.data.TestGroups;
import common.pageobject.InventoryPage;
import common.pageobject.LoginPage;
import common.pageobject.component.HeaderComponent;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Slf4j
public class LoginTests extends BaseTestCase {

  private final ThreadLocal<LoginPage> loginPage = new ThreadLocal<>();

  @BeforeMethod(alwaysRun = true)
  public void setupPages() {
    log.info("Initializing page objects for LoginTests");
    loginPage.set(new LoginPage(getDriver()));
  }

  // parallel = true allows data provider rows to execute concurrently;
  // ThreadLocal WebDriver ensures each row gets its own browser.
  @DataProvider(name = "invalidUsers", parallel = true)
  public Object[][] invalidUsers() {
    return new Object[][] {
      {
        new Credentials("locked_out_user", ConfigFactory.getConfig().appPassword()),
        "Epic sadface: Sorry, this user has been locked out."
      },
      {
        new Credentials("invalid_user", "invalid_password"),
        "Epic sadface: Username and password do not match any user in this service"
      }
    };
  }

  @Test(
      testName = "Verify login error messages",
      groups = {TestGroups.LOGIN},
      dataProvider = "invalidUsers")
  public void verifyLoginShowsExpectedErrorMessage(
      Credentials credentials, String expectedErrorMessage) {
    log.info("Starting test: verifyLoginShowsExpectedErrorMessage for {}", credentials.username());
    loginPage()
        .login(ConfigFactory.getConfig().appUrl(), credentials.username(), credentials.password());

    log.info("Verifying login error message");
    String error = loginPage().getErrorMessage();
    assertThat(error)
        .as("The login error message should match the expected scenario")
        .contains(expectedErrorMessage);
    log.info("Finished test successfully: verifyLoginShowsExpectedErrorMessage");
  }

  @Test(
      testName = "Verify user can logout",
      groups = {TestGroups.SMOKE, TestGroups.LOGIN})
  public void verifyUserCanLogout() {
    log.info("Starting test: verifyUserCanLogout");
    util.AuthService.injectLoginCookieAndNavigate(getDriver());
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    log.info("Performing logout operation");
    header.logout();
    assertThat(loginPage().isLoginButtonVisible())
        .as("Logout should return the browser to the login page")
        .isTrue();
    log.info("Finished test successfully: verifyUserCanLogout");
  }

  private LoginPage loginPage() {
    return loginPage.get();
  }
}
