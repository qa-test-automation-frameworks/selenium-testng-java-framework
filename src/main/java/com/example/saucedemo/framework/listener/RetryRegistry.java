package com.example.saucedemo.framework.listener;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.testng.ITestResult;

final class RetryRegistry {

  private static final Queue<String> RETRIES = new ConcurrentLinkedQueue<>();

  private RetryRegistry() {}

  static void record(ITestResult result, int attempt, int maxAttempts) {
    RETRIES.add(
        String.format(
            "%s.%s retried %d/%d",
            result.getTestClass().getName(),
            result.getMethod().getMethodName(),
            attempt,
            maxAttempts));
  }

  static List<String> snapshot() {
    return List.copyOf(RETRIES);
  }

  static void clear() {
    RETRIES.clear();
  }
}
