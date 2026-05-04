package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum OrderingBasis implements NamedEnum {
  SERVICE("service"),
  SERVICE_REFERENCE("service-reference");

  private final String value;

  private OrderingBasis(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
