package com.example.saucedemo.framework.pageobject;

/** Contract for page objects that can wait for their own ready state. */
public interface PageLoadable<T> {

  T waitUntilLoaded();
}
