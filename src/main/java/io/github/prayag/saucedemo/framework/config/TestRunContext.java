package io.github.prayag.saucedemo.framework.config;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public record TestRunContext(
    FrameworkConfig config, String runId, Instant startedAt, Path artifactDirectory) {

  public static TestRunContext load() {
    FrameworkConfig config = ConfigFactory.getConfig();
    String resultsDirectory =
        System.getProperty("allure.results.directory", "target/allure-results");
    return new TestRunContext(
        config, UUID.randomUUID().toString(), Instant.now(), Path.of(resultsDirectory));
  }
}
