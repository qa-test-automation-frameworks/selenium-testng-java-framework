package io.github.selenium.saucedemo.tests.data;

import io.github.selenium.saucedemo.framework.config.ConfigFactory;

public final class LoginScenario {

  private static final Credentials INVALID_CREDENTIALS =
      new Credentials("invalid_user", "invalid_password");

  private LoginScenario() {}

  public static Credentials lockedOutUser() {
    return SauceDemoUser.LOCKED_OUT.credentials(ConfigFactory.getConfig().appPassword());
  }

  public static Credentials problemUser() {
    return SauceDemoUser.PROBLEM.credentials(ConfigFactory.getConfig().appPassword());
  }

  public static Credentials errorUser() {
    return SauceDemoUser.ERROR.credentials(ConfigFactory.getConfig().appPassword());
  }

  public static Credentials performanceGlitchUser() {
    return SauceDemoUser.PERFORMANCE_GLITCH.credentials(ConfigFactory.getConfig().appPassword());
  }

  public static Credentials invalidUser() {
    return INVALID_CREDENTIALS;
  }

  public static Credentials emptyCredentials() {
    return new Credentials("", "");
  }

  public static Credentials missingUsername() {
    return new Credentials("", "not_used_password");
  }

  public static Credentials missingPassword() {
    return new Credentials(ConfigFactory.getConfig().appUsername(), "");
  }

  public record InvalidLoginCase(
      Credentials credentials, String expectedErrorMessage, boolean requiresConfiguredPassword) {}
}
