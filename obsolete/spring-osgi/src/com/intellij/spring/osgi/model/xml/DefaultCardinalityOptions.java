package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum DefaultCardinalityOptions implements NamedEnum {
  TdefaultCardinalityOptions_0__X("0..X"),
  TdefaultCardinalityOptions_1__X("1..X");

  private final String value;

  private DefaultCardinalityOptions(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
