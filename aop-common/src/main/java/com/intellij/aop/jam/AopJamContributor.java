/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.sem.SemContributor;
import consulo.language.sem.SemRegistrar;

import static com.intellij.java.language.patterns.PsiJavaPatterns.*;

/**
 * @author peter
 */
@ExtensionImpl
public class AopJamContributor extends SemContributor {
  @Override
  public void registerSemProviders(SemRegistrar registrar) {
    AopAspectImpl.ASPECT_META.register(registrar, psiClass().withAnnotation(AopConstants.ASPECT_ANNO).andNot(psiElement().compiled()));

    AopPointcutImpl.POINTCUT_METHOD_META.register(registrar, psiMethod().andNot(psiElement().compiled()).withAnnotation(AopConstants.POINTCUT_ANNO));
  }
}
