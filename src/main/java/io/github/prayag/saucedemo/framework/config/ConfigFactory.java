package io.github.prayag.saucedemo.framework.config;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Factory class for retrieving framework configuration from defaults, property files, environment
 * variables, and system properties.
 */
public final class ConfigFactory {

  private static final ConfigLoader CONFIG_LOADER = new ConfigLoader();
  private static final AtomicReference<FrameworkConfig> CONFIG = new AtomicReference<>();

  private ConfigFactory() {}

  public static FrameworkConfig getConfig() {
    FrameworkConfig existingConfig = CONFIG.get();
    if (existingConfig != null) {
      return existingConfig;
    }
    FrameworkConfig loadedConfig = CONFIG_LOADER.load(ConfigSources.systemSources());
    if (CONFIG.compareAndSet(null, loadedConfig)) {
      return loadedConfig;
    }
    return CONFIG.get();
  }

  static void resetForTesting() {
    CONFIG.set(null);
  }

  static FrameworkConfig loadForTesting(ConfigSources sources) {
    return CONFIG_LOADER.load(sources);
  }

  public static void requireLoginPassword(FrameworkConfig config) {
    if (config.appPassword() == null || config.appPassword().isBlank()) {
      throw new FrameworkConfigurationException("APP_PASSWORD is required for UI login scenarios");
    }
  }
}
