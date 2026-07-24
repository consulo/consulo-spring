/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public interface AopReferenceTarget {
  @Nonnull
  String getQualifiedName();

  PointcutMatchDegree canBeInstance(PsiClass psiClass, boolean allowPatterns);

  PointcutMatchDegree accepts(PsiType actualType);

  @Nullable
  String getTypePattern();

  @Nullable
  PsiClass findClass();

  boolean isAssignableFrom(PsiType type);
}
