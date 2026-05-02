package common.pageobject;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class LoginPage extends BasePage {

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  private final By usernameField = By.id("user-name");
  private final By passwordField = By.id("password");
  private final By loginButton = By.id("login-button");

  @Step("Enter username: {0}")
  public void enterUsername(String username) {
    log.info("Entering username: {}", username);
    assertThat(driver.findElement(usernameField).isDisplayed())
        .as("Username field should be displayed")
        .isTrue();
    driver.findElement(usernameField).sendKeys(username);
  }

  @Step("Enter password")
  public void enterPassword(String password) {
    log.info("Entering password");
    assertThat(driver.findElement(passwordField).isDisplayed())
        .as("Password field should be displayed")
        .isTrue();
    driver.findElement(passwordField).sendKeys(password);
  }

  @Step("Click login button")
  public void clickLoginButton() {
    log.info("Clicking login button");
    assertThat(driver.findElement(loginButton).isEnabled())
        .as("Login button should be enabled")
        .isTrue();
    driver.findElement(loginButton).click();
  }

  @Step("Login to {0} with username {1}")
  public void login(String url, String username, String password) {
    log.info("Starting login process for user: {} at URL: {}", username, url);
    navigateTo(url);
    enterUsername(username);
    enterPassword(password);
    clickLoginButton();
    waitUtils.waitForPageLoad();
    log.info("Login process completed for user: {}", username);
  }

  @Step("Logout from application")
  public void logout() {
    log.info("Starting logout process");
    waitUtils.waitForPageLoad();
    common.pageobject.component.HeaderComponent header = new common.pageobject.component.HeaderComponent(driver);
    header.openMenu();
    header.clickLogoutButton();
    log.info("Logout process completed");
  }
}
