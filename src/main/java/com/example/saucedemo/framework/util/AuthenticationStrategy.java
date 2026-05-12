package com.example.saucedemo.framework.util;

import org.openqa.selenium.WebDriver;

/** Strategy abstraction for application-specific authentication shortcuts. */
public interface AuthenticationStrategy {

  void authenticate(WebDriver driver);
}
