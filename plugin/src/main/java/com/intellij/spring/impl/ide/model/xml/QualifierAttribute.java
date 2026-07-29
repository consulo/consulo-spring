package com.intellij.spring.impl.ide.model.xml;

import consulo.util.collection.HashingStrategy;
import consulo.util.lang.Comparing;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * @author Dmitry Avdeev
 */
public interface QualifierAttribute {
  @Nullable
  String getAttributeKey();

  @Nullable
  String getAttributeValue();

  HashingStrategy<QualifierAttribute> HASHING_STRATEGY = new HashingStrategy<>() {
    @Override
    public int hashCode(QualifierAttribute object) {
      String key = object.getAttributeKey();
      String value = object.getAttributeValue();
      return (key == null ? 0 : key.hashCode()) + (value == null ? 0 : value.hashCode());
    }

    @Override
    public boolean equals(QualifierAttribute o1, QualifierAttribute o2) {
      return Objects.equals(o1.getAttributeKey(), o2.getAttributeKey()) && Objects.equals(o1.getAttributeValue(), o2.getAttributeValue());
    }
  };
}
