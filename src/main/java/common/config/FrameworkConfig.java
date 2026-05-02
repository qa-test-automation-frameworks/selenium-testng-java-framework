package common.config;

import org.aeonbits.owner.Config;

@Config.Sources({
  "system:properties",
  "system:env",
  "file:${user.dir}/src/test/resources/${env}.properties",
  "file:${user.dir}/src/test/resources/config.properties"
})
public interface FrameworkConfig extends Config {

  @Key("browser")
  @DefaultValue("CHROME")
  String browser();

  @Key("execution.type")
  @DefaultValue("local")
  String executionType();

  @Key("remote.url")
  String remoteUrl();

  @Key("headless")
  @DefaultValue("false")
  boolean headless();

  @Key("app.url")
  @DefaultValue("https://www.saucedemo.com/")
  String appUrl();

  @Key("APP_USERNAME")
  @DefaultValue("standard_user")
  String appUsername();

  @Key("APP_PASSWORD")
  String appPassword();

  @Key("retry.enabled")
  @DefaultValue("false")
  boolean retryEnabled();

  @Key("retry.count")
  @DefaultValue("2")
  int retryCount();

  @Key("explicit.wait.seconds")
  @DefaultValue("10")
  int explicitWaitSeconds();

  @Key("page.load.timeout.seconds")
  @DefaultValue("30")
  int pageLoadTimeoutSeconds();

  @Key("script.timeout.seconds")
  @DefaultValue("30")
  int scriptTimeoutSeconds();

  @Key("polling.interval.ms")
  @DefaultValue("500")
  long pollingIntervalMs();
}
