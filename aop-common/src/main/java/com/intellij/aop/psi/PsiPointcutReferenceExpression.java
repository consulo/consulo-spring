/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopPointcut;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiParameterList;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiPointcutReferenceExpression extends AopElementBase implements PsiPointcutExpression{
  public PsiPointcutReferenceExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  @Nullable
  public AopReferenceExpression getReferenceExpression() {
    return findChildByClass(AopReferenceExpression.class);
  }

  @Nullable
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  public String toString() {
    return "PsiPointcutReferenceExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression != null) {
      final AopPointcut pointcut = expression.resolvePointcut();
      if (pointcut != null) {
        final PsiPointcutExpression pointcutExpression = pointcut.getExpression().getValue();
        if (pointcutExpression != null) {
          return pointcutExpression.acceptsSubject(createContext(context, pointcutExpression), member);
        }
      }
    }
    return PointcutMatchDegree.FALSE;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression != null) {
      final AopPointcut pointcut = expression.resolvePointcut();
      if (pointcut != null) {
        final PsiPointcutExpression pointcutExpression = pointcut.getExpression().getValue();
        if (pointcutExpression != null) {
          return pointcutExpression.getPatterns();
        }
      }
    }
    return Arrays.asList(AopPsiTypePattern.FALSE);
  }

  private PointcutContext createContext(final PointcutContext context, final PsiPointcutExpression pointcutExpression) {
    final PsiMethod pointcutMethod = pointcutExpression.getContainingFile().getAopModel().getPointcutMethod();
    final PointcutContext newContext = new PointcutContext(pointcutMethod);
    if (pointcutMethod != null) {
      final PsiParameterList javaList = pointcutMethod.getParameterList();
      final AopParameterList aopList = getParameterList();
      if (aopList != null) {
        final PsiElement[] aopParameters = aopList.getParameters();
        final PsiParameter[] psiParameters = javaList.getParameters();
        if (javaList.getParametersCount() == aopParameters.length) {
          for (int i = 0; i < psiParameters.length; i++) {
            final PsiElement aopParameter = aopParameters[i];
            if (aopParameter instanceof AopReferenceHolder) {
              newContext.addParameter(psiParameters[i].getName(), context.resolve((AopReferenceHolder)aopParameter));
            }
          }
        }
      }
    }
    return newContext;
  }
}
