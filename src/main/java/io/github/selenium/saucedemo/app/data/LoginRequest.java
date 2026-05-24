package io.github.selenium.saucedemo.app.data;

/** Immutable login request payload for page-object login actions. */
public record LoginRequest(String url, String username, String password) {

  @Override
  public String toString() {
    return "LoginRequest[url=" + url + ", username=" + username + ", password=<redacted>]";
  }
}
