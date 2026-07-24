/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.jam.JamConverter;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteral;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.language.psi.PsiReference;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
class MethodParameterConverter extends JamConverter<PsiParameter> {
  public static final MethodParameterConverter INSTANCE = new MethodParameterConverter();

  private MethodParameterConverter() {
  }

  @Override
  public PsiParameter fromString(@Nullable String name, JamStringAttributeElement<PsiParameter> context) {
    PsiAnnotation annotation = context.getParentAnnotationElement().getPsiElement();
    PsiMethod method = (PsiMethod)annotation.getParent().getParent();
    return name != null ? AopAdviceImpl.findParameter(method, name) : null;
  }

  @Nonnull
  @Override
  public PsiReference[] createReferences(JamStringAttributeElement<PsiParameter> context) {
    PsiLiteral expression = context.getPsiLiteral();
    if (expression == null) return PsiReference.EMPTY_ARRAY;

    return new PsiReference[]{new AopAnnoParameterReference(expression)};
  }
}
