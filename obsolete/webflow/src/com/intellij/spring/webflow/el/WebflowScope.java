package com.intellij.spring.webflow.el;

import org.jetbrains.annotations.NonNls;

public enum WebflowScope {
  @NonNls FLOW("flowScope"),
  @NonNls VIEW("viewScope"),
  @NonNls FLASH("flashScope"),
  @NonNls CONVERSATION("conversationScope"),
  @NonNls REQUEST("requestScope"),
  @NonNls REQUEST_PATARAMETERS("requestParameters");

  private final String myName;

  private WebflowScope(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }

  public String toString() {
    return getName();
  }
}