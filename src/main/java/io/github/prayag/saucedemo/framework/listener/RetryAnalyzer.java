package io.github.prayag.saucedemo.framework.listener;

import io.github.prayag.saucedemo.framework.config.ConfigFactory;
import io.qameta.allure.Allure;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

@Slf4j
public class RetryAnalyzer implements IRetryAnalyzer {

  private int count = 0;

  @Override
  public boolean retry(ITestResult result) {
    int maxRetryCount = ConfigFactory.getConfig().retryCount();
    if (!result.isSuccess()) {
      if (count < maxRetryCount) {
        count++;
        String failureType =
            result.getThrowable() == null ? "unknown" : result.getThrowable().getClass().getName();
        String retryReason = retryReason(result);
        log.warn(
            "Retrying test: {} after {} because {} (Attempt {}/{})",
            result.getMethod().getMethodName(),
            failureType,
            retryReason,
            count,
            maxRetryCount);
        Allure.label("retried", "true");
        Allure.label("retry.failure.type", failureType);
        Allure.label("retry.reason", retryReason);
        Allure.addAttachment(
            "Retry Info",
            String.format(
                "Retrying test %s after %s. Reason: %s. Attempt %d/%d",
                result.getMethod().getMethodName(),
                failureType,
                retryReason,
                count,
                maxRetryCount));
        RetryRegistry.record(result, count, maxRetryCount);
        return true;
      }
    }
    return false;
  }

  private String retryReason(ITestResult result) {
    Method method = result.getMethod().getConstructorOrMethod().getMethod();
    if (method == null || !method.isAnnotationPresent(Retryable.class)) {
      return "No retry reason declared";
    }
    return method.getAnnotation(Retryable.class).reason();
  }
}
