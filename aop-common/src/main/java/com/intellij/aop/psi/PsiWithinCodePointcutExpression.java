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
public class PsiWithinCodePointcutExpression extends MethodPatternPointcut {

  public PsiWithinCodePointcutExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    return PointcutMatchDegree.FALSE;
  }

  @Override
  public String toString() {
    return "PsiWithinCodePointcutExpression";
  }
}