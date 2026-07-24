/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author peter
 */
public class AopGenericTypeExpression extends AopElementBase implements AopTypeExpression{
  public AopGenericTypeExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopParameterizedTypeExpression";
  }

  @Nonnull
  public AopTypeExpression getRawTypeReference() {
    return findNotNullChildByClass(AopTypeExpression.class);
  }

  @Nonnull
  public AopTypeParameterList getTypeParameterList() {
    return findNotNullChildByClass(AopTypeParameterList.class);
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    Collection<AopPsiTypePattern> erasurePatterns = getRawTypeReference().getPatterns();
    PsiElement[] parameters = getTypeParameterList().getParameters();
    AopPsiTypePattern[][] parameterPatterns = new AopPsiTypePattern[parameters.length][];
    for (int i = 0; i < parameters.length; i++) {
      AopTypeExpression expression = ((AopReferenceHolder)parameters[i]).getTypeExpression();
      if (expression == null) return Collections.emptyList();
      Collection<AopPsiTypePattern> patterns = expression.getPatterns();
      parameterPatterns[i] = patterns.toArray(new AopPsiTypePattern[patterns.size()]);
    }
    Set<AopPsiTypePattern> result = new HashSet<AopPsiTypePattern>();
    for (AopPsiTypePattern erasurePattern : erasurePatterns) {
      int[] indices = new int[parameterPatterns.length];
      while (true) {
        AopPsiTypePattern[] paramVariant = new AopPsiTypePattern[parameterPatterns.length];
        for (int i = 0; i < paramVariant.length; i++) {
          paramVariant[i] = parameterPatterns[i][indices[i]];
        }
        result.add(new GenericPattern(erasurePattern, paramVariant));

        int j = indices.length - 1;
        while (j >= 0 && indices[j] == parameterPatterns[j].length - 1) j--;
        if (j < 0) break;
        indices[j]++;
        while (++j < indices.length) indices[j] = 0;
      }
    }
    return result;
  }

  public String getTypePattern() {
    return "'_";
  }


}