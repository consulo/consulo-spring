/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.aop;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.xml.aop.SpringAspect;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class SpringAspectImpl implements SpringAspect {
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }

  @Nullable
  public PsiClass getPsiClass() {
    SpringBeanPointer pointer = getRef().getValue();
    return pointer != null ? pointer.getBeanClass() : null;
  }

}