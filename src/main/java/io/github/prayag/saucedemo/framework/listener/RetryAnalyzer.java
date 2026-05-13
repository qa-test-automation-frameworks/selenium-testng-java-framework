package io.github.prayag.saucedemo.framework.listener;

import io.github.prayag.saucedemo.framework.config.ConfigFactory;
import io.qameta.allure.Allure;
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
        log.warn(
            "Retrying test: {} after {} (Attempt {}/{})",
            result.getMethod().getMethodName(),
            failureType,
            count,
            maxRetryCount);
        Allure.label("retried", "true");
        Allure.label("retry.failure.type", failureType);
        Allure.addAttachment(
            "Retry Info",
            String.format(
                "Retrying test %s after %s. Attempt %d/%d",
                result.getMethod().getMethodName(), failureType, count, maxRetryCount));
        RetryRegistry.record(result, count, maxRetryCount);
        return true;
      }
    }
    return false;
  }
}
