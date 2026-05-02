package common.config;

import org.aeonbits.owner.Config;

@Config.Sources({
    "system:properties",
    "system:env",
    "file:${user.dir}/src/main/resources/${env}.properties",
    "file:${user.dir}/src/main/resources/config.properties"
})
public interface FrameworkConfig extends Config {

    @Key("driverType")
    @DefaultValue("CHROME")
    String driverType();

    @Key("execution_type")
    @DefaultValue("local")
    String executionType();

    @Key("REMOTE_URL")
    String remoteUrl();

    @Key("headless")
    @DefaultValue("false")
    boolean headless();

    @Key("app.url")
    @DefaultValue("https://www.saucedemo.com/")
    String appUrl();

    @Key("app.username")
    @DefaultValue("standard_user")
    String appUsername();

    @Key("app.password")
    @DefaultValue("secret_sauce")
    String appPassword();
}
