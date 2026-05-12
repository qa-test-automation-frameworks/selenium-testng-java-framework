package com.example.saucedemo.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Factory class for retrieving framework configuration from defaults, property files, environment
 * variables, and system properties.
 */
public final class ConfigFactory {

  private static final AtomicBoolean validated = new AtomicBoolean(false);
  private static final FrameworkConfig CONFIG = loadConfig();

  private ConfigFactory() {}

  /**
   * Retrieves the global framework configuration. Defaults to 'qa' environment if 'env' system
   * property is not set.
   *
   * @return An instance of {@link FrameworkConfig}.
   */
  public static FrameworkConfig getConfig() {
    if (validated.compareAndSet(false, true)) {
      validateConfig(CONFIG);
    }
    return CONFIG;
  }

  public static FrameworkConfig load() {
    FrameworkConfig config = loadConfig();
    validateConfig(config);
    return config;
  }

  private static FrameworkConfig loadConfig() {
    String environment = System.getProperty("env", System.getenv().getOrDefault("env", "qa"));
    Properties properties = defaultProperties();
    loadFromFile(
        properties,
        Path.of(System.getProperty("user.dir"), "src/test/resources/config.properties"));
    loadFromFile(
        properties,
        Path.of(System.getProperty("user.dir"), "src/test/resources", environment + ".properties"));
    applyEnvironmentOverrides(properties);
    applySystemPropertyOverrides(properties);
    return new DefaultFrameworkConfig(properties);
  }

  private static Properties defaultProperties() {
    Properties properties = new Properties();
    properties.setProperty("browser", "CHROME");
    properties.setProperty("execution.type", "local");
    properties.setProperty("remote.url", "");
    properties.setProperty("headless", "false");
    properties.setProperty("maximize.window", "true");
    properties.setProperty("viewport.width", "1920");
    properties.setProperty("viewport.height", "1080");
    properties.setProperty("thread.count", System.getProperty("thread.count", "1"));
    properties.setProperty("app.url", "https://www.saucedemo.com/");
    properties.setProperty("APP_USERNAME", "standard_user");
    properties.setProperty("APP_PASSWORD", "");
    properties.setProperty("retry.enabled", "false");
    properties.setProperty("retry.count", "2");
    properties.setProperty("explicit.wait.seconds", "10");
    properties.setProperty("page.load.timeout.seconds", "30");
    properties.setProperty("script.timeout.seconds", "30");
    properties.setProperty("polling.interval.ms", "500");
    properties.setProperty("diagnostics.network.logs.enabled", "false");
    properties.setProperty("diagnostics.grid.video.base.url", "");
    return properties;
  }

  private static void loadFromFile(Properties properties, Path path) {
    if (!Files.exists(path)) {
      return;
    }
    try (InputStream inputStream = Files.newInputStream(path)) {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new FrameworkConfigurationException(
          "Unable to load configuration file: " + path.toAbsolutePath(), e);
    }
  }

  private static void applyEnvironmentOverrides(Properties properties) {
    properties
        .stringPropertyNames()
        .forEach(
            key -> {
              String exactValue = System.getenv(key);
              String normalizedValue = System.getenv(toEnvironmentVariableName(key));
              if (exactValue != null) {
                properties.setProperty(key, exactValue);
              } else if (normalizedValue != null) {
                properties.setProperty(key, normalizedValue);
              }
            });
  }

  private static void applySystemPropertyOverrides(Properties properties) {
    properties
        .stringPropertyNames()
        .forEach(
            key -> {
              String value = System.getProperty(key);
              if (value != null) {
                properties.setProperty(key, value);
              }
            });
  }

  private static String toEnvironmentVariableName(String key) {
    return key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
  }

  /**
   * Validates mandatory configuration parameters based on execution mode.
   *
   * @param config The configuration instance to validate.
   * @throws FrameworkConfigurationException if mandatory parameters are missing.
   */
  private static void validateConfig(FrameworkConfig config) {
    validateExecutionType(config.executionType());
    if (config.threadCount() < 1) {
      throw new FrameworkConfigurationException("thread.count must be greater than zero");
    }
    if (config.viewportWidth() < 1 || config.viewportHeight() < 1) {
      throw new FrameworkConfigurationException(
          "viewport.width and viewport.height must be positive");
    }
    if (config.retryCount() < 0) {
      throw new FrameworkConfigurationException("retry.count must not be negative");
    }
    if (config.executionType().equalsIgnoreCase("remote")
        && (config.remoteUrl() == null || config.remoteUrl().isBlank())) {
      throw new FrameworkConfigurationException(
          "remote.url must be provided when execution.type is remote");
    }
    if (config.appUrl() == null || config.appUrl().isBlank()) {
      throw new FrameworkConfigurationException("app.url must be provided");
    }
    if (config.appPassword() == null || config.appPassword().isBlank()) {
      throw new FrameworkConfigurationException(
          "APP_PASSWORD must be provided through an environment variable or Maven system property");
    }
  }

  private static void validateExecutionType(String executionType) {
    if (!"local".equalsIgnoreCase(executionType) && !"remote".equalsIgnoreCase(executionType)) {
      throw new FrameworkConfigurationException(
          String.format(
              "Unsupported execution.type '%s'. Supported values: local, remote", executionType));
    }
  }

  private record DefaultFrameworkConfig(Properties properties) implements FrameworkConfig {

    @Override
    public String browser() {
      return get("browser");
    }

    @Override
    public String executionType() {
      return get("execution.type");
    }

    @Override
    public String remoteUrl() {
      return get("remote.url");
    }

    @Override
    public boolean headless() {
      return getBoolean("headless");
    }

    @Override
    public boolean maximizeWindow() {
      return getBoolean("maximize.window");
    }

    @Override
    public int viewportWidth() {
      return getInt("viewport.width");
    }

    @Override
    public int viewportHeight() {
      return getInt("viewport.height");
    }

    @Override
    public int threadCount() {
      return getInt("thread.count");
    }

    @Override
    public String appUrl() {
      return get("app.url");
    }

    @Override
    public String appUsername() {
      return get("APP_USERNAME");
    }

    @Override
    public String appPassword() {
      return get("APP_PASSWORD");
    }

    @Override
    public boolean retryEnabled() {
      return getBoolean("retry.enabled");
    }

    @Override
    public int retryCount() {
      return getInt("retry.count");
    }

    @Override
    public int explicitWaitSeconds() {
      return getInt("explicit.wait.seconds");
    }

    @Override
    public int pageLoadTimeoutSeconds() {
      return getInt("page.load.timeout.seconds");
    }

    @Override
    public int scriptTimeoutSeconds() {
      return getInt("script.timeout.seconds");
    }

    @Override
    public long pollingIntervalMs() {
      return getLong("polling.interval.ms");
    }

    @Override
    public boolean networkLogsEnabled() {
      return getBoolean("diagnostics.network.logs.enabled");
    }

    @Override
    public String gridVideoBaseUrl() {
      return get("diagnostics.grid.video.base.url");
    }

    private String get(String key) {
      return properties.getProperty(key, "");
    }

    private boolean getBoolean(String key) {
      return Boolean.parseBoolean(get(key));
    }

    private int getInt(String key) {
      try {
        return Integer.parseInt(get(key));
      } catch (NumberFormatException e) {
        throw new FrameworkConfigurationException(
            String.format("Configuration value '%s' must be an integer", key), e);
      }
    }

    private long getLong(String key) {
      try {
        return Long.parseLong(get(key));
      } catch (NumberFormatException e) {
        throw new FrameworkConfigurationException(
            String.format("Configuration value '%s' must be a long", key), e);
      }
    }
  }
}
