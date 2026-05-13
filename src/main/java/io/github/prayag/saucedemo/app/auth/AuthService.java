package io.github.prayag.saucedemo.app.auth;

import java.util.Objects;
import org.openqa.selenium.WebDriver;

/** Coordinates application authentication setup for tests that do not exercise the login UI. */
public final class AuthService {

  private static final AuthService DEFAULT = new AuthService(new SauceDemoCookieAuthentication());

  private final AuthenticationStrategy authenticationStrategy;

  public AuthService(AuthenticationStrategy authenticationStrategy) {
    this.authenticationStrategy = Objects.requireNonNull(authenticationStrategy);
  }

  public static void injectLoginCookieAndNavigate(WebDriver driver) {
    DEFAULT.authenticate(driver);
  }

  public void authenticate(WebDriver driver) {
    authenticationStrategy.authenticate(driver);
  }
}
