package common.pageobject;

import org.openqa.selenium.WebDriver;
import util.WaitUtils;

/** Base class for reusable page fragments that need WebDriver access and explicit waits. */
public abstract class BaseComponent {

  protected final WebDriver driver;
  protected final WaitUtils waitUtils;

  protected BaseComponent(WebDriver driver) {
    this.driver = driver;
    this.waitUtils = new WaitUtils(driver);
  }
}
