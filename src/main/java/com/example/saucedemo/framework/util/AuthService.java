package com.example.saucedemo.framework.util;

import com.example.saucedemo.framework.config.ConfigFactory;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

@Slf4j
public final class AuthService {

  private AuthService() {}

  public static void injectLoginCookieAndNavigate(WebDriver driver) {
    log.info("Performing global login for test setup via cookie injection");
    String url = ConfigFactory.getConfig().appUrl();
    driver.navigate().to(url);
    Cookie loginCookie = new Cookie("session-username", ConfigFactory.getConfig().appUsername());
    driver.manage().addCookie(loginCookie);
    driver.navigate().to(URI.create(url).resolve("inventory.html").toString());
    new WaitUtils(driver).waitForPageLoad();
  }
}
