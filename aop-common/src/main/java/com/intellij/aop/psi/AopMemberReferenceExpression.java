/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class AopMemberReferenceExpression extends AopElementBase {
  public AopMemberReferenceExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Nullable
  @RequiredReadAction
  public AopReferenceExpression getReferenceExpression() {
    AopTypeExpression aopTypeExpression = getTypeExpression();
    if (aopTypeExpression instanceof AopReferenceExpression refExpr) {
      return refExpr;
    }
    if (aopTypeExpression instanceof AopAnnotatedTypeExpression annotatedTypeExpr
        && annotatedTypeExpr.getTypeExpression() instanceof AopReferenceExpression refExpr) {
      return refExpr;
    }
    return null;
  }

  @Nullable
  @RequiredReadAction
  public AopTypeExpression getTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  @RequiredReadAction
  public Collection<AopPsiTypePattern> getQualifierPatterns() {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression == null) return Arrays.asList(AopPsiTypePattern.TRUE);

    AopReferenceQualifier qualifier = expression.getGeneralizedQualifier();
    if (qualifier == null) return Arrays.asList(AopPsiTypePattern.TRUE);

    AopTypeExpression typeExpression = getTypeExpression();

    return typeExpression instanceof AopAnnotatedTypeExpression annotatedTypeExpr ? annotatedTypeExpr.getQualifierPatterns() : qualifier
      .getPatterns();
  }

  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    AopReferenceExpression expression = getReferenceExpression();
    Collection<AopPsiTypePattern> patterns = getQualifierPatterns();
    if (expression != null && expression.isDoubleDot()) {
      return ContainerUtil.map(patterns, it -> new ConcatenationPattern(it, AopPsiTypePattern.TRUE, true));
    }
    return patterns;
  }

  @Override
  public String toString() {
    return "AopMemberReferenceExpression";
  }
}