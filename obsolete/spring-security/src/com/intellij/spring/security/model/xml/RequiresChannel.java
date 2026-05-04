package com.intellij.spring.security.model.xml;

/**
 * http://www.springframework.org/schema/security:requires-channelAttrType enumeration.
 */
public enum RequiresChannel implements com.intellij.util.xml.NamedEnum {
  ANY("any"),
  HTTP("http"),
  HTTPS("https");

  private final String value;

  private RequiresChannel(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
