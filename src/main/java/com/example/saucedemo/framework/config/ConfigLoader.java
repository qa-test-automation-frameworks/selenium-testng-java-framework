package com.example.saucedemo.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Loads framework configuration from defaults, classpath resources, environment variables, system
 * properties, and an optional external file.
 */
public final class ConfigLoader {

  public FrameworkConfig load(ConfigSources sources) {
    Properties properties = defaultProperties(sources.systemProperties());
    loadFromClasspath(properties, sources.classLoader(), "config.properties");

    String environment = resolveEnvironment(sources.systemProperties(), sources.environment());
    properties.setProperty("env", environment);
    loadEnvironmentProfile(
        properties, sources.classLoader(), environment, sources.systemProperties());
    loadFromExternalPathIfPresent(properties, sources.systemProperties());
    applyEnvironmentOverrides(properties, sources.environment());
    applySystemPropertyOverrides(properties, sources.systemProperties());

    FrameworkConfig config = new DefaultFrameworkConfig(properties);
    validateConfig(config);
    return config;
  }

  String resolveEnvironment(Properties systemProperties, Map<String, String> environment) {
    String systemPropertyValue = systemProperties.getProperty("env");
    if (systemPropertyValue != null && !systemPropertyValue.isBlank()) {
      return systemPropertyValue;
    }

    String uppercaseEnvironment = environment.get("ENV");
    if (uppercaseEnvironment != null && !uppercaseEnvironment.isBlank()) {
      return uppercaseEnvironment;
    }

    String lowercaseEnvironment = environment.get("env");
    if (lowercaseEnvironment != null && !lowercaseEnvironment.isBlank()) {
      return lowercaseEnvironment;
    }

    return "qa";
  }

  private Properties defaultProperties(Properties systemProperties) {
    Properties properties = new Properties();
    properties.setProperty("env", resolveEnvironment(systemProperties, Map.of()));
    properties.setProperty("browser", "CHROME");
    properties.setProperty("execution.type", "local");
    properties.setProperty("remote.url", "");
    properties.setProperty("headless", "false");
    properties.setProperty("maximize.window", "true");
    properties.setProperty("viewport.width", "1920");
    properties.setProperty("viewport.height", "1080");
    properties.setProperty("thread.count", systemProperties.getProperty("thread.count", "1"));
    properties.setProperty("app.url", "https://www.saucedemo.com/");
    properties.setProperty("APP_USERNAME", "standard_user");
    properties.setProperty("retry.enabled", "false");
    properties.setProperty("retry.count", "2");
    properties.setProperty("explicit.wait.seconds", "10");
    properties.setProperty("page.load.timeout.seconds", "30");
    properties.setProperty("script.timeout.seconds", "30");
    properties.setProperty("polling.interval.ms", "500");
    properties.setProperty("diagnostics.network.logs.enabled", "false");
    properties.setProperty("diagnostics.grid.video.base.url", "");
    properties.setProperty("diagnostics.attach.screenshot.on.failure", "true");
    properties.setProperty("diagnostics.attach.page.source.on.failure", "true");
    properties.setProperty("diagnostics.attach.browser.logs.on.failure", "true");
    properties.setProperty("diagnostics.attach.framework.logs.on.failure", "true");
    return properties;
  }

