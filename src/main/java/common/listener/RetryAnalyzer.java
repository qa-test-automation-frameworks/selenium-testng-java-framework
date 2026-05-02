package common.listener;

import common.config.ConfigFactory;
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
        log.warn(
            "Retrying test: {} (Attempt {}/{})",
            result.getMethod().getMethodName(),
            count,
            maxRetryCount);
        Allure.label("retried", "true");
        Allure.addAttachment(
            "Retry Info",
            String.format(
                "Retrying test %s. Attempt %d/%d",
                result.getMethod().getMethodName(), count, maxRetryCount));
        return true;
      }
    }
    return false;
  }
}
