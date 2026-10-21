package io.github.selenium.saucedemo.framework.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.selenium.saucedemo.framework.config.BrowserType;
import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import io.github.selenium.saucedemo.tests.BaseTestCase;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** Deterministic controls for driver creation, disconnect cleanup, and worker isolation. */
public class WebDriverLifecycleExperimentTests {

  @AfterMethod(alwaysRun = true)
  public void cleanThreadState() {
    WebDriverFactory.cleanUpDriver();
    WebDriverFactory.resetDriverProviderForTesting();
  }

  @Test
  public void failedSessionCreationLeavesNoThreadLocalDriver() {
    WebDriverFactory.setDriverProviderForTesting(
        config -> {
          throw new WebDriverException("controlled session creation failure");
        });

    assertThatThrownBy(() -> WebDriverFactory.initThreadLocalDriver(config()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to initialize WebDriver");
    assertThat(WebDriverFactory.getThreadLocalWebDriver()).isNull();
  }

  @Test
  public void setupFailureAfterSessionCreationQuitsTheDriverAndClearsThreadLocals() {
    AtomicInteger quitCalls = new AtomicInteger();
    WebDriver driver = driver("setup-failure", true, quitCalls);
    WebDriverFactory.setDriverProviderForTesting(ignored -> driver);

    assertThatThrownBy(() -> WebDriverFactory.initThreadLocalDriver(config()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to initialize WebDriver");

    assertThat(quitCalls).hasValue(1);
    assertThat(WebDriverFactory.getThreadLocalWebDriver()).isNull();
  }

  @Test
  public void disconnectDuringTeardownStillClearsThreadLocals() {
    AtomicBoolean disconnected = new AtomicBoolean();
    AtomicInteger quitCalls = new AtomicInteger();
    WebDriver driver = driver("disconnected", false, quitCalls, disconnected);
    WebDriverFactory.setDriverProviderForTesting(ignored -> driver);
    WebDriverFactory.initThreadLocalDriver(config());
    disconnected.set(true);

    WebDriverFactory.cleanUpDriver();

    assertThat(quitCalls).hasValue(1);
    assertThat(WebDriverFactory.getThreadLocalWebDriver()).isNull();
  }

  @Test
  public void baseTestCleanupRunsAfterFailedOrUnsupportedDiagnosticProtection() {
    for (boolean javascriptSupported : List.of(true, false)) {
      AtomicInteger quitCalls = new AtomicInteger();
      AtomicInteger screenshotCalls = new AtomicInteger();
      WebDriver driver =
          diagnosticDriver(javascriptSupported, quitCalls, screenshotCalls);
      WebDriverFactory.setDriverProviderForTesting(ignored -> driver);
      WebDriverFactory.initThreadLocalDriver(config());

      new BaseTestCase() {}.afterMethod(failedTestResult());

      assertThat(quitCalls).hasValue(1);
      assertThat(screenshotCalls).hasValue(0);
      assertThat(WebDriverFactory.getThreadLocalWebDriver()).isNull();
    }
  }

  @Test(timeOut = 5_000)
  public void parallelWorkersKeepDistinctDriverSessionsAndCleanTheirOwnState() throws Exception {
    CountDownLatch initialized = new CountDownLatch(2);
    CountDownLatch release = new CountDownLatch(1);
    AtomicInteger nextWorkerId = new AtomicInteger(1);
    WebDriverFactory.setDriverProviderForTesting(
        ignored -> driver(Thread.currentThread().getName(), false, new AtomicInteger()));

    try (var executor =
        Executors.newFixedThreadPool(
            2,
            runnable -> new Thread(runnable, "worker-session-" + nextWorkerId.getAndIncrement()))) {
      List<Future<String>> futures =
          List.of(
              executor.submit(() -> captureWorkerSession(initialized, release)),
              executor.submit(() -> captureWorkerSession(initialized, release)));
      assertThat(initialized.await(2, TimeUnit.SECONDS)).isTrue();
      release.countDown();
      List<String> sessions =
          futures.stream()
              .map(
                  future -> {
                    try {
                      return future.get(2, TimeUnit.SECONDS);
                    } catch (Exception failure) {
                      throw new IllegalStateException(failure);
                    }
                  })
              .toList();
      assertThat(sessions).hasSize(2).doesNotHaveDuplicates();
      assertThat(sessions).allMatch(session -> session.startsWith("worker-session-"));
    } finally {
      release.countDown();
    }
  }

  private static String captureWorkerSession(CountDownLatch initialized, CountDownLatch release) {
    String worker = Thread.currentThread().getName();
    WebDriverFactory.initThreadLocalDriver(config());
    WebDriver session = WebDriverFactory.getThreadLocalWebDriver();
    String sessionId = session.toString();
    initialized.countDown();
    try {
      if (!release.await(2, TimeUnit.SECONDS)) {
        throw new AssertionError("Worker isolation barrier timed out");
      }
      assertThat(sessionId).isEqualTo(worker);
      assertThat(WebDriverFactory.getThreadLocalWebDriver()).isSameAs(session);
      return sessionId;
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Worker isolation probe was interrupted", interrupted);
    } finally {
      WebDriverFactory.cleanUpDriver();
      assertThat(WebDriverFactory.getThreadLocalWebDriver()).isNull();
    }
  }

  private static FrameworkConfig config() {
    return (FrameworkConfig)
        Proxy.newProxyInstance(
            FrameworkConfig.class.getClassLoader(),
            new Class<?>[] {FrameworkConfig.class},
            (proxy, method, arguments) -> {
              return switch (method.getName()) {
                case "browserType" -> BrowserType.CHROME;
                case "executionType" -> "local";
                case "headless" -> true;
                case "pageLoadTimeoutSeconds", "scriptTimeoutSeconds" -> 5;
                case "explicitWaitSeconds" -> 1;
                case "pollingIntervalMs" -> 50L;
                case "networkLogsEnabled" -> false;
                case "toString" -> "synthetic lifecycle test configuration";
                default -> defaultValue(method.getReturnType());
              };
            });
  }

  private static Object defaultValue(Class<?> type) {
    if (type == boolean.class) return false;
    if (type == int.class) return 0;
    if (type == long.class) return 0L;
    if (type == double.class) return 0D;
    if (type == float.class) return 0F;
    if (type == short.class) return (short) 0;
    if (type == byte.class) return (byte) 0;
    if (type == char.class) return '\0';
    return null;
  }

  private static WebDriver driver(
      String sessionId, boolean failDuringSetup, AtomicInteger quitCalls) {
    return driver(sessionId, failDuringSetup, quitCalls, new AtomicBoolean());
  }

  private static WebDriver driver(
      String sessionId,
      boolean failDuringSetup,
      AtomicInteger quitCalls,
      AtomicBoolean disconnected) {
    InvocationHandler handler =
        (proxy, method, arguments) -> {
          return switch (method.getName()) {
            case "toString" -> sessionId;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == arguments[0]; // NOPMD - preserve identity without recursion
            case "quit" -> {
              quitCalls.incrementAndGet();
              if (disconnected.get()) {
                throw new WebDriverException("controlled browser disconnect during quit");
              }
              yield null;
            }
            case "manage" -> {
              if (failDuringSetup) {
                throw new WebDriverException("controlled browser setup failure");
              }
              yield options();
            }
            default -> defaultValue(method.getReturnType());
          };
        };
    return (WebDriver)
        Proxy.newProxyInstance(
            WebDriver.class.getClassLoader(), new Class<?>[] {WebDriver.class}, handler);
  }

  private static WebDriver diagnosticDriver(
      boolean javascriptSupported, AtomicInteger quitCalls, AtomicInteger screenshotCalls) {
    Class<?>[] interfaces =
        javascriptSupported
            ? new Class<?>[] {WebDriver.class, JavascriptExecutor.class, TakesScreenshot.class}
            : new Class<?>[] {WebDriver.class, TakesScreenshot.class};
    InvocationHandler handler =
        (proxy, method, arguments) -> {
          return switch (method.getName()) {
            case "toString" -> "diagnostic-protection-cleanup-driver";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == arguments[0];
            case "quit" -> {
              quitCalls.incrementAndGet();
              yield null;
            }
            case "manage" -> options();
            case "getCurrentUrl" -> "https://synthetic.invalid/failure";
            case "getTitle" -> "Synthetic failure";
            case "getPageSource" -> "<main>synthetic page</main>";
            case "executeScript" ->
                throw new JavascriptException("controlled masking script failure");
            case "getScreenshotAs" -> {
              screenshotCalls.incrementAndGet();
              yield new byte[] {1, 2};
            }
            default -> defaultValue(method.getReturnType());
          };
        };
    return (WebDriver)
        Proxy.newProxyInstance(WebDriver.class.getClassLoader(), interfaces, handler);
  }

  private static ITestResult failedTestResult() {
    ITestNGMethod method =
        (ITestNGMethod)
            Proxy.newProxyInstance(
                ITestNGMethod.class.getClassLoader(),
                new Class<?>[] {ITestNGMethod.class},
                (proxy, invokedMethod, arguments) ->
                    invokedMethod.getName().equals("getMethodName")
                        ? "controlledProtectedScreenshotFailure"
                        : defaultValue(invokedMethod.getReturnType()));
    return (ITestResult)
        Proxy.newProxyInstance(
            ITestResult.class.getClassLoader(),
            new Class<?>[] {ITestResult.class},
            (proxy, methodCall, arguments) -> {
              return switch (methodCall.getName()) {
                case "getStatus" -> ITestResult.FAILURE;
                case "getMethod" -> method;
                default -> defaultValue(methodCall.getReturnType());
              };
            });
  }

  private static WebDriver.Options options() {
    InvocationHandler handler =
        (proxy, method, arguments) -> {
          return switch (method.getName()) {
            case "timeouts" -> timeouts();
            case "toString" -> "synthetic WebDriver options";
            default -> defaultValue(method.getReturnType());
          };
        };
    return (WebDriver.Options)
        Proxy.newProxyInstance(
            WebDriver.Options.class.getClassLoader(),
            new Class<?>[] {WebDriver.Options.class},
            handler);
  }

  private static WebDriver.Timeouts timeouts() {
    InvocationHandler handler =
        (proxy, method, arguments) -> {
          if (method.getName().equals("implicitlyWait")
              || method.getName().equals("pageLoadTimeout")
              || method.getName().equals("scriptTimeout")) {
            return proxy;
          }
          if (method.getName().equals("toString")) return "synthetic WebDriver timeouts";
          return defaultValue(method.getReturnType());
        };
    return (WebDriver.Timeouts)
        Proxy.newProxyInstance(
            WebDriver.Timeouts.class.getClassLoader(),
            new Class<?>[] {WebDriver.Timeouts.class},
            handler);
  }
}
