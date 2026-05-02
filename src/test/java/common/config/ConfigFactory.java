package common.config;

import org.aeonbits.owner.ConfigCache;

/**
 * Factory class for retrieving the framework configuration.
 * Uses the OWNER library for type-safe property management and environment switching.
 */
public final class ConfigFactory {

  private ConfigFactory() {}

  /**
   * Retrieves the global framework configuration.
   * Defaults to 'qa' environment if 'env' system property is not set.
   *
   * @return An instance of {@link FrameworkConfig}.
   */
  public static FrameworkConfig getConfig() {
    if (System.getProperty("env") == null) {
      System.setProperty("env", "qa");
    }
    FrameworkConfig config = ConfigCache.getOrCreate(FrameworkConfig.class);
    validateConfig(config);
    return config;
  }

  /**
   * Validates mandatory configuration parameters based on execution mode.
   *
   * @param config The configuration instance to validate.
   * @throws RuntimeException if mandatory parameters (like remote.url) are missing.
   */
  private static void validateConfig(FrameworkConfig config) {
    if (config.executionType().equalsIgnoreCase("remote")
        && (config.remoteUrl() == null || config.remoteUrl().isBlank())) {
      throw new RuntimeException("remote.url must be provided when execution.type is remote");
    }
  }
}
