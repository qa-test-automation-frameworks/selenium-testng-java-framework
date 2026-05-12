package com.example.saucedemo.app.auth;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.config.FrameworkConfig;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

/** Sauce Demo-specific cookie authentication shortcut used for non-login scenarios. */
@Slf4j
public final class SauceDemoCookieAuthentication implements AuthenticationStrategy {

  @Override
  public void authenticate(WebDriver driver) {
    log.info("Performing Sauce Demo login setup via cookie injection");
    FrameworkConfig config = ConfigFactory.getConfig();
    String url = config.appUrl();
    driver.navigate().to(url);
    driver.manage().addCookie(new Cookie("session-username", config.appUsername()));
    driver.navigate().to(URI.create(url).resolve("inventory.html").toString());
    new InventoryPage(driver).waitUntilLoaded();
  }
}
