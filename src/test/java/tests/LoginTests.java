package tests;

import static org.assertj.core.api.Assertions.assertThat;

import common.config.ConfigFactory;
import common.data.Credentials;
import common.pageobject.LoginPage;
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
      groups = {"login"},
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

  private LoginPage loginPage() {
    return loginPage.get();
  }
}
