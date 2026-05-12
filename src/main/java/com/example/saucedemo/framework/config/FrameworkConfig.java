package com.example.saucedemo.framework.config;

/** Type-safe access to framework configuration values. */
public interface FrameworkConfig {

  String browser();

  String executionType();

  String remoteUrl();

  boolean headless();

  boolean maximizeWindow();

  int viewportWidth();

  int viewportHeight();

  int threadCount();

  String appUrl();

  String appUsername();

  String appPassword();

  boolean retryEnabled();

  int retryCount();

  int explicitWaitSeconds();

  int pageLoadTimeoutSeconds();

  int scriptTimeoutSeconds();

  long pollingIntervalMs();

  boolean networkLogsEnabled();

  String gridVideoBaseUrl();
}
