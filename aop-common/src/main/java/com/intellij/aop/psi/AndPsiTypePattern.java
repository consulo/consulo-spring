/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.java.language.psi.PsiType;
import consulo.application.util.function.CommonProcessors;
import consulo.language.psi.PsiManager;
import consulo.util.lang.ref.SimpleReference;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author peter
*/
public class AndPsiTypePattern extends AopPsiTypePattern {
  private final AopPsiTypePattern[] myPatterns;

  public AndPsiTypePattern(final AopPsiTypePattern... patterns) {
    myPatterns = patterns;
  }

  public AopPsiTypePattern[] getPatterns() {
    return myPatterns;
  }

  @Override
  public boolean accepts(@Nonnull final PsiType type) {
    for (final AopPsiTypePattern typePattern : myPatterns) {
      if (!typePattern.accepts(type)) return false;
    }
    return true;
  }

  @Override
  public boolean processPackages(PsiManager manager, Predicate<PsiJavaPackage> processor) {
    SimpleReference<Set<PsiJavaPackage>> set = SimpleReference.create(new HashSet<PsiJavaPackage>());
    myPatterns[0].processPackages(manager, new CommonProcessors.CollectProcessor<>(set.get()));
    for (int i = 1; i < myPatterns.length; i++) {
      AopPsiTypePattern pattern = myPatterns[i];
      Set<PsiJavaPackage> all = set.get();
      set.set(new HashSet<>());
      pattern.processPackages(manager, psiPackage -> {
        if (all.contains(psiPackage)) {
          set.get().add(psiPackage);
        }
        return true;
      });
    }
    for (final PsiJavaPackage psiPackage : set.get()) {
      if (!processor.test(psiPackage)) return false;
    }
    return true;
  }
}
