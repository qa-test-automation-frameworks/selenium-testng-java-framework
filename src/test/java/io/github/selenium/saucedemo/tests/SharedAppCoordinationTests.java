package io.github.selenium.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class SharedAppCoordinationTests {

  @Test
  public void memberBSignalsReadyAndReceivesTheClaimCreatedByMemberA() throws Exception {
    CompletableFuture<String> memberBReady = new CompletableFuture<>();
    CompletableFuture<String> claimIdForMemberB = new CompletableFuture<>();

    String sessionHash = SharedAppCoordination.produce(memberBReady, () -> "member-b-session");
    claimIdForMemberB.complete("new-claim-id");

    assertThat(SharedAppCoordination.await(memberBReady, Duration.ofSeconds(1)))
        .isEqualTo(sessionHash);
    assertThat(SharedAppCoordination.await(claimIdForMemberB, Duration.ofSeconds(1)))
        .isEqualTo("new-claim-id");
  }

  @Test
  public void producerFailureReachesTheWaitingConsumerWithoutBeingWrapped() {
    CompletableFuture<String> memberBReady = new CompletableFuture<>();
    IllegalStateException failure = new IllegalStateException("controlled producer failure");

    assertThatThrownBy(
            () ->
                SharedAppCoordination.produce(
                    memberBReady,
                    () -> {
                      throw failure;
                    }))
        .isSameAs(failure);
    assertThatThrownBy(() -> SharedAppCoordination.await(memberBReady, Duration.ofSeconds(2)))
        .isSameAs(failure);
  }

  @Test
  public void memberBInitializationFailureFailsBothHandoffsImmediately() {
    CompletableFuture<String> memberBReady = new CompletableFuture<>();
    CompletableFuture<String> claimIdForMemberB = new CompletableFuture<>();
    IllegalStateException initializationFailure =
        new IllegalStateException("controlled member B initialization failure");

    try {
      SharedAppCoordination.produce(
          memberBReady,
          () -> {
            throw initializationFailure;
          });
    } catch (Exception failure) {
      SharedAppCoordination.completeExceptionallyIfPending(claimIdForMemberB, failure);
    }

    assertThatThrownBy(() -> SharedAppCoordination.await(memberBReady, Duration.ofSeconds(2)))
        .isSameAs(initializationFailure);
    assertThatThrownBy(() -> SharedAppCoordination.await(claimIdForMemberB, Duration.ofSeconds(2)))
        .isSameAs(initializationFailure);
  }

  @Test
  @SuppressWarnings("PMD.CloseResource")
  public void cancellationStopsTheWorkerAndCompletesUnresolvedHandoffs() throws Exception {
    CompletableFuture<String> memberBReady = new CompletableFuture<>();
    CompletableFuture<String> claimIdForMemberB = new CompletableFuture<>();
    CancellationException cancellation = new CancellationException("controlled scenario close");
    CountDownLatch workerStarted = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<?> worker =
        executor.submit(
            () -> {
              workerStarted.countDown();
              try {
                SharedAppCoordination.await(claimIdForMemberB, Duration.ofSeconds(20));
              } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
              } catch (Exception failure) {
                throw new IllegalStateException(failure);
              }
            });

    try {
      assertThat(workerStarted.await(1, TimeUnit.SECONDS)).isTrue();
      assertThat(worker.cancel(true)).isTrue();
    } finally {
      SharedAppCoordination.completeExceptionallyIfPending(memberBReady, cancellation);
      SharedAppCoordination.completeExceptionallyIfPending(claimIdForMemberB, cancellation);
      worker.cancel(true);
      executor.shutdownNow();
      assertThat(executor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }
    assertThat(worker.isCancelled()).isTrue();

    assertThatThrownBy(() -> SharedAppCoordination.await(memberBReady, Duration.ofSeconds(2)))
        .isSameAs(cancellation);
    assertThatThrownBy(() -> SharedAppCoordination.await(claimIdForMemberB, Duration.ofSeconds(2)))
        .isSameAs(cancellation);
  }

  @Test
  public void eachScenarioInvocationGetsFreshHandoffs() throws Exception {
    CompletableFuture<String> firstReady = new CompletableFuture<>();
    CompletableFuture<String> firstClaim = new CompletableFuture<>();
    CompletableFuture<String> secondReady = new CompletableFuture<>();
    CompletableFuture<String> secondClaim = new CompletableFuture<>();

    SharedAppCoordination.produce(firstReady, () -> "first-session");
    firstClaim.complete("first-claim");

    assertThat(secondReady).isNotSameAs(firstReady).isNotDone();
    assertThat(secondClaim).isNotSameAs(firstClaim).isNotDone();
    SharedAppCoordination.produce(secondReady, () -> "second-session");
    secondClaim.complete("second-claim");

    assertThat(SharedAppCoordination.await(firstReady, Duration.ofSeconds(1)))
        .isEqualTo("first-session");
    assertThat(SharedAppCoordination.await(firstClaim, Duration.ofSeconds(1)))
        .isEqualTo("first-claim");
    assertThat(SharedAppCoordination.await(secondReady, Duration.ofSeconds(1)))
        .isEqualTo("second-session");
    assertThat(SharedAppCoordination.await(secondClaim, Duration.ofSeconds(1)))
        .isEqualTo("second-claim");
  }
}
