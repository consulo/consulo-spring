/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author peter
 */
public class PsiAnnotatedTypePattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myAnnotationPattern;

  public PsiAnnotatedTypePattern(AopPsiTypePattern annotationPattern) {
    myAnnotationPattern = annotationPattern;
  }

  public AopPsiTypePattern getAnnotationPattern() {
    return myAnnotationPattern;
  }

  @Override
  @RequiredReadAction
  public boolean accepts(@Nonnull PsiType type) {
    if (type instanceof PsiClassType classType) {
      PsiClass psiClass = classType.resolve();
      if (psiClass != null && acceptsAnnotationPattern(psiClass, myAnnotationPattern, false)) return true;
    }
    return false;
  }

  @RequiredReadAction
  public static boolean acceptsAnnotationPattern(@Nonnull PsiModifierListOwner owner, AopPsiTypePattern annoPattern, boolean shouldBeInherited) {
    return acceptsAnnotationPattern(owner, annoPattern, shouldBeInherited, new HashSet<>());
  }

  @RequiredReadAction
  private static boolean acceptsAnnotationPattern(
    PsiModifierListOwner owner,
    AopPsiTypePattern annoPattern,
    boolean shouldBeInherited,
    Set<PsiModifierListOwner> visited
  ) {
    visited.add(owner);
    if (annoPattern instanceof NotPattern notPattern) {
      return !acceptsAnnotationPattern(owner, notPattern.getInnerPattern(), shouldBeInherited);
    }

    PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      for (PsiAnnotation annotation : modifierList.getAnnotations()) {
        PsiJavaCodeReferenceElement element = annotation.getNameReferenceElement();
        if (element != null
          && element.resolve() instanceof PsiClass annoClass
          && annoPattern.accepts(JavaPsiFacade.getInstance(annoClass.getProject()).getElementFactory().createType(annoClass))) {
          PsiModifierList list = annoClass.getModifierList();
          return !shouldBeInherited || list != null && list.findAnnotation(CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED) != null;
        }
        String qualifiedName = annotation.getQualifiedName();
        if (qualifiedName != null && annoPattern.accepts(qualifiedName)) {
          return true;
        }
      }
    }
    if (owner instanceof PsiClass psiClass) {
      PsiClass superClass = psiClass.getSuperClass();
      return superClass != null && !visited.contains(superClass) && acceptsAnnotationPattern(superClass, annoPattern, true);
    }

    return false;
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public PointcutMatchDegree canBeAssignableFrom(@Nonnull PsiType type) {
    return PointcutMatchDegree.valueOf(accepts(type));
  }
}
