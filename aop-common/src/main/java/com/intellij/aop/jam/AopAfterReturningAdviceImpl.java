/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopAfterReturningAdvice;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiType;

/**
 * @author peter
 */
public abstract class AopAfterReturningAdviceImpl extends AopAdviceWithPointcutAttribute implements AopAfterReturningAdvice {

  public AopAfterReturningAdviceImpl() {
    super(AopAdviceType.AFTER_RETURNING, AopAdviceMetas.AFTER_RETURNING_META);
  }

  @Override
  public JamStringAttributeElement<PsiParameter> getReturning() {
    return myAnnoMeta.getAttribute(getPsiElement(), AopAdviceMetas.RETURNING_META);
  }

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
