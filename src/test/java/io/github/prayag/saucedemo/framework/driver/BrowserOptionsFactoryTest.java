package io.github.prayag.saucedemo.framework.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.prayag.saucedemo.framework.config.ConfigLoader;
import io.github.prayag.saucedemo.framework.config.ConfigSources;
import io.github.prayag.saucedemo.framework.config.FrameworkConfigurationException;
import java.util.Map;
import java.util.Properties;
import org.testng.annotations.Test;

public class BrowserOptionsFactoryTest {

  @Test(groups = "framework")
  public void configuredRemoteCapabilitiesAreAppliedToBrowserOptions() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("browser.version", "stable");
    systemProperties.setProperty("platform.name", "linux");
    systemProperties.setProperty("accept.insecure.certs", "true");
    systemProperties.setProperty("remote.capabilities", "{\"se:name\":\"portfolio-run\"}");
    var config =
        new ConfigLoader()
            .load(new ConfigSources(Map.of(), systemProperties, getClass().getClassLoader()));
    var capabilities = new BrowserOptionsFactory().chromeOptions(config);

    new BrowserOptionsFactory().applyConfiguredRemoteCapabilities(capabilities, config);

    assertThat(capabilities.getCapability("browserVersion")).isEqualTo("stable");
    assertThat(capabilities.getCapability("platformName")).hasToString("linux");
    assertThat(capabilities.getCapability("acceptInsecureCerts")).isEqualTo(true);
    assertThat(capabilities.getCapability("se:name")).isEqualTo("portfolio-run");
  }

  @Test(groups = "framework")
  public void invalidRemoteCapabilityJsonFailsFast() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("remote.capabilities", "{invalid");
    var config =
        new ConfigLoader()
            .load(new ConfigSources(Map.of(), systemProperties, getClass().getClassLoader()));
    var capabilities = new BrowserOptionsFactory().chromeOptions(config);

    assertThatThrownBy(
            () ->
                new BrowserOptionsFactory().applyConfiguredRemoteCapabilities(capabilities, config))
        .isInstanceOf(FrameworkConfigurationException.class)
        .hasMessageContaining("remote.capabilities");
  }
}
