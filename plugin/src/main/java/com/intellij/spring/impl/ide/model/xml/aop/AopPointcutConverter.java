/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.aop;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.jam.AopModuleService;
import consulo.annotation.access.RequiredReadAction;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.function.Condition;
import consulo.xml.language.psi.XmlElement;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.ResolvingConverter;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopPointcutConverter extends ResolvingConverter<AopPointcut> {
  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<? extends AopPointcut> getVariants(ConvertContext context) {
    XmlElement element = context.getXmlElement();
    if (element == null) return Collections.emptyList();

    return AopModuleService.getAopModel(element.getModule()).getPointcuts();
  }

  @Override
  @RequiredReadAction
  public AopPointcut fromString(@Nullable String s, ConvertContext context) {
    return s == null ? null : ContainerUtil.find(getVariants(context), o -> s.equals(o.getQualifiedName().getStringValue()));
  }

  @Override
  public String toString(@Nullable AopPointcut aopPointcut, ConvertContext context) {
    return aopPointcut == null ? null : aopPointcut.getQualifiedName().getStringValue();
  }
}
