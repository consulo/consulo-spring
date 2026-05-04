package com.intellij.spring.osgi.model.xml.compendium;

import com.intellij.util.xml.NamedEnum;

public enum UpdateStrategy implements NamedEnum {
  BEAN_MANAGED("bean-managed"),
  CONTAINER_MANAGED("container-managed"),
  NONE("none");

  private final String value;

  private UpdateStrategy(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
