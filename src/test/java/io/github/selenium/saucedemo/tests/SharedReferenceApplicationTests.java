package io.github.selenium.saucedemo.tests;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Small Grid-backed smoke of the shared fictional claims application. */
public class SharedReferenceApplicationTests extends BaseTestCase {

  private static final CountDownLatch claimSubmitted = new CountDownLatch(1);
  private static final AtomicReference<String> memberAClaimId = new AtomicReference<>();

  @Test(description = "member claim can be reviewed while another member remains isolated")
  public void memberAClaimCanBeReviewed() {
    var wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
    getDriver().get(referenceAppUrl());
    startSession(wait, "member-a");

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("claim-form-section")));
    new Select(getDriver().findElement(By.id("policy"))).selectByIndex(0);
    getDriver().findElement(By.id("amount")).sendKeys("123.45");
    new Select(getDriver().findElement(By.id("category"))).selectByValue("auto");
    getDriver().findElement(By.cssSelector("#claim-form button[type='submit']")).click();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(By.id("notice"), "claim was submitted"));

    WebElement claimRow =
        wait.until(
            driver -> {
              var rows = driver.findElements(By.cssSelector("#claims-body tr"));
              return rows.isEmpty() ? null : rows.get(0);
            });
    String claimId = claimRow.findElement(By.cssSelector("td:first-child")).getText();
    Assert.assertTrue(
        claimId.matches("[0-9a-f-]{36}"), "the UI should expose the persisted claim UUID");
    memberAClaimId.set(claimId);
    claimSubmitted.countDown();

    getDriver().findElement(By.id("sign-out")).click();
    startSession(wait, "reviewer");
    WebElement reviewerRow = wait.until(driver -> rowWithStatus(claimId, "submitted"));
    reviewerRow.findElement(By.xpath(".//button[normalize-space()='Set under review']")).click();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(
            By.id("notice"), "moved to under review"));
    reviewerRow = wait.until(driver -> rowWithStatus(claimId, "under review"));
    reviewerRow.findElement(By.xpath(".//button[normalize-space()='Set approved']")).click();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(By.id("notice"), "moved to approved"));
    Assert.assertNotNull(wait.until(driver -> rowWithStatus(claimId, "approved")));
  }

  @Test(description = "member B cannot see member A's claim in a separate Grid session")
  public void memberBDoesNotSeeMemberAClaim() throws InterruptedException {
    Assert.assertTrue(
        claimSubmitted.await(120, TimeUnit.SECONDS), "member A must submit a claim first");
    var wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
    getDriver().get(referenceAppUrl());
    startSession(wait, "member-b");
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("claims-section")));
    Assert.assertEquals(getDriver().findElements(By.cssSelector("#claims-body tr")).size(), 0);
    Assert.assertFalse(
        getDriver().findElement(By.id("claims-body")).getText().contains(memberAClaimId.get()),
        "cross-member data must remain hidden in the UI");
  }

  private void startSession(WebDriverWait wait, String identity) {
    new Select(getDriver().findElement(By.id("identity"))).selectByValue(identity);
    getDriver().findElement(By.cssSelector("#session-form button[type='submit']")).click();
    wait.until(
        ExpectedConditions.textToBePresentInElementLocated(
            By.id("notice"), "Data is scoped to this application run"));
  }

  private WebElement rowForClaim(String claimId) {
    return getDriver().findElements(By.cssSelector("#claims-body tr")).stream()
        .filter(row -> row.findElement(By.cssSelector("td:first-child")).getText().equals(claimId))
        .findFirst()
        .orElse(null);
  }

  private WebElement rowWithStatus(String claimId, String status) {
    WebElement row = rowForClaim(claimId);
    return row != null && row.getText().contains(status) ? row : null;
  }

  private String referenceAppUrl() {
    String url = System.getProperty("app.url", "");
    Assert.assertFalse(url.isBlank(), "pass -Dapp.url for the shared reference target");
    return url.endsWith("/") ? url : url + "/";
  }
}
