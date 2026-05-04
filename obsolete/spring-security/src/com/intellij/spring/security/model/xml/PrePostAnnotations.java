package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:pre-post-annotationsAttrType enumeration.
 */
public enum PrePostAnnotations implements NamedEnum {
  DISABLED("disabled"),
  ENABLED("enabled");

  private final String value;

  private PrePostAnnotations(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
