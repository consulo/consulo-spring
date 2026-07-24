package com.intellij.spring.impl.ide.model.xml;

import consulo.util.lang.Comparing;
import consulo.util.collection.HashingStrategy;

import jakarta.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 */
public interface QualifierAttribute {

  @Nullable
  String getAttributeKey();

  @Nullable
  String getAttributeValue();

  HashingStrategy<QualifierAttribute> HASHING_STRATEGY = new HashingStrategy<QualifierAttribute>() {

    public int hashCode(QualifierAttribute object) {
      String key = object.getAttributeKey();
      String value = object.getAttributeValue();
      return (key == null ? 0 : key.hashCode()) + (value == null ? 0 : value.hashCode());
    }

    public boolean equals(QualifierAttribute o1, QualifierAttribute o2) {
      return Comparing.equal(o1.getAttributeKey(), o2.getAttributeKey()) && Comparing.equal(o1.getAttributeValue(), o2.getAttributeValue());
    }
  };
}
