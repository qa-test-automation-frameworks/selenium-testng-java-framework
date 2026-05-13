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
}
