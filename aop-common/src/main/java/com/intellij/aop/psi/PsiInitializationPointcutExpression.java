/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class PsiInitializationPointcutExpression extends MethodPatternPointcut{
  private final boolean myPre;

  public PsiInitializationPointcutExpression(@Nonnull ASTNode node, boolean isPre) {
    super(node);
    myPre = isPre;
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    return PointcutMatchDegree.FALSE;
  }

  @Override
  public String toString() {
    return "PsiInitializationPointcutExpression";
  }
}