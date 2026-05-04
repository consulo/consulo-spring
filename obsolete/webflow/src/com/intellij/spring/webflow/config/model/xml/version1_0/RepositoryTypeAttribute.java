package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

/**
 * http://www.springframework.org/schema/webflow-config:repositoryTypeAttribute enumeration.
 */
public enum RepositoryTypeAttribute implements NamedEnum {
  @NonNls CLIENT("client"),
  @NonNls CONTINUATION("continuation"),
  @NonNls SIMPLE("simple"),
  @NonNls SINGLEKEY("singlekey");

  private final String value;

  private RepositoryTypeAttribute(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
