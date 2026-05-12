package com.example.saucedemo.framework.config;

import java.util.Map;
import java.util.Properties;

/** Immutable configuration input sources for {@link ConfigLoader}. */
public record ConfigSources(
    Map<String, String> environment, Properties systemProperties, ClassLoader classLoader) {

  public static ConfigSources systemSources() {
    return new ConfigSources(
        System.getenv(), System.getProperties(), ConfigSources.class.getClassLoader());
  }
}
