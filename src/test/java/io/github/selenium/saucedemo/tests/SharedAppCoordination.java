package io.github.selenium.saucedemo.tests;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Per-invocation handoff helpers for the shared-app two-session scenario. */
final class SharedAppCoordination {

  private SharedAppCoordination() {}

  @FunctionalInterface
  interface CheckedSupplier<T> {
    T get() throws Exception;
  }

  static <T> T produce(CompletableFuture<T> signal, CheckedSupplier<T> producer) throws Exception {
    try {
      T value = producer.get();
      signal.complete(value);
      return value;
    } catch (Exception | Error failure) {
      signal.completeExceptionally(failure);
      throw failure;
    }
  }

  static <T> T await(Future<T> future, Duration timeout) throws Exception {
    try {
      return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (ExecutionException executionFailure) {
      Throwable cause = executionFailure.getCause();
      if (cause instanceof Exception exception) {
        throw exception;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw new IllegalStateException("Shared-app worker failed without a cause", cause);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw interrupted;
    } catch (TimeoutException timeoutFailure) {
      throw timeoutFailure;
    }
  }

  static void completeExceptionallyIfPending(
      CompletableFuture<?> future, Throwable failure) {
    if (!future.isDone()) {
      future.completeExceptionally(failure);
    }
  }
}
