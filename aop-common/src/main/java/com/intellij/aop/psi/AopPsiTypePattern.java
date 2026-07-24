/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiWildcardType;
import consulo.language.psi.PsiManager;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author peter
 */
public abstract class AopPsiTypePattern {
  public static final AopPsiTypePattern FALSE = new AopPsiTypePattern() {
    @Override
    public boolean accepts(@Nonnull PsiType type) {
      return false;
    }

    @Nonnull
    @Override
    public PointcutMatchDegree canBeAssignableFrom(@Nonnull PsiType type) {
      return PointcutMatchDegree.FALSE;
    }
  };
  public static final AopPsiTypePattern TRUE = new AopPsiTypePattern() {
    @Override
    public boolean accepts(@Nonnull PsiType type) {
      return true;
    }

    @Override
    public boolean accepts(@Nonnull String qualifiedName) {
      return true;
    }

    @Override
    public boolean processPackages(PsiManager manager, Predicate<PsiJavaPackage> processor) {
      return processSubPackages(JavaPsiFacade.getInstance(manager.getProject()).findPackage(""), processor);
    }
    
    @Nonnull
    @Override
    public PointcutMatchDegree canBeAssignableFrom(@Nonnull PsiType type) {
      return PointcutMatchDegree.TRUE;
    }
  };

  public abstract boolean accepts(@Nonnull PsiType type);

  public boolean accepts(@Nonnull String qualifiedName) {
    return false;
  }

  public boolean processPackages(PsiManager manager, Predicate<PsiJavaPackage> processor) {
    return true;
  }

  @Nonnull
  public PointcutMatchDegree canBeAssignableFrom(@Nonnull PsiType type) {
    return canBeAssignableFrom(type, new HashSet<>());
  }

  private PointcutMatchDegree canBeAssignableFrom(PsiType type, Set<PsiType> visited) {
    visited.add(type);
    if (accepts(type)) return PointcutMatchDegree.TRUE;
    boolean maybe = false;
    for (PsiType superType : getSuperTypes(type)) {
      if (!visited.contains(superType)) {
        PointcutMatchDegree degree = canBeAssignableFrom(superType, visited);
        if (degree == PointcutMatchDegree.TRUE) return degree;
        maybe = degree == PointcutMatchDegree.MAYBE;
      }
    }
    return maybe ? PointcutMatchDegree.MAYBE : PointcutMatchDegree.FALSE;
  }

  private static PsiType[] getSuperTypes(PsiType type) {
    if (type instanceof PsiWildcardType && ((PsiWildcardType)type).getBound() == null) {
      return PsiType.EMPTY_ARRAY;
    }
    return type.getSuperTypes();
  }

  protected static boolean processSubPackages(PsiJavaPackage pkg, Predicate<PsiJavaPackage> processor) {
    if (!processor.test(pkg)) return false;
    for (PsiJavaPackage aPackage : pkg.getSubPackages()) {
      if (!processSubPackages(aPackage, processor)) return false;
    }
    return true;
  }

  public static PointcutMatchDegree accepts(AopTypeExpression expression, PsiType psiType) {
    return accepts(expression.getPatterns(), psiType);
  }

  public static PointcutMatchDegree accepts(Collection<AopPsiTypePattern> patterns, PsiType psiType) {
    for (AopPsiTypePattern pattern : patterns) {
      if (pattern.accepts(psiType)) return PointcutMatchDegree.TRUE;
    }
    return PointcutMatchDegree.FALSE;
  }
}
