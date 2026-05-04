package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum SingleReferenceCardinality implements NamedEnum {
  TsingleReferenceCardinality_0__1("0..1"),
  TsingleReferenceCardinality_1__1("1..1");

  private final String value;

  private SingleReferenceCardinality(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
