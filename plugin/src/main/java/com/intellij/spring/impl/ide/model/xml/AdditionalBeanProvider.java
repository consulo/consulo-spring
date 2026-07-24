/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml;

import consulo.module.Module;
import consulo.xml.dom.DomElement;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author peter
 */
public interface AdditionalBeanProvider<T extends DomElement> {
  @Nullable
  List<CommonSpringBean> getAdditionalBeans(@Nonnull T t, @Nonnull Module module);
}
