/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.java.language.psi.CommonClassNames;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiManager;

import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * @author peter
 */
public abstract class AopAdvisedElementsSearcher implements Predicate<Predicate<PsiClass>> {
  private final PsiManager myManager;

  protected AopAdvisedElementsSearcher(PsiManager manager) {
    myManager = manager;
  }

  public PsiManager getManager() {
    return myManager;
  }

  @Override
  public abstract boolean test(Predicate<PsiClass> processor);

  public boolean shouldSuppressErrors() {
    return false;
  }

  public boolean acceptsBoundMethod(@Nonnull PsiMethod method) {
    if (method.isConstructor() || method.isAbstract()) return false;

    PsiClass containingClass = method.getContainingClass();
    if (containingClass == null) return false;

    return !CommonClassNames.JAVA_LANG_OBJECT.equals(containingClass.getQualifiedName()) && isAcceptable(containingClass);
  }

  public boolean acceptsBoundMethodHeavy(@Nonnull PsiMethod method) {
    return true;
  }

  public boolean isAcceptable(PsiClass psiClass) {
    return false;
  }
}
