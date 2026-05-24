package io.github.selenium.saucedemo.app.auth;

import org.openqa.selenium.WebDriver;

/** Application-level authentication setup strategy for UI tests. */
public interface AuthenticationStrategy {

  void authenticate(WebDriver driver);
}
