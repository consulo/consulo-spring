/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

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
  public AopReferenceExpression getReferenceExpression() {
    AopTypeExpression aopTypeExpression = getTypeExpression();
    if (aopTypeExpression instanceof AopReferenceExpression) {
      return (AopReferenceExpression)aopTypeExpression;
    }
    if (aopTypeExpression instanceof AopAnnotatedTypeExpression) {
      AopTypeExpression expression1 = ((AopAnnotatedTypeExpression)aopTypeExpression).getTypeExpression();
      if (expression1 instanceof AopReferenceExpression) {
        return (AopReferenceExpression)expression1;
      }
    }
    return null;
  }

  @Nullable
  public AopTypeExpression getTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  public Collection<AopPsiTypePattern> getQualifierPatterns() {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression == null) return Arrays.asList(AopPsiTypePattern.TRUE);

    AopReferenceQualifier qualifier = expression.getGeneralizedQualifier();
    if (qualifier == null) return Arrays.asList(AopPsiTypePattern.TRUE);

    AopTypeExpression typeExpression = getTypeExpression();

    return typeExpression instanceof AopAnnotatedTypeExpression ? ((AopAnnotatedTypeExpression)typeExpression).getQualifierPatterns() : qualifier
      .getPatterns();
  }

  public Collection<AopPsiTypePattern> getPatterns() {
    AopReferenceExpression expression = getReferenceExpression();
    Collection<AopPsiTypePattern> patterns = getQualifierPatterns();
    if (expression != null && expression.isDoubleDot()) {
      return ContainerUtil.map(patterns, it -> new ConcatenationPattern(it, AopPsiTypePattern.TRUE, true));
    }
    return patterns;
  }

  public String toString() {
    return "AopMemberReferenceExpression";
  }

}