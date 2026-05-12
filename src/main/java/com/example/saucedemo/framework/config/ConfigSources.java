package com.example.saucedemo.framework.config;

import java.util.Map;
import java.util.Properties;

/** Immutable configuration input sources for {@link ConfigLoader}. */
public record ConfigSources(
    Map<String, String> environment, Properties systemProperties, ClassLoader classLoader) {

  public ConfigSources {
    environment = Map.copyOf(environment);
    Properties copiedSystemProperties = new Properties();
    copiedSystemProperties.putAll(systemProperties);
    systemProperties = copiedSystemProperties;
  }

  @Override
  public Map<String, String> environment() {
    return Map.copyOf(environment);
  }

  @Override
  public Properties systemProperties() {
    Properties copiedSystemProperties = new Properties();
    copiedSystemProperties.putAll(systemProperties);
    return copiedSystemProperties;
  }

  public static ConfigSources systemSources() {
    return new ConfigSources(
        System.getenv(), System.getProperties(), ConfigSources.class.getClassLoader());
  }
}
