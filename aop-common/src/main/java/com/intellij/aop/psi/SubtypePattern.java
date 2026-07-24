/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.CommonClassNames;
import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiWildcardType;
import consulo.language.psi.PsiManager;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * @author peter
 */
public class SubtypePattern extends AopPsiTypePattern {
  private final AopPsiTypePattern myBoundPattern;

  public SubtypePattern(final AopPsiTypePattern boundPattern) {
    myBoundPattern = boundPattern;
  }

  @Override
  public boolean accepts(@Nonnull PsiType type) {
    return canBeAssignableFrom(type) == PointcutMatchDegree.TRUE;
  }

  public AopPsiTypePattern getBoundPattern() {
    return myBoundPattern;
  }

  @Override
  public boolean processPackages(PsiManager manager, Predicate<PsiJavaPackage> processor) {
    return TRUE.processPackages(manager, processor);
  }

  @Nonnull
  @Override
  public final PointcutMatchDegree canBeAssignableFrom(@Nonnull final PsiType type) {
    if (type instanceof PsiWildcardType && !(myBoundPattern instanceof WildcardPattern)) {
      if (myBoundPattern instanceof PsiClassTypePattern) {
        final PsiClassTypePattern pattern = (PsiClassTypePattern)myBoundPattern;
        if (!CommonClassNames.JAVA_LANG_OBJECT.equals(pattern.getText())) {
          return PointcutMatchDegree.FALSE;
        }
      } else {
        return PointcutMatchDegree.FALSE;
      }
    }

    return myBoundPattern.canBeAssignableFrom(type);
  }
}
