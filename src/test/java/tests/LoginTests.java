package tests;

import static org.assertj.core.api.Assertions.assertThat;

import common.config.ConfigFactory;
import common.pageobject.LoginPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
public class LoginTests extends BaseTestCase {

  private LoginPage loginPage;

  @BeforeMethod(alwaysRun = true)
  public void setupPages() {
    log.info("Initializing page objects for LoginTests");
    loginPage = new LoginPage(getDriver());
  }

  @Test(testName = "Verify error message for locked out user")
  public void testLockedOutUser() {
    log.info("Starting test: testLockedOutUser");
    loginPage.login(
        ConfigFactory.getConfig().appUrl(),
        "locked_out_user",
        ConfigFactory.getConfig().appPassword());

    log.info("Verifying error message for locked out user");
    String error = getDriver().findElement(By.cssSelector("[data-test='error']")).getText();
    assertThat(error)
        .as("The login error message for a locked out user should be displayed")
        .contains("Epic sadface: Sorry, this user has been locked out.");
    log.info("Finished test successfully: testLockedOutUser");
  }

  @Test(testName = "Verify error message for invalid credentials")
  public void testInvalidLogin() {
    log.info("Starting test: testInvalidLogin");
    loginPage.login(ConfigFactory.getConfig().appUrl(), "invalid_user", "invalid_password");

    log.info("Verifying error message for invalid credentials");
    String error = getDriver().findElement(By.cssSelector("[data-test='error']")).getText();
    assertThat(error)
        .as("The login error message for invalid credentials should be displayed")
        .contains("Epic sadface: Username and password do not match any user in this service");
    log.info("Finished test successfully: testInvalidLogin");
  }
}
