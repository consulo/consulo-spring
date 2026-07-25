/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopNotExpression extends AopElementBase implements PsiPointcutExpression, AopTypeExpression, AopAnnotationPattern {
  public AopNotExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "AopNotExpression";
  }

  @Nullable
  @RequiredReadAction
  public AopPatternContainer getInnerExpression() {
    return findChildByClass(AopPatternContainer.class);
  }

  @Nonnull
  @RequiredReadAction
  public PsiElement getNotToken() {
    return findChildByType(AopElementTypes.AOP_NOT);
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    AopPatternContainer expression = getInnerExpression();
    return expression instanceof PsiPointcutExpression pointcutExpr
        ? PointcutMatchDegree.not(pointcutExpr.acceptsSubject(context, member))
        : PointcutMatchDegree.FALSE;
  }

  @Nonnull
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getInnerPatterns() {
    AopPatternContainer typeExpression = getInnerExpression();
    if (typeExpression != null) return typeExpression.getPatterns();
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    return ContainerUtil.map2List(getInnerPatterns(), NotPattern::new);
  }

  @Override
  @RequiredReadAction
  public String getTypePattern() {
    if (getInnerExpression() instanceof AopTypeExpression typeExpr) {
      String pattern = typeExpr.getTypePattern();
      if (pattern == null) return null;

      return "'_:[!is(\"" + pattern + "\")]";
    }
    return null;
  }
}