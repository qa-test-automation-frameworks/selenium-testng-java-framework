package io.github.prayag.saucedemo.framework.ui;

/** Contract for page objects that can wait for their own ready state. */
public interface PageLoadable<T> {

  T waitUntilLoaded();
}
