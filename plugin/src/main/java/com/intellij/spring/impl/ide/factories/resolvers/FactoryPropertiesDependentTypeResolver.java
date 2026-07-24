/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FactoryPropertiesDependentTypeResolver extends AbstractTypeResolver {
  private final List<String> myPropertyNames;

  public FactoryPropertiesDependentTypeResolver(List<String> propertyNames) {
    myPropertyNames = propertyNames;
  }

  @Nonnull
  public Set<String> getObjectType(@Nonnull CommonSpringBean context) {
    for (String propertyName : myPropertyNames) {
      String propertyValue = getPropertyValue(context, propertyName);
      if (propertyValue != null) {
        return Collections.singleton(propertyValue);
      }
    }
    return Collections.emptySet();
  }

  public boolean accept(@Nonnull String factoryClassName) {
    return true;
  }
}