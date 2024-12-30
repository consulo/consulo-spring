/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiAtWithinExpression extends PsiTypedPointcutExpression implements PsiAtPointcutDesignator{

  public PsiAtWithinExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtWithinExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    //todo this is only Spring-specific!
    return PsiAtArgsExpression.canHaveAnnotation(member.getContainingClass(), getTypeReference(), context, PointcutMatchDegree.TRUE, PointcutMatchDegree.FALSE);
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}