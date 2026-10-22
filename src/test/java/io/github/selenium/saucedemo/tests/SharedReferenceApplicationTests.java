package io.github.selenium.saucedemo.tests;

import io.github.selenium.saucedemo.framework.driver.WebDriverFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** One complete two-session review journey against the isolated fictional claims application. */
public class SharedReferenceApplicationTests extends BaseTestCase {

  private static final String EVIDENCE_ATTRIBUTE = "sharedAppScenarioEvidence";
  private static final Duration BROWSER_WAIT = Duration.ofSeconds(20);
  private static final Duration HANDOFF_WAIT = Duration.ofSeconds(25);
  private static final Duration WORKER_SHUTDOWN_WAIT = Duration.ofSeconds(5);

  @Test(
      invocationCount = 2,
      description = "member claim review stays isolated across two fresh scenario invocations")
  @SuppressWarnings("PMD.CloseResource")
  public void memberClaimCanBeReviewedWhileAnotherMemberRemainsIsolated() throws Exception {
    WebDriver reviewer = getDriver();
    CompletableFuture<String> memberBReady = new CompletableFuture<>();
    CompletableFuture<String> claimIdForMemberB = new CompletableFuture<>();
    ExecutorService memberBExecutor =
        Executors.newSingleThreadExecutor(
            task -> new Thread(task, "shared-app-member-b-" + UUID.randomUUID()));
    Future<MemberBObservation> memberBTask =
        memberBExecutor.submit(
            () -> observeAsMemberB(memberBReady, claimIdForMemberB));

    try {
      reviewer.get(referenceAppUrl());
      startSession(reviewer, "member-a");
      String memberASessionHash = sessionIdentityHash(reviewer);

      String memberBSessionHash = SharedAppCoordination.await(memberBReady, HANDOFF_WAIT);
      Assert.assertNotEquals(
          memberASessionHash,
          memberBSessionHash,
          "member A and member B must own different remote sessions");

      WebDriverWait reviewerWait = new WebDriverWait(reviewer, BROWSER_WAIT);
      Set<String> existingClaimIds = visibleClaimIds(reviewer, reviewerWait);
      String claimId = submitNewClaim(reviewer, reviewerWait, existingClaimIds);
      Assert.assertTrue(
          claimIdForMemberB.complete(claimId), "the new claim ID should be delivered exactly once");

      reviewClaimAsReviewer(reviewer, reviewerWait, claimId);
      MemberBObservation memberB = SharedAppCoordination.await(memberBTask, HANDOFF_WAIT);
      Assert.assertEquals(memberB.sessionIdentityHash(), memberBSessionHash);
      Assert.assertEquals(memberB.claimIdentityHash(), sha256(claimId));
      Assert.assertTrue(memberB.cleaned(), "member B must clean up its own driver thread");

      Reporter.getCurrentTestResult().setAttribute(
          EVIDENCE_ATTRIBUTE,
          new ScenarioEvidence(
              UUID.randomUUID().toString(),
              memberASessionHash,
              memberBSessionHash,
              sha256(claimId),
              memberB.cleaned()));
    } catch (Exception | Error failure) {
      SharedAppCoordination.completeExceptionallyIfPending(memberBReady, failure);
      SharedAppCoordination.completeExceptionallyIfPending(claimIdForMemberB, failure);
      throw failure;
    } finally {
      CancellationException cancellation =
          new CancellationException("shared-app scenario is closing");
      SharedAppCoordination.completeExceptionallyIfPending(memberBReady, cancellation);
      SharedAppCoordination.completeExceptionallyIfPending(claimIdForMemberB, cancellation);
      if (!memberBTask.isDone()) {
        memberBTask.cancel(true);
      }
      memberBExecutor.shutdownNow();
      if (!memberBExecutor.awaitTermination(WORKER_SHUTDOWN_WAIT.toSeconds(), TimeUnit.SECONDS)) {
        throw new IllegalStateException("member B worker did not stop within five seconds");
      }
    }
  }

  @Override
  @AfterMethod(alwaysRun = true)
  public void afterMethod(ITestResult result) {
    try {
      super.afterMethod(result);
    } finally {
      boolean memberACleaned = WebDriverFactory.getThreadLocalWebDriver() == null;
      Object evidence = result.getAttribute(EVIDENCE_ATTRIBUTE);
      if (evidence instanceof ScenarioEvidence scenario) {
        writeSanitizedEvidence(scenario, memberACleaned);
        Assert.assertTrue(memberACleaned, "BaseTestCase must clean up member A's driver");
      }
    }
  }

  private MemberBObservation observeAsMemberB(
      CompletableFuture<String> memberBReady, CompletableFuture<String> claimIdForMemberB)
      throws Exception {
    MemberBObservation observation;
    try {
      String memberBSessionHash =
          SharedAppCoordination.produce(
              memberBReady,
              () -> {
                WebDriverFactory.initThreadLocalDriver();
                WebDriver memberB = WebDriverFactory.getThreadLocalWebDriver();
                Assert.assertNotNull(memberB, "member B must initialize its own driver");
                memberB.get(referenceAppUrl());
                startSession(memberB, "member-b");
                return sessionIdentityHash(memberB);
              });

      String claimId = SharedAppCoordination.await(claimIdForMemberB, HANDOFF_WAIT);
      WebDriver memberB = WebDriverFactory.getThreadLocalWebDriver();
      Assert.assertNotNull(memberB, "member B must keep using its own session");
      memberB.navigate().refresh();
      startSession(memberB, "member-b");
      WebDriverWait memberBWait = new WebDriverWait(memberB, BROWSER_WAIT);
      Set<String> visibleClaimsAfterRefresh = visibleClaimIds(memberB, memberBWait);
      Assert.assertFalse(
          visibleClaimsAfterRefresh.contains(claimId),
          "member B must not see the exact claim created by member A after refresh");
      observation = new MemberBObservation(memberBSessionHash, sha256(claimId), false);
    } catch (Exception | Error failure) {
      SharedAppCoordination.completeExceptionallyIfPending(memberBReady, failure);
      SharedAppCoordination.completeExceptionallyIfPending(claimIdForMemberB, failure);
      throw failure;
    } finally {
      WebDriverFactory.cleanUpDriver();
    }

    boolean memberBCleaned = WebDriverFactory.getThreadLocalWebDriver() == null;
    if (!memberBCleaned) {
      throw new AssertionError("member B thread still owns a WebDriver after cleanup");
    }
    return new MemberBObservation(
        observation.sessionIdentityHash(), observation.claimIdentityHash(), memberBCleaned);
  }

