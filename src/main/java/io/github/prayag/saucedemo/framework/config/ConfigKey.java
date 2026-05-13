package io.github.prayag.saucedemo.framework.config;

import java.util.Locale;

/** Central definition of supported framework configuration keys and defaults. */
enum ConfigKey {
  ENV("env", "qa"),
  BROWSER("browser", "CHROME"),
  EXECUTION_TYPE("execution.type", "local"),
  REMOTE_URL("remote.url", ""),
  BROWSER_VERSION("browser.version", ""),
  PLATFORM_NAME("platform.name", ""),
  ACCEPT_INSECURE_CERTS("accept.insecure.certs", "false"),
  REMOTE_CAPABILITIES("remote.capabilities", ""),
  HEADLESS("headless", "false"),
  MAXIMIZE_WINDOW("maximize.window", "true"),
  VIEWPORT_WIDTH("viewport.width", "1920"),
  VIEWPORT_HEIGHT("viewport.height", "1080"),
  THREAD_COUNT("thread.count", "1"),
  APP_URL("app.url", "https://www.saucedemo.com/"),
  APP_USERNAME("APP_USERNAME", "standard_user"),
  APP_PASSWORD("APP_PASSWORD", ""),
  SENSITIVE_DOM_SELECTORS(
      "diagnostics.sensitive.dom.selectors",
      "input[type='password'], input[name*='password'], input[id*='password'], "
          + "input[autocomplete='current-password'], input[autocomplete='new-password'], "
          + "input[type='email'], input[name*='email'], input[id*='email'], "
          + "input[name*='token'], input[id*='token'], input[name*='secret'], "
          + "input[id*='secret'], [data-sensitive='true']"),
  ALLOW_PASSWORDLESS_SKIPS("allow.passwordless.skips", "false"),
  RETRY_ENABLED("retry.enabled", "false"),
  RETRY_COUNT("retry.count", "2"),
  EXPLICIT_WAIT_SECONDS("explicit.wait.seconds", "10"),
  PAGE_LOAD_TIMEOUT_SECONDS("page.load.timeout.seconds", "30"),
  SCRIPT_TIMEOUT_SECONDS("script.timeout.seconds", "30"),
  POLLING_INTERVAL_MS("polling.interval.ms", "500"),
  NETWORK_LOGS_ENABLED("diagnostics.network.logs.enabled", "false"),
  GRID_VIDEO_BASE_URL("diagnostics.grid.video.base.url", ""),
  ATTACH_SCREENSHOT_ON_FAILURE("diagnostics.attach.screenshot.on.failure", "true"),
  ATTACH_PAGE_SOURCE_ON_FAILURE("diagnostics.attach.page.source.on.failure", "true"),
  ATTACH_BROWSER_LOGS_ON_FAILURE("diagnostics.attach.browser.logs.on.failure", "true"),
  ATTACH_FRAMEWORK_LOGS_ON_FAILURE("diagnostics.attach.framework.logs.on.failure", "true");

  private final String key;
  private final String defaultValue;

  ConfigKey(String key, String defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  String key() {
    return key;
  }

  String defaultValue() {
    return defaultValue;
  }

  String environmentName() {
    return key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
  }
}
