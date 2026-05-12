package com.example.saucedemo.framework.config;

import java.io.Serial;

/** Exception thrown when framework configuration is missing or invalid. */
public class FrameworkConfigurationException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public FrameworkConfigurationException(String message) {
    super(message);
  }

  public FrameworkConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
