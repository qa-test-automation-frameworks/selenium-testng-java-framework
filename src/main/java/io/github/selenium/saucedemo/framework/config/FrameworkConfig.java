package io.github.selenium.saucedemo.framework.config;

/** Type-safe access to framework configuration values. */
public interface FrameworkConfig {

  String environment();

  String browser();

  BrowserType browserType();

  String executionType();

  String remoteUrl();

  String browserVersion();

  String platformName();

  boolean acceptInsecureCerts();

  String remoteCapabilities();

  boolean headless();

  boolean maximizeWindow();

  int viewportWidth();

  int viewportHeight();

  int threadCount();

  String appUrl();

  String appUsername();

  String appPassword();

  String sensitiveDomSelectors();

  boolean allowPasswordlessSkips();

  boolean retryEnabled();

  int retryCount();

  int explicitWaitSeconds();

  int pageLoadTimeoutSeconds();

  int scriptTimeoutSeconds();

  long pollingIntervalMs();

  boolean networkLogsEnabled();

  String gridVideoBaseUrl();

  boolean visualAutoApprove();

  String visualBaselineDir();

  boolean attachScreenshotsOnFailure();

  boolean attachPageSourceOnFailure();

  boolean attachBrowserLogsOnFailure();

  boolean attachFrameworkLogsOnFailure();
}
