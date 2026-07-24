/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopAfterThrowingAdvice;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.java.language.psi.PsiParameter;

/**
 * @author peter
 */
public abstract class AopAfterThrowingAdviceImpl extends AopAdviceWithPointcutAttribute implements AopAfterThrowingAdvice {
  public AopAfterThrowingAdviceImpl() {
    super(AopAdviceType.AFTER_THROWING, AopAdviceMetas.AFTER_THROWING_META);
  }

  @Override
  public JamStringAttributeElement<PsiParameter> getThrowing() {
    return myAnnoMeta.getAttribute(getPsiElement(), AopAdviceMetas.THROWING_META);
  }
}
