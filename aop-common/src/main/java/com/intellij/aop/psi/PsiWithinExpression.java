/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiWithinExpression extends PsiTypedPointcutExpression {

  public PsiWithinExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiWithinExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    AopReferenceHolder holder = getTypeReference();
    if (holder == null) return PointcutMatchDegree.FALSE;

    PsiClass psiClass = member.getContainingClass();
    PointcutMatchDegree degree = PointcutMatchDegree.FALSE;
    while (psiClass != null) {
      degree = PointcutMatchDegree.or(degree, holder.accepts(JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass)));
      psiClass = psiClass.getContainingClass();
    }
    return degree;

  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}
