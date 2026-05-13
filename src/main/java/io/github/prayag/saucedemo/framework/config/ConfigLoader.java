package io.github.prayag.saucedemo.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    for (ConfigKey configKey : ConfigKey.values()) {
      properties.setProperty(configKey.key(), configKey.defaultValue());
    }
    properties.setProperty(ConfigKey.ENV.key(), resolveEnvironment(systemProperties, Map.of()));
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
    String resourceName = environment.toLowerCase(java.util.Locale.ROOT) + ".properties";
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
    // Only known keys are eligible for env overrides. Add a default or profile key for each new
    // setting so the supported configuration surface stays explicit.
    for (ConfigKey configKey : ConfigKey.values()) {
      String exactValue = environment.get(configKey.key());
      String normalizedValue = environment.get(configKey.environmentName());
      if (exactValue != null) {
        properties.setProperty(configKey.key(), exactValue);
      } else if (normalizedValue != null) {
        properties.setProperty(configKey.key(), normalizedValue);
      }
    }
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

  private void validateConfig(FrameworkConfig config) {
    config.browserType();
    ExecutionType.from(config.executionType());
    config.acceptInsecureCerts();
    config.headless();
    config.maximizeWindow();
    config.allowPasswordlessSkips();
    config.retryEnabled();
    config.networkLogsEnabled();
    config.attachScreenshotsOnFailure();
    config.attachPageSourceOnFailure();
    config.attachBrowserLogsOnFailure();
    config.attachFrameworkLogsOnFailure();
    validateRequiredStrings(config);
    validatePositiveNumbers(config);
    validateRemoteExecution(config);
  }

  private void validateRequiredStrings(FrameworkConfig config) {
    if (config.sensitiveDomSelectors() == null || config.sensitiveDomSelectors().isBlank()) {
      throw new FrameworkConfigurationException(
          "diagnostics.sensitive.dom.selectors must be provided");
    }
    if (config.appUrl() == null || config.appUrl().isBlank()) {
      throw new FrameworkConfigurationException("app.url must be provided");
    }
    if (config.appUsername() == null || config.appUsername().isBlank()) {
      throw new FrameworkConfigurationException("APP_USERNAME must be provided");
    }
  }

  private void validatePositiveNumbers(FrameworkConfig config) {
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
  }

  private void validateRemoteExecution(FrameworkConfig config) {
    if (config.executionType().equalsIgnoreCase("remote")
        && (config.remoteUrl() == null || config.remoteUrl().isBlank())) {
      throw new FrameworkConfigurationException(
          "remote.url must be provided when execution.type is remote");
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
    public String browserVersion() {
      return get("browser.version");
    }

    @Override
    public String platformName() {
      return get("platform.name");
    }

    @Override
    public boolean acceptInsecureCerts() {
      return getBoolean("accept.insecure.certs");
    }

    @Override
    public String remoteCapabilities() {
      return get("remote.capabilities");
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
    public String sensitiveDomSelectors() {
      return get("diagnostics.sensitive.dom.selectors");
    }

    @Override
    public boolean allowPasswordlessSkips() {
      return getBoolean("allow.passwordless.skips");
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
      String value = get(key).trim().toLowerCase(java.util.Locale.ROOT);
      if ("true".equals(value) || "false".equals(value)) {
        return Boolean.parseBoolean(value);
      }
      throw new FrameworkConfigurationException(
          String.format("Configuration value '%s' must be true or false", key));
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
