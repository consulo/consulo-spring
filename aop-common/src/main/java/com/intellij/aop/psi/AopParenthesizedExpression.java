/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopParenthesizedExpression extends AopElementBase implements PsiPointcutExpression, AopTypeExpression, AopReferenceQualifier{
  public AopParenthesizedExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "AopParenthesizedExpression";
  }

  @Nullable
  @RequiredReadAction
  public PsiPointcutExpression getInnerPointcutExpression() {
    return findChildByClass(PsiPointcutExpression.class);
  }

  @Nullable
  @RequiredReadAction
  public AopTypeExpression getInnerTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    PsiPointcutExpression pointcutExpression = getInnerPointcutExpression();
    return pointcutExpression != null ? pointcutExpression.acceptsSubject(context, member) : PointcutMatchDegree.FALSE;
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    AopTypeExpression typeExpression = getInnerTypeExpression();
    if (typeExpression != null) return typeExpression.getPatterns();
    PsiPointcutExpression pointcutExpression = getInnerPointcutExpression();
    if (pointcutExpression != null) return pointcutExpression.getPatterns();
    return Collections.emptyList();
  }

  @Override
  @RequiredReadAction
  public String getTypePattern() {
    AopTypeExpression expression = getInnerTypeExpression();
    if (expression != null) {
      String pattern = expression.getTypePattern();
      if (pattern == null) return null;

      return "'_:[is(\"" + pattern + "\")]";
    }
    return null;
  }

  @Override
  public AopReferenceExpression.Resolvability getResolvability() {
    return AopReferenceExpression.Resolvability.NONE;
  }
}