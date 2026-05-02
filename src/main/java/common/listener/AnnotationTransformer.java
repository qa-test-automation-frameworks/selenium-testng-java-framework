package common.listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class AnnotationTransformer implements IAnnotationTransformer {

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (common.config.ConfigFactory.getConfig().retryEnabled()) {
      annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
  }
}
