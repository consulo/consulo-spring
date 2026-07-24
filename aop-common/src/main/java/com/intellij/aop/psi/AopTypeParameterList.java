/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiType;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class AopTypeParameterList extends AopAbstractList<PsiType> {

  public AopTypeParameterList(@Nonnull ASTNode node) {
    super(node);
  }

  protected PsiType getPsiType(@Nonnull PsiType psiType) {
    return psiType;
  }

  public String toString() {
    return "AopTypeParameterList";
  }

}