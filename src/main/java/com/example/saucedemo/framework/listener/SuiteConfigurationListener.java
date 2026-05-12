package com.example.saucedemo.framework.listener;

import com.example.saucedemo.framework.config.ConfigFactory;
import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

public class SuiteConfigurationListener implements IAlterSuiteListener {

  @Override
  public void alter(List<XmlSuite> suites) {
    int threadCount = ConfigFactory.getConfig().threadCount();
    suites.forEach(suite -> suite.setThreadCount(threadCount));
  }
}
