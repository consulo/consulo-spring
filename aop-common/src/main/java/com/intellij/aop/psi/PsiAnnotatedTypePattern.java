/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;

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

  public boolean accepts(@Nonnull PsiType type) {
    if (type instanceof PsiClassType) {
      PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null && acceptsAnnotationPattern(psiClass, myAnnotationPattern, false)) return true;
    }
    return false;
  }

  public static boolean acceptsAnnotationPattern(@Nonnull PsiModifierListOwner owner, AopPsiTypePattern annoPattern, boolean shoulBeInherited) {
    return acceptsAnnotationPattern(owner, annoPattern, shoulBeInherited, new HashSet<PsiModifierListOwner>());
  }

  private static boolean acceptsAnnotationPattern(PsiModifierListOwner owner, AopPsiTypePattern annoPattern,
                                                  boolean shoulBeInherited,
                                                  Set<PsiModifierListOwner> visited) {
    visited.add(owner);
    if (annoPattern instanceof NotPattern) {
      return !acceptsAnnotationPattern(owner, ((NotPattern)annoPattern).getInnerPattern(), shoulBeInherited);
    }

    PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      for (PsiAnnotation annotation : modifierList.getAnnotations()) {
        PsiJavaCodeReferenceElement element = annotation.getNameReferenceElement();
        if (element != null) {
          PsiElement psiElement = element.resolve();
          if (psiElement instanceof PsiClass) {
            PsiClass annoClass = (PsiClass)psiElement;
            if (annoPattern.accepts(JavaPsiFacade.getInstance(psiElement.getProject()).getElementFactory().createType(annoClass))) {
              PsiModifierList list = annoClass.getModifierList();
              return !shoulBeInherited || list != null && list.findAnnotation(CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED) != null;
            }
          }
        }
        String qualifiedName = annotation.getQualifiedName();
        if (qualifiedName != null && annoPattern.accepts(qualifiedName)) {
          return true;
        }
      }
    }
    if (owner instanceof PsiClass) {
      PsiClass superClass = ((PsiClass) owner).getSuperClass();
      return superClass != null && !visited.contains(superClass) && acceptsAnnotationPattern(superClass, annoPattern, true);
    }

    return false;
  }

  @Nonnull
  public PointcutMatchDegree canBeAssignableFrom(@Nonnull PsiType type) {
    return PointcutMatchDegree.valueOf(accepts(type));
  }
}
