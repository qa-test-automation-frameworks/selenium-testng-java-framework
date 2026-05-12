package com.example.saucedemo.tests.data;

public record Credentials(String username, String password) {

  @Override
  public String toString() {
    return "Credentials[username=" + username + ", password=<redacted>]";
  }
}
