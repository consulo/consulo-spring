/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.aop;

import com.intellij.aop.jam.AopAdviceImpl;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.aop.localize.AopLocalize;
import consulo.localize.LocalizeValue;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.ResolvingConverter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AdviceParameterConverter extends ResolvingConverter<PsiParameter> {
  @Nonnull
  @Override
  public Collection<? extends PsiParameter> getVariants(final ConvertContext context) {
    final BasicAdvice advice = (BasicAdvice)context.getInvocationElement().getParent();
    final PsiMethod method = advice.getMethod().getValue();
    if (method != null) {
      return Arrays.asList(method.getParameterList().getParameters());

    }
    return Collections.emptyList();
  }

  @Override
  public LocalizeValue buildUnresolvedMessage(@Nullable String s, ConvertContext context) {
    return AopLocalize.errorCannotResolveParameter(s);
  }

  @Override
  public PsiParameter fromString(@Nullable String s, ConvertContext context) {
    return s == null ? null : AopAdviceImpl.findParameter(((BasicAdvice)context.getInvocationElement().getParent()).getMethod().getValue(), s);
  }

  @Override
  public String toString(@Nullable PsiParameter psiParameter, final ConvertContext context) {
    return psiParameter == null ? null : psiParameter.getName();
  }
}
