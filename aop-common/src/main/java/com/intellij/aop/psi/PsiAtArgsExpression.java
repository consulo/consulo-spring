/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
import consulo.language.ast.ASTNode;
import consulo.util.lang.function.PairFunction;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author peter
 */
public class PsiAtArgsExpression extends AopElementBase implements PsiPointcutExpression, PsiAtPointcutDesignator{

  public PsiAtArgsExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtArgsExpression";
  }

  @Nullable
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    if (!(member instanceof PsiMethod)) return PointcutMatchDegree.FALSE;

    AopParameterList list = getParameterList();
    if (list == null) return PointcutMatchDegree.FALSE;



    return list.matches(context, ((PsiMethod)member).getParameterList(), new PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree>() {
      public PointcutMatchDegree fun(PsiType actualType, AopReferenceTarget holder) {
        return actualType instanceof PsiClassType
               ? canHaveAnnotation(((PsiClassType)actualType).resolve(), holder, PointcutMatchDegree.TRUE, PointcutMatchDegree.MAYBE)
               : PointcutMatchDegree.FALSE;
      }
    });
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }


  public static PointcutMatchDegree canHaveAnnotation(@Nullable PsiClass psiClass, @Nullable AopReferenceHolder holder, PointcutContext context,
                                                      PointcutMatchDegree maybeTrue, PointcutMatchDegree maybeFalse) {
    if (holder == null) return PointcutMatchDegree.FALSE;
    return canHaveAnnotation(psiClass, context.resolve(holder), maybeTrue, maybeFalse);
  }

  public static PointcutMatchDegree canHaveAnnotation(@Nullable PsiClass psiClass, @Nonnull AopReferenceTarget holder,
                                                      PointcutMatchDegree maybeTrue, PointcutMatchDegree maybeFalse) {
    if (psiClass == null) return PointcutMatchDegree.FALSE;

    PsiModifierList modifierList = psiClass.getModifierList();
    if (modifierList == null) return PointcutMatchDegree.FALSE;

    String annoName = holder.getQualifiedName();
    PsiClass annoClass = holder.findClass();
    if (annoClass == null || !annoClass.isAnnotationType()) return PointcutMatchDegree.FALSE;

    PsiModifierList annoModifierList = annoClass.getModifierList();
    boolean isInheritedAnno = annoModifierList != null && annoModifierList.findAnnotation(CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED) != null;
    boolean isFinal = modifierList.hasModifierProperty(PsiModifier.FINAL);
    boolean hasAnno = modifierList.findAnnotation(annoName) != null;
    if (hasAnno) return isFinal || isInheritedAnno ? PointcutMatchDegree.TRUE : maybeTrue;
    if (isFinal) return PointcutMatchDegree.FALSE;
    if (!isInheritedAnno) return maybeFalse;

    if (psiClass.isInterface()) return maybeFalse;

    Set<PsiClass> visited = new HashSet<PsiClass>();
    visited.add(psiClass);

    while (true) {
      psiClass = psiClass.getSuperClass();
      if (visited.contains(psiClass)) break;
      visited.add(psiClass);

      if (psiClass == null) break;
      modifierList = psiClass.getModifierList();
      if (modifierList != null && modifierList.findAnnotation(annoName) != null) return PointcutMatchDegree.TRUE;
    }
    return maybeFalse;
  }
}