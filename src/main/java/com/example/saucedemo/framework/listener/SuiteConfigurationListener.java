package com.example.saucedemo.framework.listener;

import com.example.saucedemo.framework.config.FrameworkConfig;
import com.example.saucedemo.framework.config.TestRunContext;
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
    TestRunContext runContext = TestRunContext.load();
    FrameworkConfig config = runContext.config();
    int threadCount = config.threadCount();
    suites.forEach(suite -> suite.setThreadCount(threadCount));
    generateAllureEnvironmentFile(runContext);
  }

  private void generateAllureEnvironmentFile(TestRunContext runContext) {
    FrameworkConfig config = runContext.config();
    Properties props = new Properties();
    props.setProperty("Run ID", runContext.runId());
    props.setProperty("Run Started At", runContext.startedAt().toString());
    props.setProperty("Environment", config.environment().toUpperCase());
    props.setProperty("App URL", config.appUrl());
    props.setProperty("Browser Requested", config.browser());
    props.setProperty("Execution Type", config.executionType());
    props.setProperty("OS", System.getProperty("os.name"));
    props.setProperty("Java Version", System.getProperty("java.version"));

    try {
      Files.createDirectories(runContext.artifactDirectory());
      try (FileOutputStream fos =
          new FileOutputStream(
              Paths.get(runContext.artifactDirectory().toString(), "environment.properties")
                  .toString())) {
        props.store(fos, "Allure Environment Properties");
        log.info(
            "Allure environment.properties generated successfully in {}",
            runContext.artifactDirectory());
      }
    } catch (IOException e) {
      log.error("Failed to generate Allure environment.properties", e);
    }
  }
}
