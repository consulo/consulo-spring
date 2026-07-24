/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;

//@see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeCustomInitMethod
public class SpringBeanInitMethodConverter extends SpringBeanMethodConverter {

  protected boolean checkModifiers(PsiMethod method) {
    return !method.hasModifierProperty(PsiModifier.ABSTRACT);
  }
}