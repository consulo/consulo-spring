/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.java.language.psi.util.MethodSignatureUtil;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiTargetExpression extends PsiTypedPointcutExpression {

  public PsiTargetExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiTargetExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    PsiClass psiClass = member.getContainingClass();
    AopReferenceHolder baseClassPattern = getTypeReference();
    if (baseClassPattern == null || psiClass == null) return PointcutMatchDegree.FALSE;

    PsiClass myClass = context.resolve(baseClassPattern).findClass();
    if (myClass == null || !InheritanceUtil.isInheritorOrSelf(myClass, psiClass, true)) return PointcutMatchDegree.FALSE;

    if (member instanceof PsiMethod) {
      PsiMethod method = (PsiMethod)member;
      if (MethodSignatureUtil.findMethodBySuperMethod(myClass, method, true) == method) {
        return PointcutMatchDegree.TRUE;
      }
    }
    return PointcutMatchDegree.FALSE;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }

  public static PointcutMatchDegree canBeInstanceOf(PsiClass psiClass, boolean allowPatterns, @Nullable AopTypeExpression typeExpression) {
    if (typeExpression == null) return PointcutMatchDegree.FALSE;

    if (typeExpression instanceof AopReferenceExpression) {
      PsiElement psiElement = ((AopReferenceExpression)typeExpression).resolve();
      if (psiElement instanceof PsiClass) {
        PointcutMatchDegree degree = canBeInstanceOf(allowPatterns, (PsiClass)psiElement, psiClass);
        if (degree != null) return degree;
      }
      if (!allowPatterns) return PointcutMatchDegree.FALSE;
    }
    if (typeExpression instanceof AopSubtypeExpression && !allowPatterns) return PointcutMatchDegree.FALSE;


    Collection<AopPsiTypePattern> typePatterns = typeExpression.getPatterns();
    boolean maybe = false;
    for (AopPsiTypePattern typePattern : typePatterns) {
      PointcutMatchDegree degree = typePattern.canBeAssignableFrom(
        JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass));
      if (degree == PointcutMatchDegree.TRUE) return PointcutMatchDegree.TRUE;
      if (degree == PointcutMatchDegree.MAYBE) maybe = true;
    }
    return maybe ? PointcutMatchDegree.MAYBE : PointcutMatchDegree.FALSE;
  }

  @Nullable
  public static PointcutMatchDegree canBeInstanceOf(boolean allowPatterns, PsiClass superClass, PsiClass subClass) {
    if (subClass.getManager().areElementsEquivalent(subClass, superClass) || !allowPatterns && subClass.isInheritor(superClass, true)) {
      return PointcutMatchDegree.TRUE;
    }
    if (subClass.hasModifierProperty(PsiModifier.FINAL)) return PointcutMatchDegree.FALSE;
    if (!allowPatterns && superClass.isInterface()) return PointcutMatchDegree.MAYBE;
    return null;
  }
}
