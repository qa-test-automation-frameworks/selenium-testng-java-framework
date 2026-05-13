package io.github.prayag.saucedemo.framework.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Properties;
import org.testng.annotations.Test;

public class ConfigLoaderTest {

  @Test(groups = "framework")
  public void booleanConfigRejectsInvalidValues() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("headless", "ture");

    assertThatThrownBy(
            () ->
                ConfigFactory.loadForTesting(
                    new ConfigSources(Map.of(), systemProperties, getClass().getClassLoader())))
        .isInstanceOf(FrameworkConfigurationException.class)
        .hasMessageContaining("headless")
        .hasMessageContaining("true or false");
  }

  @Test(groups = "framework")
  public void knownEnvironmentKeysOverrideDefaults() {
    FrameworkConfig config =
        ConfigFactory.loadForTesting(
            new ConfigSources(
                Map.of("HEADLESS", "true", "UNKNOWN_SETTING", "ignored"),
                new Properties(),
                getClass().getClassLoader()));

    assertThat(config.headless()).isTrue();
  }

  @Test(groups = "framework")
  public void remoteCapabilitySettingsAreLoadedAndValidated() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("browser.version", "stable");
    systemProperties.setProperty("platform.name", "linux");
    systemProperties.setProperty("accept.insecure.certs", "true");
    systemProperties.setProperty("remote.capabilities", "{\"se:name\":\"portfolio-run\"}");

    FrameworkConfig config =
        ConfigFactory.loadForTesting(
            new ConfigSources(Map.of(), systemProperties, getClass().getClassLoader()));

    assertThat(config.browserVersion()).isEqualTo("stable");
    assertThat(config.platformName()).isEqualTo("linux");
    assertThat(config.acceptInsecureCerts()).isTrue();
    assertThat(config.remoteCapabilities()).contains("portfolio-run");
  }
}
