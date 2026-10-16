package io.github.selenium.saucedemo.framework.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

/** Bounded local Selenium Grid probe that records session creation latency and cycle time. */
public class GridCapacityExperimentTests {

  private static final int SAMPLES_PER_WORKER_COUNT = 3;
  private static final List<Integer> WORKER_COUNTS = List.of(1, 2, 4);
  private static final Duration START_BARRIER_TIMEOUT = Duration.ofSeconds(15);

  @Test(timeOut = 720_000)
  public void recordsSessionQueueAndCycleTimeAtOneTwoAndFourWorkers() throws Exception {
    String remoteUrl = System.getProperty("remote.url", "http://127.0.0.1:4444/wd/hub");
    URI.create(remoteUrl).toURL();
    List<SessionSample> samples = new ArrayList<>();
    List<String> failures = new ArrayList<>();

    for (int workerCount : WORKER_COUNTS) {
      for (int repetition = 1; repetition <= SAMPLES_PER_WORKER_COUNT; repetition++) {
        sample(workerCount, repetition, remoteUrl, samples, failures);
      }
    }

    int orphanSessions = waitForNoActiveSessions(remoteUrl);
    if (orphanSessions != 0) {
      failures.add("Grid still reported " + orphanSessions + " active sessions after teardown.");
    }
    writeReport(remoteUrl, samples, failures, orphanSessions);
    assertThat(failures)
        .as("every bounded Grid session should start, serve the probe page, and quit")
        .isEmpty();
    assertThat(samples).hasSize(21);
    assertThat(samples.stream().map(SessionSample::sessionIdSha256).distinct().count())
        .isEqualTo(21);
  }

  private static void sample(
      int workerCount,
      int repetition,
      String remoteUrl,
      List<SessionSample> samples,
      List<String> failures)
      throws Exception {
    CountDownLatch ready = new CountDownLatch(workerCount);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<SessionSample>> futures = new ArrayList<>();
    try (ExecutorService executor = Executors.newFixedThreadPool(workerCount)) {
      try {
        for (int worker = 1; worker <= workerCount; worker++) {
          int workerId = worker;
          futures.add(
              executor.submit(
                  () -> openSession(workerCount, repetition, workerId, remoteUrl, ready, start)));
        }
        if (!ready.await(START_BARRIER_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
          failures.add("worker barrier timed out at concurrency " + workerCount);
        }
        start.countDown();
        for (Future<SessionSample> future : futures) {
          try {
            samples.add(future.get(120, TimeUnit.SECONDS));
          } catch (Exception failure) {
            failures.add(
                "concurrency " + workerCount + ", repetition " + repetition + ": " + failure);
          }
        }
      } finally {
        start.countDown();
        futures.forEach(future -> future.cancel(true));
      }
    }
  }

  private static SessionSample openSession(
      int workerCount,
      int repetition,
      int workerId,
      String remoteUrl,
      CountDownLatch ready,
      CountDownLatch start)
      throws Exception {
    ready.countDown();
    if (!start.await(START_BARRIER_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
      throw new IllegalStateException("session start barrier timed out");
    }
    long requestedAt = System.nanoTime();
    RemoteWebDriver driver = null;
    try {
      driver = new RemoteWebDriver(URI.create(remoteUrl).toURL(), chromeOptions());
      long sessionStartedAt = System.nanoTime();
      driver.get("data:text/html,<title>grid-capacity-probe</title>");
      assertThat(driver.getTitle()).isEqualTo("grid-capacity-probe");
      String browserVersion = driver.getCapabilities().getBrowserVersion();
      String sessionId = driver.getSessionId().toString();
      String sessionIdSha256 = sessionIdDigest(sessionId);
      driver.quit();
      driver = null;
      long completedAt = System.nanoTime();
      return new SessionSample(
          workerCount,
          repetition,
          workerId,
          sessionIdSha256,
          browserVersion,
          millis(sessionStartedAt - requestedAt),
          millis(completedAt - requestedAt));
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }
  }

  private static ChromeOptions chromeOptions() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    return options;
  }

  private static double millis(long nanos) {
    return nanos / 1_000_000.0;
  }

  private static String sessionIdDigest(String sessionId) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(sessionId.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(digest);
  }

  private static int waitForNoActiveSessions(String remoteUrl) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
    int activeSessions;
    do {
      activeSessions = activeSessionCount(remoteUrl);
      if (activeSessions == 0) return 0;
      Thread.sleep(500);
    } while (System.nanoTime() < deadline);
    return activeSessions;
  }

  private static int activeSessionCount(String remoteUrl) throws Exception {
    URI statusUri = URI.create(remoteUrl).resolve("/status");
    HttpRequest request = HttpRequest.newBuilder(statusUri).GET().build();
    HttpResponse<String> response =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build()
            .send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    var nodes = new ObjectMapper().readTree(response.body()).path("value").path("nodes");
    assertThat(nodes.isArray()).as("Grid /status exposes its node and slot inventory").isTrue();
    int active = 0;
    for (var node : nodes) {
      for (var slot : node.path("slots")) {
        var session = slot.path("session");
        if (!session.isMissingNode() && !session.isNull()) active++;
      }
    }
    return active;
  }

  private static Long environmentLong(String name) {
    String value = System.getenv(name);
    return value == null || value.isBlank() ? null : Long.parseLong(value);
  }

  private static void writeReport(
      String remoteUrl, List<SessionSample> samples, List<String> failures, int orphanSessions)
      throws Exception {
    var report =
        new CapacityReport(
            1,
            "local Selenium Grid",
            remoteUrl,
            "selenium/node-chrome:4.43.0@sha256:9e60c032f8f0950bb962868e73263fb503cb6fbf3f14fdd31e81c1f14b6f247b",
            "2 GiB container memory limit and 2 GiB shared memory per Chrome node",
            environmentLong("GRID_MEMORY_LIMIT_BYTES"),
            environmentLong("GRID_SHARED_MEMORY_BYTES"),
            WORKER_COUNTS,
            SAMPLES_PER_WORKER_COUNT,
            samples,
            orphanSessions,
            failures,
            "Small owned Grid sample; not a general throughput or production capacity claim.");
    var directory = java.nio.file.Path.of("target");
    java.nio.file.Files.createDirectories(directory);
    var path = directory.resolve("grid-capacity-sample.json");
    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(path.toFile(), report);
  }

  private record SessionSample(
      int workers,
      int repetition,
      int worker,
      String sessionIdSha256,
      String browserVersion,
      double sessionRequestToCreatedMilliseconds,
      double fullCycleMilliseconds) {}

  private record CapacityReport(
      int schemaVersion,
      String target,
      String remoteUrl,
      String browserNodeImage,
      String declaredResourceLimit,
      Long actualMemoryLimitBytes,
      Long actualSharedMemoryBytes,
      List<Integer> workerCounts,
      int samplesPerWorkerCount,
      List<SessionSample> samples,
      int activeSessionsAfterTeardown,
      List<String> failures,
      String limitation) {}
}
