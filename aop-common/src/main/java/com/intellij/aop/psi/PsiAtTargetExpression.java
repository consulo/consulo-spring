/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiAtTargetExpression extends PsiTypedPointcutExpression implements PsiAtPointcutDesignator{

  public PsiAtTargetExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtTargetExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    return PsiAtArgsExpression.canHaveAnnotation(member.getContainingClass(), getTypeReference(), context, PointcutMatchDegree.TRUE, PointcutMatchDegree.FALSE);
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}