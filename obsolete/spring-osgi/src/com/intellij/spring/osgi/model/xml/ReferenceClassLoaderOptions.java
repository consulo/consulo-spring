package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum ReferenceClassLoaderOptions implements NamedEnum {
  CLIENT("client"),
  SERVICE_PROVIDER("service-provider"),
  UNMANAGED("unmanaged");

  private final String value;

  private ReferenceClassLoaderOptions(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
