package com.example.saucedemo.framework.listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class AnnotationTransformer implements IAnnotationTransformer {

  @Override
  @SuppressWarnings("rawtypes")
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (com.example.saucedemo.framework.config.ConfigFactory.getConfig().retryEnabled()) {
      annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
  }
}
