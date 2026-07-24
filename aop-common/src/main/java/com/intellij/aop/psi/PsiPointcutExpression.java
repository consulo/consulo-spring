/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public interface PsiPointcutExpression extends AopPatternContainer {
  @Nonnull
  PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member);

  @Nonnull
  AopPointcutExpressionFile getContainingFile();
}
