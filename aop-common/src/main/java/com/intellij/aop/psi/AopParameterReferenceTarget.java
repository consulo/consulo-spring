/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class AopParameterReferenceTarget implements AopReferenceTarget {
  private final PsiParameter myParameter;

  public AopParameterReferenceTarget(PsiParameter parameter) {
    myParameter = parameter;
  }

  @Nonnull
  public String getQualifiedName() {
    return myParameter.getType().getCanonicalText();
  }

  public PointcutMatchDegree canBeInstance(PsiClass psiClass, boolean allowPatterns) {
    PsiType type = myParameter.getType();
    if (type instanceof PsiClassType) {
      PsiClass superClass = ((PsiClassType)type).resolve();
      if (superClass != null) {
        PointcutMatchDegree degree = PsiTargetExpression.canBeInstanceOf(allowPatterns, superClass, psiClass);
        if (degree != null) {
          return degree;
        }
      }
    }
    return PointcutMatchDegree.FALSE;
  }

  public PointcutMatchDegree accepts(PsiType actualType) {
    return PointcutMatchDegree.valueOf(actualType.equals(myParameter.getType()));
  }

  public String getTypePattern() {
    throw new UnsupportedOperationException("Method getTypePattern is not yet implemented in " + getClass().getName());
  }

  @Nullable
  public PsiClass findClass() {
    PsiType type = myParameter.getType();
    return type instanceof PsiClassType ? ((PsiClassType)type).resolve() : null;
  }

  public boolean isAssignableFrom(PsiType type) {
    return myParameter.getType().isAssignableFrom(type);
  }
}
