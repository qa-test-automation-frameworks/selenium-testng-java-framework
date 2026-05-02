package util;

import common.driver.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Slf4j
public class WaitUtils {

    private final Duration defaultExplicitWait = Duration.ofSeconds(10);

    protected WebDriver getWebDriver() {
        return WebDriverFactory.getThreadLocalWebDriver();
    }

    public void waitForElementToBeClickable(WebElement element) {
        new WebDriverWait(getWebDriver(), defaultExplicitWait).until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForElementToDisappear(WebElement element) {
        new WebDriverWait(getWebDriver(), defaultExplicitWait).until(ExpectedConditions.invisibilityOf(element));
    }

    public void waitForPageLoad(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
