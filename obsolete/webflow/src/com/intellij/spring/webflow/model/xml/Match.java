package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.NamedEnum;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;

@ModelVersion(WebflowVersion.Webflow_2_0)
public enum Match implements NamedEnum {
  ALL("all"),
  ANY("any");

  private final String value;

  private Match(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
