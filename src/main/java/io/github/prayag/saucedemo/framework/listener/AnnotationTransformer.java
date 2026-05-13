package io.github.prayag.saucedemo.framework.listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class AnnotationTransformer implements IAnnotationTransformer {

  @Override
  @SuppressWarnings("rawtypes") // Required by the TestNG IAnnotationTransformer API signature.
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (io.github.prayag.saucedemo.framework.config.ConfigFactory.getConfig().retryEnabled()) {
      if (testMethod != null && testMethod.isAnnotationPresent(Retryable.class)) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
      }
    }
  }
}
