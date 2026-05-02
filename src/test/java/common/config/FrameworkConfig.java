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
}
