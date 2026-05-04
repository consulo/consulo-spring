package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:user-details-classAttrType enumeration.
 */
public enum UserDetailsClass implements NamedEnum {
  INET_ORG_PERSON("inetOrgPerson"),
  PERSON("person");

  private final String value;

  private UserDetailsClass(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
