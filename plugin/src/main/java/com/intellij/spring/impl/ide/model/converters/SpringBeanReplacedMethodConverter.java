/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.PsiMethod;

public class SpringBeanReplacedMethodConverter extends SpringBeanMethodConverter {

  protected boolean checkModifiers(final PsiMethod method) {
    return super.checkModifiers(method);
  }

  protected boolean checkParameterList(final PsiMethod method) {
    return true;
  }

}