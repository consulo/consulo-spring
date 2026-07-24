package com.intellij.spring.impl.ide.model.xml.jms;

import consulo.xml.dom.NamedEnum;

public enum Acknowledge implements NamedEnum {
  AUTO("auto"),
  CLIENT("client"),
  DUPS_OK("dups-ok"),
  TRANSACTED("transacted");

  private final String value;

  private Acknowledge(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }
}
