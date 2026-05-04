/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.web.mvc.jam;

import static com.intellij.patterns.PsiJavaPatterns.psiClass;
import static com.intellij.patterns.PsiJavaPatterns.psiMethod;
import com.intellij.semantic.SemContributor;
import com.intellij.semantic.SemRegistrar;

/**
 * @author peter
 */
public class SpringMvcJamContributor extends SemContributor {
  public void registerSemProviders(SemRegistrar registrar) {

    SpringMVCRequestMapping.ClassMapping.META.register(registrar, psiClass().withAnnotation(SpringMVCRequestMapping.REQUEST_MAPPING));

    SpringMVCRequestMapping.MethodMapping.META.register(registrar, psiMethod().withAnnotation(SpringMVCRequestMapping.REQUEST_MAPPING));

  }
}
