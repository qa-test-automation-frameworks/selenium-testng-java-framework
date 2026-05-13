package io.github.prayag.saucedemo.app.ui.page;

import io.github.prayag.saucedemo.app.data.LoginRequest;
import io.github.prayag.saucedemo.framework.ui.BasePage;
import io.github.prayag.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class LoginPage extends BasePage implements PageLoadable<LoginPage> {

  private static final By USERNAME_FIELD = By.id("user-name");
  private static final By PASSWORD_FIELD = By.id("password");
  private static final By LOGIN_BUTTON = By.id("login-button");
  private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  @Override
  public LoginPage waitUntilLoaded() {
    waitUtils.waitUntilVisible(LOGIN_BUTTON);
    return this;
  }

  @Step("Open login page at {0}")
  public LoginPage open(String url) {
    navigateTo(url);
    return waitUntilLoaded();
  }

  @Step("Enter username: {0}")
  public LoginPage enterUsername(String username) {
    log.info("Entering username");
    waitUtils.type(USERNAME_FIELD, username);
    return this;
  }

  @Step("Enter password")
  public LoginPage enterPassword(String password) {
    log.info("Entering password");
    waitUtils.type(PASSWORD_FIELD, password);
    return this;
  }

  @Step("Submit login and expect success")
  public InventoryPage loginExpectingSuccess() {
    log.info("Clicking login button and expecting inventory page");
    waitUtils.click(LOGIN_BUTTON);
    return new InventoryPage(driver).waitUntilLoaded();
  }

  @Step("Submit login and expect error")
  public LoginPage loginExpectingError() {
    log.info("Clicking login button and expecting login error");
    waitUtils.click(LOGIN_BUTTON);
    waitUtils.waitUntilVisible(ERROR_MESSAGE);
    return this;
  }

  /**
   * Opens the login page, submits credentials, and waits for a successful inventory landing page.
   * Call {@link #open(String)} only for standalone navigation or assertions before submitting the
   * form.
   */
  @Step("Login to {0} with username {1}")
  public InventoryPage login(String url, String username, String password) {
    return login(new LoginRequest(url, username, password));
  }

  /**
   * Opens the login page, submits credentials, and waits for a successful inventory landing page.
   * Use {@link #loginExpectingError()} after manually entering credentials for negative scenarios.
   */
  @Step("Login using request for username {0.username}")
  public InventoryPage login(LoginRequest request) {
    log.info("Starting login process at URL: {}", request.url());
    open(request.url());
    enterUsername(request.username());
    enterPassword(request.password());
    InventoryPage inventoryPage = loginExpectingSuccess();
    log.info("Login process completed");
    return inventoryPage;
  }

  @Step("Get login error message")
  public String getErrorMessage() {
    log.info("Retrieving login error message");
    return waitUtils.waitUntilVisible(ERROR_MESSAGE).getText();
  }

  public boolean isLoginButtonVisible() {
    boolean visible = waitUtils.isVisible(LOGIN_BUTTON);
    log.debug("Login button visibility: {}", visible);
    return visible;
  }
}
