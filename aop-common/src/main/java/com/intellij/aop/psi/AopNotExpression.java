/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
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

  public String toString() {
    return "AopNotExpression";
  }

  @Nullable
  public AopPatternContainer getInnerExpression() {
    return findChildByClass(AopPatternContainer.class);
  }

  @Nonnull
  public PsiElement getNotToken() {
    return findChildByType(AopElementTypes.AOP_NOT);
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    AopPatternContainer expression = getInnerExpression();
    return expression instanceof PsiPointcutExpression ? PointcutMatchDegree.not(((PsiPointcutExpression)expression).acceptsSubject(context,
                                                                                                                                    member)) : PointcutMatchDegree.FALSE;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getInnerPatterns() {
    AopPatternContainer typeExpression = getInnerExpression();
    if (typeExpression != null) return typeExpression.getPatterns();
    return Collections.emptyList();
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return ContainerUtil.map2List(getInnerPatterns(), NotPattern::new);
  }

  public String getTypePattern() {
    AopPatternContainer expression = getInnerExpression();
    if (expression instanceof AopTypeExpression) {
      String pattern = ((AopTypeExpression)expression).getTypePattern();
      if (pattern == null) return null;

      return "'_:[!is(\"" + pattern + "\")]";
    }
    return null;
  }
}