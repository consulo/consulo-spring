/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.aop.AopIntroduction;
import com.intellij.aop.jam.AopJavaAnnotator;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiElementFactory;
import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiThisExpression extends PsiTypedPointcutExpression {

  public PsiThisExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiThisExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    AopReferenceHolder reference = getTypeReference();
    if (reference == null) return PointcutMatchDegree.FALSE;

    PsiClass containingClass = member.getContainingClass();
    if (containingClass == null) return PointcutMatchDegree.FALSE;

    AopReferenceTarget target = context.resolve(reference);

    PointcutMatchDegree baseResult = target.canBeInstance(containingClass, false);
    if (baseResult == PointcutMatchDegree.TRUE) {
      return PointcutMatchDegree.TRUE;
    }

    PsiElementFactory elementFactory = JavaPsiFacade.getInstance(member.getProject()).getElementFactory();
    for (AopIntroduction introduction : AopJavaAnnotator.getBoundIntroductions(containingClass)) {
      PsiClass introIntf = introduction.getImplementInterface().getValue();
      if (introIntf != null && target.accepts(elementFactory.createType(introIntf)) == PointcutMatchDegree.TRUE) {
        return PointcutMatchDegree.TRUE;
      }
    }

    return baseResult;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}
