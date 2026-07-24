/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.util.lang.Couple;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author peter
 */
public class AopBinaryExpression extends AopElementBase implements PsiPointcutExpression, AopTypeExpression {

  public AopBinaryExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  @Nullable
  @RequiredReadAction
  public final Couple<AopPatternContainer> getOperands() {
    final AopPatternContainer[] containers = findChildrenByClass(AopPatternContainer.class);
    if (containers.length != 2) return null;
    return Couple.of(containers[0], containers[1]);
  }

  @Nonnull
  @RequiredReadAction
  public AopOperation getOperation() {
    return findChildByType(AopElementTypes.AOP_OR) != null ? AopOperation.OR : AopOperation.AND;
  }

  @Nullable
  @RequiredReadAction
  public PsiElement getOpToken() {
    final PsiElement or = findChildByType(AopElementTypes.AOP_OR);
    return or != null ? or : findChildByType(AopElementTypes.AOP_AND);
  }

  @Override
  public String toString() {
    return "AopBinaryExpression";
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    Couple<AopPatternContainer> pair = getOperands();
    if (pair == null || !(pair.first instanceof PsiPointcutExpression)) return PointcutMatchDegree.FALSE;

    final PsiPointcutExpression leftOperand = (PsiPointcutExpression)pair.first;
    final PsiPointcutExpression rightOperand = (PsiPointcutExpression)pair.second;

    final boolean and = getOperation() == AopOperation.AND;
    PointcutMatchDegree leftResult = leftOperand.acceptsSubject(context, member);
    if (and && leftResult == PointcutMatchDegree.FALSE) {
      return PointcutMatchDegree.FALSE;
    }
    else if (!and && leftResult == PointcutMatchDegree.TRUE) {
      return PointcutMatchDegree.TRUE;
    }

    PointcutMatchDegree rightResult = rightOperand.acceptsSubject(context, member);
    return and ? PointcutMatchDegree.and(leftResult, rightResult) : PointcutMatchDegree.or(leftResult, rightResult);
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    Couple<AopPatternContainer> pair = getOperands();
    if (pair == null) return Collections.emptyList();

    final Collection<AopPsiTypePattern> leftPatterns = pair.first.getPatterns();
    final Collection<AopPsiTypePattern> rightPatterns = pair.second.getPatterns();
    Collection<AopPsiTypePattern> result = new HashSet<>();
    if (getOperation() == AopOperation.AND) {
      conjunctPatterns(leftPatterns, rightPatterns, result);
    } else {
      result.addAll(leftPatterns);
      result.addAll(rightPatterns);
    }
    return result;
  }

  @Override
  @RequiredReadAction
  public String getTypePattern() {
    final AopTypeExpression[] expressions = findChildrenByClass(AopTypeExpression.class);
    if (expressions.length != 2) return null;

    final String pattern0 = expressions[0].getTypePattern();
    final String pattern1 = expressions[1].getTypePattern();
    if (pattern0 == null || pattern1 == null) return null;

    Couple<AopPatternContainer> pair = getOperands();
    if (pair == null) return null;

    return "'_:[is(\"" + pattern0 + "\") " + (getOperation() == AopOperation.AND ? "&&" : "||") + " is(\"" + pattern1 + "\")]";
  }

  public static void conjunctPatterns(final Collection<AopPsiTypePattern> leftPatterns,
                                     final Collection<AopPsiTypePattern> rightPatterns,
                                     final Collection<AopPsiTypePattern> result) {
    for (final AopPsiTypePattern leftPattern : leftPatterns) {
      for (final AopPsiTypePattern rightPattern : rightPatterns) {
        result.add(AopPsiTypePatternsUtil.conjunctPatterns(leftPattern, rightPattern));
      }
    }
  }

  public enum AopOperation {
    AND, OR
  }
}
