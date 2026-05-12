package com.example.saucedemo.framework.listener;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.config.FrameworkConfig;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

@Slf4j
public class SuiteConfigurationListener implements IAlterSuiteListener {

  @Override
  public void alter(List<XmlSuite> suites) {
    FrameworkConfig config = ConfigFactory.getConfig();
    int threadCount = config.threadCount();
    suites.forEach(suite -> suite.setThreadCount(threadCount));
    generateAllureEnvironmentFile(config);
  }

  private void generateAllureEnvironmentFile(FrameworkConfig config) {
    Properties props = new Properties();
    props.setProperty("Environment", System.getProperty("env", "qa").toUpperCase());
    props.setProperty("App URL", config.appUrl());
    props.setProperty("Browser", config.browser());
    props.setProperty("Execution Type", config.executionType());
    props.setProperty("OS", System.getProperty("os.name"));
    props.setProperty("Java Version", System.getProperty("java.version"));

    String resultsDir = System.getProperty("allure.results.directory", "target/allure-results");
    try {
      Files.createDirectories(Paths.get(resultsDir));
      try (FileOutputStream fos = new FileOutputStream(resultsDir + "/environment.properties")) {
        props.store(fos, "Allure Environment Properties");
        log.info("Allure environment.properties generated successfully in {}", resultsDir);
      }
    } catch (IOException e) {
      log.error("Failed to generate Allure environment.properties", e);
    }
  }
}
