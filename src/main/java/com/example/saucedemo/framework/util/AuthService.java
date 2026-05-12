package com.example.saucedemo.framework.util;

import org.openqa.selenium.WebDriver;

public final class AuthService {

  private static final AuthenticationStrategy DEFAULT_AUTHENTICATION_STRATEGY =
      new SauceDemoCookieAuthentication();

  private AuthService() {}

  public static void injectLoginCookieAndNavigate(WebDriver driver) {
    DEFAULT_AUTHENTICATION_STRATEGY.authenticate(driver);
  }
}
