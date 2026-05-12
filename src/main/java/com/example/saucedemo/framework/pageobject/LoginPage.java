package com.example.saucedemo.framework.pageobject;

import com.example.saucedemo.framework.data.LoginRequest;
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
    log.info("Entering username: {}", username);
    waitUtils.type(USERNAME_FIELD, username);
    return this;
  }

  @Step("Enter password")
  public LoginPage enterPassword(String password) {
    log.info("Entering password");
    waitUtils.type(PASSWORD_FIELD, password);
    return this;
  }

  @Step("Click login button")
  public void clickLoginButton() {
    log.info("Clicking login button");
    waitUtils.click(LOGIN_BUTTON);
  }

  @Step("Login to {0} with username {1}")
  public LoginPage login(String url, String username, String password) {
    return login(new LoginRequest(url, username, password));
  }

  @Step("Login using request for username {0.username}")
  public LoginPage login(LoginRequest request) {
    log.info("Starting login process for user: {} at URL: {}", request.username(), request.url());
    open(request.url());
    enterUsername(request.username());
    enterPassword(request.password());
    clickLoginButton();
    log.info("Login process completed for user: {}", request.username());
    return this;
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
