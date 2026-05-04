package com.intellij.spring.webflow.model;

import org.jetbrains.annotations.NotNull;

public enum WebflowVersion implements DomModelVersion {
  Webflow_1_0("1.0"),
  Webflow_2_0("2.0"),
  Webflow_2_0_3("2.0.3");

  private final String myName;

  private WebflowVersion(String name) {
    myName = name;
  }

  @Override
  public String toString() {
    return myName;
  }

  @NotNull
  public String getVersion() {
    return myName;
  }
}
