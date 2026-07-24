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
public class PsiAtAnnotationExpression extends PsiTypedPointcutExpression implements PsiAtPointcutDesignator{

  public PsiAtAnnotationExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtAnnotationExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    AopReferenceHolder pattern = getTypeReference();
    if (pattern == null) return PointcutMatchDegree.FALSE;

    return PointcutMatchDegree.valueOf(member.getModifierList().findAnnotation(context.resolve(pattern).getQualifiedName()) != null);
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}