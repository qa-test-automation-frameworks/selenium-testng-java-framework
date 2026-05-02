import common.config.ConfigFactory;
import common.driver.WebDriverFactory;
import common.pageobject.LoginPO;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
public class BaseTestCase  implements ITestListener {

    protected void login() {
        new LoginPO().login(
                ConfigFactory.getConfig().appUrl(),
                ConfigFactory.getConfig().appUsername(),
                ConfigFactory.getConfig().appPassword()
        );
    }

    protected void quitWebDriver() {
        try {
            WebDriverFactory.cleanUpDriver();
        }  catch (WebDriverException e) {
            log.error(e.getMessage());
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        WebDriverFactory.initThreadLocalDriver();
        login();
    }

    @Override
    public void onTestStart(ITestResult result) {
        setTestCaseNameInAllure(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Allure.addAttachment("Test Failure", result.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        quitWebDriver();
    }

    private static void setTestCaseNameInAllure(final ITestResult result) {
        final var method = result.getMethod().getConstructorOrMethod().getMethod();
        final var testAnnotation = method.getAnnotation(Test.class);
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setName(testAnnotation.testName()));
    }

    @AfterSuite
    public void afterSuite() {
    }
}
