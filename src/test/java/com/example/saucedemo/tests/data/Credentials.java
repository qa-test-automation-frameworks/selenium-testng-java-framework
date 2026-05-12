package com.example.saucedemo.tests.data;

/**
 * Immutable credential pair used by login scenarios.
 *
 * <p>The record keeps the generated {@code password()} accessor for test execution, so callers must
 * avoid logging that value directly. The overridden {@link #toString()} remains redacted for safe
 * diagnostic output.
 */
public record Credentials(String username, String password) {

  @Override
  public String toString() {
    return "Credentials[username=" + username + ", password=<redacted>]";
  }
}
