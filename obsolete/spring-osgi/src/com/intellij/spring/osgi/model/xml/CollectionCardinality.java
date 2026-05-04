package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum CollectionCardinality implements NamedEnum {
  CollectionCardinality_0__N("0..N"),
  CollectionCardinality_1__N("1..N");

  private final String value;

  private CollectionCardinality(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
