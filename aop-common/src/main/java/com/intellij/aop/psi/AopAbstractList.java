/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiType;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.NotNullLazyValue;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;

import java.util.function.BiFunction;

/**
 * @author peter
 */
public abstract class AopAbstractList<T> extends AopElementBase {
  private static final TokenSet LIST_ELEMENT_TYPES = TokenSet.create(AopElementTypes.AOP_DOT_DOT, AopElementTypes.AOP_REFERENCE_HOLDER);
  private final NotNullLazyValue<ArrayTailCondition<T>> myMatcher;

  public AopAbstractList(@Nonnull final ASTNode node) {
    super(node);
    myMatcher = new NotNullLazyValue<>() {
      @Nonnull
      @Override
      @RequiredReadAction
      protected ArrayTailCondition<T> compute() {
        return createMatcher(getParameters(), 0);
      }
    };
  }

  protected abstract PsiType getPsiType(@Nonnull T t);

  @RequiredReadAction
  public final PsiElement[] getParameters() {
    return findChildrenByType(LIST_ELEMENT_TYPES, PsiElement.class);
  }

  public final PointcutMatchDegree accepts(
    PointcutContext context,
    T[] list,
    BiFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher
  ) {
    return myMatcher.getValue().value(context, matcher, list, 0);
  }

  private ArrayTailCondition<T> createMatcher(final PsiElement[] aopParameters, final int matchStart) {
    if (matchStart >= aopParameters.length) return (context, matcher, array, start) -> PointcutMatchDegree.valueOf(start >= array.length);
    final PsiElement psiElement = aopParameters[matchStart];
    final ArrayTailCondition<T> tail = createMatcher(aopParameters, matchStart + 1);
    if (psiElement instanceof AopReferenceHolder pattern) {
      return (context, matcher, array, start) -> {
        if (start >= array.length) return PointcutMatchDegree.FALSE;
        return PointcutMatchDegree.and(matcher.apply(getPsiType(array[start]), context.resolve(pattern)),
                                       tail.value(context, matcher, array, start + 1));
      };
    }
    return (context, matcher, array, start) -> {
      PointcutMatchDegree result = PointcutMatchDegree.FALSE;
      for (int i = start; i < array.length; i++) {
        result = PointcutMatchDegree.or(result, tail.value(context, matcher, array, i));
      }
      return PointcutMatchDegree.or(result, PointcutMatchDegree.valueOf(matchStart == aopParameters.length - 1));
    };
  }

  private interface ArrayTailCondition<T> {
    ArrayTailCondition TRUE = (context, matcher, array, start) -> PointcutMatchDegree.TRUE;

    PointcutMatchDegree value(
      PointcutContext context,
      BiFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher,
      T[] array,
      int start
    );
  }
}
