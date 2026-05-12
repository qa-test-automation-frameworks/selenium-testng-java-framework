package com.example.saucedemo.framework.config;

public record TestRunContext(FrameworkConfig config) {

  public static TestRunContext load() {
    return new TestRunContext(ConfigFactory.getConfig());
  }
}
