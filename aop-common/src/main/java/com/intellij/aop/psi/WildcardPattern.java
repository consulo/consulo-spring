/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiWildcardType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class WildcardPattern extends AopPsiTypePattern{
  @Nullable
  private final AopPsiTypePattern myBound;
  private final boolean mySuper;

  public WildcardPattern(AopPsiTypePattern bound, boolean aSuper) {
    myBound = bound;
    mySuper = aSuper;
  }

  @Nullable
  public AopPsiTypePattern getBound() {
    return myBound;
  }

  public boolean isSuper() {
    return mySuper;
  }

  public boolean accepts(@Nonnull PsiType type) {
    if (type instanceof PsiWildcardType) {
      PsiWildcardType wildcardType = (PsiWildcardType)type;
      PsiType bound = wildcardType.getBound();
      if (myBound == null) return bound == null;

      if (bound == null) {
        return myBound.accepts(wildcardType.getExtendsBound());
      }

      if (mySuper) {
        return wildcardType.isSuper() && myBound.accepts(bound);
      }
      return wildcardType.isExtends() && myBound.accepts(bound);
    }
    return false;
  }

  @Nonnull
  public PointcutMatchDegree canBeAssignableFrom(@Nonnull PsiType type) {
    if (myBound == null) return PointcutMatchDegree.TRUE;
    if (mySuper) {
      if (type instanceof PsiWildcardType) {
        PsiWildcardType wildcardType = (PsiWildcardType)type;
        if (!wildcardType.isSuper()) return PointcutMatchDegree.FALSE;
        if (myBound.accepts(wildcardType.getBound())) return PointcutMatchDegree.TRUE;
      }

      if (myBound.accepts(type)) return PointcutMatchDegree.TRUE;
      return PointcutMatchDegree.FALSE;
    }
    return myBound.canBeAssignableFrom(type);
  }
}
