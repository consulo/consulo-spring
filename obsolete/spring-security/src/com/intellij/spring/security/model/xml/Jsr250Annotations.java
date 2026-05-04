package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:jsr250-annotationsAttrType enumeration.
 */
public enum   Jsr250Annotations implements NamedEnum {
  DISABLED("disabled"),
  ENABLED("enabled");

  private final String value;

  private Jsr250Annotations(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