  private static void startSession(WebDriver driver, String identity) {
    new Select(driver.findElement(By.id("identity"))).selectByValue(identity);
    driver.findElement(By.cssSelector("#session-form button[type='submit']")).click();
    new WebDriverWait(driver, BROWSER_WAIT)
        .until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.id("notice"), "Data is scoped to this application run"));
  }

  private static Set<String> visibleClaimIds(WebDriver driver, WebDriverWait wait) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("claims-section")));
    return driver.findElements(By.cssSelector("#claims-body tr")).stream()
        .map(SharedReferenceApplicationTests::claimId)
        .collect(Collectors.toUnmodifiableSet());
  }

  private static String submitNewClaim(
      WebDriver driver, WebDriverWait wait, Set<String> existingClaimIds) {
    new Select(driver.findElement(By.id("policy"))).selectByIndex(0);
    driver.findElement(By.id("amount")).sendKeys("123.45");
    new Select(driver.findElement(By.id("category"))).selectByValue("auto");
    driver.findElement(By.cssSelector("#claim-form button[type='submit']")).click();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(
            By.id("notice"), "claim was submitted"));

    String claimId =
        wait.until(
            currentDriver ->
                currentDriver.findElements(By.cssSelector("#claims-body tr")).stream()
                    .map(SharedReferenceApplicationTests::claimId)
                    .filter(candidate -> !existingClaimIds.contains(candidate))
                    .findFirst()
                    .orElse(null));
    Assert.assertTrue(
        claimId.matches("[0-9a-f-]{36}"), "the UI should expose a persisted claim UUID");
    return claimId;
  }

  private static void reviewClaimAsReviewer(WebDriver driver, WebDriverWait wait, String claimId) {
    driver.findElement(By.id("sign-out")).click();
    startSession(driver, "reviewer");
    WebElement claimRow =
        wait.until(currentDriver -> rowWithStatus(currentDriver, claimId, "submitted"));
    claimRow.findElement(By.xpath(".//button[normalize-space()='Set under review']")).click();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(
            By.id("notice"), "moved to under review"));
    claimRow = wait.until(currentDriver -> rowWithStatus(currentDriver, claimId, "under review"));
    claimRow.findElement(By.xpath(".//button[normalize-space()='Set approved']")).click();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(By.id("notice"), "moved to approved"));
    Assert.assertNotNull(
        wait.until(currentDriver -> rowWithStatus(currentDriver, claimId, "approved")));
  }

  private static WebElement rowWithStatus(WebDriver driver, String claimId, String status) {
    return driver.findElements(By.cssSelector("#claims-body tr")).stream()
        .filter(row -> claimId(row).equals(claimId))
        .filter(row -> row.getText().contains(status))
        .findFirst()
        .orElse(null);
  }

  private static String claimId(WebElement row) {
    return row.findElement(By.cssSelector("td:first-child")).getText();
  }

  private static String sessionIdentityHash(WebDriver driver) {
    if (!(driver instanceof RemoteWebDriver remoteDriver)) {
      throw new AssertionError("shared-app experiment requires an identifiable remote session");
    }
    SessionId sessionId = remoteDriver.getSessionId();
    if (sessionId == null) {
      throw new AssertionError("shared-app experiment requires an identifiable remote session");
    }
    return sha256(sessionId.toString());
  }

  private static String sha256(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException("SHA-256 is unavailable", impossible);
    }
  }

  private static String referenceAppUrl() {
    String url = System.getProperty("app.url", "");
    Assert.assertFalse(url.isBlank(), "pass -Dapp.url for the shared reference target");
    return url.endsWith("/") ? url : url + "/";
  }

  private static void writeSanitizedEvidence(ScenarioEvidence evidence, boolean memberACleaned) {
    Path outputDirectory = Path.of("target", "shared-app-controls");
    Path output = outputDirectory.resolve("control-" + evidence.scenarioId() + ".json");
    String json =
        "{\"scenarioId\":\""
            + evidence.scenarioId()
            + "\",\"memberASessionSha256\":\""
            + evidence.memberASessionHash()
            + "\",\"memberBSessionSha256\":\""
            + evidence.memberBSessionHash()
            + "\",\"claimIdSha256\":\""
            + evidence.claimIdentityHash()
            + "\",\"memberACleaned\":"
            + memberACleaned
            + ",\"memberBCleaned\":"
            + evidence.memberBCleaned()
            + "}\n";
    try {
      Files.createDirectories(outputDirectory);
      Files.writeString(output, json, StandardCharsets.UTF_8);
    } catch (IOException failure) {
      throw new IllegalStateException(
          "Unable to write sanitized shared-app control record", failure);
    }
  }

  private record MemberBObservation(
      String sessionIdentityHash, String claimIdentityHash, boolean cleaned) {}

  private record ScenarioEvidence(
      String scenarioId,
      String memberASessionHash,
      String memberBSessionHash,
      String claimIdentityHash,
      boolean memberBCleaned) {}
}
