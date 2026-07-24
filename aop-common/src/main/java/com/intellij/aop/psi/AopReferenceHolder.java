/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
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
  public AopTypeExpression getTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  public String toString() {
    return "AopReferenceHolder";
  }

  public PointcutMatchDegree accepts(@Nonnull PsiType psiType) {
    AopTypeExpression typeExpression = getTypeExpression();
    return typeExpression != null ? AopPsiTypePattern.accepts(typeExpression, psiType) : PointcutMatchDegree.FALSE;
  }

  @Nullable
  public String getTypePattern() {
    AopTypeExpression expression = getTypeExpression();
    if (expression == null) return null;

    return expression.getTypePattern();
  }

  public final Collection<AopPsiTypePattern> getPatterns() {
    AopTypeExpression expression = getTypeExpression();
    return expression == null ? Collections.<AopPsiTypePattern>emptyList() : expression.getPatterns();
  }

  @Nullable
  public PsiClass findClass() {
    AopTypeExpression expression = getTypeExpression();
    if (expression instanceof AopReferenceExpression) {
      PsiElement psiElement = ((AopReferenceExpression)expression).resolve();
      if (psiElement instanceof PsiClass) {
        return (PsiClass)psiElement;
      }
    }
    return null;
  }

  public boolean isAssignableFrom(PsiType type) {
    AopTypeExpression expression = getTypeExpression();
    return expression != null && isAssignable(expression, type);
  }

  private static boolean isAssignable(@Nonnull AopTypeExpression expression, PsiType type) {
    if (type instanceof PsiArrayType) {
      PsiArrayType arrayType = (PsiArrayType)type;
      if (expression instanceof AopArrayExpression) {
        AopArrayExpression arrayExpression = (AopArrayExpression)expression;
        return arrayExpression.isVarargs() == arrayType instanceof PsiEllipsisType && isAssignable(arrayExpression.getTypeReference(), arrayType.getComponentType());
      }
      return false;
    }
    PsiType exprType;
    if (expression instanceof AopReferenceExpression) {
      PsiElement superClass = ((AopReferenceExpression)expression).resolve();
      if (!(superClass instanceof PsiClass)) return false;

      exprType = JavaPsiFacade.getInstance(expression.getProject()).getElementFactory().createType((PsiClass)superClass);
    } else if (expression instanceof AopPrimitiveTypeExpression) {
      exprType = ((AopPrimitiveTypeExpression) expression).getPsiType();
    } else {
      return false;
    }

    if (exprType == null) return false;

    return exprType.isAssignableFrom(type);
  }

  @Nonnull
  public String getQualifiedName() {
    PsiClass psiClass = findClass();
    if (psiClass != null) {
      String qname = psiClass.getQualifiedName();
      if (qname != null) {
        return qname;
      }
    }
    return getText().trim();
  }

  public PointcutMatchDegree canBeInstance(PsiClass psiClass, boolean allowPatterns) {
    return PsiTargetExpression.canBeInstanceOf(psiClass, allowPatterns, getTypeExpression());
  }

}
