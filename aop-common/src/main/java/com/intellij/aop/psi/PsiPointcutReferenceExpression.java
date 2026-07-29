/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopPointcut;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiParameterList;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiPointcutReferenceExpression extends AopElementBase implements PsiPointcutExpression{
  public PsiPointcutReferenceExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Nullable
  @RequiredReadAction
  public AopReferenceExpression getReferenceExpression() {
    return findChildByClass(AopReferenceExpression.class);
  }

  @Nullable
  @RequiredReadAction
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  @Override
  public String toString() {
    return "PsiPointcutReferenceExpression";
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression != null) {
      AopPointcut pointcut = expression.resolvePointcut();
      if (pointcut != null) {
        PsiPointcutExpression pointcutExpression = pointcut.getExpression().getValue();
        if (pointcutExpression != null) {
          return pointcutExpression.acceptsSubject(createContext(context, pointcutExpression), member);
        }
      }
    }
    return PointcutMatchDegree.FALSE;
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression != null) {
      AopPointcut pointcut = expression.resolvePointcut();
      if (pointcut != null) {
        PsiPointcutExpression pointcutExpression = pointcut.getExpression().getValue();
        if (pointcutExpression != null) {
          return pointcutExpression.getPatterns();
        }
      }
    }
    return Arrays.asList(AopPsiTypePattern.FALSE);
  }

  @RequiredReadAction
  private PointcutContext createContext(PointcutContext context, PsiPointcutExpression pointcutExpression) {
    PsiMethod pointcutMethod = pointcutExpression.getContainingFile().getAopModel().getPointcutMethod();
    PointcutContext newContext = new PointcutContext(pointcutMethod);
    if (pointcutMethod != null) {
      PsiParameterList javaList = pointcutMethod.getParameterList();
      AopParameterList aopList = getParameterList();
      if (aopList != null) {
        PsiElement[] aopParameters = aopList.getParameters();
        PsiParameter[] psiParameters = javaList.getParameters();
        if (javaList.getParametersCount() == aopParameters.length) {
          for (int i = 0; i < psiParameters.length; i++) {
            if (aopParameters[i] instanceof AopReferenceHolder referenceHolder) {
              newContext.addParameter(psiParameters[i].getName(), context.resolve(referenceHolder));
            }
          }
        }
      }
    }
    return newContext;
  }
}
