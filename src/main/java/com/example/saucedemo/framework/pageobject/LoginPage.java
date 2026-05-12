package com.example.saucedemo.framework.pageobject;

import com.example.saucedemo.framework.data.LoginRequest;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class LoginPage extends BasePage implements PageLoadable<LoginPage> {

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  private final By usernameField = By.id("user-name");
  private final By passwordField = By.id("password");
  private final By loginButton = By.id("login-button");

  @Override
  public LoginPage waitUntilLoaded() {
    waitUtils.waitUntilVisible(loginButton);
    return this;
  }

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
  public void clickLoginButton() {
    log.info("Clicking login button");
    waitUtils.click(loginButton);
  }

  @Step("Login to {0} with username {1}")
  public void login(String url, String username, String password) {
    login(new LoginRequest(url, username, password));
  }

  @Step("Login using request for username {0.username}")
  public void login(LoginRequest request) {
    log.info("Starting login process for user: {} at URL: {}", request.username(), request.url());
    navigateTo(request.url());
    waitUntilLoaded();
    enterUsername(request.username());
    enterPassword(request.password());
    clickLoginButton();
    log.info("Login process completed for user: {}", request.username());
  }

  @Step("Get login error message")
  public String getErrorMessage() {
    log.info("Retrieving login error message");
    return waitUtils.waitUntilVisible(By.cssSelector("[data-test='error']")).getText();
  }

  public boolean isLoginButtonVisible() {
    boolean visible = waitUtils.isVisible(loginButton);
    log.debug("Login button visibility: {}", visible);
    return visible;
  }
}
