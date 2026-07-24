/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.java.language.psi.PsiType;
import consulo.language.psi.PsiManager;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * @author peter
 */
public class NotPattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myInnerPattern;

  public NotPattern(AopPsiTypePattern innerPattern) {
    myInnerPattern = innerPattern;
  }

  public AopPsiTypePattern getInnerPattern() {
    return myInnerPattern;
  }

  @Override
  public boolean accepts(@Nonnull PsiType type) {
    return !myInnerPattern.accepts(type);
  }

  @Override
  public boolean accepts(@Nonnull String qualifiedName) {
    return !myInnerPattern.accepts(qualifiedName);
  }

  @Override
  public boolean processPackages(PsiManager manager, Predicate<PsiJavaPackage> processor) {
    return TRUE.processPackages(manager, processor);
  }
}
