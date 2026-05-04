package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:filtersAttrType enumeration.
 */
public enum Filters implements NamedEnum {
  NONE("none");

  private final String value;

  private Filters(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
