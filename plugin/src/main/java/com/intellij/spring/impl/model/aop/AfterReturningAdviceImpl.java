/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.aop.AfterReturningAdvice;

/**
 * @author peter
 */
public abstract class AfterReturningAdviceImpl extends BasicAdviceImpl implements AfterReturningAdvice {
  @Override
  public PointcutMatchDegree accepts(final PsiMethod method) {
    final PsiParameter parameter = getReturning().getValue();
    if (parameter != null) {
      final PsiType returnType = method.getReturnType();
      if (returnType != null && !parameter.getType().isAssignableFrom(returnType)) return PointcutMatchDegree.FALSE;
    }
    return super.accepts(method);
  }
}
