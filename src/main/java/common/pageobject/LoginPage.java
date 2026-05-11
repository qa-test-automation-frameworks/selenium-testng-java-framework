package common.pageobject;

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
  public LoginPage enterUsername(String username) {
    log.info("Entering username: {}", username);
    waitUtils.type(usernameField, username);
    return this;
  }

  @Step("Enter password")
  public LoginPage enterPassword(String password) {
    log.info("Entering password");
    waitUtils.type(passwordField, password);
    return this;
  }

  @Step("Click login button")
  public LoginPage clickLoginButton() {
    log.info("Clicking login button");
    waitUtils.click(loginButton);
    return this;
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

  @Step("Get login error message")
  public String getErrorMessage() {
    log.info("Retrieving login error message");
    return waitUtils.waitUntilVisible(By.cssSelector("[data-test='error']")).getText();
  }

  @Step("Check login button visibility")
  public boolean isLoginButtonVisible() {
    boolean visible =
        !driver.findElements(loginButton).isEmpty()
            && driver.findElement(loginButton).isDisplayed();
    log.debug("Login button visibility: {}", visible);
    return visible;
  }
}
