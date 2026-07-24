/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAdviceType;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.java.language.psi.PsiAnnotationMemberValue;

import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class AopAdviceWithPointcutAttribute extends AopAdviceImpl{
  public static final JamStringAttributeMeta.Single<String> POINTCUT_ATTR = JamAttributeMeta.singleString(AopConstants.POINTCUT_PARAM);

  public AopAdviceWithPointcutAttribute(AopAdviceType type, JamAnnotationMeta annoName) {
    super(type, annoName);
  }

  @Nullable
  @Override
  protected PsiAnnotationMemberValue getAnnoParam() {
    PsiAnnotationMemberValue value = super.getAnnoParam();
    if (value == null) {
      return myAnnoMeta.getAttribute(getPsiElement(), POINTCUT_ATTR).getPsiElement();
    }
    return value;
  }
}
