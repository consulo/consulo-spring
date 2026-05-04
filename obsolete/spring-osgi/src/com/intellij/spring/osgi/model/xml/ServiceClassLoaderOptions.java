package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum ServiceClassLoaderOptions implements NamedEnum {
  SERVICE_PROVIDER("service-provider"),
  UNMANAGED("unmanaged");

  private final String value;

  private ServiceClassLoaderOptions(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
