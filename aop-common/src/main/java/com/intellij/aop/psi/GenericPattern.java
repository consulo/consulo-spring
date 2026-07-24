/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiType;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class GenericPattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myErasure;
  private final AopPsiTypePattern[] myParameters;

  public GenericPattern(AopPsiTypePattern erasure, AopPsiTypePattern... parameters) {
    myErasure = erasure;
    myParameters = parameters;
  }

  public AopPsiTypePattern getErasure() {
    return myErasure;
  }

  public AopPsiTypePattern[] getParameters() {
    return myParameters;
  }

  public boolean accepts(@Nonnull PsiType type) {
    return accepts(type, false);
  }

  private boolean accepts(@Nonnull PsiType type, boolean allowWildcardAssignability) {
    if (type instanceof PsiClassType) {
      if (!myErasure.accepts(type)) return false;

      PsiClassType classType = (PsiClassType)type;
      if (classType.isRaw()) return allowWildcardAssignability;

      PsiType[] parameters = classType.getParameters();
      if (myParameters.length != parameters.length) return false;

      for (int i = 0; i < parameters.length; i++) {
        AopPsiTypePattern paramPattern = myParameters[i];
        PsiType parameter = parameters[i];
        if (!(allowWildcardAssignability && paramPattern instanceof WildcardPattern ? paramPattern.canBeAssignableFrom(parameter) == PointcutMatchDegree.TRUE : paramPattern.accepts(parameter))) return false;
      }

      return true;
    }

    return false;
  }

  @Nonnull
  public PointcutMatchDegree canBeAssignableFrom(@Nonnull PsiType type) {
    if (accepts(type, true)) return PointcutMatchDegree.TRUE;
    boolean maybe = false;
    for (PsiType psiType : type.getSuperTypes()) {
      PointcutMatchDegree degree = canBeAssignableFrom(psiType);
      if (degree == PointcutMatchDegree.TRUE) return degree;
      maybe = degree == PointcutMatchDegree.MAYBE;
    }
    return maybe ? PointcutMatchDegree.MAYBE : PointcutMatchDegree.FALSE;
  }
  
}
