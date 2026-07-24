/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiParameterList;
import com.intellij.java.language.psi.PsiType;

//@see org.springframework.beans.factory.support.DisposableBeanAdapter#invokeCustomDestroyMethod
public class SpringBeanDestroyMethodConverter extends SpringBeanMethodConverter {

  protected boolean checkParameterList(PsiMethod method) {
    PsiParameterList parameterList = method.getParameterList();

    return parameterList.getParametersCount() == 0 ||
           (parameterList.getParametersCount() == 1 && PsiType.BOOLEAN.equals(parameterList.getParameters()[0].getType()));
  }

  protected boolean checkModifiers(PsiMethod method) {
    return !method.hasModifierProperty(PsiModifier.ABSTRACT);
  }
}