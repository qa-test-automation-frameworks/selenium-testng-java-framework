package io.github.selenium.saucedemo.framework.config;

import java.util.Locale;

/** Supported browser types for local and remote UI execution. */
public enum BrowserType {
  CHROME,
  FIREFOX,
  EDGE,
  SAFARI;

  public static BrowserType from(String value) {
    try {
      return BrowserType.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new FrameworkConfigurationException(
          String.format(
              "Unsupported browser '%s'. Supported browsers: CHROME, FIREFOX, EDGE, SAFARI", value),
          e);
    }
  }
}
