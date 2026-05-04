package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:create-sessionAttrType enumeration.
 */
public enum CreateSession implements NamedEnum {
  ALWAYS("always"),
  IF_REQUIRED("ifRequired"),
  NEVER("never");

  private final String value;

  private CreateSession(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
