package io.github.prayag.saucedemo.tests.data;

import io.github.prayag.saucedemo.framework.config.ConfigFactory;

public final class LoginScenario {

  private LoginScenario() {}

  public static Credentials lockedOutUser() {
    return new Credentials("locked_out_user", ConfigFactory.getConfig().appPassword());
  }

  public static Credentials problemUser() {
    return new Credentials("problem_user", ConfigFactory.getConfig().appPassword());
  }

  public static Credentials performanceGlitchUser() {
    return new Credentials("performance_glitch_user", ConfigFactory.getConfig().appPassword());
  }

  public static Credentials invalidUser() {
    return new Credentials("invalid_user", "invalid_password");
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
