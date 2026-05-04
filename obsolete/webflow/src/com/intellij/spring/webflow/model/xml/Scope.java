package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.NamedEnum;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;

@ModelVersion(WebflowVersion.Webflow_1_0)
public enum Scope implements NamedEnum {
  CONVERSATION("conversation"),
  DEFAULT("default"),
  FLASH("flash"),
  FLOW("flow"),
  REQUEST("request");

  private final String value;

  private Scope(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
