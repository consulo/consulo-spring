/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiArrayType;
import com.intellij.java.language.psi.PsiEllipsisType;
import com.intellij.java.language.psi.PsiType;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class ArrayPattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myComponentPattern;
  private final boolean myVarargs;

  public ArrayPattern(AopPsiTypePattern componentPattern, boolean isVarargs) {
    myComponentPattern = componentPattern;
    myVarargs = isVarargs;
  }

  public boolean isVarargs() {
    return myVarargs;
  }

  public boolean accepts(@Nonnull PsiType type) {
    return type instanceof PsiArrayType && myVarargs == type instanceof PsiEllipsisType &&
           myComponentPattern.accepts(((PsiArrayType)type).getComponentType());
  }
}
