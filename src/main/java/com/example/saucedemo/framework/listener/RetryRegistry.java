package com.example.saucedemo.framework.listener;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.testng.ITestResult;

final class RetryRegistry {

  private static final Queue<String> retries = new ConcurrentLinkedQueue<>();

  private RetryRegistry() {}

  static void record(ITestResult result, int attempt, int maxAttempts) {
    retries.add(
        String.format(
            "%s.%s retried %d/%d",
            result.getTestClass().getName(),
            result.getMethod().getMethodName(),
            attempt,
            maxAttempts));
  }

  static List<String> snapshot() {
    return List.copyOf(retries);
  }

  static void clear() {
    retries.clear();
  }
}
