/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.spring.impl.ide.model.highlighting.ReplacedMethodsInspection;
import consulo.xml.dom.ConvertContext;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;

/**
 * @see ReplacedMethodsInspection
 */
public class ReplacedMethodBeanConverter extends SpringBeanResolveConverterForDefiniteClasses {
  @NonNls public final static String METHOD_REPLACER_CLASS = "org.springframework.beans.factory.support.MethodReplacer";

  @Nonnull
  protected String[] getClassNames(ConvertContext context) {
    return new String[]{METHOD_REPLACER_CLASS};
  }
}