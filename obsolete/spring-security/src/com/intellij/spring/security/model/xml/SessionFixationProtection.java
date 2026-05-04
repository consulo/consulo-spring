package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:session-fixation-protectionAttrType enumeration.
 */
public enum SessionFixationProtection implements NamedEnum {
  MIGRATE_SESSION("migrateSession"),
  NEW_SESSION("newSession"),
  NONE("none");

  private final String value;

  private SessionFixationProtection(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
