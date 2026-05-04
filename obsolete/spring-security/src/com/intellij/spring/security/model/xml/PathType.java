package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:path-typeAttrType enumeration.
 */
public enum PathType implements NamedEnum {
  ANT("ant"),
  REGEX("regex");

  private final String value;

  private PathType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
