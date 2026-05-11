package common.config;

/** Type-safe access to framework configuration values. */
public interface FrameworkConfig {

  String browser();

  String executionType();

  String remoteUrl();

  boolean headless();

  String appUrl();

  String appUsername();

  String appPassword();

  boolean retryEnabled();

  int retryCount();

  int explicitWaitSeconds();

  int pageLoadTimeoutSeconds();

  int scriptTimeoutSeconds();

  long pollingIntervalMs();
}
