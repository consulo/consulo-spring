package com.intellij.spring.impl.ide.model.xml;

import consulo.util.lang.Comparing;
import consulo.util.collection.HashingStrategy;

import javax.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 */
public interface QualifierAttribute {

  @Nullable
  String getAttributeKey();

  @Nullable
  String getAttributeValue();

  HashingStrategy<QualifierAttribute> HASHING_STRATEGY = new HashingStrategy<QualifierAttribute>() {

    public int hashCode(final QualifierAttribute object) {
      final String key = object.getAttributeKey();
      final String value = object.getAttributeValue();
      return (key == null ? 0 : key.hashCode()) + (value == null ? 0 : value.hashCode());
    }

    public boolean equals(final QualifierAttribute o1, final QualifierAttribute o2) {
      return Comparing.equal(o1.getAttributeKey(), o2.getAttributeKey()) && Comparing.equal(o1.getAttributeValue(), o2.getAttributeValue());
    }
  };
}
