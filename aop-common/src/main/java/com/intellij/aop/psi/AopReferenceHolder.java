/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopReferenceHolder extends AopElementBase implements AopReferenceTarget{

  public AopReferenceHolder(@Nonnull ASTNode node) {
    super(node);
  }

  @Nullable
  @RequiredReadAction
  public AopTypeExpression getTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  @Override
  public String toString() {
    return "AopReferenceHolder";
  }

  @Override
  @RequiredReadAction
  public PointcutMatchDegree accepts(@Nonnull PsiType psiType) {
    AopTypeExpression typeExpression = getTypeExpression();
    return typeExpression != null ? AopPsiTypePattern.accepts(typeExpression, psiType) : PointcutMatchDegree.FALSE;
  }

  @Nullable
  @Override
  @RequiredReadAction
  public String getTypePattern() {
    AopTypeExpression expression = getTypeExpression();
    if (expression == null) return null;

    return expression.getTypePattern();
  }

  @RequiredReadAction
  public final Collection<AopPsiTypePattern> getPatterns() {
    AopTypeExpression expression = getTypeExpression();
    return expression == null ? Collections.<AopPsiTypePattern>emptyList() : expression.getPatterns();
  }

  @Nullable
  @Override
  @RequiredReadAction
  public PsiClass findClass() {
    AopTypeExpression expression = getTypeExpression();
    if (expression instanceof AopReferenceExpression refExpr && refExpr.resolve() instanceof PsiClass psiClass) {
      return psiClass;
    }
    return null;
  }

  @Override
  @RequiredReadAction
  public boolean isAssignableFrom(PsiType type) {
    AopTypeExpression expression = getTypeExpression();
    return expression != null && isAssignable(expression, type);
  }

  @RequiredReadAction
  private static boolean isAssignable(@Nonnull AopTypeExpression expression, PsiType type) {
    if (type instanceof PsiArrayType arrayType) {
      if (expression instanceof AopArrayExpression) {
        AopArrayExpression arrayExpression = (AopArrayExpression)expression;
        return arrayExpression.isVarargs() == arrayType instanceof PsiEllipsisType && isAssignable(arrayExpression.getTypeReference(), arrayType.getComponentType());
      }
      return false;
    }
    PsiType exprType;
    if (expression instanceof AopReferenceExpression refExpr) {
      PsiElement superClass = refExpr.resolve();
      if (!(superClass instanceof PsiClass)) return false;

      exprType = JavaPsiFacade.getInstance(expression.getProject()).getElementFactory().createType((PsiClass)superClass);
    } else if (expression instanceof AopPrimitiveTypeExpression primitiveTypeExpr) {
      exprType = primitiveTypeExpr.getPsiType();
    } else {
      return false;
    }

    if (exprType == null) return false;

    return exprType.isAssignableFrom(type);
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public String getQualifiedName() {
    PsiClass psiClass = findClass();
    if (psiClass != null) {
      String qName = psiClass.getQualifiedName();
      if (qName != null) {
        return qName;
      }
    }
    return getText().trim();
  }

  @Override
  @RequiredReadAction
  public PointcutMatchDegree canBeInstance(PsiClass psiClass, boolean allowPatterns) {
    return PsiTargetExpression.canBeInstanceOf(psiClass, allowPatterns, getTypeExpression());
  }
}
