/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.spring.impl.ide.factories.ObjectTypeResolver;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

public class SingleObjectTypeResolver implements ObjectTypeResolver {

  private final Set<String> myTypes;

  public SingleObjectTypeResolver(String type) {
    myTypes = new HashSet<String>(StringUtil.split(type, ","));
  }

  @Nonnull
  public Set<String> getObjectType(@Nonnull CommonSpringBean bean) {
    return myTypes;
  }

  public boolean accept(@Nonnull String factoryClassName) {
    return true;
  }
}