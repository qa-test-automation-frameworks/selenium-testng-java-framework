package com.example.saucedemo.tests.data;

import com.example.saucedemo.framework.config.ConfigFactory;

public final class LoginScenario {

  private LoginScenario() {}

  public static Credentials lockedOutUser() {
    return new Credentials("locked_out_user", ConfigFactory.getConfig().appPassword());
  }

  public static Credentials invalidUser() {
    return new Credentials("invalid_user", "invalid_password");
  }
}
