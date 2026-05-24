package io.github.selenium.saucedemo.tests.data;

/** Public Sauce Demo personas used by UI scenarios. */
public enum SauceDemoUser {
  STANDARD("standard_user"),
  LOCKED_OUT("locked_out_user"),
  PROBLEM("problem_user"),
  ERROR("error_user"),
  PERFORMANCE_GLITCH("performance_glitch_user");

  private final String username;

  SauceDemoUser(String username) {
    this.username = username;
  }

  public String username() {
    return username;
  }

  public Credentials credentials(String password) {
    return new Credentials(username, password);
  }
}
