package com.intellij.spring.security.model.xml;

/**
 * http://www.springframework.org/schema/security:secured-annotationsAttrType enumeration.
 */
public enum SecuredAnnotations implements com.intellij.util.xml.NamedEnum {
  DISABLED("disabled"),
  ENABLED("enabled");

  private final String value;

  private SecuredAnnotations(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