  private void loadFromClasspath(
      Properties properties, ClassLoader classLoader, String resourceName) {
    try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        return;
      }
      properties.load(inputStream);
    } catch (IOException e) {
      throw new FrameworkConfigurationException(
          "Unable to load configuration resource: " + resourceName, e);
    }
  }

  private void loadEnvironmentProfile(
      Properties properties,
      ClassLoader classLoader,
      String environment,
      Properties systemProperties) {
    String resourceName = environment.toLowerCase(Locale.ROOT) + ".properties";
    boolean loaded = loadFromClasspathIfPresent(properties, classLoader, resourceName);
    boolean externalConfigProvided =
        systemProperties.getProperty("config.file") != null
            && !systemProperties.getProperty("config.file").isBlank();
    if (!loaded && !"qa".equalsIgnoreCase(environment) && !externalConfigProvided) {
      throw new FrameworkConfigurationException(
          "Environment profile not found on classpath: "
              + resourceName
              + ". Add a safe profile resource or provide overrides with -Dconfig.file.");
    }
  }

  private boolean loadFromClasspathIfPresent(
      Properties properties, ClassLoader classLoader, String resourceName) {
    try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        return false;
      }
      properties.load(inputStream);
      return true;
    } catch (IOException e) {
      throw new FrameworkConfigurationException(
          "Unable to load configuration resource: " + resourceName, e);
    }
  }

  private void loadFromExternalPathIfPresent(Properties properties, Properties systemProperties) {
    String externalConfigPath = systemProperties.getProperty("config.file");
    if (externalConfigPath == null || externalConfigPath.isBlank()) {
      return;
    }
    loadFromFile(properties, Path.of(externalConfigPath));
  }

  private void loadFromFile(Properties properties, Path path) {
    if (!Files.exists(path)) {
      throw new FrameworkConfigurationException(
          "External configuration file does not exist: " + path.toAbsolutePath());
    }
    try (InputStream inputStream = Files.newInputStream(path)) {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new FrameworkConfigurationException(
          "Unable to load configuration file: " + path.toAbsolutePath(), e);
    }
  }

  private void applyEnvironmentOverrides(Properties properties, Map<String, String> environment) {
    properties
        .stringPropertyNames()
        .forEach(
            key -> {
              String exactValue = environment.get(key);
              String normalizedValue = environment.get(toEnvironmentVariableName(key));
              if (exactValue != null) {
                properties.setProperty(key, exactValue);
              } else if (normalizedValue != null) {
                properties.setProperty(key, normalizedValue);
              }
            });
  }

  private void applySystemPropertyOverrides(Properties properties, Properties systemProperties) {
    properties
        .stringPropertyNames()
        .forEach(
            key -> {
              String value = systemProperties.getProperty(key);
              if (value != null) {
                properties.setProperty(key, value);
              }
            });
  }

  private String toEnvironmentVariableName(String key) {
    return key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
  }

  private void validateConfig(FrameworkConfig config) {
    config.browserType();
    ExecutionType.from(config.executionType());
    if (config.threadCount() < 1) {
      throw new FrameworkConfigurationException(
          "thread.count must be greater than zero; actual value was " + config.threadCount());
    }
    if (config.viewportWidth() < 1 || config.viewportHeight() < 1) {
      throw new FrameworkConfigurationException(
          String.format(
              "viewport.width and viewport.height must be positive; actual values were %d x %d",
              config.viewportWidth(), config.viewportHeight()));
    }
    if (config.retryCount() < 0) {
      throw new FrameworkConfigurationException(
          "retry.count must not be negative; actual value was " + config.retryCount());
    }
    if (config.explicitWaitSeconds() < 1) {
      throw new FrameworkConfigurationException(
          "explicit.wait.seconds must be greater than zero; actual value was "
              + config.explicitWaitSeconds());
    }
    if (config.pageLoadTimeoutSeconds() < 1) {
      throw new FrameworkConfigurationException(
          "page.load.timeout.seconds must be greater than zero; actual value was "
              + config.pageLoadTimeoutSeconds());
    }
    if (config.scriptTimeoutSeconds() < 1) {
      throw new FrameworkConfigurationException(
          "script.timeout.seconds must be greater than zero; actual value was "
              + config.scriptTimeoutSeconds());
    }
    if (config.pollingIntervalMs() < 1) {
      throw new FrameworkConfigurationException(
          "polling.interval.ms must be greater than zero; actual value was "
              + config.pollingIntervalMs());
    }
    if (config.executionType().equalsIgnoreCase("remote")
        && (config.remoteUrl() == null || config.remoteUrl().isBlank())) {
      throw new FrameworkConfigurationException(
          "remote.url must be provided when execution.type is remote");
    }
    if (config.appUrl() == null || config.appUrl().isBlank()) {
      throw new FrameworkConfigurationException("app.url must be provided");
    }
    if (config.appUsername() == null || config.appUsername().isBlank()) {
      throw new FrameworkConfigurationException("APP_USERNAME must be provided");
    }
  }

  private record DefaultFrameworkConfig(Properties properties) implements FrameworkConfig {

    @Override
    public String environment() {
      return get("env");
    }

    @Override
    public String browser() {
      return get("browser");
    }

    @Override
    public BrowserType browserType() {
      return BrowserType.from(browser());
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

    @Override
    public boolean attachScreenshotsOnFailure() {
      return getBoolean("diagnostics.attach.screenshot.on.failure");
    }

    @Override
    public boolean attachPageSourceOnFailure() {
      return getBoolean("diagnostics.attach.page.source.on.failure");
    }

    @Override
    public boolean attachBrowserLogsOnFailure() {
      return getBoolean("diagnostics.attach.browser.logs.on.failure");
    }

    @Override
    public boolean attachFrameworkLogsOnFailure() {
      return getBoolean("diagnostics.attach.framework.logs.on.failure");
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
