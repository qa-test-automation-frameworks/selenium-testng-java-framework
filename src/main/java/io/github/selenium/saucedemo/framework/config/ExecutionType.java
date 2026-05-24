package io.github.selenium.saucedemo.framework.config;

import java.util.Locale;

public enum ExecutionType {
  LOCAL,
  REMOTE;

  public static ExecutionType from(String value) {
    try {
      return ExecutionType.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new FrameworkConfigurationException(
          String.format("Unsupported execution.type '%s'. Supported values: local, remote", value),
          e);
    }
  }
}
